# ArgoCD Deployment Guide

Dit document beschrijft hoe je FME Server/Flow versies deployed met ArgoCD.

## Overzicht

Dit repository gebruikt een **1 versie = 1 omgeving** model:
- Elke FME Server versie is gekoppeld aan één specifieke namespace
- Versie-specifieke DNS namen (fme-2023-2.itlusions.nl, etc.)
- ApplicationSet deploy alle 4 versies automatisch

Er zijn twee manieren om te deployen met ArgoCD:

1. **ApplicationSet** - Deploy alle 4 versies tegelijk (aanbevolen)
2. **Individual Application** - Deploy één specifieke versie

## ApplicationSet Deployment

### Wat is een ApplicationSet?

Een ApplicationSet is een ArgoCD resource die automatisch meerdere Applications genereert op basis van een template. Voor FME Server genereert het 4 applicaties - één per versie:

- `fmeserver-2023-2` → namespace `fmeserver-dev`
- `fmeserver-2024-0` → namespace `fmeserver-test`
- `fmeserver-2024-2` → namespace `fmeserver-staging`
- `fmeserver-2025-2` → namespace `fmeserver-prod`

**Belangrijk:** Elke versie heeft precies één omgeving. De configuratie (resources, storage, engines) wordt gelezen uit de `values.yaml` in elke chart directory.

### Deploy ApplicationSet

```bash
# Deploy de ApplicationSet
kubectl apply -f argocd/applicationset.yaml

# Check status van alle applications
kubectl get applications -n argocd | grep fmeserver

# Check specifieke application
kubectl get application fmeserver-2025-2 -n argocd -o yaml
```

### ApplicationSet Features

- **Automatisch** - Alle versies worden tegelijk gedeployed
- **Simpel** - Geen value overrides, gebruikt chart `values.yaml` direct
- **Version-specific DNS** - Elke versie krijgt eigen hostname (fme-{version}.itlusions.nl)
- **1 versie = 1 omgeving** - Elke versie heeft één vaste namespace
- **Self-healing** - Automatische sync bij drift detection
- **Git as source of truth** - Alle configuratie in values.yaml

## Individual Application Deployment

### Wanneer gebruiken?

Gebruik een individual Application als je:
- Slechts één versie wilt deployen
- Meer controle wilt over sync timing
- Experimenteert met nieuwe configuraties

### Deploy Single Version

```bash
# Deploy alleen FME Flow 2025.2 (Production)
kubectl apply -f argocd/application.yaml

# Of maak custom application voor andere versie
cp argocd/application.yaml argocd/application-2024-0.yaml
# Edit application-2024-0.yaml en pas aan naar 2024.0 settings
```

### Custom Application

Voor andere versies, pas aan:

```yaml
metadata:
  name: fmeserver-2024-0
spec:
  source:
    path: charts/fmeserver-2024.0
  destination:
    namespace: fmeserver-test
```

## ArgoCD UI

### Access ArgoCD

```bash
# Port forward naar ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Get admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

Open browser: https://localhost:8080

### ApplicationSet in UI

1. Navigate to Applications
2. Zoek naar "fmeserver-versions" ApplicationSet
3. Klik om alle gegenereerde applications te zien
4. Elke application toont:
   - Sync status
   - Health status
   - Resource tree
   - Deployed version
   - Hostname URL

## Sync Policies

### Automated Sync

ApplicationSet is geconfigureerd met automated sync:

```yaml
syncPolicy:
  automated:
    prune: true       # Verwijder resources die niet in Git staan
    selfHeal: true    # Auto-sync bij drift detection
    allowEmpty: false # Voorkom lege deployments
```

### Manual Sync

Force sync via CLI:

```bash
# Sync specifieke versie
argocd app sync fmeserver-2025-2

# Sync met prune
argocd app sync fmeserver-2025-2 --prune

