# FME Workspace Voorbeeld: Brandweer en Noodsteunpunten

Dit is een voorbeeld FME workspace voor het verwerken van Nederlandse calamiteitenbeheer data, specifiek brandweerkazernes en noodopvanglocaties voor rampbestrijding.

## 📋 Overzicht

Deze workspace demonstreert een complete FME workflow voor ruimtelijke data-integratie en analyse van calamiteitenbeheer infrastructuur in Nederland.

### Features

- ✅ **Multi-source data reading**: PostgreSQL, WFS services, CSV, GeoJSON
- ✅ **Spatial processing**: Coördinaattransformatie (RD New → WGS84)
- ✅ **Bereikbaarheidsanalyse**: 5km bufferzones rond brandweerkazernes
- ✅ **Data validation**: Geometrie validatie, duplicate removal
- ✅ **Spatial analytics**: Intersectie analyses, afstandsberekeningen
- ✅ **Database output**: PostGIS met spatial indexing en constraints

### Use Case

Deze workspace is ontworpen voor:
- **Veiligheidsregio's**: Analyseer dekking van brandweercapaciteit
- **Gemeenten**: Inventariseer noodopvanglocaties
- **Rampenbestrijding**: Bereken evacuatiecapaciteit per gebied
- **Beleid & Planning**: Identificeer gebieden met onvoldoende dekking

## 🚀 Quick Start

### 1. Database Setup

Maak eerst de PostGIS database structuur aan met het meegeleverde SQL script:

```bash
# Via psql command line
psql -h localhost -U postgres -d postgres -c "CREATE DATABASE calamiteiten;"
psql -h localhost -U postgres -d calamiteiten -f database_setup.sql
```

Of via **pgAdmin**:
1. Open pgAdmin en connect naar je PostgreSQL server
2. Create database `calamiteiten`
3. Open Query Tool
4. Load `database_setup.sql`
5. Execute (F5)

**Wat doet het script?**
- ✅ Maakt schema `calamiteiten` aan
- ✅ Enabled PostGIS + PostGIS Topology extensions
- ✅ Creëert 3 tabellen: `brandweer_kazernes`, `noodsteunpunten`, `veiligheidsregios`
- ✅ Maakt spatial indices (GIST) voor performance
- ✅ Voegt constraints toe voor data validatie
- ✅ Maakt 3 analyse views voor rapportage
- ✅ Creëert 2 utility functions voor spatial queries
- ✅ Voegt voorbeelddata toe (7 kazernes, 8 noodsteunpunten)
- ✅ Configureert permissions voor FME user

**Verificatie:**

```sql
-- Check of tabellen zijn aangemaakt
SELECT 
    'brandweer_kazernes' as tabel, 
    COUNT(*) as aantal 
FROM calamiteiten.brandweer_kazernes
UNION ALL
SELECT 'noodsteunpunten', COUNT(*) 
FROM calamiteiten.noodsteunpunten;

-- Expected output:
-- brandweer_kazernes | 7
-- noodsteunpunten    | 8

-- Check spatial data
SELECT 
    naam, 
    plaats,
    ST_AsText(geom) as locatie_rd
FROM calamiteiten.brandweer_kazernes 
LIMIT 3;
```

### 2. FME Desktop Usage

#### Open de Workspace

```bash
# Windows
"C:\Program Files\FME\fme.exe" brandweer-noodsteunpunten.fmw

# Of dubbelklik op brandweer-noodsteunpunten.fmw
```

#### Configureer Published Parameters

Klik in FME Workbench op **Run** → **Prompt for User Parameters**:

**Database Connectie:**
| Parameter | Waarde | Voorbeeld |
|-----------|--------|-----------|
| `DB_HOST` | Database server hostname | `localhost` of `postgres.itlusions.nl` |
| `DB_PORT` | Database poort | `5432` |
| `DB_NAME` | Database naam | `calamiteiten` |
| `DB_SCHEMA` | Schema naam | `calamiteiten` |
| `DB_USER` | Database gebruiker | `fmeuser` |
| `DB_PASSWORD` | Database wachtwoord | `••••••` |

**Input Bestanden (optioneel):**
| Parameter | Beschrijving |
|-----------|--------------|
| `CSV_INPUT_PATH` | Pad naar CSV met brandweer data |
| `GEOJSON_INPUT_PATH` | Pad naar GeoJSON met noodsteunpunten |

**WFS Settings (optioneel):**
| Parameter | Beschrijving |
|-----------|--------------|
| `WFS_URL` | WFS service URL (bijv. PDOK) |
| `WFS_TYPENAME` | Feature type naam |

**Spatial Settings:**
| Parameter | Default | Beschrijving |
|-----------|---------|--------------|
| `SOURCE_CRS` | EPSG:28992 | Bron coördinaatsysteem (RD New) |
| `TARGET_CRS` | EPSG:4326 | Doel coördinaatsysteem (WGS84) |
| `BUFFER_DISTANCE` | 5000 | Buffer afstand in meters |

#### Run de Workspace

1. Klik op **Run** (groene play button) of druk F5
2. Monitor de Translation Log voor progress
3. Check voor warnings of errors

**Verwachte output:**

```
FME 2023.2 (Build 23774 - win64)
==============================
Translation was SUCCESSFUL
==============================
Features Read: 150
Features Written: 148
Features Filtered (Invalid): 2
Elapsed Time: 3.2 seconds
==============================
```

### 3. FME Server Deployment

#### Upload naar FME Server

**Optie A: Via FME Workbench**

1. In FME Workbench: **File** → **Publish to FME Server...**
2. Server URL: `https://fme-2025-2.itlusions.nl`
3. Username/Password: Vul in
4. Repository: Selecteer of maak `Calamiteiten`
5. Klik **Publish**

**Optie B: Via FME Server Web Interface**

1. Open browser: `https://fme-2025-2.itlusions.nl`
2. Login met credentials
3. Navigate naar **Workspaces**
4. Klik **Upload**
5. Selecteer `brandweer-noodsteunpunten.fmw`
6. Repository: `Calamiteiten`
7. Click **Upload**

