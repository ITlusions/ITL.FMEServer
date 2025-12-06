# ITL FME Server Helm Charts

Repository voor meerdere FME Server versies als Helm charts.

## Deployment Opties

### 🚀 ArgoCD (Aanbevolen)

Voor automatische GitOps deployment van alle versies:

```bash
# Deploy alle versies met ApplicationSet
kubectl apply -f argocd/applicationset.yaml

# Of deploy enkele versie
kubectl apply -f argocd/application.yaml
```

📖 **Zie [docs/ARGOCD.md](docs/ARGOCD.md) voor complete ArgoCD guide**

### 📦 Helm (Handmatig)

Voor directe deployment met Helm CLI:

```bash
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -n itl-fme-prod-2025-2 --create-namespace
```

📖 **Zie [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) voor Helm deployment guide**

## Structuur

```
ITL.FMEServer/
├── argocd/                   # 🚀 ArgoCD configuratie
│   ├── applicationset.yaml   # Deploy alle versies
│   └── application.yaml      # Deploy enkele versie
├── charts/
│   ├── fmeserver-2023.2/     # FME Server 2023.2.x (Development)
│   │   ├── Chart.yaml
│   │   ├── values.yaml
│   │   ├── charts/           # Dependencies (lokaal)
│   │   └── templates/
│   ├── fmeserver-2024.0/     # FME Flow 2024.0.x (Test)
│   │   ├── Chart.yaml
│   │   ├── values.yaml
│   │   ├── charts/           # Dependencies (lokaal)
│   │   └── templates/
│   ├── fmeserver-2024.2/     # FME Flow 2024.2.x (Staging)
│   │   ├── Chart.yaml
│   │   ├── values.yaml
│   │   ├── charts/           # Dependencies (lokaal)
│   │   └── templates/
│   └── fmeserver-2025.2/     # FME Flow 2025.2.x (Production - Latest)
│       ├── Chart.yaml
│       ├── values.yaml
│       ├── charts/           # Dependencies (lokaal)
│       └── templates/
├── docs/                     # 📚 Alle documentatie
│   ├── README.md             # Documentatie index
│   ├── ARGOCD.md             # ArgoCD deployment guide
│   ├── DEPLOYMENT.md         # Helm deployment guide
│   ├── VERSIONS.md           # Versie vergelijking
│   ├── OFFLINE_DEPLOYMENT.md # Air-gapped setup
│   └── SETUP_COMPLETE.md     # Migration guide
├── examples/                 # 💡 FME Workspace voorbeelden
│   ├── README.md             # Voorbeeld projecten documentatie
│   ├── brandweer-noodsteunpunten.fmw  # Calamiteitenbeheer workspace
│   └── database_setup.sql    # PostgreSQL/PostGIS schema setup
├── archive/                  # Oude chart backup
├── .gitignore
└── README.md                 # Dit bestand
```

## Gebruik

### Installatie van specifieke versie

```bash
# FME Server 2023.2
helm install fmeserver-dev ./charts/fmeserver-2023.2 \
  -f ./charts/fmeserver-2023.2/values.yaml \
  -n itl-fme-dev-2023-2 --create-namespace

# FME Server 2024.0
helm install fmeserver-test ./charts/fmeserver-2024.0 \
  -f ./charts/fmeserver-2024.0/values.yaml \
  -n itl-fme-test-2024-0 --create-namespace

# FME Server 2024.2 (nieuwste)
helm install fmeserver-prod ./charts/fmeserver-2024.2 \
  -f ./charts/fmeserver-2024.2/values.yaml \
  -n itl-fme-staging-2024-2 --create-namespace
```

### Upgrade

```bash
# Upgrade naar nieuwere patch versie binnen zelfde major/minor
helm upgrade fmeserver-dev ./charts/fmeserver-2023.2 \
  -f ./charts/fmeserver-2023.2/values.yaml \
  -n itl-fme-dev-2023-2

# Migratie naar nieuwe major/minor versie
# 1. Backup huidige installatie
# 2. Deploy nieuwe versie in nieuwe namespace
# 3. Migreer data
# 4. Switch DNS/ingress
```

