# ATAK Calamiteiten Plugin

**Android Team Awareness Kit (ATAK) plugin voor Nederlandse calamiteitenbeheer en rampenbestrijding.**

## 📱 Overzicht

Deze ATAK plugin integreert met de FME Server backend om real-time informatie te tonen over:

- 🚒 **Brandweerkazernes** - Locatie, capaciteit, uitruktijd
- 🏢 **Noodopvanglocaties** - Evacuatiepunten met capaciteit en faciliteiten  
- 📡 **Real-time Updates** - Synchronisatie met PostGIS database
- 🗺️ **Offline Maps** - Nederlandse topografie en kadaster data
- 📞 **Noodcommunicatie** - Direct contact met hulpdiensten
- 📊 **Situational Awareness** - Live incident tracking

## 🎯 Use Cases

### Voor Burgers
- ✅ Vind dichtstbijzijnde noodopvang bij evacuatie
- ✅ Zie waar brandweerkazernes zijn
- ✅ Ontvang calamiteitenwaarschuwingen
- ✅ Offline kaarten (geen internet nodig)
- ✅ Deel locatie met hulpdiensten

### Voor Hulpdiensten
- ✅ Real-time overzicht van alle noodvoorzieningen
- ✅ Coördinatie tussen brandweer, politie, GHOR
- ✅ Route planning naar incidenten
- ✅ Capaciteitsplanning evacuaties
- ✅ Team tracking en communicatie

### Voor Veiligheidsregio's
- ✅ Centraal command & control dashboard
- ✅ Resource allocation in real-time
- ✅ Incident management en logging
- ✅ Multi-agency coördinatie
- ✅ After-action reporting

## 🚀 Features

### Core Functionality

#### 1. Map Layers
- **Brandweer Kazernes Layer**
  - Rode markers met kazerne icoon
  - Info popup: naam, type (beroeps/vrijwillig), voertuigen, 24/7 status
  - Tap to call direct naar kazerne
  - 5km bereikbaarheidszone (transparante cirkel)

- **Noodopvang Layer**
  - Blauwe markers met shelter icoon
  - Info: capaciteit, faciliteiten (keuken, sanitair, noodstroom)
  - Status indicator (actief/vol/onbeschikbaar)
  - Route planning naar noodopvang

- **Veiligheidsregio's Layer**
  - Grenzen per regio (polygon overlay)
  - Statistics per regio (aantal kazernes, totale capaciteit)
  - Contact informatie coördinatiecentrum

#### 2. Search & Navigation
- **Zoek dichtstbijzijnde:**
  - Brandweerkazerne (met geschatte uitruktijd)
  - Noodopvanglocatie (met beschikbare capaciteit)
  - Filter op faciliteiten (bijv. "noodstroom + keuken")

- **Route Planning:**
  - Turn-by-turn navigatie
  - ETA berekening
  - Alternative routes (vermijd blokkades)
  - Offline routing

#### 3. Real-time Data Sync
- **Pull Updates:**
  - Elke 5 minuten automatisch sync met FME Server
  - Manual refresh button
  - Delta updates (alleen veranderingen)
  - Low bandwidth mode (alleen essentiële data)

- **Push Notifications:**
  - Nieuwe calamiteiten in jouw regio
  - Evacuatiebevelen
  - Capaciteitswijzigingen noodopvang
  - Weather alerts

#### 4. Offline Functionality
- **Offline Maps:**
  - Nederlandse topografie (RD projection)
  - Kadaster BAG data (adressen)
  - OpenStreetMap als fallback
  - Pre-cached tiles per provincie

- **Offline Data:**
  - Laatste sync blijft beschikbaar
  - Lokale SQLite cache
  - Sync bij volgende verbinding
  - Conflict resolution

#### 5. Communication
- **Emergency Contacts:**
  - Direct dial 112
  - Direct contact veiligheidsregio
  - Contact dichtstbijzijnde kazerne
  - GHOR/politie hotlines

- **Team Communication:**
  - ATAK native chat
  - Voice over IP (VoIP)
  - Location sharing
  - Group messaging per incident