#### Configureer Database Connection in FME Server

**Stap 1: Maak Database Connection**

1. Navigate naar **Resources** → **Connections** → **Database**
2. Klik **New**
3. Vul parameters in:

```
Connection Name: calamiteiten_db
Connection Type: PostgreSQL
Host: postgres.itlusions.nl
Port: 5432
Database: calamiteiten
Schema: calamiteiten
Username: fmeuser
Password: ••••••••
```

4. Klik **Test Connection** → moet succesvol zijn
5. Save

**Stap 2: Link Connection aan Workspace**

1. Ga naar workspace → **Parameters** tab
2. Bij `DB_CONNECTION` → selecteer `$(FME_DB_calamiteiten_db)`
3. Dit vervangt alle individuele DB parameters
4. Save

#### Schedule een Job

**Dagelijkse sync (bijv. 02:00 uur):**

1. Navigate naar **Schedules**
2. Klik **New Schedule**
3. Parameters:
   ```
   Name: Brandweer Data Sync Dagelijks
   Repository: Calamiteiten
   Workspace: brandweer-noodsteunpunten.fmw
   Enabled: Yes
   
   Schedule:
   - Frequency: Daily
   - Time: 02:00
   - Time Zone: Europe/Amsterdam
   ```
4. **Email Notification** (optioneel):
   ```
   On Success: admin@itlusions.nl
   On Failure: admin@itlusions.nl
   ```
5. Save

**Webhook trigger (on-demand):**

Voor API-driven workflows:

1. Navigate naar workspace → **Services** tab
2. Enable **Data Download** service
3. Copy webhook URL:
   ```
   https://fme-2025-2.itlusions.nl/fmedatadownload/Calamiteiten/brandweer-noodsteunpunten.fmw
   ```
4. Gebruik in externe systemen (CI/CD, monitoring tools, etc.)

Example cURL:
```bash
curl -X POST "https://fme-2025-2.itlusions.nl/fmedatadownload/Calamiteiten/brandweer-noodsteunpunten.fmw" \
  -H "Authorization: fmetoken token=YOUR_TOKEN" \
  -d "opt_servicemode=async"
```

### 4. Kubernetes Deployment (via ArgoCD)

Als je FME Server draait in Kubernetes via de Helm charts in deze repository:

#### Upload Workspace naar Persistent Volume

**Via kubectl:**

```bash
# Copy workspace file naar FME Server pod
kubectl cp brandweer-noodsteunpunten.fmw \
  itl-fme-prod-2025-2/fmeserver-core-0:/data/workspaces/Calamiteiten/ \
  -c fmeserver-core

# Verify upload
kubectl exec -it fmeserver-core-0 -n itl-fme-prod-2025-2 -c fmeserver-core -- \
  ls -lh /data/workspaces/Calamiteiten/

# Output should show:
# -rw-r--r-- 1 fmeserver fmeserver 250K Dec  6 10:30 brandweer-noodsteunpunten.fmw
```

**Via FME Server REST API:**

```bash
# Get API token first
TOKEN="your-fme-server-token"

# Upload workspace
curl -X POST "https://fme-2025-2.itlusions.nl/fmerest/v3/repositories/Calamiteiten/items" \
  -H "Authorization: fmetoken token=$TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@brandweer-noodsteunpunten.fmw" \
  -F "repositoryName=Calamiteiten" \
  -F "createDirectories=true"
```

#### Configureer Database Secret in Kubernetes

**Create Secret:**

```bash
# Database credentials als Kubernetes secret
kubectl create secret generic fme-calamiteiten-db \
  --from-literal=host='postgres.itlusions.nl' \
  --from-literal=port='5432' \
  --from-literal=database='calamiteiten' \
  --from-literal=schema='calamiteiten' \
  --from-literal=username='fmeuser' \
  --from-literal=password='YOUR_SECURE_PASSWORD' \
  -n itl-fme-prod-2025-2

# Verify secret
kubectl get secret fme-calamiteiten-db -n itl-fme-prod-2025-2 -o yaml
```

**Mount in Helm Values:**

Update `charts/fmeserver-2025.2/values.yaml`:

```yaml
# Extra environment variables from secrets
extraEnvVars:
  - name: DB_HOST
    valueFrom:
      secretKeyRef:
        name: fme-calamiteiten-db
        key: host
  - name: DB_PORT
    valueFrom:
      secretKeyRef:
        name: fme-calamiteiten-db
        key: port
  - name: DB_NAME
    valueFrom:
      secretKeyRef:
        name: fme-calamiteiten-db
        key: database
  - name: DB_SCHEMA
    valueFrom:
      secretKeyRef:
        name: fme-calamiteiten-db
        key: schema
  - name: DB_USER
    valueFrom:
      secretKeyRef:
        name: fme-calamiteiten-db
        key: username
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: fme-calamiteiten-db
        key: password
```

**GitOps Sync via ArgoCD:**

```bash
# Commit workspace en secret configuratie
git add examples/ charts/fmeserver-2025.2/values.yaml
git commit -m "Add calamiteiten workspace + database secret config"
git push origin main

# ArgoCD synct automatisch (binnen 3 min)
# Of force sync:
argocd app sync fmeserver-2025-2 -n argocd

# Check sync status
argocd app get fmeserver-2025-2
```

## 📊 Data Bronnen

### Ondersteunde Input Formaten

| Format | Reader | Use Case | Example |
|--------|--------|----------|---------|
| **PostgreSQL** | JDBC | Bestaande database | `postgres://host/db` |
| **WFS** | OGC WFS | PDOK, andere web services | `https://service.pdok.nl/...` |
| **CSV** | CSV Reader | Excel exports, spreadsheets | `brandweer.csv` |
| **GeoJSON** | GeoJSON Reader | Web APIs, JavaScript apps | `data.geojson` |

### Open Data Nederland

**PDOK (Publieke Dienstverlening Op de Kaart)**
- Website: https://www.pdok.nl/
- WFS Services voor administratieve grenzen, adressen, topografie

**Relevante WFS Services:**

