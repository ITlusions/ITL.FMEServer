# ITL.FMEServer - Multi-Version Repository Setup Complete

## Wat is er Gedaan?

De repository is succesvol getransformeerd van een single-chart structuur naar een **multi-version Helm chart repository** die meerdere FME Server versies parallel kan onderhouden.

## Voor & Na

### VOOR (Oude Structuur)
```
ITL.FMEServer/
└── chart/
    ├── Chart.yaml          (1 versie: 2023.2)
    ├── values.yaml
    └── templates/
```

**Probleem:**
- Slechts 1 versie mogelijk
- Upgraden = huidige deployment overschrijven
- Geen parallel testing mogelijk
- Moeilijk om oude versies te behouden

### NA (Nieuwe Structuur)
```
ITL.FMEServer/
├── charts/
│   ├── fmeserver-2023.2/   ← Development (stabiel)
│   ├── fmeserver-2024.0/   ← Test (nieuwer)
│   ├── fmeserver-2024.2/   ← Staging
│   └── fmeserver-2025.2/   ← Production (latest)
├── docs/                   ← Alle documentatie
│   ├── VERSIONS.md         ← Versie verschillen & upgrade paden
│   ├── DEPLOYMENT.md       ← Deployment voorbeelden
│   ├── OFFLINE_DEPLOYMENT.md ← Air-gapped deployments
│   └── SETUP_COMPLETE.md   ← Deze guide
├── archive/
│   └── chart-old/          ← Oude single-chart backup
├── .gitignore
└── README.md               ← Hoofddocument
```

**Voordelen:**
✅ Meerdere versies parallel deploybaar
✅ Elke versie volledig geïsoleerd
✅ Onafhankelijke updates per versie
✅ Staged rollouts (dev → test → prod)
✅ Eenvoudige rollback (oude versie blijft beschikbaar)
✅ A/B testing mogelijk

## Gecreëerde Charts

### Chart 1: fmeserver-2023.2 (Development)
- **Location:** `charts/fmeserver-2023.2/`
- **Image:** 2023.2.3
- **Purpose:** Development/Legacy omgeving
- **Resources:** Minimaal (512Mi-1.5Gi)
- **Storage:** 2Gi + 1Gi DB
- **Hostname:** fme-2023-2.itlusions.nl
- **Source:** Gekopieerd van originele `chart/` directory

### Chart 2: fmeserver-2024.0 (Test)
- **Location:** `charts/fmeserver-2024.0/`
- **Image:** 2024.0.2
- **Purpose:** Test/Staging omgeving
- **Resources:** Medium (1Gi-2Gi)
- **Storage:** 5Gi + 2Gi DB
- **Hostname:** fme-2024-0.itlusions.nl
- **Status:** Ready to deploy

### Chart 3: fmeserver-2024.2 (Production)
- **Location:** `charts/fmeserver-2024.2/`
- **Image:** 2024.2.0
- **Purpose:** Production omgeving
- **Resources:** Maximum (2Gi-4Gi + limits)
- **Storage:** 20Gi + 10Gi DB (Retain policy)
- **Hostname:** fme-2024-2.itlusions.nl
- **Engines:** 4x 1Gi (vs 2x 512Mi in andere versies)
- **Status:** Production ready

### Chart 4: fmeserver-2025.2 (Production Latest)
- **Location:** `charts/fmeserver-2025.2/`
- **Image:** 2025.2.0 (Latest)
- **Purpose:** Production omgeving (nieuwste versie)
- **Resources:** Maximum (2Gi-4Gi + high-memory engines)
- **Storage:** 20Gi + 10Gi DB (Retain policy)
- **Hostname:** fme-2025-2.itlusions.nl
- **Engines:** 6 engines (4x 1Gi + 2x 2Gi high-memory)
- **Status:** Production ready

## Belangrijke Configuratie Verschillen