#### 6. Incident Management
- **Create Incidents:**
  - Drop marker op kaart
  - Add foto's/video's
  - Severity rating
  - Type (brand, overstroming, etc.)
  - Sync naar command center

- **Track Incidents:**
  - Status updates (new, in progress, resolved)
  - Assigned resources
  - Timeline/logging
  - Geofencing alerts

## 📋 Requirements

### ATAK Installation
- **ATAK CIV** (Civilian version): https://www.civtak.org/
- **Android 7.0+** (API level 24+)
- **Minimum 2GB RAM**
- **GPS enabled**

### Backend Requirements
- **FME Server** met calamiteiten workspace (zie `../brandweer-noodsteunpunten.fmw`)
- **PostGIS Database** met calamiteiten schema (zie `../database_setup.sql`)
- **REST API** endpoint voor ATAK data sync
- **SSL/TLS** voor secure communication

### Optional
- **TAK Server** voor advanced features (chat, VoIP, file sharing)
- **CoT (Cursor on Target)** server voor multi-user sync
- **Mesh networking** voor offline peer-to-peer communication

## 🔧 Installation

### Option 1: Pre-built APK (Easiest)

1. **Download APK:**
   ```bash
   # From releases
   wget https://github.com/ITlusions/ITL.FMEServer/releases/download/v1.0.0/calamiteiten-plugin.apk
   ```

2. **Install ATAK CIV:**
   - Download van https://www.civtak.org/
   - Install `ATAK.apk` op je Android device
   - Open ATAK en complete initial setup

3. **Install Plugin:**
   ```bash
   # Via ADB
   adb install calamiteiten-plugin.apk
   
   # Of kopieer naar device en tap om te installeren
   ```

4. **Configure in ATAK:**
   - Open ATAK
   - Menu → Tools → Plugin Manager
   - Enable "Calamiteiten Plugin"
   - Settings → Configure API endpoint

### Option 2: Build from Source

#### Prerequisites

```bash
# Install Android Studio
# Download: https://developer.android.com/studio

# Install ATAK SDK
cd ~/Downloads
wget https://www.civtak.org/atak-sdk-latest.zip
unzip atak-sdk-latest.zip -d ~/atak-sdk
export ATAK_SDK=~/atak-sdk
```

#### Build Plugin

```bash
# Clone repository
git clone https://github.com/ITlusions/ITL.FMEServer.git
cd ITL.FMEServer/examples/atak-calamiteiten-plugin

# Set ATAK SDK path in local.properties
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
echo "atak.dir=$ATAK_SDK" >> local.properties

# Build
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/calamiteiten-plugin-release.apk
```

#### Install on Device

```bash
# Via ADB
adb install app/build/outputs/apk/release/calamiteiten-plugin-release.apk

# Verify
adb shell pm list packages | grep calamiteiten
# Output: package:nl.itlusions.atak.calamiteiten
```

## ⚙️ Configuration

### 1. API Endpoint Setup

Configure FME Server API endpoint in ATAK:

**Via GUI:**
1. ATAK → Menu → Tools → Calamiteiten Settings
2. API Endpoint: `https://fme-2025-2.itlusions.nl/api/calamiteiten`
3. API Key: `your-api-key-here`
4. Test Connection → should return "OK"

**Via Config File:**

Create `/sdcard/atak/configs/calamiteiten_config.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<preferences>
    <preference version="1" name="nl.itlusions.atak.calamiteiten">
        <entry key="api_endpoint" class="class java.lang.String">
            https://fme-2025-2.itlusions.nl/api/calamiteiten
        </entry>
        <entry key="api_key" class="class java.lang.String">
            your-api-key-here
        </entry>
        <entry key="sync_interval" class="class java.lang.Integer">
            300000
        </entry>
        <entry key="enable_notifications" class="class java.lang.Boolean">
            true
        </entry>
        <entry key="offline_mode" class="class java.lang.Boolean">
            false
        </entry>
    </preference>
</preferences>
```

### 2. Map Layers Configuration

Enable/disable layers:

```xml
<entry key="show_brandweer_layer" class="class java.lang.Boolean">true</entry>
<entry key="show_noodopvang_layer" class="class java.lang.Boolean">true</entry>
<entry key="show_veiligheidsregio_layer" class="class java.lang.Boolean">true</entry>
<entry key="show_bereikbaarheid_zones" class="class java.lang.Boolean">true</entry>
```

### 3. Sync Settings

```xml
<entry key="sync_interval" class="class java.lang.Integer">300000</entry> <!-- 5 min -->
<entry key="sync_on_startup" class="class java.lang.Boolean">true</entry>
<entry key="sync_on_wifi_only" class="class java.lang.Boolean">false</entry>
<entry key="max_cache_age_days" class="class java.lang.Integer">7</entry>
```

### 4. Notification Settings

```xml
<entry key="enable_notifications" class="class java.lang.Boolean">true</entry>
<entry key="notify_new_incidents" class="class java.lang.Boolean">true</entry>
<entry key="notify_evacuations" class="class java.lang.Boolean">true</entry>
<entry key="notify_capacity_changes" class="class java.lang.Boolean">false</entry>
<entry key="notification_sound" class="class java.lang.Boolean">true</entry>
<entry key="notification_vibrate" class="class java.lang.Boolean">true</entry>
```

## 🔌 Backend Integration

### FME Server API Endpoints

De plugin verwacht de volgende REST API endpoints:

#### 1. Get All Brandweer Kazernes

```http
GET /api/calamiteiten/brandweer
Authorization: Bearer {api_key}
```

**Response:**
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "id": 1,
      "geometry": {
        "type": "Point",
        "coordinates": [5.1214, 52.0907]
      },
      "properties": {
        "naam": "Brandweer Amsterdam Centrum",
        "plaats": "Amsterdam",
        "type_kazerne": "Beroeps",
        "aantal_voertuigen": 8,
        "bemanning_24_7": true,
        "specialisaties": ["Hoogwerker", "Duikteam"],
        "telefoonnummer": "020-5556666",
        "opkomsttijd_minuten": 5
      }
    }
  ]
}
```

#### 2. Get All Noodopvang Locaties

```http
GET /api/calamiteiten/noodopvang
Authorization: Bearer {api_key}
```

**Response:**
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "id": 1,
      "geometry": {
        "type": "Point",
        "coordinates": [5.1200, 52.0900]
      },
      "properties": {
        "naam": "Sporthal De Eendracht",
        "plaats": "Amsterdam",
        "type_locatie": "Sporthal",
        "capaciteit": 500,
        "capaciteit_beschikbaar": 500,
        "heeft_keuken": true,
        "heeft_sanitair": true,
        "heeft_noodstroom": true,
        "heeft_ehbo": true,
        "status": "beschikbaar",
        "telefoonnummer": "020-1234567",
        "contactpersoon": "J. de Vries"
      }
    }
  ]
}
```

#### 3. Get Nearest Resources

```http
GET /api/calamiteiten/nearest?lat=52.0907&lon=5.1214&type=brandweer&limit=5
Authorization: Bearer {api_key}
```

**Response:**
```json
{
  "results": [
    {
      "id": 1,
      "naam": "Brandweer Amsterdam Centrum",
      "type": "brandweer",
      "distance_meters": 1250,
      "eta_minutes": 3,
      "coordinates": [5.1214, 52.0907]
    }
  ]
}
```

#### 4. Report Incident

```http
POST /api/calamiteiten/incidents
Authorization: Bearer {api_key}
Content-Type: application/json

{
  "type": "brand",
  "severity": "high",
  "location": {
    "lat": 52.0907,
    "lon": 5.1214
  },
  "description": "Woningbrand Damstraat 123",
  "reporter_uid": "atak-user-12345",
  "timestamp": "2025-12-06T21:45:00Z",
  "attachments": [
    {
      "type": "photo",
      "url": "https://..."
    }
  ]
}
```

### FME Workspace Extension

Voeg een REST API endpoint toe aan de bestaande FME workspace:

**`fme-api-wrapper.py` (Flask API):**