```bash
# BAG (Basisregistratie Adressen en Gebouwen)
https://service.pdok.nl/lv/bag/wfs/v2_0

# Bestuurlijke Grenzen (gemeenten, provincies)
https://service.pdok.nl/kadaster/bestuurlijkegebieden/wfs/v1_0

# TOP10NL (Topografie)
https://service.pdok.nl/brt/top10nl/wfs/v1_0
```

**Brandweer Nederland Open Data:**
- https://data.overheid.nl/
- Search: "brandweer", "veiligheidsregio"

**Veiligheidsregio's Nederland:**
- https://www.veiligheidsregio.nl/
- 25 regio's met coördinatie brandweer, GHOR, rampenbestrijding

### Coördinaatsystemen

**RD New (Rijksdriehoekscoördinaten)** - EPSG:28992
- Standaard voor Nederlandse overheidsdataINSTALL
- Eenheid: meters
- X-bereik: 0 tot 280.000 (west-oost)
- Y-bereik: 300.000 tot 625.000 (zuid-noord)
- Gebruikt door: Kadaster, CBS, provincies, gemeenten

**WGS84** - EPSG:4326
- Wereldwijd standaard voor web mapping
- Eenheid: decimale graden
- Longitude: -180° tot 180°
- Latitude: -90° tot 90°
- Gebruikt door: Google Maps, Leaflet, OpenStreetMap, GPS

**Conversie:** Deze workspace transformeert automatisch van RD New naar WGS84 voor web applicaties.

## 🗂️ Database Schema

Het `database_setup.sql` script creëert de volgende structuur:

### Tabellen

#### 1. `calamiteiten.brandweer_kazernes`

Brandweerkazernes met uitruk- en capaciteitsinformatie.

| Column | Type | Beschrijving |
|--------|------|--------------|
| `id` | SERIAL | Primary key |
| `naam` | VARCHAR(255) | Naam kazerne |
| `adres` | VARCHAR(255) | Straat + huisnummer |
| `plaats` | VARCHAR(100) | Plaatsnaam |
| `postcode` | VARCHAR(10) | Postcode |
| `telefoonnummer` | VARCHAR(20) | Contactnummer |
| `email` | VARCHAR(100) | Email adres |
| `type_kazerne` | VARCHAR(50) | `Beroeps`, `Vrijwillig`, `Gemengd` |
| `veiligheidsregio` | VARCHAR(100) | Naam veiligheidsregio |
| `aantal_voertuigen` | INTEGER | Aantal brandweerwagens |
| `bemanning_24_7` | BOOLEAN | 24/7 bemand? |
| `specialisaties` | TEXT[] | Array: `['Hoogwerker', 'Duikteam']` |
| `opkomsttijd_minuten` | INTEGER | Gemiddelde opkomsttijd |
| `website` | VARCHAR(255) | Website URL |
| `opmerkingen` | TEXT | Vrije tekst |
| `geom` | GEOMETRY(POINT, 28992) | Locatie in RD New |
| `created_at` | TIMESTAMP | Aanmaakdatum |
| `updated_at` | TIMESTAMP | Laatste wijziging |

**Indices:**
- GIST index op `geom` (spatial queries)
- B-tree index op `veiligheidsregio`, `type_kazerne`, `plaats`

#### 2. `calamiteiten.noodsteunpunten`

Noodopvanglocaties voor evacuaties.

| Column | Type | Beschrijving |
|--------|------|--------------|
| `id` | SERIAL | Primary key |
| `naam` | VARCHAR(255) | Naam locatie |
| `adres` | VARCHAR(255) | Adres |
| `plaats` | VARCHAR(100) | Plaatsnaam |
| `type_locatie` | VARCHAR(50) | `Sporthal`, `School`, `Gemeentehuis`, etc. |
| `capaciteit` | INTEGER | Max aantal personen |
| `capaciteit_bedden` | INTEGER | Aantal bedden/slaapplaatsen |
| `faciliteiten` | TEXT | Comma-separated lijst |
| `heeft_keuken` | BOOLEAN | Keuken aanwezig? |
| `heeft_sanitair` | BOOLEAN | Sanitair aanwezig? |
| `heeft_noodstroom` | BOOLEAN | Noodstroomvoorziening? |
| `heeft_ehbo` | BOOLEAN | EHBO voorzieningen? |
| `beschikbaarheid` | VARCHAR(20) | `24/7`, `Op aanvraag`, `Daguren` |
| `contactpersoon` | VARCHAR(100) | Naam contactpersoon |
| `telefoonnummer` | VARCHAR(20) | Contact nummer |
| `toegankelijk_mindervaliden` | BOOLEAN | Toegankelijk? |
| `parkeerplaatsen` | INTEGER | Aantal parkeerplaatsen |
| `laatste_inspectie` | DATE | Datum laatste inspectie |
| `actief` | BOOLEAN | Actief in gebruik? |
| `geom` | GEOMETRY(POINT, 28992) | Locatie in RD New |

**Indices:**
- GIST index op `geom`
- B-tree index op `type_locatie`, `actief`, `capaciteit`

#### 3. `calamiteiten.veiligheidsregios`

Referentiedata voor 25 Nederlandse veiligheidsregio's.

| Column | Type | Beschrijving |
|--------|------|--------------|
| `id` | SERIAL | Primary key |
| `naam` | VARCHAR(100) | Naam veiligheidsregio |
| `code` | VARCHAR(10) | Regio code (bijv. VRAA) |
| `provincie` | VARCHAR(50) | Hoofdprovincie |
| `aantal_gemeenten` | INTEGER | Aantal gemeenten in regio |
| `inwoners` | INTEGER | Totaal aantal inwoners |
| `oppervlakte_km2` | DECIMAL | Oppervlakte in km² |
| `website` | VARCHAR(255) | Website URL |
| `telefoonnummer` | VARCHAR(20) | Centraal nummer |
| `geom` | GEOMETRY(MULTIPOLYGON, 28992) | Grens polygonen |

### Views

#### `v_brandweer_dekking`

Brandweerkazernes met aantal bereikbare noodsteunpunten binnen 5km.

