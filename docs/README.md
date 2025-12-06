# ITL.FMEServer Documentatie

Overzicht van alle documentatie voor de multi-version FME Server Helm Charts repository.

## 📚 Documentatie Index

### 🚀 Quick Start
Start hier als je snel wilt beginnen met deployment:
- **[../README.md](../README.md)** - Hoofddocument met repository overzicht en deployment opties

### � Examples & Tutorials

#### [../examples/README.md](../examples/README.md) - FME Workspace Voorbeelden ⭐ **NIEUW**
Complete FME workspace voorbeelden met database setup.

**Inhoud:**
- **Calamiteitenbeheer Workspace:** Brandweer & Noodsteunpunten verwerking
- Multi-source data reading (PostgreSQL, WFS, CSV, GeoJSON)
- Spatial processing & coordinate transformatie (RD New → WGS84)
- PostGIS database setup met spatial indices
- Complete deployment guide (Desktop, Server, Kubernetes)
- Integration examples (QGIS, Leaflet, Python)
- Troubleshooting & best practices

**Bestanden:**
- `brandweer-noodsteunpunten.fmw` - FME Workspace
- `database_setup.sql` - PostgreSQL/PostGIS schema

**Gebruik wanneer:** Je een praktisch voorbeeld nodig hebt van FME workspaces met spatial data processing.

---

### �📖 Detailed Guides

#### [ARGOCD.md](ARGOCD.md) - ArgoCD Deployment Guide ⭐ **NIEUW**
GitOps deployment met ArgoCD ApplicationSet.

**Inhoud:**
- ApplicationSet vs Individual Application
- Deploy alle versies tegelijk
- Version-specific DNS configuratie
- Automated sync & self-healing
- Secrets management voor ArgoCD
- Monitoring & troubleshooting
- Rollback procedures
- Best practices voor GitOps
- Multi-cluster deployment
- Progressive delivery

**Gebruik wanneer:** Je wilt automatische, GitOps-based deployment van FME Server met ArgoCD.

---

#### [DEPLOYMENT.md](DEPLOYMENT.md) - Helm Deployment Guide
Praktische Helm deployment voorbeelden en commando's.

**Inhoud:**
- Basic deployments (Development, Test, Staging, Production)
- Advanced deployments (custom values, CLI overrides)
- Upgrades (patch updates, major version upgrades)
- Dry-run & debugging
- Rollback procedures
- Multi-environment setup
- Monitoring commands
- Secrets management
- Useful aliases en tips

**Gebruik wanneer:** Je handmatig met Helm CLI wilt deployen of upgraden.

---

#### [VERSIONS.md](VERSIONS.md) - Version Comparison Guide
Overzicht van alle FME Server versies en hun verschillen.

**Inhoud:**
- Beschikbare versies (2023.2, 2024.0, 2024.2, 2025.2)
- Versie verschillen (resources, storage, engines)
- Upgrade paden met stap-voor-stap instructies
- Configuratie aanpassingen
- Monitoring en troubleshooting
- Best practices

**Gebruik wanneer:** Je wilt weten welke versie het beste past bij jouw use case of wilt upgraden.

---

#### [OFFLINE_DEPLOYMENT.md](OFFLINE_DEPLOYMENT.md) - Air-Gapped Deployment Guide
Guide voor offline/air-gapped Kubernetes clusters zonder internet.

**Inhoud:**
- Waarom lokale dependencies
- Repository structuur
- Deployment zonder internet (3 stappen)
- Dependencies updaten
- Verificatie procedures
- Troubleshooting
- Product rebranding (FME Server → FME Flow)

**Gebruik wanneer:** Je Kubernetes cluster heeft geen internet toegang.

---

#### [SETUP_COMPLETE.md](SETUP_COMPLETE.md) - Migration & Setup Guide
Complete guide over de repository restructuring en migratie van oude setup.

**Inhoud:**
- Voor/Na vergelijking
- Alle 4 charts in detail
- Configuratie verschillen
- Use cases en scenarios
- Git workflow
- Maintenance checklist
- Warnings en best practices

**Gebruik wanneer:** Je de repository net hebt geërfd of wilt begrijpen hoe de structuur werkt.

---

## 🎯 Welke Documentatie Heb Ik Nodig?

### Ik wil...

#### ...snel een FME Server deployen
→ Voor GitOps: [ARGOCD.md](ARGOCD.md) sectie "Deploy ApplicationSet"  
→ Voor Helm: [DEPLOYMENT.md](DEPLOYMENT.md) sectie "Basic Deployments"  
→ Start met [../README.md](../README.md) voor overzicht

#### ...alle versies tegelijk deployen
→ Gebruik [ARGOCD.md](ARGOCD.md) sectie "ApplicationSet Deployment"  
→ ApplicationSet deployed automatisch alle 4 versies