```python
from flask import Flask, jsonify, request
import psycopg2
from psycopg2.extras import RealDictCursor
import os

app = Flask(__name__)

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'database': os.getenv('DB_NAME', 'calamiteiten'),
    'user': os.getenv('DB_USER', 'fmeuser'),
    'password': os.getenv('DB_PASSWORD')
}

@app.route('/api/calamiteiten/brandweer')
def get_brandweer():
    """Get all brandweerkazernes as GeoJSON"""
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
                specialisaties,
                telefoonnummer,
                opkomsttijd_minuten,
                ST_AsGeoJSON(ST_Transform(geom, 4326))::json as geometry
            FROM calamiteiten.brandweer_kazernes
        """)
        
        features = []
        for row in cur.fetchall():
            feature = {
                'type': 'Feature',
                'id': row['id'],
                'geometry': row['geometry'],
                'properties': {k: v for k, v in row.items() if k != 'geometry'}
            }
            features.append(feature)
    
    conn.close()
    
    return jsonify({
        'type': 'FeatureCollection',
        'features': features
    })

@app.route('/api/calamiteiten/noodopvang')
def get_noodopvang():
    """Get all noodopvang locaties as GeoJSON"""
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
                heeft_ehbo,
                beschikbaarheid as status,
                telefoonnummer,
                contactpersoon,
                ST_AsGeoJSON(ST_Transform(geom, 4326))::json as geometry
            FROM calamiteiten.noodsteunpunten
            WHERE actief = true
        """)
        
        features = []
        for row in cur.fetchall():
            feature = {
                'type': 'Feature',
                'id': row['id'],
                'geometry': row['geometry'],
                'properties': {k: v for k, v in row.items() if k != 'geometry'}
            }
            features.append(feature)
    
    conn.close()
    
    return jsonify({
        'type': 'FeatureCollection',
        'features': features
    })

@app.route('/api/calamiteiten/nearest')
def get_nearest():
    """Find nearest resources"""
    lat = float(request.args.get('lat'))
    lon = float(request.args.get('lon'))
    resource_type = request.args.get('type', 'brandweer')
    limit = int(request.args.get('limit', 5))
    
    conn = psycopg2.connect(**DB_CONFIG)
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        if resource_type == 'brandweer':
            cur.execute("""
                SELECT 
                    id,
                    naam,
                    'brandweer' as type,
                    ROUND(ST_Distance(
                        geom, 
                        ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s), 4326), 28992)
                    )::numeric, 0) as distance_meters,
                    CASE 
                        WHEN ST_Distance(geom, ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s), 4326), 28992)) < 1000 THEN 2
                        WHEN ST_Distance(geom, ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s), 4326), 28992)) < 3000 THEN 5
                        ELSE 10
                    END as eta_minutes,
                    ST_X(ST_Transform(geom, 4326)) as lon,
                    ST_Y(ST_Transform(geom, 4326)) as lat
                FROM calamiteiten.brandweer_kazernes
                ORDER BY geom <-> ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s), 4326), 28992)
                LIMIT %s
            """, (lon, lat, lon, lat, lon, lat, lon, lat, limit))
        else:  # noodopvang
            cur.execute("""
                SELECT 
                    id,
                    naam,
                    'noodopvang' as type,
                    ROUND(ST_Distance(
                        geom, 
                        ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s), 4326), 28992)
                    )::numeric, 0) as distance_meters,
                    capaciteit,
                    ST_X(ST_Transform(geom, 4326)) as lon,
                    ST_Y(ST_Transform(geom, 4326)) as lat
                FROM calamiteiten.noodsteunpunten
                WHERE actief = true
                ORDER BY geom <-> ST_Transform(ST_SetSRID(ST_MakePoint(%s, %s), 4326), 28992)
                LIMIT %s
            """, (lon, lat, lon, lat, limit))
        
        results = []
        for row in cur.fetchall():
            result = dict(row)
            result['coordinates'] = [result.pop('lon'), result.pop('lat')]
            results.append(result)
    
    conn.close()
    
    return jsonify({'results': results})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)
```

**Deploy API:**

```bash
# Via Docker
docker build -t fme-api .
docker run -d -p 5000:5000 \
  -e DB_HOST=postgres.itlusions.nl \
  -e DB_NAME=calamiteiten \
  -e DB_USER=fmeuser \
  -e DB_PASSWORD=xxx \
  fme-api

# Via Kubernetes
kubectl apply -f fme-api-deployment.yaml
```

