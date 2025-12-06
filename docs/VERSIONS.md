# FME Server Versie Overzicht

## Beschikbare Versies

| Versie | Chart Directory | Status | Omgeving | Hostname |
|--------|----------------|--------|----------|----------|
| 2023.2.3 | `charts/fmeserver-2023.2` | ✅ Actief | Development | fme-2023-2.itlusions.nl |
| 2024.0.x | `charts/fmeserver-2024.0` | ✅ Beschikbaar | Test | fme-2024-0.itlusions.nl |
| 2024.2.x | `charts/fmeserver-2024.2` | ✅ Actief | Staging | fme-2024-2.itlusions.nl |
| 2025.2.x | `charts/fmeserver-2025.2` | ✅ Latest | Production | fme-2025-2.itlusions.nl |

## Quick Deployment

### Development (2023.2)

```bash
# Deploy FME Server 2023.2 naar development
helm install fmeserver-dev ./charts/fmeserver-2023.2 \
  -f ./charts/fmeserver-2023.2/values.yaml \
  -n fmeserver-dev \
  --create-namespace

# Check status
kubectl get pods -n fmeserver-dev
kubectl get ingress -n fmeserver-dev
```

### Test (2024.0)

```bash
# Deploy FME Server 2024.0 naar test
helm install fmeserver-test ./charts/fmeserver-2024.0 \
  -f ./charts/fmeserver-2024.0/values.yaml \
  -n fmeserver-test \
  --create-namespace

# Check status
kubectl get pods -n fmeserver-test
```

### Production (2025.2)

```bash
# Deploy FME Server 2025.2 naar productie (Latest)
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod \
  --create-namespace

# Check status
kubectl get pods -n fmeserver-prod
kubectl get ingress -n fmeserver-prod
```

### Staging (2024.2)

```bash
# Deploy FME Server 2024.2 naar staging
helm install fmeserver-staging ./charts/fmeserver-2024.2 \
  -f ./charts/fmeserver-2024.2/values.yaml \
  -n fmeserver-staging \
  --create-namespace

# Check status
kubectl get pods -n fmeserver-staging
kubectl get ingress -n fmeserver-prod
```

## Versie Verschillen

### 2023.2 (Development)
- **Image Tag:** 2023.2.3
- **PostgreSQL:** 15.4.0
- **Resources:** Minimaal (dev sizing)
- **Storage:** 2Gi FME + 1Gi DB
- **Engines:** 2 standard engines (512Mi RAM)

### 2024.0 (Test)
- **Image Tag:** 2024.0.2
- **PostgreSQL:** 15.8.0
- **Resources:** Medium (test sizing)
- **Storage:** 5Gi FME + 2Gi DB
- **Engines:** 2 standard engines (512Mi RAM)
- **Nieuwe Features:** [Check SafeSoftware release notes]

### 2024.2 (Staging)
- **Image Tag:** 2024.2.0
- **PostgreSQL:** 16.4.0
- **Resources:** Maximum (prod sizing)
- **Storage:** 20Gi FME + 10Gi DB (Retain policy)
- **Engines:** 4 standard engines (1Gi RAM each)
- **CPU:** 2 cores
- **Resource Limits:** Gedefinieerd voor web component

### 2025.2 (Production - Latest)
- **Image Tag:** 2025.2.0
- **PostgreSQL:** 16.6.0
- **Resources:** Enhanced (prod sizing with limits)
- **Storage:** 20Gi FME + 10Gi DB (Retain policy)
- **Engines:** 6 engines total
  - Standard group: 4 engines (1Gi RAM, 500m CPU each)
  - High-memory group: 2 engines (2Gi RAM, 1000m CPU each)
- **Resource Limits:** Gedefinieerd voor alle componenten:
  - Core: 4Gi request / 8Gi limit
  - Web: 2Gi request / 4Gi limit
  - Queue: 512Mi request / 1Gi limit
  - Websocket: 1Gi request / 2Gi limit
- **Nieuwe Features:** High-memory engine group voor demanding workloads