```sql
SELECT * FROM calamiteiten.v_brandweer_dekking;
```

Output:
- Kazerne info (naam, plaats, type)
- `aantal_noodsteunpunten_binnen_5km`
- `totale_capaciteit_binnen_5km`

#### `v_statistieken_per_regio`

Aggregaties per veiligheidsregio.

```sql
SELECT * FROM calamiteiten.v_statistieken_per_regio;
```

Output:
- `veiligheidsregio`
- `aantal_kazernes`
- `totaal_voertuigen`
- `kazernes_24_7`
- `aantal_noodsteunpunten`
- `totale_evacuatiecapaciteit`
- `gemiddelde_capaciteit_per_punt`

#### `v_noodsteun_zonder_dekking`

Noodsteunpunten buiten 5km bereik van brandweerkazerne.

```sql
SELECT * FROM calamiteiten.v_noodsteun_zonder_dekking
WHERE afstand_dichtstbijzijnde_kazerne_km > 10;
```

### Functions

#### `get_nearest_kazerne(point_geom GEOMETRY)`

Vind dichtstbijzijnde brandweerkazerne voor een locatie.

```sql
-- Example: incident op Dam Amsterdam
SELECT * FROM calamiteiten.get_nearest_kazerne(
    ST_SetSRID(ST_MakePoint(121500, 487500), 28992)
);
```

Returns:
- `kazerne_naam`
- `afstand_meter`
- `opkomsttijd_geschat` (geschat op basis van afstand)
- `geom` (kazerne locatie)

#### `get_evacuatie_capaciteit(kazerne_id INTEGER, bereik_meters INTEGER)`

Bereken totale evacuatiecapaciteit binnen bereik van een kazerne.

```sql
-- Capaciteit binnen 5km van kazerne met ID 1
SELECT calamiteiten.get_evacuatie_capaciteit(1, 5000) as capaciteit;
```

## 🔧 Workspace Architectuur

### Data Flow Diagram

```
┌─────────────────────┐
│   INPUT READERS     │
├─────────────────────┤
│ PostgreSQL/PostGIS  │──┐
│ WFS Service (PDOK)  │──┤
│ CSV Files           │──┼──> AttributeManager
│ GeoJSON Files       │──┘       │
└─────────────────────┘           │
                                  ▼
                        ┌───────────────────┐
                        │   TRANSFORMERS    │
                        ├───────────────────┤
                        │ AttributeManager  │ (Map attributes)
                        │ Reprojector       │ (RD → WGS84)
                        │ GeometryValidator │ (Validate/repair)
                        │ DuplicateFilter   │ (Remove duplicates)
                        │ SpatialFilter     │ (Bounding box NL)
                        │ BufferGenerator   │ (5km bereikzone)
                        │ SpatialRelator    │ (Intersection)
                        │ Aggregator        │ (Group by region)
                        │ StatisticsCalc    │ (Count, sum, avg)
                        │ DateTimeStamper   │ (Add timestamp)
                        └───────────────────┘
                                  │
                                  ▼
                        ┌───────────────────┐
                        │  OUTPUT WRITERS   │
                        ├───────────────────┤
                        │ PostGIS           │
                        │ - Spatial Index   │
                        │ - Constraints     │
                        │ - Triggers        │
                        └───────────────────┘
```

### Key Transformers

1. **AttributeManager**
   - Maps input attributes naar database kolommen
   - Renames, creates, removes attributes
   - Type conversions

2. **Reprojector**
   - Coordinate system transformation
   - Input: EPSG:28992 (RD New)
   - Output: EPSG:4326 (WGS84)

3. **GeometryValidator**
   - Validates geometry volgens OGC Simple Features spec
   - Auto-repair invalide geometrieën
   - Filters out geometries that can't be repaired

4. **DuplicateFilter**
   - Removes duplicate features
   - Based on: `naam` + `ST_X(geom)` + `ST_Y(geom)`
   - Keeps first occurrence

5. **SpatialFilter**
   - Filter features binnen Nederland bounding box
   - RD New: X=0-280000, Y=300000-625000
   - Removes features outside NL

6. **BufferGenerator**
   - Creates 5km buffer zones rond brandweerkazernes
   - Used for bereikbaarheidsanalyse
   - Output: polygon geometries

7. **SpatialRelator**
   - Spatial intersection analysis
   - Finds noodsteunpunten binnen buffer zones
   - Enriches data with spatial relationships

8. **Aggregator**
   - Groups features per veiligheidsregio
   - Calculates statistics (count, sum, average)
   - Creates summary records

9. **StatisticsCalculator**
   - Calculates min, max, mean, sum
   - Per attribute (capaciteit, aantal_voertuigen)
   - Adds as new attributes

10. **DateTimeStamper**
    - Adds `created_at`, `updated_at` timestamps
    - Format: `YYYY-MM-DD HH:MM:SS`

## 💡 Example Queries

### Basic Queries

```sql
-- Alle brandweerkazernes
SELECT 
    naam, 
    plaats, 
    type_kazerne, 
    aantal_voertuigen,
    ST_AsText(geom) as locatie_rd
FROM calamiteiten.brandweer_kazernes
ORDER BY plaats, naam;

-- Noodsteunpunten met hoge capaciteit (>1000 personen)
SELECT 
    naam, 
    plaats, 
    capaciteit, 
    type_locatie,
    beschikbaarheid
FROM calamiteiten.noodsteunpunten
WHERE capaciteit >= 1000
ORDER BY capaciteit DESC;

-- Alle 24/7 beroepsbrandweerkazernes
SELECT 
    naam, 
    plaats, 
    veiligheidsregio,
    aantal_voertuigen
FROM calamiteiten.brandweer_kazernes
WHERE type_kazerne = 'Beroeps' 
  AND bemanning_24_7 = true
ORDER BY aantal_voertuigen DESC;

-- Noodsteunpunten met volledige voorzieningen
SELECT 
    naam, 
    plaats, 
    capaciteit,
    type_locatie
FROM calamiteiten.noodsteunpunten
WHERE actief = true 
  AND heeft_keuken = true
  AND heeft_sanitair = true
  AND heeft_noodstroom = true
  AND heeft_ehbo = true
ORDER BY capaciteit DESC;
```