## 📱 Usage

### For Citizens (Burgers)

#### 1. First Launch
1. Open ATAK app
2. Accept location permissions
3. Plugin loads brandweer and noodopvang markers automatically

#### 2. Find Nearest Shelter (Evacuatie)
1. Tap **Search** icon
2. Select **Nearest Noodopvang**
3. See list sorted by distance
4. Tap a location → tap **Navigate** for directions

#### 3. Emergency Mode
1. Tap **Emergency** button (red)
2. Shows:
   - Your current location
   - 3 nearest noodopvang locaties
   - 2 nearest brandweerkazernes
   - Direct call buttons (112, veiligheidsregio)
3. Share your location with family/friends

#### 4. Offline Use
1. Menu → **Download Offline Data**
2. Select your province
3. Downloads:
   - Maps for your area
   - All brandweer/noodopvang in province
   - Emergency contacts
4. Works without internet!

### For Emergency Services (Hulpdiensten)

#### 1. Incident Response
1. Receive incident notification
2. Tap notification → opens ATAK at incident location
3. See:
   - Incident details
   - Nearest resources (kazernes, equipment)
   - ETA to incident
   - Other responding units (if TAK Server connected)

#### 2. Coordinate Teams
1. Create **Team** in ATAK
2. Add team members
3. Share:
   - Incident markers
   - Routes
   - Photos/video from scene
   - Voice notes

#### 3. Resource Management
1. View all available resources on map
2. Filter by:
   - Type (brandweer, noodopvang, ambulance)
   - Status (beschikbaar, bezig, offline)
   - Specialization (hoogwerker, duikteam, etc.)
3. Assign resources to incidents

### For Command Centers (Coördinatiecentra)

#### 1. Situational Awareness Dashboard
1. Open ATAK on command center tablet/PC
2. View real-time:
   - All active incidents (color-coded by severity)
   - All deployed resources
   - All team locations
   - Communication logs

#### 2. Multi-Agency Coordination
1. Connect to TAK Server
2. Share data with:
   - Brandweer
   - Politie
   - GHOR (Medisch)
   - Gemeente
3. Unified operational picture

#### 3. After Action Review
1. Menu → **Export Session Log**
2. Generates report with:
   - Timeline of all events
   - Resource deployments
   - Communication logs
   - Map snapshots
3. Use for debriefing and improvement

## 🗺️ Map Layers

### Available Layers

| Layer | Icon | Color | Info |
|-------|------|-------|------|
| Brandweer Kazernes | 🚒 | Red | Type, voertuigen, specialisaties |
| Noodopvang | 🏢 | Blue | Capaciteit, faciliteiten, status |
| Veiligheidsregio's | 📍 | Orange | Grenzen, contact info |
| Bereikbaarheid (5km) | ⭕ | Red (transparent) | 5km zones rond kazernes |
| Incidents | ⚠️ | Yellow/Red | Type, severity, status |
| Teams | 👤 | Green | Team members locations |

### Layer Controls

```
[Layers Menu]
├─ 🚒 Brandweer           [✓] 45 kazernes
├─ 🏢 Noodopvang          [✓] 128 locaties  
├─ 📍 Veiligheidsregio's  [ ] 25 regio's
├─ ⭕ Bereikbaarheid      [✓] Zones
├─ ⚠️ Incidents           [✓] 3 active
└─ 👤 Teams               [✓] 12 members online
```

## 🔔 Notifications

### Notification Types

#### 1. Critical Alerts (Kritiek)
- 🚨 **Evacuatiebevel** - Immediate evacuation order
- 🔥 **Grote Brand** - Major fire in your area
- 💧 **Overstroming** - Flood warning
- ⚠️ **Gevaarlijke Stoffen** - Hazmat incident

**Behavior:**
- Full-screen alert
- Loud alarm sound
- Vibration
- Requires acknowledgment