### Lijst geïnstalleerde releases

```bash
helm list -A | grep fmeserver
```

## Omgevingen

| Omgeving | Versie | Namespace | Hostname |
|----------|--------|-----------|----------|
| Development | 2023.2.3 | itl-fme-dev-2023-2 | fme-2023-2.itlusions.nl |
| Test | 2024.0.x | itl-fme-test-2024-0 | fme-2024-0.itlusions.nl |
| Staging | 2024.2.x | itl-fme-staging-2024-2 | fme-2024-2.itlusions.nl |
| Production | 2025.2.x | itl-fme-prod-2025-2 | fme-2025-2.itlusions.nl |

**Namespace Patroon:** `itl-fme-{env}-{version}`

## Versie Management

### Toevoegen nieuwe versie

1. **Kopieer bestaande chart:**
   ```bash
   cp -r charts/fmeserver-2024.2 charts/fmeserver-2025.2
   ```

2. **Update Chart.yaml:**
   ```yaml
   name: fmeserver-2025-2
   appVersion: "2025.2"
   dependencies:
     - name: fmeserver-2025-2
       repository: https://safesoftware.github.io/helm-charts
       version: x.x.x
   ```

3. **Update values.yaml:**
   ```yaml
   # Voor 2023.2 (oude fmeserver chart):
   fmeserver-2023-2:
     fmeserver:
       image:
         tag: "2023.2.x"
   
   # Voor 2024.0+ (nieuwe fmeflow chart):
   fmeflow:
     fmeserver:
       image:
         tag: "2025.2.0"
   ```

4. **Test deployment:**
   ```bash
   helm install fmeserver-test ./charts/fmeserver-2024.2 \
     --dry-run --debug
   ```

### Verwijderen oude versie

Wanneer een versie niet meer ondersteund wordt:

```bash
# 1. Verwijder deployment
helm uninstall fmeserver-old -n fmeserver-old-ns

# 2. Verwijder namespace (inclusief PVCs)
kubectl delete namespace fmeserver-old-ns

# 3. Verwijder chart directory (optioneel, voor archivering)
# mv charts/fmeserver-2023.0 archive/
```

## Upstream Updates

Check SafeSoftware Helm Charts voor updates:
```bash
helm repo add safesoftware https://safesoftware.github.io/helm-charts
helm repo update
helm search repo safesoftware/fmeserver --versions
```

## Configuratie per Omgeving

Elke chart heeft eigen `values.yaml` met omgeving-specifieke configuratie:

- **Database credentials** (per omgeving verschillend)
- **Hostname/Ingress** (dev/test/prod URLs)
- **Resource limits** (dev heeft minder, prod heeft meer)
- **Storage size** (dev klein, prod groot)
- **Engine configuration** (aantal workers)

## Migratie Strategie

### Van 2023.2 naar 2024.x

1. **Backup maken:**
   ```bash
   # Backup FME Server data via web interface
   # Of backup PostgreSQL database
   kubectl exec -n fmeserver-dev fmeserver-core-0 -- \
     pg_dump -U fmeserver fmeserver > backup.sql
   ```

2. **Deploy nieuwe versie in parallel:**
   ```bash
   helm install fmeserver-2024 ./charts/fmeserver-2024.0 \
     -n fmeserver-2024 --create-namespace
   ```

3. **Migreer data:**
   - Via FME Server Migration API
   - Of restore database backup in nieuwe instance

4. **Cutover:**
   - Update DNS/Ingress naar nieuwe versie
   - Monitor en valideer
   - Oude versie in standby voor rollback

## Troubleshooting

### Chart niet gevonden

```bash
# Verify chart structure
helm lint ./charts/fmeserver-2023.2

# Check dependencies
helm dependency update ./charts/fmeserver-2023.2
```

### Values niet toegepast

```bash
# Debug met dry-run
helm install fmeserver-dev ./charts/fmeserver-2023.2 \
  -f ./charts/fmeserver-2023.2/values.yaml \
  --dry-run --debug > output.yaml

# Check generated manifests
cat output.yaml
```