# Dry-run sync
argocd app sync fmeserver-2025-2 --dry-run
```

### Sync Waves

FME Server gebruikt sync wave 3 (deploy na infrastructure):

- Wave 0: Namespaces
- Wave 1: Secrets, ConfigMaps
- Wave 2: Storage, PVCs
- **Wave 3: FME Server** ← Hier
- Wave 4: Monitoring

## Configuration

### Version to Environment Mapping

| Version | Chart Path | Namespace | Hostname |
|---------|------------|-----------|----------|
| 2023.2 | charts/fmeserver-2023.2 | fmeserver-dev | fme-2023-2.itlusions.nl |
| 2024.0 | charts/fmeserver-2024.0 | fmeserver-test | fme-2024-0.itlusions.nl |
| 2024.2 | charts/fmeserver-2024.2 | fmeserver-staging | fme-2024-2.itlusions.nl |
| 2025.2 | charts/fmeserver-2025.2 | fmeserver-prod | fme-2025-2.itlusions.nl |

### Configuratie Beheer

**Waar configuratie aanpassen:**
- Alle versie-specifieke settings staan in `charts/fmeserver-{version}/values.yaml`
- ApplicationSet leest deze values direct - geen overrides nodig
- Wijzig values.yaml, commit naar Git, ArgoCD synct automatisch

**Voorbeeld wijziging:**

```bash
# Edit values voor 2025.2
nano charts/fmeserver-2025.2/values.yaml

# Commit
git add charts/fmeserver-2025.2/values.yaml
git commit -m "Update 2025.2 resources"
git push

# ArgoCD detecteert wijziging en synct automatisch
```

## Secrets Management

### Database Passwords

Standaard gebruikt elke chart de password uit zijn eigen `values.yaml`. Voor production gebruik secrets:

```bash
# Create secret met echte password
kubectl create secret generic fmeserver-db-secret \
  --from-literal=fmeserver-db-password=YOUR_SECURE_PASSWORD \
  --from-literal=postgres-password=YOUR_POSTGRES_PASSWORD \
  -n fmeserver-prod

# Update values.yaml in Git om secret te gebruiken
# In charts/fmeserver-2025.2/values.yaml:
fmeflow:
  fmeserver:
    database:
      passwordSecret: fmeserver-db-secret
      passwordSecretKey: fmeserver-db-password
      adminPasswordSecret: fmeserver-db-secret
      adminPasswordSecretKey: postgres-password

# Commit en push
git add charts/fmeserver-2025.2/values.yaml
git commit -m "Use secrets for production database"
git push
```

### TLS Certificates

Cert-manager genereert automatisch certificates:

```yaml
certManager:
  issuerName: letsencrypt-issuer
  issuerType: cluster
```

Controleer certificate status:

```bash
kubectl get certificate -n fmeserver-prod
kubectl describe certificate fme-2025-2-itlusions-nl-tls -n fmeserver-prod
```

## Monitoring

### Application Health

```bash
# Check health van alle FME applications
kubectl get applications -n argocd -l app.kubernetes.io/name=fmeserver

# Detailed status
argocd app get fmeserver-2025-2

# Application tree
argocd app get fmeserver-2025-2 --show-operation
```

### Sync Status

```bash
# Check sync status
argocd app list | grep fmeserver

# Watch sync progress
argocd app sync fmeserver-2025-2 --watch

# Check sync history
argocd app history fmeserver-2025-2
```

### Notifications

ApplicationSet is geconfigureerd voor Slack notificaties:

```yaml
annotations:
  notifications.argoproj.io/subscribe.on-sync-succeeded.slack: fme-deployments
  notifications.argoproj.io/subscribe.on-sync-failed.slack: fme-deployments
```

## Troubleshooting

### Application OutOfSync

```bash
# Check wat er uit sync is
argocd app diff fmeserver-2025-2

# Force sync
argocd app sync fmeserver-2025-2 --force

# Refresh application
argocd app get fmeserver-2025-2 --refresh
```

### Helm Dependencies

Als dependencies niet gevonden worden:

```bash
# Check of charts/*.tgz bestanden in Git staan
ls -la charts/fmeserver-2025.2/charts/