#### ...upgraden naar een nieuwe versie
→ Check [VERSIONS.md](VERSIONS.md) sectie "Upgrade Paden"  
→ Met ArgoCD: [ARGOCD.md](ARGOCD.md) - auto-sync bij Git push  
→ Met Helm: [DEPLOYMENT.md](DEPLOYMENT.md) sectie "Upgrades"

#### ...deployen zonder internet
→ Volg [OFFLINE_DEPLOYMENT.md](OFFLINE_DEPLOYMENT.md) stap voor stap

#### ...automatische sync en self-healing
→ Gebruik [ARGOCD.md](ARGOCD.md) met automated sync policy  
→ Drift wordt automatisch gedetecteerd en gecorrigeerd

#### ...begrijpen hoe de repository werkt
→ Lees [SETUP_COMPLETE.md](SETUP_COMPLETE.md) voor complete context  
→ Check [VERSIONS.md](VERSIONS.md) voor versie details

#### ...troubleshooten
→ ArgoCD issues: [ARGOCD.md](ARGOCD.md) sectie "Troubleshooting"  
→ Helm issues: [DEPLOYMENT.md](DEPLOYMENT.md) sectie "Monitoring Commands"  
→ Versie issues: [VERSIONS.md](VERSIONS.md) sectie "Troubleshooting"  
→ Offline issues: [OFFLINE_DEPLOYMENT.md](OFFLINE_DEPLOYMENT.md) sectie "Troubleshooting"

#### ...weten welke versie te gebruiken
→ Check [VERSIONS.md](VERSIONS.md) sectie "Versie Verschillen"  
→ Zie tabel in [../README.md](../README.md) sectie "Omgevingen"

---

## 📋 Charts Overzicht

| Chart | Versie | Omgeving | Documentatie |
|-------|--------|----------|--------------|
| fmeserver-2023.2 | 2023.2.3 | Development | [VERSIONS.md](VERSIONS.md#2023-2-development) |
| fmeserver-2024.0 | 2024.0.x | Test | [VERSIONS.md](VERSIONS.md#2024-0-test) |
| fmeserver-2024.2 | 2024.2.x | Staging | [VERSIONS.md](VERSIONS.md#2024-2-staging) |
| fmeserver-2025.2 | 2025.2.x | Production | [VERSIONS.md](VERSIONS.md#2025-2-production---latest) |

---

## 🔑 Key Concepts

### Multi-Version Repository
Deze repository ondersteunt **meerdere FME Server versies parallel**. Elke versie heeft:
- Eigen chart directory (`charts/fmeserver-X.Y/`)
- Eigen configuratie (`values.yaml`)
- Eigen namespace bij deployment
- Onafhankelijke lifecycle

**Voordeel:** Deploy verschillende versies tegelijk, test nieuwe versies parallel aan productie.

### Offline/Air-Gapped Support
Alle Helm chart dependencies zijn **lokaal opgeslagen** (~160KB totaal):
- `charts/*/charts/*.tgz` - Upstream SafeSoftware charts
- Git tracked (niet in .gitignore)
- Geen internet nodig tijdens deployment

**Voordeel:** Deploy in secure/offline Kubernetes clusters zonder externe dependencies.

### FME Server → FME Flow Rebranding
SafeSoftware hernoemde het product vanaf versie 2024.0:
- **2023.2:** Gebruikt `fmeserver-2023-2` chart
- **2024.0+:** Gebruikt `fmeflow` chart

**Impact:** values.yaml top-level key verandert, maar functionaliteit blijft hetzelfde.  
**Details:** Zie [OFFLINE_DEPLOYMENT.md](OFFLINE_DEPLOYMENT.md) sectie "Product Rebranding"

---

## 🆘 Support

### Problemen Met Charts
1. Check [VERSIONS.md](VERSIONS.md) sectie "Troubleshooting"
2. Check [DEPLOYMENT.md](DEPLOYMENT.md) sectie "Monitoring Commands"
3. Run `helm lint ./charts/fmeserver-X.Y`

### FME Server Software Issues
- Safe Software Support: https://www.safe.com/support/
- Documentation: https://docs.safe.com/fme/

### Helm Chart Upstream
- SafeSoftware Helm Charts: https://github.com/safesoftware/helm-charts
- Chart Repository: https://safesoftware.github.io/helm-charts

---

## 📝 Contributing

Verbeteringen aan documentatie? Update de relevante bestanden:
- Algemeen overzicht: `README.md` (root)
- Deployment info: `docs/DEPLOYMENT.md`
- Versie info: `docs/VERSIONS.md`
- Offline info: `docs/OFFLINE_DEPLOYMENT.md`
- Setup info: `docs/SETUP_COMPLETE.md`

---

**Last Updated:** December 6, 2025  
**Charts:** 4 versies (2023.2, 2024.0, 2024.2, 2025.2)  
**Status:** ✅ Complete & Ready