### Resource conflicts

```bash
# Gebruik verschillende namespaces per versie
helm install fmeserver-2023 ./charts/fmeserver-2023.2 -n fmeserver-2023
helm install fmeserver-2024 ./charts/fmeserver-2024.0 -n fmeserver-2024
```

## Best Practices

✅ **Gebruik semantic versioning voor chart versies**
✅ **Elke major/minor FME versie krijgt eigen chart directory**
✅ **Test nieuwe versies in dev/test voor productie**
✅ **Documenteer breaking changes in Chart.yaml**
✅ **Gebruik namespaces om versies te isoleren**
✅ **Maak backups voor migratie**
❌ **Upgrade NIET in-place tussen major versies**
❌ **Share GEEN databases tussen versies**

## Voorbeeld Projecten

💡 **FME Workspace voorbeelden:** Zie [examples/README.md](examples/README.md)

De `examples/` folder bevat complete FME workspace voorbeelden met database setup scripts:

### Calamiteitenbeheer: Brandweer & Noodsteunpunten

Complete workflow voor Nederlandse calamiteitenbeheer data:

**Features:**
- Multi-source data reading (PostgreSQL, WFS, CSV, GeoJSON)
- Spatial processing (RD New → WGS84 transformatie)
- Bereikbaarheidsanalyse (5km bufferzones)
- PostGIS output met spatial indexing

**Bestanden:**
- `examples/brandweer-noodsteunpunten.fmw` - FME Workspace (2023.2+)
- `examples/database_setup.sql` - Complete PostGIS database setup
- `examples/README.md` - Volledige documentatie met:
  - Database setup instructies
  - FME Desktop & Server deployment
  - Kubernetes/ArgoCD integration
  - Sample queries en troubleshooting
  - QGIS/Leaflet integration voorbeelden

**Quick Start:**

```bash
# 1. Setup database
psql -h localhost -U postgres -d calamiteiten -f examples/database_setup.sql

# 2. Open workspace in FME Workbench
fme.exe examples/brandweer-noodsteunpunten.fmw

# 3. Of deploy naar FME Server
# Zie examples/README.md voor details
```

📖 **[Lees de volledige documentatie →](examples/README.md)**

## Support Matrix

| FME Server | Kubernetes | Helm | PostgreSQL |
|------------|------------|------|------------|
| 2023.2.x   | 1.24-1.28  | 3.x  | 13-15      |
| 2024.0.x   | 1.25-1.29  | 3.x  | 14-16      |
| 2024.2.x   | 1.27-1.30  | 3.x  | 15-16      |
| 2025.2.x   | 1.28-1.31  | 3.x  | 16-17      |

## Contact

- **FME Server Admin:** FME Team
- **Kubernetes Admin:** Platform Team
- **Chart Maintenance:** DevOps Team

## Belangrijke Documentatie

> 📚 **Volledige documentatie:** Zie [docs/README.md](docs/README.md) voor een georganiseerd overzicht

- **[docs/README.md](docs/README.md)** - 📑 Documentatie index en navigatie guide
- **[docs/OFFLINE_DEPLOYMENT.md](docs/OFFLINE_DEPLOYMENT.md)** - 🚀 Air-gapped deployment (dependencies lokaal)
- **[docs/VERSIONS.md](docs/VERSIONS.md)** - 📊 Versie verschillen en upgrade paden
- **[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)** - ⚙️ Deployment voorbeelden en commando's
- **[docs/SETUP_COMPLETE.md](docs/SETUP_COMPLETE.md)** - 📖 Migratie guide en repository structuur

## Product Rebranding Notice

⚠️ **FME Server → FME Flow:** SafeSoftware heeft het product hernoemd vanaf versie 2024.0:
- **2023.2:** Gebruikt `fmeserver-2023-2` upstream chart
- **2024.0+:** Gebruikt `fmeflow` upstream chart (versies 1.0.7 / 2.9.0)

Onze wrapper charts blijven `fmeserver-*` heten voor consistentie en backwards compatibility.