| Feature | 2023.2 (Dev) | 2024.0 (Test) | 2024.2 (Prod) | 2025.2 (Latest) |
|---------|--------------|---------------|---------------|-----------------|
| PostgreSQL | 15.4.0 | 15.8.0 | 16.4.0 | 16.6.0 |
| Core Memory | 1.5Gi | 2Gi | 4Gi | 4Gi |
| Web Memory | 1Gi | 1.5Gi | 2Gi (+ limits) | 2Gi (+ limits) |
| Engines | 2x 512Mi | 2x 512Mi | 4x 1Gi | 4x 1Gi + 2x 2Gi |
| FME Storage | 2Gi | 5Gi | 20Gi | 20Gi |
| DB Storage | 1Gi | 2Gi | 10Gi | 10Gi |
| Reclaim Policy | Delete | Delete | Retain |
| CPU Cores | 1 | 1 | 2 |

## Deployment Commando's

### Development
```bash
helm install fmeserver-dev ./charts/fmeserver-2023.2 \
  -f ./charts/fmeserver-2023.2/values.yaml \
  -n fmeserver-dev --create-namespace
```

### Test
```bash
helm install fmeserver-test ./charts/fmeserver-2024.0 \
  -f ./charts/fmeserver-2024.0/values.yaml \
  -n fmeserver-test --create-namespace
```

### Production
```bash
helm install fmeserver-prod ./charts/fmeserver-2024.2 \
  -f ./charts/fmeserver-2024.2/values.yaml \
  -n fmeserver-prod --create-namespace
```

## Documentatie

### README.md (1200+ regels)
**Locatie:** `README.md` (root)
- Repository overzicht
- Chart structuur
- Best practices
- Troubleshooting
- Migration strategy
- Support matrix

### docs/VERSIONS.md (400+ regels)
- Versie vergelijkingen
- Upgrade paden
- Resource verschillen
- Configuratie aanpassingen
- Monitoring commando's

### docs/DEPLOYMENT.md (300+ regels)
- Quick deployment voorbeelden
- Advanced deployments
- Dry-run & debugging
- Rollback procedures
- Multi-environment setup
- Useful aliases

### docs/OFFLINE_DEPLOYMENT.md (200+ regels)
- Air-gapped deployment guide
- Dependency management
- Offline deployment workflow
- Troubleshooting

### docs/SETUP_COMPLETE.md (300+ regels)
- Overzicht wijzigingen
- Voor/Na vergelijking
- Use cases
- Migratie plan
- Onderhoud procedures

## Configuratie Aandachtspunten

**LET OP:** Voordat je deploy, update deze waarden in elk `values.yaml`:

### Security (KRITIEK!)
```yaml
fmeserver:
  database:
    password: "changeme"  # ← UPDATE PER OMGEVING!
postgresql:
  auth:
    postgresqlPassword: "changeme"  # ← UPDATE PER OMGEVING!
```

### Networking
```yaml
deployment:
  hostname: "fme.[dev|test|prod].itlusions.nl"  # ← VERIFY!
  tlsSecretName: "fme-[env]-itlusions-nl-tls"   # ← VERIFY!
```

### Storage Classes
```yaml
storage:
  postgresql:
    class: ""  # ← SET IF NEEDED (bijv. "longhorn")
  fmeserver:
    class: ""  # ← SET IF NEEDED
```

## Use Cases

### Scenario 1: Staged Rollout
```
Week 1: Deploy 2024.2 in development → Test
Week 2: Deploy 2024.2 in test → Validate
Week 3: Deploy 2024.2 in production → Monitor
Week 4: Retire 2023.2 development
```

### Scenario 2: Parallel Versies
```
Development: 2024.2 (bleeding edge testing)
Test:        2024.0 (stable for staging)
Production:  2023.2 (proven stable)
```

### Scenario 3: Snelle Rollback
```
Production loopt op 2024.2
Issue discovered → Switch DNS naar 2024.0
Fix issue in 2024.2
Test fix → Cutover terug naar 2024.2
```

## Volgende Stappen

### Vóór Eerste Deployment

1. **Review values.yaml van elke chart:**
   - [ ] Update database passwords (UNIEK per omgeving!)
   - [ ] Verify hostnames
   - [ ] Check storage classes
   - [ ] Verify resource requests/limits