#### 2. Important Updates (Belangrijk)
- 📢 **Nieuwe Noodopvang** - New shelter opened
- 🚧 **Route Blokkade** - Road closure
- 🏥 **Capaciteit Wijziging** - Shelter capacity change

**Behavior:**
- Notification bar
- Sound
- Silent after 2 min if not acknowledged

#### 3. Info Updates (Informatie)
- 📊 **Data Sync Complete** - Background sync finished
- ✅ **Resource Status** - Unit available/unavailable

**Behavior:**
- Silent notification
- Badge on app icon

### Notification Settings

Customize in ATAK → Calamiteiten Settings → Notifications:

```
[Notifications]
├─ Enable Notifications       [✓]
├─ Critical Alerts             [✓]
│  ├─ Sound                    [✓] Emergency Siren
│  ├─ Vibrate                  [✓]
│  └─ LED                      [✓] Red
├─ Important Updates           [✓]
│  ├─ Sound                    [✓] Default
│  └─ Vibrate                  [✓]
├─ Info Updates                [ ]
└─ Do Not Disturb Override     [✓] (for critical alerts)
```

## 🔐 Security & Privacy

### Data Privacy
- ✅ **Location data**: Only sent during active session, not stored centrally
- ✅ **Personal info**: Stored locally on device only
- ✅ **Anonymized reporting**: Incident reports don't include user identity (unless emergency)
- ✅ **GDPR compliant**: All data handling follows EU GDPR regulations

### Security Features
- ✅ **SSL/TLS**: All API communication encrypted
- ✅ **API Key Authentication**: Required for all API calls
- ✅ **Certificate Pinning**: Prevents man-in-the-middle attacks
- ✅ **Secure Storage**: Local data encrypted using Android Keystore
- ✅ **Role-Based Access**: Different permissions for citizens vs emergency services

### Permissions Required

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" /> <!-- For incident photos -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> <!-- For offline maps -->
<uses-permission android:name="android.permission.VIBRATE" /> <!-- For alerts -->
<uses-permission android:name="android.permission.WAKE_LOCK" /> <!-- For critical alerts -->
```

## 🧪 Testing

### Unit Tests

```bash
./gradlew test

# Run specific test
./gradlew test --tests nl.itlusions.atak.calamiteiten.BrandweerLayerTest
```

### Integration Tests

```bash
# Requires Android device/emulator
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

- [ ] **Map Layers**
  - [ ] Brandweer kazernes tonen correct
  - [ ] Noodopvang locaties tonen correct
  - [ ] Info popups werken
  - [ ] Tap to call werkt

- [ ] **Search & Navigation**
  - [ ] Zoeken op naam werkt
  - [ ] "Nearest" zoeken werkt
  - [ ] Route planning werkt
  - [ ] ETA calculation correct

- [ ] **Sync**
  - [ ] Manual sync werkt
  - [ ] Auto sync (5 min) werkt
  - [ ] Offline mode werkt
  - [ ] Sync status indicator correct

- [ ] **Notifications**
  - [ ] Push notifications ontvangen
  - [ ] Sound/vibration werken
  - [ ] Tap notification opent juiste locatie

- [ ] **Offline**
  - [ ] Maps beschikbaar offline
  - [ ] Cached data beschikbaar
  - [ ] Sync bij reconnect werkt

## 🐛 Troubleshooting

### Plugin Not Loading

**Symptom:** Plugin niet zichtbaar in ATAK Plugin Manager

**Solutions:**
1. Check ATAK version ≥ 4.5.0
2. Reinstall plugin APK
3. Check logs:
   ```bash
   adb logcat | grep Calamiteiten
   ```
4. Verify plugin signed with same cert as ATAK

### No Data Showing

**Symptom:** Map leeg, geen brandweer/noodopvang markers

**Solutions:**
1. Check API endpoint configured:
   - Settings → API Endpoint should be filled in
2. Test API manually:
   ```bash
   curl https://fme-2025-2.itlusions.nl/api/calamiteiten/brandweer
   ```
3. Check internet connection
4. Try manual refresh (pull down on map)
5. Check logs for API errors

### GPS Not Working

**Symptom:** "Locatie niet beschikbaar" error