## Upgrade Paden

### Van 2023.2 naar 2024.0

```bash
# 1. Backup maken
kubectl exec -n fmeserver-dev fmeserver-core-0 -- \
  fmeserver backup create --name upgrade-backup-$(date +%Y%m%d)

# 2. Export backup
kubectl cp fmeserver-dev/fmeserver-core-0:/data/backups/ ./backups/

# 3. Deploy nieuwe versie
helm install fmeserver-2024 ./charts/fmeserver-2024.0 \
  -n fmeserver-2024 --create-namespace

# 4. Restore backup in nieuwe versie
kubectl cp ./backups/ fmeserver-2024/fmeserver-core-0:/data/backups/
kubectl exec -n fmeserver-2024 fmeserver-core-0 -- \
  fmeserver backup restore --name upgrade-backup-YYYYMMDD

# 5. Verify en cutover ingress
kubectl patch ingress fmeserver-dev -n fmeserver-dev -p '{"spec":{"rules":[]}}'
kubectl patch ingress fmeserver-2024 -n fmeserver-2024 -p '{"spec":{"rules":[...]}}'
```

### Van 2024.0 naar 2024.2

Zelfde proces als hierboven, maar met chart `fmeserver-2024.2`.

### Van 2024.2 naar 2025.2 (Recommended)

```bash
# 1. Backup maken
kubectl exec -n fmeserver-staging fmeserver-core-0 -- \
  fmeserver backup create --name upgrade-2025-$(date +%Y%m%d)

# 2. Export backup
kubectl cp fmeserver-staging/fmeserver-core-0:/data/backups/ ./backups/

# 3. Deploy nieuwe versie
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -n fmeserver-prod --create-namespace

# 4. Restore backup in nieuwe versie
kubectl cp ./backups/ fmeserver-prod/fmeserver-core-0:/data/backups/
kubectl exec -n fmeserver-prod fmeserver-core-0 -- \
  fmeserver backup restore --name upgrade-2025-YYYYMMDD

# 5. Verify nieuwe features (high-memory engines)
kubectl get pods -n fmeserver-prod | grep engine

# 6. Cutover ingress
kubectl patch ingress fmeserver-staging -n fmeserver-staging -p '{"spec":{"rules":[]}}'
```

## Patch Updates (binnen zelfde versie)

```bash
# Update image tag in values.yaml
# Bijvoorbeeld: 2025.2.0 → 2025.2.1

# Upgrade deployment
helm upgrade fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod

# Rollback indien nodig
helm rollback fmeserver-prod -n fmeserver-prod
```

## Configuratie Aanpassingen

### Database Passwords

Update per omgeving in `values.yaml`:

```yaml
# Voor 2023.2 (oude fmeserver chart):
fmeserver-2023-2:
  fmeserver:
    database:
      password: "[UNIQUE_PER_ENV]"
  postgresql:
    auth:
      postgresqlPassword: "[UNIQUE_PER_ENV]"

# Voor 2024.0+ (nieuwe fmeflow chart):
fmeflow:
  fmeserver:
    database:
      password: "[UNIQUE_PER_ENV]"
  postgresql:
    auth:
      postgresqlPassword: "[UNIQUE_PER_ENV]"
```

### Resource Scaling

Voor specifieke omgevingen aanpassen in `values.yaml`:

```yaml
# Voor 2023.2:
fmeserver-2023-2:
  resources:
    core:
      requests:
        memory: 4Gi
        cpu: 1000m

# Voor 2024.0+:
fmeflow:
  resources:
    core:
      requests:
        memory: 4Gi
        cpu: 1000m
  engines:
    groups:
      - name: "high-memory-group"
        engines: 2
        resources:
          requests:
            memory: 2Gi
            cpu: 1000m
```

### Ingress Hostname

Per omgeving verschillend in `values.yaml`:

```yaml
# Voor 2023.2:
fmeserver-2023-2:
  deployment:
    hostname: fme.[dev|test|prod].itlusions.nl
    tlsSecretName: fme-[dev|test|prod]-itlusions-nl-tls

# Voor 2024.0+:
fmeflow:
  deployment:
    hostname: fme.[dev|test|prod].itlusions.nl
    tlsSecretName: fme-[dev|test|prod]-itlusions-nl-tls
```

## Monitoring

### Check Helm Releases

```bash
# Lijst alle FME Server deployments
helm list -A | grep fmeserver

# Details van specifieke release
helm status fmeserver-dev -n fmeserver-dev
helm get values fmeserver-dev -n fmeserver-dev
```

### Check Resources

```bash
# Pods per versie
kubectl get pods -n fmeserver-dev -l app=fmeserver
kubectl get pods -n fmeserver-test -l app=fmeserver
kubectl get pods -n fmeserver-prod -l app=fmeserver

# Resource usage
kubectl top pods -n fmeserver-prod
kubectl top nodes
```

### Check Storage

```bash
# PVCs per namespace
kubectl get pvc -n fmeserver-dev
kubectl get pvc -n fmeserver-test
kubectl get pvc -n fmeserver-prod

# Storage class
kubectl get sc
```

## Troubleshooting

### Chart Validation

```bash
# Lint chart
helm lint ./charts/fmeserver-2023.2
helm lint ./charts/fmeserver-2024.0
helm lint ./charts/fmeserver-2024.2

# Dry-run deployment
helm install fmeserver-test ./charts/fmeserver-2024.0 \
  -f ./charts/fmeserver-2024.0/values.yaml \
  --dry-run --debug
```

### Dependency Issues

```bash
# Update chart dependencies
cd charts/fmeserver-2024.2
helm dependency update
helm dependency list
```

### Pod Issues

```bash
# Logs
kubectl logs -n fmeserver-prod fmeserver-core-0 -f
kubectl logs -n fmeserver-prod fmeserver-engine-standard-0 -f

# Describe pod
kubectl describe pod fmeserver-core-0 -n fmeserver-prod

# Events
kubectl get events -n fmeserver-prod --sort-by='.lastTimestamp'
```

## Cleanup

### Verwijder Deployment

```bash
# Uninstall release (behoudt PVCs)
helm uninstall fmeserver-dev -n fmeserver-dev

# Verwijder namespace (inclusief PVCs!)
kubectl delete namespace fmeserver-dev

# Of: Verwijder alleen PVCs
kubectl delete pvc -n fmeserver-dev --all
```

### Archive Old Version

```bash
# Verplaats naar archive directory
mkdir -p archive
mv charts/fmeserver-2023.0 archive/
git add archive/
git commit -m "Archive FME Server 2023.0 - end of life"
```

## Upstream Chart Updates

```bash
# Add SafeSoftware repo
helm repo add safesoftware https://safesoftware.github.io/helm-charts
helm repo update

# Check available versions
helm search repo safesoftware/fmeserver --versions

# Download specific version
helm pull safesoftware/fmeserver-2024-2 --version 0.4.0
tar -xzf fmeserver-2024-2-0.4.0.tgz

# Compare with current chart
diff -r fmeserver-2024-2/ charts/fmeserver-2024.2/
```

## Best Practices

✅ **Gebruik verschillende namespaces per versie/omgeving**
✅ **Tag image versions expliciet (geen 'latest')**
✅ **Test nieuwe versies in dev/test voor productie**
✅ **Maak backups voor elke migratie**
✅ **Documenteer custom values in comments**
✅ **Gebruik 'Retain' reclaimPolicy voor productie storage**
✅ **Monitor resource usage en schaal indien nodig**

❌ **Upgrade NIET in-place tussen major versies**
❌ **Hergebruik GEEN database tussen versies**
❌ **Commit GEEN passwords in Git**
❌ **Deploy NIET zonder eerst te testen**
❌ **Verwijder GEEN PVCs zonder backup**

## Support & Contact

- **FME Server Issues:** Safe Software Support
- **Chart Issues:** DevOps Team
- **Deployment Help:** Platform Team
- **Upstream Charts:** https://github.com/safesoftware/helm-charts