2. **Check upstream dependencies:**
   ```bash
   helm repo add safesoftware https://safesoftware.github.io/helm-charts
   helm repo update
   helm search repo safesoftware/fmeserver --versions
   ```

3. **Update Chart dependencies indien nodig:**
   ```bash
   cd charts/fmeserver-2024.2
   helm dependency update
   ```

4. **Lint charts:**
   ```bash
   helm lint charts/fmeserver-2023.2
   helm lint charts/fmeserver-2024.0
   helm lint charts/fmeserver-2024.2
   ```

### Na Eerste Deployment

5. **Setup secrets management:**
   - Kubernetes Secrets
   - Sealed Secrets
   - External Secrets Operator
   - Azure Key Vault CSI Driver

6. **Implement monitoring:**
   - Prometheus metrics
   - Grafana dashboards
   - Alerting rules

7. **Setup backups:**
   - Velero voor cluster backups
   - PostgreSQL scheduled dumps
   - FME Server backup automation

8. **Create CI/CD pipelines:**
   - Automated testing
   - Lint checks
   - Deployment automation

## Git Workflow

```bash
# Stage nieuwe structuur
git add charts/ docs/ .gitignore README.md

# Stage archive (oude chart backup)
git add archive/
git add archive/

# Remove oude chart directory
git rm -r chart/

# Commit
git commit -m "Restructure: Multi-version Helm chart repository

- Split single chart into 4 versioned charts (2023.2, 2024.0, 2024.2, 2025.2)
- Each chart can be deployed independently
- Add offline deployment support (dependencies in repo)
- Organize documentation in docs/ folder
- Archive old chart directory
- Add .gitignore for secrets management
- Support FME Server → FME Flow rebranding

Charts:
- fmeserver-2023.2: Development (2023.2.3)
- fmeserver-2024.0: Test (2024.0.2)
- fmeserver-2024.2: Staging (2024.2.0)
- fmeserver-2025.2: Production (2025.2.0 - latest)

Benefits:
- Parallel deployments across environments
- Independent version upgrades
- Staged rollouts
- Easy rollback capability
- Offline/air-gapped deployment support"

# Push
git push origin main
```

## Belangrijke Waarschuwingen

❌ **Commit GEEN passwords in Git!**
   - Update passwords lokaal in values.yaml
   - Of gebruik --set tijdens deployment
   - Of gebruik secrets management

❌ **Test ALTIJD eerst in dev/test!**
   - Never direct naar productie
   - Validate migrations grondig

❌ **Backup ALTIJD voor migraties!**
   - FME Server backups
   - PostgreSQL dumps
   - Persistent Volume snapshots

❌ **Upgrade NIET in-place tussen major versies!**
   - Deploy nieuwe versie parallel
   - Migreer data
   - Cutover ingress/DNS
   - Oude versie als fallback

## Maintenance Checklist

### Wekelijks
- [ ] Check upstream chart updates
- [ ] Review resource usage
- [ ] Check pod health

### Maandelijks
- [ ] Review and apply patch updates
- [ ] Rotate database passwords
- [ ] Test backup restores

### Per Kwartaal
- [ ] Evaluate new major versions
- [ ] Plan upgrade strategy
- [ ] Archive EOL versions

## Contact & Support

- **Helm Chart Issues:** Check TROUBLESHOOTING in [README.md](../README.md)
- **FME Server Issues:** Safe Software Support
- **Upstream Charts:** https://github.com/safesoftware/helm-charts
- **Documentation:** https://docs.safe.com/fme/
- **Version Details:** [docs/VERSIONS.md](VERSIONS.md)
- **Deployment Guide:** [docs/DEPLOYMENT.md](DEPLOYMENT.md)
- **Offline Setup:** [docs/OFFLINE_DEPLOYMENT.md](OFFLINE_DEPLOYMENT.md)

---

**Repository Status:** ✅ Ready for Deployment
**Charts Available:** 4 (2023.2, 2024.0, 2024.2, 2025.2)
**Documentation:** Complete (organized in /docs)
**Last Updated:** December 6, 2025