### Spatial Queries

```sql
-- Alle noodsteunpunten binnen 5km van Amsterdam Centrum brandweer
SELECT 
    n.naam as noodsteunpunt,
    n.capaciteit,
    n.type_locatie,
    ROUND(ST_Distance(b.geom, n.geom)::numeric, 0) as afstand_meter
FROM calamiteiten.brandweer_kazernes b
JOIN calamiteiten.noodsteunpunten n 
    ON ST_DWithin(b.geom, n.geom, 5000)
WHERE b.naam = 'Brandweer Amsterdam Centrum'
  AND n.actief = true
ORDER BY afstand_meter;

-- Dichtstbijzijnde brandweerkazerne voor elk noodsteunpunt
SELECT 
    n.naam as noodsteunpunt,
    n.plaats,
    n.capaciteit,
    b.naam as dichtstbijzijnde_kazerne,
    b.type_kazerne,
    ROUND(ST_Distance(n.geom, b.geom)::numeric, 0) as afstand_meter,
    CASE 
        WHEN ST_Distance(n.geom, b.geom) < 3000 THEN 'Uitstekend'
        WHEN ST_Distance(n.geom, b.geom) < 5000 THEN 'Goed'
        WHEN ST_Distance(n.geom, b.geom) < 10000 THEN 'Voldoende'
        ELSE 'Onvoldoende'
    END as dekking_rating
FROM calamiteiten.noodsteunpunten n
CROSS JOIN LATERAL (
    SELECT naam, type_kazerne, geom
    FROM calamiteiten.brandweer_kazernes
    ORDER BY n.geom <-> geom
    LIMIT 1
) b
WHERE n.actief = true
ORDER BY afstand_meter DESC;

-- Totale evacuatiecapaciteit per veiligheidsregio
SELECT 
    b.veiligheidsregio,
    COUNT(DISTINCT b.id) as aantal_kazernes,
    COUNT(DISTINCT n.id) as aantal_noodsteunpunten_binnen_10km,
    SUM(n.capaciteit) as totale_evacuatiecapaciteit,
    ROUND(AVG(n.capaciteit)::numeric, 0) as gem_capaciteit_per_locatie,
    SUM(b.aantal_voertuigen) as totaal_brandweerwagens
FROM calamiteiten.brandweer_kazernes b
LEFT JOIN calamiteiten.noodsteunpunten n 
    ON ST_DWithin(b.geom, n.geom, 10000)
    AND n.actief = true
GROUP BY b.veiligheidsregio
HAVING SUM(n.capaciteit) IS NOT NULL
ORDER BY totale_evacuatiecapaciteit DESC;

-- Find noodsteunpunten zonder adequate brandweerdekking (>10km)
SELECT 
    n.id,
    n.naam,
    n.plaats,
    n.capaciteit,
    n.type_locatie,
    ROUND((ST_Distance(n.geom, b_nearest.geom) / 1000)::numeric, 2) as afstand_km
FROM calamiteiten.noodsteunpunten n
CROSS JOIN LATERAL (
    SELECT geom
    FROM calamiteiten.brandweer_kazernes
    ORDER BY n.geom <-> geom
    LIMIT 1
) b_nearest
WHERE NOT EXISTS (
    SELECT 1 
    FROM calamiteiten.brandweer_kazernes b
    WHERE ST_DWithin(n.geom, b.geom, 10000)
)
AND n.actief = true
ORDER BY afstand_km DESC;
```

### Using Views

```sql
-- Overzicht dekking per veiligheidsregio
SELECT * 
FROM calamiteiten.v_statistieken_per_regio
ORDER BY totale_evacuatiecapaciteit DESC;

-- Noodsteunpunten zonder adequate dekking (<5km)
SELECT * 
FROM calamiteiten.v_noodsteun_zonder_dekking
WHERE afstand_dichtstbijzijnde_kazerne_km > 5
ORDER BY afstand_dichtstbijzijnde_kazerne_km DESC;

-- Brandweerkazernes met bereikbare noodopvang
SELECT 
    naam,
    plaats,
    type_kazerne,
    aantal_noodsteunpunten_binnen_5km,
    totale_capaciteit_binnen_5km
FROM calamiteiten.v_brandweer_dekking
WHERE aantal_noodsteunpunten_binnen_5km < 3  -- Mogelijk problematisch
ORDER BY aantal_noodsteunpunten_binnen_5km;
```

### Using Functions

```sql
-- Vind dichtstbijzijnde kazerne voor incident locatie (Dam Amsterdam)
SELECT 
    kazerne_naam,
    ROUND(afstand_meter::numeric, 0) as afstand_m,
    opkomsttijd_geschat as geschatte_opkomsttijd_min
FROM calamiteiten.get_nearest_kazerne(
    ST_SetSRID(ST_MakePoint(121500, 487500), 28992)
);

-- Bereken evacuatiecapaciteit binnen verschillende bereiken
SELECT 
    b.naam as kazerne,
    b.plaats,
    calamiteiten.get_evacuatie_capaciteit(b.id, 3000) as capaciteit_3km,
    calamiteiten.get_evacuatie_capaciteit(b.id, 5000) as capaciteit_5km,
    calamiteiten.get_evacuatie_capaciteit(b.id, 10000) as capaciteit_10km
FROM calamiteiten.brandweer_kazernes b
ORDER BY capaciteit_5km DESC;
```

## 🔍 Troubleshooting

### Database Connection Issues

**Symptom:** `Could not connect to database`

```
ERROR: JDBC connection failed: Connection refused (Connection refused)
```

**Diagnose:**

```bash
# Test database bereikbaar
ping postgres.itlusions.nl

# Test poort open
telnet postgres.itlusions.nl 5432
# Of op Windows:
Test-NetConnection -ComputerName postgres.itlusions.nl -Port 5432

# Test credentials
psql -h postgres.itlusions.nl -U fmeuser -d calamiteiten
```