# Rebuild dependencies (lokaal)
cd charts/fmeserver-2025.2
helm dependency update
git add charts/*.tgz
git commit -m "Update dependencies"
git push
```

### Resource Issues

```bash
# Check pod status
kubectl get pods -n fmeserver-prod

# Check events
kubectl get events -n fmeserver-prod --sort-by='.lastTimestamp'

# Check resource usage
kubectl top pods -n fmeserver-prod

# Check PVC status
kubectl get pvc -n fmeserver-prod
```

### Sync Failures

```bash
# Check application controller logs
kubectl logs -n argocd deployment/argocd-application-controller | grep fmeserver

# Check repo server logs
kubectl logs -n argocd deployment/argocd-repo-server | grep fmeserver

# Check detailed sync result
argocd app sync fmeserver-2025-2 --info
```

## Rollback

### Via ArgoCD

```bash
# Check history
argocd app history fmeserver-2025-2

# Rollback naar vorige versie
argocd app rollback fmeserver-2025-2

# Rollback naar specifieke revision
argocd app rollback fmeserver-2025-2 --revision 5
```

### Via Git

```bash
# Revert Git commit
cd /path/to/ITL.FMEServer
git revert HEAD
git push

# ArgoCD detecteert automatisch en synct
```

## Best Practices

### 1. Use ApplicationSet for Multiple Environments

ApplicationSet zorgt voor consistentie tussen environments en vermindert duplication.

### 2. Version Control Everything

Alle configuratie staat in Git. Maak geen handmatige wijzigingen in cluster.

### 3. Test in Lower Environments

Test wijzigingen eerst in lagere versies voordat je naar production gaat:

```bash
# Test eerst in 2024.0 (test environment)
git checkout -b feature/new-config
nano charts/fmeserver-2024.0/values.yaml  # Wijzig configuratie
git commit -m "Test nieuwe config in 2024.0"
git push

# Valideer in ArgoCD
argocd app get fmeserver-2024-0
kubectl get pods -n fmeserver-test

# Na validatie, apply dezelfde wijziging naar production
nano charts/fmeserver-2025.2/values.yaml  # Zelfde wijziging
git commit -m "Apply to production 2025.2"
git push
```

### 4. Monitor Sync Status

Setup monitoring alerts voor sync failures:

```yaml
# Prometheus alert
- alert: ArgoCDSyncFailure
  expr: argocd_app_sync_total{name=~"fmeserver-.*", phase="Failed"} > 0
  for: 5m
  annotations:
    summary: "FME Server sync failure"
```

### 5. Use Secrets Properly

Nooit passwords in Git! Gebruik external secrets management:

- Kubernetes Secrets
- Sealed Secrets
- External Secrets Operator
- Vault

### 6. Resource Limits

Altijd resource limits instellen om noisy neighbor te voorkomen.

### 7. Health Checks

ApplicationSet ignored replica differences voor HPA compatibility:

```yaml
ignoreDifferences:
  - group: apps
    kind: Deployment
    jsonPointers:
      - /spec/replicas
```

## Advanced Configuration

### Multi-Cluster Deployment

Deploy naar verschillende clusters:

```yaml
spec:
  generators:
  - matrix:
      generators:
      - list:
          elements:
          - cluster: prod-cluster-1
            server: https://prod-cluster-1.k8s.local
          - cluster: prod-cluster-2
            server: https://prod-cluster-2.k8s.local
      - list:
          elements:
          - version: "2025.2"
            # ... version config
  
  template:
    spec:
      destination:
        server: '{{cluster.server}}'
```

### Progressive Delivery

Gebruik Argo Rollouts voor canary/blue-green:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: fmeserver-2025-2
spec:
  strategy:
    canary:
      steps:
      - setWeight: 20
      - pause: {duration: 10m}
      - setWeight: 40
      - pause: {duration: 10m}
      - setWeight: 100
```

## Links

- [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- [ApplicationSet Documentation](https://argocd-applicationset.readthedocs.io/)
- [FME Server Repository](https://github.com/ITlusions/ITL.FMEServer)
- [Helm Documentation](https://helm.sh/docs/)

## Support

Voor vragen of problemen:
- Check [DEPLOYMENT.md](DEPLOYMENT.md) voor deployment basics
- Check [VERSIONS.md](VERSIONS.md) voor versie specifieke info
- Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) voor common issues
- Open een issue in GitHub repository