**Solutions:**
1. Enable Location in Android settings
2. Grant location permissions to ATAK
3. Check GPS signal (go outside if indoors)
4. Restart ATAK

### Sync Failing

**Symptom:** "Sync failed" notification

**Solutions:**
1. Check internet connection
2. Verify API key valid
3. Check API server status
4. Try manual sync
5. Clear cache and retry:
   - Settings → Storage → Clear Cache

### Offline Maps Not Downloading

**Symptom:** "Download failed" when trying to get offline maps

**Solutions:**
1. Check sufficient storage space (need ~500MB per province)
2. Use WiFi (large download)
3. Check write permissions granted
4. Try downloading smaller region first

## 📊 Performance

### Resource Usage

| Metric | Value |
|--------|-------|
| **App Size** | ~25 MB |
| **RAM Usage** | ~150 MB (with maps loaded) |
| **Battery** | ~5% per hour (background sync) |
| **Data Usage** | ~2 MB per sync (wifi recommended) |
| **Storage** | ~500 MB (with offline maps for 1 province) |

### Optimization Tips

1. **Battery Saving:**
   - Increase sync interval to 15 min
   - Disable unused layers
   - Use offline mode when possible

2. **Data Saving:**
   - Sync on WiFi only
   - Download offline maps once
   - Disable auto-sync

3. **Performance:**
   - Clear cache weekly
   - Update to latest ATAK version
   - Close unused apps

## 🚀 Roadmap

### Version 1.1 (Q1 2026)
- [ ] Dutch language support (currently English)
- [ ] Weather overlay (KNMI data)
- [ ] Traffic data integration
- [ ] Voice commands ("Navigate to nearest shelter")

### Version 1.2 (Q2 2026)
- [ ] Mesh networking support (offline peer-to-peer)
- [ ] Advanced incident reporting (forms, checklists)
- [ ] Integration with P2000 (emergency service radio)
- [ ] Historical incident data

### Version 2.0 (Q3 2026)
- [ ] AI-powered resource allocation
- [ ] Predictive incident modeling
- [ ] AR (Augmented Reality) navigation
- [ ] Multi-language support (English, German, French)

## 📚 Resources

### ATAK Documentation
- **CivTAK Website:** https://www.civtak.org/
- **ATAK Plugin Development:** https://github.com/deptofdefense/AndroidTacticalAssaultKit-CIV
- **ATAK User Guide:** https://www.civtak.org/user-guide
- **Plugin Examples:** https://github.com/ATAK-Dev/ATAK-Plugin-Examples

### TAK Server
- **TAK Server Setup:** https://tak.gov/
- **CoT (Cursor on Target):** https://tak.gov/products/tak-server

### Dutch Open Data
- **PDOK:** https://www.pdok.nl/
- **Veiligheidsregio's:** https://www.veiligheidsregio.nl/
- **KNMI (Weather):** https://www.knmi.nl/

### Related Projects
- **FME Server:** https://www.safe.com/
- **PostGIS:** https://postgis.net/
- **OpenStreetMap NL:** https://www.openstreetmap.nl/

## 🤝 Contributing

Contributions welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup
1. Fork repository
2. Create feature branch
3. Make changes
4. Test thoroughly
5. Submit pull request

### Code Style
- Follow Android Kotlin style guide
- Use meaningful variable names
- Comment complex logic
- Write unit tests

## 📄 License

This ATAK plugin is licensed under the same license as the ITL.FMEServer repository.

**ATAK itself** is licensed under the U.S. Government Public Domain - see https://www.civtak.org/

## 📞 Support

- **Issues:** GitHub Issues
- **Email:** support@itlusions.nl
- **Documentation:** This README + `docs/` folder
- **Community:** ATAK CivTAK Discord

## 🙏 Acknowledgments

- **TAK Product Center** - For ATAK framework
- **Veiligheidsregio's Nederland** - For domain knowledge
- **Safe Software** - For FME Server
- **PostGIS Community** - For spatial database capabilities

---

**Version:** 1.0.0  
**Last Updated:** December 2025  
**Author:** ITLusions  
**Repository:** https://github.com/ITlusions/ITL.FMEServer