**Oplossingen:**

1. **Check PostgreSQL running:**
   ```bash
   sudo systemctl status postgresql
   ```

2. **Check `postgresql.conf`:**
   ```
   listen_addresses = '*'  # Of specifiek IP
   port = 5432
   ```

3. **Check `pg_hba.conf`:**
   ```
   # Allow FME Server
   host  calamiteiten  fmeuser  10.0.0.0/8  md5
   # Of specifiek IP:
   host  calamiteiten  fmeuser  10.20.30.40/32  md5
   ```

4. **Reload PostgreSQL:**
   ```bash
   sudo systemctl reload postgresql
   ```

**Symptom:** `Permission denied for schema calamiteiten`

```
ERROR: permission denied for schema calamiteiten
```

**Oplossing:**

```sql
-- Grant all permissions
GRANT USAGE ON SCHEMA calamiteiten TO fmeuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA calamiteiten TO fmeuser;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA calamiteiten TO fmeuser;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA calamiteiten TO fmeuser;

-- Make it permanent for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA calamiteiten
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO fmeuser;
```

### Geometry Issues

**Symptom:** `Invalid geometry detected`

```
GeometryValidator_<Rejected>: 5 features failed validation
```

**Diagnose:**

```sql
-- Check invalide geometrieën
SELECT 
    id, 
    naam,
    ST_IsValid(geom) as is_valid,
    ST_IsValidReason(geom) as invalid_reason
FROM calamiteiten.brandweer_kazernes
WHERE NOT ST_IsValid(geom);
```

**Oplossingen:**

1. **In FME:** Enable auto-repair in GeometryValidator
   - GeometryValidator Parameters
   - Set "Repair" → "Yes"

2. **In database:**
   ```sql
   -- Repair all invalid geometries
   UPDATE calamiteiten.brandweer_kazernes 
   SET geom = ST_MakeValid(geom) 
   WHERE NOT ST_IsValid(geom);
   
   -- Verify
   SELECT COUNT(*) FROM calamiteiten.brandweer_kazernes 
   WHERE NOT ST_IsValid(geom);
   -- Should return 0
   ```

**Symptom:** `Coordinate system not recognized: EPSG:28992`

```
Reprojector: Unknown coordinate system 'EPSG:28992'
```

**Oplossing:**

1. **Update FME:**
   - Help → Check for Updates
   - Install "Coordinate System Updates"

2. **Handmatig toevoegen:**
   - Tools → FME Options → Coordinate Systems
   - Add → Import from EPSG database
   - Search "28992" → Add

3. **Verify:**
   ```
   EPSG:28992 - Amersfoort / RD New
   ```

### Performance Issues

**Symptom:** Workspace loopt traag (>10 min voor 1000 features)

**Diagnose:**

```sql
-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    n_live_tup as row_count
FROM pg_stat_user_tables
WHERE schemaname = 'calamiteiten'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check missing indices
SELECT 
    schemaname,
    tablename,
    attname as column_name,
    n_distinct,
    correlation
FROM pg_stats
WHERE schemaname = 'calamiteiten'
  AND tablename IN ('brandweer_kazernes', 'noodsteunpunten');
```

**Oplossingen:**

1. **Database Indices:**
   ```sql
   -- Check spatial indices exist
   SELECT 
       tablename, 
       indexname, 
       indexdef 
   FROM pg_indexes 
   WHERE schemaname = 'calamiteiten'
     AND indexdef LIKE '%USING gist%';
   
   -- Create if missing
   CREATE INDEX CONCURRENTLY idx_brandweer_geom 
   ON calamiteiten.brandweer_kazernes USING GIST(geom);
   
   CREATE INDEX CONCURRENTLY idx_noodsteun_geom 
   ON calamiteiten.noodsteunpunten USING GIST(geom);
   
   -- Analyze tables
   ANALYZE calamiteiten.brandweer_kazernes;
   ANALYZE calamiteiten.noodsteunpunten;
   ```

2. **FME Bulk Mode:**
   - PostGIS Writer Parameters
   - Feature Operation → INSERT
   - Transaction Interval → 1000
   - Table Handling → Use Existing

3. **Parallel Processing:**
   - Workspace → Workspace Parameters
   - Maximum Parallel Processes → 4-8 (depends on CPU cores)
   - Per transformer: Enable "Parallel Processing" where applicable

4. **Disable Debug Features:**
   - Remove/disable all Inspector transformers
   - Set Log Filter → Errors and Warnings only

5. **Optimize Spatial Queries:**
   ```sql
   -- Use bounding box operator (<->) for nearest neighbor
   -- Instead of ST_Distance in ORDER BY
   SELECT * FROM calamiteiten.brandweer_kazernes
   ORDER BY geom <-> ST_SetSRID(ST_MakePoint(121500, 487500), 28992)
   LIMIT 1;
   ```

### WFS Service Issues

**Symptom:** `WFS request timed out`

```
WFS Reader: Request timeout after 60 seconds
```

**Oplossingen:**

1. **Verhoog timeout:**
   - WFS Reader Parameters
   - Advanced → Timeout: 300 seconds

2. **Use bounding box:**
   - WFS Reader Parameters  
   - Bounding Box: `0,300000,280000,625000` (Nederland RD bbox)

3. **Paginate large requests:**
   - WFS Reader Parameters
   - Max Features: 1000
   - Use Creator + Loop voor iteratie

**Symptom:** `WFS service unavailable`

```
WFS Reader: HTTP 503 Service Unavailable
```

**Diagnose:**

```bash
# Test WFS capabilities
curl "https://service.pdok.nl/kadaster/bestuurlijkegebieden/wfs/v1_0?SERVICE=WFS&REQUEST=GetCapabilities"

# Should return XML with FeatureTypeList
```

**Oplossingen:**

1. **Check proxy settings:**
   - Tools → FME Options → Network
   - Proxy Settings → Configure if behind corporate proxy

2. **Test alternative endpoint:**
   - Try WFS 2.0.0 instead of 1.0.0
   - Check PDOK status: https://www.pdok.nl/status

