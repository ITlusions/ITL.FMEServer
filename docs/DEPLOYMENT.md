# Deployment Examples

Quick reference voor het deployen van verschillende FME Server versies.

## Directory Structure

```
charts/
├── fmeserver-2023.2/    # Voor development/legacy
├── fmeserver-2024.0/    # Voor test
├── fmeserver-2024.2/    # Voor staging
└── fmeserver-2025.2/    # Voor productie (latest)
```

## Basic Deployments

### Deploy Development (2023.2)

```bash
helm install fmeserver-dev ./charts/fmeserver-2023.2 \
  -f ./charts/fmeserver-2023.2/values.yaml \
  -n fmeserver-dev \
  --create-namespace
```

### Deploy Test (2024.0)

```bash
helm install fmeserver-test ./charts/fmeserver-2024.0 \
  -f ./charts/fmeserver-2024.0/values.yaml \
  -n fmeserver-test \
  --create-namespace
```

### Deploy Production (2025.2 - Latest)

```bash
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod \
  --create-namespace
```

### Deploy Staging (2024.2)

```bash
helm install fmeserver-staging ./charts/fmeserver-2024.2 \
  -f ./charts/fmeserver-2024.2/values.yaml \
  -n fmeserver-staging \
  --create-namespace
```

## Advanced Deployments

### Deploy met Custom Values Override

```bash
# Create override file for 2025.2 (uses fmeflow chart)
cat > custom-values.yaml <<EOF
fmeflow:
  fmeserver:
    database:
      password: "my-secure-password"
  deployment:
    hostname: "fme.custom.domain.nl"
EOF

# Deploy met override
helm install fmeserver-custom ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -f custom-values.yaml \
  -n fmeserver-custom \
  --create-namespace
```

### Deploy met CLI Overrides

```bash
# Voor 2025.2 (uses fmeflow chart)
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod \
  --create-namespace \
  --set fmeflow.fmeserver.database.password="secure-pass" \
  --set fmeflow.deployment.hostname="fme.prod.example.nl"
```
```

## Upgrades

### Patch Update (binnen zelfde versie)

```bash
# Update image tag in values.yaml: 2025.2.0 → 2025.2.1
# Dan upgrade:
helm upgrade fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod
```

### Major Version Upgrade (met parallel deployment)

```bash
# 1. Backup huidige versie
kubectl exec -n fmeserver-staging fmeserver-staging-core-0 -- \
  fmeserver backup create --name pre-upgrade-backup

# 2. Deploy nieuwe versie in nieuwe namespace
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -n fmeserver-prod \
  --create-namespace

# 3. Migreer data (via FME Server Migration API of DB restore)

# 4. Test nieuwe versie en high-memory engines

# 5. Cutover ingress (optioneel)
```

## Dry Run & Debugging

### Test Deployment (zonder daadwerkelijk te deployen)

```bash
# Test 2025.2 deployment
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod \
  --dry-run --debug
```

### Generate Manifests

```bash
helm template fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod > manifests.yaml
```

## Uninstall

### Verwijder Deployment (behoud PVCs)

```bash
helm uninstall fmeserver-dev -n fmeserver-dev
```

### Verwijder Alles (inclusief namespace en PVCs)

```bash
# WAARSCHUWING: Dit verwijdert alle data!
kubectl delete namespace fmeserver-dev
```

### Verwijder alleen PVCs

```bash
kubectl delete pvc -n fmeserver-dev fmeserver-data
kubectl delete pvc -n fmeserver-dev fmeserver-postgresql
```

## Rollback

### Rollback naar vorige versie

```bash
# Zie release history
helm history fmeserver-prod -n fmeserver-prod

# Rollback naar specifieke revisie
helm rollback fmeserver-prod 2 -n fmeserver-prod

# Rollback naar direct vorige versie
helm rollback fmeserver-prod -n fmeserver-prod
```

## Multi-Environment Setup

### Deploy alle omgevingen tegelijk

```bash
#!/bin/bash
# deploy-all.sh

# Development
helm install fmeserver-dev ./charts/fmeserver-2023.2 \
  -f ./charts/fmeserver-2023.2/values.yaml \
  -n fmeserver-dev --create-namespace

# Test
helm install fmeserver-test ./charts/fmeserver-2024.0 \
  -f ./charts/fmeserver-2024.0/values.yaml \
  -n fmeserver-test --create-namespace

# Staging
helm install fmeserver-staging ./charts/fmeserver-2024.2 \
  -f ./charts/fmeserver-2024.2/values.yaml \
  -n fmeserver-staging --create-namespace

# Production (Latest)
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod --create-namespace

# Check status
helm list -A | grep fmeserver
```

## Monitoring Commands

### Check Status

```bash
# Helm releases
helm list -A | grep fmeserver

# Pods
kubectl get pods -n fmeserver-prod -l app=fmeserver

# Ingress
kubectl get ingress -n fmeserver-prod

# Services
kubectl get svc -n fmeserver-prod
```

### Check Values

```bash
# Show deployed values
helm get values fmeserver-prod -n fmeserver-prod

# Show all values (including defaults)
helm get values fmeserver-prod -n fmeserver-prod --all
```

### Check Logs

```bash
# Core logs
kubectl logs -n fmeserver-prod fmeserver-core-0 -f

# Engine logs
kubectl logs -n fmeserver-prod fmeserver-engine-standard-0 -f

# All pods
kubectl logs -n fmeserver-prod -l app=fmeserver --tail=100
```

## Secrets Management

### Create Database Secret (alternative to values.yaml)

```bash
# Create secret
kubectl create secret generic fmeserver-db-secret \
  -n fmeserver-prod \
  --from-literal=fmeserver-db-password='my-secure-password' \
  --from-literal=postgres-password='admin-secure-password'

# Update values.yaml to use secret
# fmeserver.database.passwordSecret: fmeserver-db-secret
# fmeserver.database.passwordSecretKey: fmeserver-db-password
```

### Create TLS Secret

```bash
# If not using cert-manager
kubectl create secret tls fme-itlusions-nl-tls \
  -n fmeserver-prod \
  --cert=path/to/tls.crt \
  --key=path/to/tls.key
```

## Useful Aliases

```bash
# Add to ~/.bashrc or ~/.zshrc
alias hmi='helm install'
alias hmu='helm upgrade'
alias hml='helm list -A'
alias hmun='helm uninstall'
alias kgp='kubectl get pods'
alias kgpa='kubectl get pods -A'
alias kl='kubectl logs -f'
alias kd='kubectl describe'

# FME specific
alias fme-dev='kubectl -n fmeserver-dev'
alias fme-test='kubectl -n fmeserver-test'
alias fme-prod='kubectl -n fmeserver-prod'
```

## Tips

✅ **Altijd eerst dry-run gebruiken bij nieuwe deployments**
✅ **Gebruik separate namespaces per omgeving/versie**
✅ **Maak backups voor upgrades**
✅ **Test in dev/test voor productie deployment**
✅ **Monitor resource usage na deployment**
✅ **Gebruik secrets voor passwords (niet plain text in values)**
