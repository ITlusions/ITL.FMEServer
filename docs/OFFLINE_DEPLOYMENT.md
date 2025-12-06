# Offline Deployment Guide

Deze repository bevat alle Helm chart dependencies lokaal voor **air-gapped / offline deployments**.

## Waarom Lokale Dependencies?

De Kubernetes cluster heeft **geen internet toegang**, daarom zijn alle SafeSoftware chart dependencies vooraf gedownload en opgeslagen in de repository.

## Structuur

```
charts/
├── fmeserver-2023.2/
│   └── charts/
│       └── fmeserver-2023-2-0.2.67.tgz    ✅ Lokaal (70KB)
├── fmeserver-2024.0/
│   └── charts/
│       └── fmeflow-1.0.7.tgz              ✅ Lokaal (70KB)
├── fmeserver-2024.2/
│   └── charts/
│       └── fmeflow-2.9.0.tgz              ✅ Lokaal (10KB)
└── fmeserver-2025.2/
    └── charts/
        └── fmeflow-2.9.0.tgz              ✅ Lokaal (10KB)
```

## Deployment Zonder Internet

### Stap 1: Clone Repository

```bash
# Op machine met internet
git clone https://github.com/ITlusions/ITL.FMEServer.git
cd ITL.FMEServer
```

### Stap 2: Transfer naar Air-Gapped Environment

```bash
# Zip hele repository
tar -czf ITL.FMEServer.tar.gz ITL.FMEServer/

# Of via USB/secure transfer naar target environment
```

### Stap 3: Deploy in Offline Cluster

```bash
# Unzip op target server
tar -xzf ITL.FMEServer.tar.gz
cd ITL.FMEServer

# Deploy direct (dependencies zijn al lokaal!)
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod \
  --create-namespace
```

## Dependencies Updaten (op machine met internet)

Als SafeSoftware nieuwe versies uitbrengt:

```bash
# Update alle dependencies
cd charts/fmeserver-2023.2
helm dependency update

cd ../fmeserver-2024.0
helm dependency update

cd ../fmeserver-2024.2
helm dependency update

cd ../fmeserver-2025.2
helm dependency update

# Commit nieuwe .tgz files
git add charts/*/charts/*.tgz
git commit -m "Update Helm dependencies"
git push
```

## Verificatie

Check dat dependencies lokaal aanwezig zijn:

```bash
# Check alle charts
ls -lh charts/*/charts/*.tgz

# Output moet zijn:
# fmeserver-2023.2/charts/fmeserver-2023-2-0.2.67.tgz  (70KB)
# fmeserver-2024.0/charts/fmeflow-1.0.7.tgz            (70KB)
# fmeserver-2024.2/charts/fmeflow-2.9.0.tgz            (10KB)
# fmeserver-2025.2/charts/fmeflow-2.9.0.tgz            (10KB)
```

## Dry Run Test

Test deployment zonder daadwerkelijk te installeren:

```bash
helm install fmeserver-prod ./charts/fmeserver-2025.2 \
  -f ./charts/fmeserver-2025.2/values.yaml \
  -n fmeserver-prod \
  --dry-run --debug
```

Als dit werkt zonder internet errors, zijn alle dependencies correct lokaal beschikbaar!

## Belangrijke Opmerkingen

✅ **Dependencies zijn GIT tracked** - De .gitignore staat toe dat `charts/*/charts/*.tgz` bestanden gecommit worden

✅ **Geen externe repos nodig** - De `Chart.yaml` verwijst naar SafeSoftware repo, maar Helm gebruikt eerst lokale charts

✅ **Kleine bestanden** - Totaal ~160KB voor alle 4 versies

❌ **Niet voor Chart.lock** - Chart.lock bestanden worden NIET gecommit (genereer lokaal)

## Troubleshooting

### Error: "failed to download"

**Probleem:** Helm probeert alsnog van internet te downloaden

**Oplossing:** 
```bash
# Controleer of charts/ directory bestaat
ls -la charts/fmeserver-2025.2/charts/

# Forceer gebruik van lokale charts
helm dependency build ./charts/fmeserver-2025.2
```

### Error: "chart not found"

**Probleem:** Chart naam mismatch

**Oplossing:**
Check `Chart.yaml` dependency naam matches met .tgz bestand:
```bash
cat charts/fmeserver-2025.2/Chart.yaml | grep -A3 dependencies
ls charts/fmeserver-2025.2/charts/
```

## Product Rebranding (FME Server → FME Flow)

⚠️ **Let op:** SafeSoftware heeft FME Server hernoemd naar **FME Flow** vanaf versie 2024.0

| Versie | Chart Naam | Upstream Dependency |
|--------|-----------|-------------------|
| 2023.2 | fmeserver-2023-2 | fmeserver-2023-2 |
| 2024.0 | fmeserver-2024-0 | **fmeflow** |
| 2024.2 | fmeserver-2024-2 | **fmeflow** |
| 2025.2 | fmeserver-2025-2 | **fmeflow** |

Onze charts blijven `fmeserver-*` heten voor consistentie, maar gebruiken de nieuwe `fmeflow` upstream charts.

## Zie Ook

- [README.md](../README.md) - General overview
- [DEPLOYMENT.md](DEPLOYMENT.md) - Deployment examples
- [VERSIONS.md](VERSIONS.md) - Version comparison