3. **Firewall:**
   - Allow outbound HTTPS (port 443)
   - Whitelist `*.pdok.nl` domain

## 🌐 Integration Examples

### QGIS Integration

**Via Python Console:**

```python
# Load brandweerkazernes
uri = QgsDataSourceUri()
uri.setConnection("localhost", "5432", "calamiteiten", "fmeuser", "password")
uri.setDataSource("calamiteiten", "brandweer_kazernes", "geom", "", "id")
uri.setSrid("28992")
uri.setWkbType(QgsWkbTypes.Point)

layer = QgsVectorLayer(uri.uri(), "Brandweerkazernes", "postgres")
if layer.isValid():
    QgsProject.instance().addMapLayer(layer)
    
    # Style: red circles
    symbol = QgsMarkerSymbol.createSimple({
        'name': 'circle',
        'color': '255,0,0,255',
        'size': '3',
        'outline_color': 'black',
        'outline_width': '0.5'
    })
    layer.renderer().setSymbol(symbol)
    layer.triggerRepaint()
else:
    print("Layer invalid!")

# Load noodsteunpunten
uri2 = QgsDataSourceUri()
uri2.setConnection("localhost", "5432", "calamiteiten", "fmeuser", "password")
uri2.setDataSource("calamiteiten", "noodsteunpunten", "geom", "actief = true", "id")
uri2.setSrid("28992")

layer2 = QgsVectorLayer(uri2.uri(), "Noodsteunpunten (Actief)", "postgres")
if layer2.isValid():
    QgsProject.instance().addMapLayer(layer2)
```

**Via GUI:**

1. Layer → Add Layer → Add PostGIS Layers
2. New Connection:
   - Name: `Calamiteiten DB`
   - Host: `localhost`
   - Port: `5432`
   - Database: `calamiteiten`
   - SSL mode: prefer
   - Username: `fmeuser`
   - Password: `••••••`
3. Connect → Select schema `calamiteiten`
4. Add tables: `brandweer_kazernes`, `noodsteunpunten`

### Web Map (Leaflet)

**index.html:**

```html
<!DOCTYPE html>
<html>
<head>
    <title>Calamiteitenbeheer NL</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        #map { height: 600px; }
    </style>
</head>
<body>
    <h1>Brandweer & Noodopvanglocaties Nederland</h1>
    <div id="map"></div>

    <script>
        // Initialize map (center Nederland)
        var map = L.map('map').setView([52.1326, 5.2913], 8);

        // Base layer
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        // Brandweerkazernes (red circles)
        var brandweer = L.geoJSON(null, {
            pointToLayer: function(feature, latlng) {
                return L.circleMarker(latlng, {
                    radius: 8,
                    fillColor: "#ff0000",
                    color: "#000",
                    weight: 1,
                    opacity: 1,
                    fillOpacity: 0.8
                });
            },
            onEachFeature: function(feature, layer) {
                var props = feature.properties;
                layer.bindPopup(`
                    <h3>${props.naam}</h3>
                    <b>Type:</b> ${props.type_kazerne}<br>
                    <b>Plaats:</b> ${props.plaats}<br>
                    <b>Voertuigen:</b> ${props.aantal_voertuigen}<br>
                    <b>24/7:</b> ${props.bemanning_24_7 ? 'Ja' : 'Nee'}
                `);
            }
        }).addTo(map);

        // Noodsteunpunten (blue squares)
        var noodsteun = L.geoJSON(null, {
            pointToLayer: function(feature, latlng) {
                return L.circleMarker(latlng, {
                    radius: 6,
                    fillColor: "#0000ff",
                    color: "#000",
                    weight: 1,
                    opacity: 1,
                    fillOpacity: 0.6
                });
            },
            onEachFeature: function(feature, layer) {
                var props = feature.properties;
                layer.bindPopup(`
                    <h3>${props.naam}</h3>
                    <b>Type:</b> ${props.type_locatie}<br>
                    <b>Plaats:</b> ${props.plaats}<br>
                    <b>Capaciteit:</b> ${props.capaciteit} personen<br>
                    <b>Faciliteiten:</b><br>
                    Keuken: ${props.heeft_keuken ? '✓' : '✗'}<br>
                    Sanitair: ${props.heeft_sanitair ? '✓' : '✗'}<br>
                    Noodstroom: ${props.heeft_noodstroom ? '✓' : '✗'}
                `);
            }
        }).addTo(map);

        // Load data from GeoServer/PostGIS via GeoJSON
        fetch('/api/calamiteiten/brandweer_kazernes')
            .then(response => response.json())
            .then(data => brandweer.addData(data))
            .catch(err => console.error('Error loading brandweer:', err));

        fetch('/api/calamiteiten/noodsteunpunten')
            .then(response => response.json())
            .then(data => noodsteun.addData(data))
            .catch(err => console.error('Error loading noodsteunpunten:', err));

        // Layer control
        var overlays = {
            "Brandweerkazernes": brandweer,
            "Noodopvanglocaties": noodsteun
        };
        L.control.layers(null, overlays).addTo(map);
    </script>
</body>
</html>
```

**Backend API (Python/Flask):**

```python
from flask import Flask, jsonify
import psycopg2
from psycopg2.extras import RealDictCursor
import json

app = Flask(__name__)

DB_CONFIG = {
    'host': 'localhost',
    'database': 'calamiteiten',
    'user': 'fmeuser',
    'password': 'your_password'
}

@app.route('/api/calamiteiten/brandweer_kazernes')
def get_brandweer():
    conn = psycopg2.connect(**DB_CONFIG)
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        cur.execute("""
            SELECT 
                id,
                naam,
                plaats,
                type_kazerne,
                aantal_voertuigen,
                bemanning_24_7,
                ST_AsGeoJSON(ST_Transform(geom, 4326))::json as geometry
            FROM calamiteiten.brandweer_kazernes
        """)
        
        features = []
        for row in cur.fetchall():
            feature = {
                'type': 'Feature',
                'geometry': row['geometry'],
                'properties': {k: v for k, v in row.items() if k != 'geometry'}
            }
            features.append(feature)
        
    conn.close()
    
    return jsonify({
        'type': 'FeatureCollection',
        'features': features
    })

@app.route('/api/calamiteiten/noodsteunpunten')
def get_noodsteun():
    conn = psycopg2.connect(**DB_CONFIG)
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        cur.execute("""
            SELECT 
                id,
                naam,
                plaats,
                type_locatie,
                capaciteit,
                heeft_keuken,
                heeft_sanitair,
                heeft_noodstroom,
                ST_AsGeoJSON(ST_Transform(geom, 4326))::json as geometry
            FROM calamiteiten.noodsteunpunten
            WHERE actief = true
        """)
        
        features = []
        for row in cur.fetchall():
            feature = {
                'type': 'Feature',
                'geometry': row['geometry'],
                'properties': {k: v for k, v in row.items() if k != 'geometry'}
            }
            features.append(feature)
        
    conn.close()
    
    return jsonify({
        'type': 'FeatureCollection',
        'features': features
    })

if __name__ == '__main__':
    app.run(debug=True, port=5000)
```

### Python Data Analysis

**analyze_coverage.py:**

```python
import psycopg2
from psycopg2.extras import RealDictCursor
import pandas as pd
import matplotlib.pyplot as plt

# Database connection
conn = psycopg2.connect(
    host="localhost",
    database="calamiteiten",
    user="fmeuser",
    password="your_password"
)

# Query: Evacuatiecapaciteit per veiligheidsregio
query_capaciteit = """
SELECT 
    veiligheidsregio,
    aantal_kazernes,
    aantal_noodsteunpunten,
    totale_evacuatiecapaciteit
FROM calamiteiten.v_statistieken_per_regio
ORDER BY totale_evacuatiecapaciteit DESC;
"""

df_capaciteit = pd.read_sql_query(query_capaciteit, conn)
print(df_capaciteit)

# Plot: Capaciteit per regio
fig, ax = plt.subplots(figsize=(12, 6))
df_capaciteit.plot(
    x='veiligheidsregio',
    y='totale_evacuatiecapaciteit',
    kind='bar',
    ax=ax,
    legend=False
)
ax.set_title('Evacuatiecapaciteit per Veiligheidsregio')
ax.set_xlabel('Veiligheidsregio')
ax.set_ylabel('Totale Capaciteit (personen)')
plt.xticks(rotation=45, ha='right')
plt.tight_layout()
plt.savefig('evacuatiecapaciteit.png', dpi=300)
plt.show()

# Query: Noodsteunpunten zonder adequate dekking
query_dekking = """
SELECT * FROM calamiteiten.v_noodsteun_zonder_dekking
WHERE afstand_dichtstbijzijnde_kazerne_km > 5;
"""

df_dekking = pd.read_sql_query(query_dekking, conn)
print(f"\nAantal locaties zonder dekking (<5km): {len(df_dekking)}")
print(df_dekking[['naam', 'plaats', 'capaciteit', 'afstand_dichtstbijzijnde_kazerne_km']])

conn.close()
```

## 📚 Best Practices

### Security

- ✅ **Wachtwoorden:** Gebruik FME Database Connections of environment variables (nooit hardcoded)
- ✅ **SSL/TLS:** Enable SSL voor database connecties in productie
- ✅ **Least Privilege:** FME user alleen permissions die strikt nodig zijn
- ✅ **Workspace Encryption:** Protect .fmw met password in FME Server
- ✅ **Secrets in K8s:** Use Kubernetes Secrets voor credentials (niet in values.yaml)

### Performance

- ✅ **Bulk Inserts:** Transaction interval 1000-5000 features (niet per feature)
- ✅ **Spatial Indices:** Altijd GIST indices op alle geometry columns
- ✅ **Bounding Box:** Filter WFS/database queries met bbox waar mogelijk
- ✅ **Parallel Processing:** 4-8 parallel processes (CPU cores - 1)
- ✅ **ANALYZE:** Run `ANALYZE` na grote data inserts voor query planning

### Maintainability

- ✅ **Published Parameters:** Alle configuratie als parameters (geen hardcoded waardes)
- ✅ **Logging:** Enable detailed logging + bewaar logs voor troubleshooting
- ✅ **Comments:** Gebruik Annotations in workspace voor documentatie
- ✅ **Version Control:** Commit .fmw + SQL files naar Git
- ✅ **Testing:** Test workspace met sample data voor deployment

### Testing Workflow

1. **Development (small dataset):**
   - Test met 10-50 features
   - Enable Inspectors voor visuele checks
   - Verify geometries, attributes, transformations

2. **Staging (medium dataset):**
   - Test met 1000-5000 features
   - Disable Inspectors
   - Measure performance (execution time)
   - Check database constraints

3. **Production (full dataset):**
   - Full data load
   - Monitor FME Server logs
   - Verify database integrity
   - Check spatial indices used (EXPLAIN ANALYZE)

## 📞 Support & Resources

### FME Community

- **Forum:** https://community.safe.com/
- **Knowledge Base:** https://knowledge.safe.com/
- **Tutorials:** https://www.safe.com/training/

### Documentation

- **FME Desktop:** https://docs.safe.com/fme/html/FME-Desktop-Documentation/
- **FME Server:** https://docs.safe.com/fme/html/FME-Server-Documentation/
- **PostGIS:** https://postgis.net/documentation/

### Dutch Open Data

- **PDOK:** https://www.pdok.nl/ (Publieke Dienstverlening Op de Kaart)
- **Data Overheid:** https://data.overheid.nl/ (Centrale open data portal)
- **Veiligheidsregio's:** https://www.veiligheidsregio.nl/

### ITLusions

- **Support:** support@itlusions.nl
- **Documentatie:** Zie `docs/` folder in deze repository
- **Issues:** GitHub Issues in deze repository

## 📄 License

Dit voorbeeld project valt onder de licentie van de ITL.FMEServer repository.

---

**Versie:** 1.0  
**Laatst bijgewerkt:** December 2025  
**Auteur:** ITLusions  
**Repository:** https://github.com/ITlusions/ITL.FMEServer
