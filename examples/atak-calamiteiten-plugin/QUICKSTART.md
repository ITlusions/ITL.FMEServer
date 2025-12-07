# ATAK Calamiteiten Plugin - Quick Start Guide

**Snel aan de slag voor burgers die de app willen gebruiken tijdens calamiteiten.**

## 📱 Wat is ATAK?

**ATAK (Android Team Awareness Kit)** is een gratis militaire-grade kaart- en communicatie-app voor Android, nu beschikbaar voor burgers via **CivTAK**. Oorspronkelijk ontwikkeld voor het Amerikaanse leger, nu wereldwijd gebruikt door hulpdiensten, NGO's en burgers.

**Waarom ATAK gebruiken?**
- ✅ **Werkt offline** - Geen internet nodig
- ✅ **Gratis** - Open source, geen kosten
- ✅ **Betrouwbaar** - Battle-tested, werkt altijd
- ✅ **Privacy** - Jouw locatie blijft privé tenzij je deelt
- ✅ **Professioneel** - Gebruikt door hulpdiensten wereldwijd

## 🚀 Installatie (5 minuten)

### Stap 1: Installeer ATAK CIV

1. **Download ATAK CIV:**
   - Website: https://www.civtak.org/
   - Klik **Download** (Android 7.0+)
   - Download `ATAK-CIV-latest.apk` (~180 MB)

2. **Installeer APK:**
   - Ga naar **Downloads** folder op je telefoon
   - Tap `ATAK-CIV-latest.apk`
   - Als je een waarschuwing krijgt: **Settings** → **Allow from this source**
   - Tap **Install**

3. **Open ATAK:**
   - Tap **Open** na installatie
   - Accept **Terms of Service**
   - Grant **Location Permission** → **Allow all the time** (aanbevolen)
   - Grant **Storage Permission** → **Allow**

4. **Eerste Setup:**
   - Kies **Callsign** (jouw naam/bijnaam, bijv. "Jan-Amsterdam")
   - Kies **Team Color** (bijv. "Blue")
   - Tap **Done**

✅ **ATAK is nu geïnstalleerd!**

### Stap 2: Installeer Calamiteiten Plugin

1. **Download plugin:**
   - Website: https://github.com/ITlusions/ITL.FMEServer/releases
   - Download `calamiteiten-plugin-v1.0.0.apk` (~25 MB)

2. **Installeer plugin:**
   - Ga naar **Downloads**
   - Tap `calamiteiten-plugin-v1.0.0.apk`
   - Tap **Install**

3. **Activeer in ATAK:**
   - Open **ATAK**
   - Tap **☰** (menu links boven)
   - **Tools** → **Plugin Manager**
   - Zoek **Calamiteiten**
   - Slide toggle naar **ON** (groen)
   - Tap **Done**

✅ **Plugin is nu actief!**

### Stap 3: Configureer API (eenmalig)

1. **Open Plugin Settings:**
   - In ATAK: **☰** → **Settings** → **Calamiteiten**

2. **Vul in:**
   ```
   API Endpoint: https://fme-2025-2.itlusions.nl/api/calamiteiten
   API Key: (vraag aan je gemeente/veiligheidsregio)
   ```
   
3. **Test Connection:**
   - Tap **Test Connection**
   - Moet zeggen: **"Connection OK ✓"**

4. **Sync Data:**
   - Tap **Sync Now**
   - Wacht 10-30 seconden
   - Melding: **"Sync complete: 45 brandweerkazernes, 128 noodopvang"**

✅ **Je bent klaar!**

## 🗺️ Basis Gebruik

### Kaart Navigatie

**Zoom:**
- **Pinch** = Zoom in/out
- **Double tap** = Zoom in
- **Two-finger tap** = Zoom out

**Pan:**
- **Drag** = Beweeg kaart

**Rotate:**
- **Two-finger rotate** = Draai kaart

**Your Location:**
- **Blue circle** = Jouw locatie
- **Blue arrow** = Jouw richting (als je beweegt)

### Markers Herkennen

**🚒 Rode markers** = Brandweerkazernes
- Tap marker → Zie info (naam, voertuigen, type)
- Tap **Call** → Bel kazerne direct
- Tap **Navigate** → Route planning

**🏢 Blauwe markers** = Noodopvang (evacuatielocaties)
- Tap marker → Zie capaciteit, faciliteiten
- **Groen** = Beschikbaar
- **Oranje** = Bijna vol
- **Rood** = Vol/gesloten
- Tap **Navigate** → Route naar noodopvang

**⭕ Rode cirkels** = Bereikbaarheidszones (5km rond brandweer)

### Zoeken

**Zoek op naam:**
1. Tap **🔍** (zoek icon rechts boven)
2. Type naam (bijv. "Amsterdam")
3. Selecteer uit lijst
4. Kaart centreert op locatie

**Vind dichtstbijzijnde:**
1. Tap **☰** → **Calamiteiten** → **Find Nearest**
2. Kies type:
   - **Brandweer** = Dichtstbijzijnde kazerne
   - **Noodopvang** = Dichtstbijzijnde evacuatielocatie
3. Zie lijst gesorteerd op afstand
4. Tap locatie → Tap **Navigate**

### Navigatie

**Route planning:**
1. Tap marker (brandweer of noodopvang)
2. Tap **Navigate** in popup
3. Kies:
   - **Drive** = Auto route
   - **Walk** = Wandel route
4. Volg route (blauwe lijn)
5. Geschatte aankomsttijd (ETA) bovenaan

**Turn-by-turn:**
- **Voice guidance** = Stem instructies
- **Text directions** = Tekst instructies onderaan
- **Re-route** = Automatisch als je afwijkt

## 🆘 Noodsituatie Gebruik

### Scenario: Evacuatie Bevel

Je ontvangt evacuatiebevel via TV/radio/sirene.

**Wat te doen:**

1. **Open ATAK** op je telefoon

2. **Vind dichtstbijzijnde noodopvang:**
   - Tap **☰** → **Calamiteiten** → **Emergency Mode**
   - Of: Tap rode **EMERGENCY** knop rechtsonder
   
3. **Zie overzicht:**
   - Jouw locatie (blauw)
   - 3 dichtstbijzijnde noodopvang locaties (groen)
   - Afstand en ETA
   - Beschikbare capaciteit

4. **Kies locatie:**
   - Check **faciliteiten** (keuken, bedden, etc.)
   - Check **capaciteit** (niet vol?)
   - Tap **Navigate**

5. **Reis:**
   - Volg route in ATAK
   - **Werkt offline!** (geen internet nodig)
   - ETA update automatisch

6. **Aangekomen:**
   - Meld je bij contactpersoon
   - ATAK blijft beschikbaar voor updates

### Scenario: Brand in de Buurt

Je ziet rook/vuur in de buurt.

**Wat te doen:**

1. **Bel 112** (altijd eerst!)

2. **Open ATAK:**
   - Tap lange tijd op **rooklocatie** op kaart
   - Menu: **Create Marker**
   - Type: **Fire** 🔥
   - Severity: **High**
   - Tap **Save**

3. **Zie brandweer:**
   - Rode kazerne markers
   - Geschatte uitruktijd
   - Bereikbaarheidszones

4. **Veilig wegkomen:**
   - **☰** → **Calamiteiten** → **Evacuation Route**
   - Kies richting weg van rook
   - Volg route naar veilige zone

### Scenario: Vermist Persoon (Familie/Vriend)

Tijdens calamiteit kun je familie/vrienden niet bereiken.

**Wat te doen (met toestemming):**

1. **Deel jouw locatie:**
   - Tap **☰** → **Tools** → **Share Location**
   - Kies contact
   - Duur: bijv. "24 hours"
   - Send via SMS/email

2. **Zie locatie van anderen:**
   - Als zij ook ATAK hebben
   - Hun marker verschijnt als **groene punt**
   - Tap marker → Zie naam, tijd
   - Tap **Navigate** → Route naar hen toe

3. **Groep chat:**
   - **☰** → **Chat**
   - Create **Team Chat**
   - Nodig familie/vrienden uit
   - Stuur berichten, locaties, foto's

## 📴 Offline Gebruik

**Voor calamiteiten: Download offline data!**

### Download Offline Kaarten

1. **☰** → **Settings** → **Calamiteiten** → **Offline Maps**

2. **Kies provincie:**
   - Noord-Holland (~350 MB)
   - Zuid-Holland (~400 MB)
   - Utrecht (~200 MB)
   - etc.

3. **Tap Download:**
   - Bij voorkeur op **WiFi** (groot bestand)
   - Duurt ~5-10 minuten
   - Controleer voldoende opslagruimte

4. **Klaar!**
   - Kaarten nu beschikbaar zonder internet
   - Brandweer/noodopvang data ook offline

✅ **Nu werkt app volledig zonder internet!**

### Wat werkt offline?

- ✅ Kaarten zien
- ✅ Brandweerkazernes zien
- ✅ Noodopvang locaties zien
- ✅ Route planning (basis)
- ✅ GPS tracking
- ✅ Create markers
- ✅ Lokale chat (via Bluetooth/WiFi Direct)

### Wat werkt NIET offline?

- ❌ Real-time updates (nieuwe kazernes/noodopvang)
- ❌ Status wijzigingen (vol/beschikbaar)
- ❌ Internet chat/calls
- ❌ Weather updates

**Tip:** Sync data dagelijks als je internet hebt!

## ⚙️ Belangrijke Settings

### Privacy Instellingen

**☰** → **Settings** → **Calamiteiten** → **Privacy**

```
Share My Location:        [OFF]  ← Alleen aan tijdens nood
Location History:         [OFF]  ← Privacy
Anonymous Reporting:      [ON]   ← Aanbevolen
```

**Belangrijk:** Jouw locatie wordt NOOIT automatisch gedeeld!

### Notificatie Instellingen

**☰** → **Settings** → **Calamiteiten** → **Notifications**

```
Enable Notifications:     [ON]
Critical Alerts:          [ON]   ← Evacuatiebevelen
  - Sound:                [Emergency Siren]
  - Vibrate:              [ON]
Important Updates:        [ON]   ← Nieuwe noodopvang
  - Sound:                [Default]
Info Updates:             [OFF]  ← Niet nodig
Do Not Disturb Override:  [ON]   ← Kritieke alerts altijd
```

### Sync Instellingen

**☰** → **Settings** → **Calamiteiten** → **Sync**

```
Auto Sync:                [ON]
Sync Interval:            [5 minutes]  ← Standaard
Sync on WiFi Only:        [OFF]        ← Ook mobiel data
Manual Sync:              [Tap "Sync Now"]
Last Sync:                [2 min ago]
```

## 🔋 Batterij Besparen

ATAK kan veel batterij gebruiken. Tips:

**Voor normale dagen (niet-nood):**
1. **Close app** als je niet gebruikt
2. **Sync interval** → 15 minuten (ipv 5)
3. **GPS mode** → "Battery Saving"
4. **Screen brightness** → Laag

**Voor noodsituaties:**
1. **Activeer Battery Saver** in Android
2. **Close andere apps**
3. **Dim screen** (maar blijf zichtbaar)
4. **Portable charger** meenemen (essentieel!)

**Battery usage:** ~5% per uur bij normaal gebruik

## ❓ Veelgestelde Vragen (FAQ)

### Algemeen

**Q: Is ATAK gratis?**
A: Ja, volledig gratis. Open source.

**Q: Werkt het echt zonder internet?**
A: Ja! Als je offline kaarten hebt gedownload. GPS werkt altijd.

**Q: Is mijn locatie zichtbaar voor anderen?**
A: Nee, tenzij je expliciet deelt via "Share Location".

**Q: Kan ik het gebruiken in België/Duitsland?**
A: Plugin is specifiek voor Nederlandse data. ATAK zelf werkt overal.

**Q: Moet ik betalen voor API?**
A: Nee, API key krijg je gratis van je gemeente/veiligheidsregio.

### Technisch

**Q: App crashed tijdens openen**
A: Herstart telefoon. Verwijder en herinstalleer ATAK. Check Android versie ≥ 7.0.

**Q: Geen brandweerkazernes zichtbaar**
A: Check API key correct ingevuld. Tap "Sync Now". Check internet connectie.

**Q: GPS werkt niet**
A: Settings → Apps → ATAK → Permissions → Location → "Allow all the time".

**Q: Kaart laadt niet**
A: Download offline kaarten. Of check internet voor online tiles.

**Q: Plugin not found**
A: ATAK → Tools → Plugin Manager → Check "Calamiteiten" is ON (groen).

### Noodsituaties

**Q: Moet ik eerst 112 bellen of ATAK gebruiken?**
A: **ALTIJD eerst 112 bellen!** ATAK is extra tool, niet vervanging.

**Q: Hoe weet ik dat noodopvang open is?**
A: Marker kleur: Groen = open. Check altijd eerst telefonisch.

**Q: Route wijkt af van normale weg**
A: ATAK probeert blokkades te vermijden. Volg veiligste route.

**Q: Batterij bijna leeg tijdens evacuatie**
A: Batterijbesparingsmodus. Close andere apps. Dim screen. Zoek stopcontact bij noodopvang.

## 📞 Hulp & Support

### Problemen met Plugin?

**Email:** support@itlusions.nl
**Website:** https://github.com/ITlusions/ITL.FMEServer
**Issues:** GitHub Issues

### Problemen met ATAK zelf?

**CivTAK Forum:** https://www.civtak.org/forum
**Discord:** CivTAK Community Discord
**Email:** support@civtak.org

### Nood (112)?

**Brand, medisch, politie:** Bel **112** (altijd eerst!)

**Niet-spoed vragen:**
- **Gemeente:** 14 + gemeentenummer (bijv. 14020 Amsterdam)
- **Veiligheidsregio:** Check nummers in app onder ⓘ

## 📋 Checklist: Ben je voorbereid?

Print deze checklist en hang op:

### Installatie
- [ ] ATAK CIV geïnstalleerd
- [ ] Calamiteiten plugin geïnstalleerd
- [ ] API key geconfigureerd
- [ ] Test sync gedaan (data geladen?)
- [ ] Offline kaarten gedownload voor jouw provincie

### Settings
- [ ] Notificaties aan (kritieke alerts)
- [ ] GPS altijd toegestaan
- [ ] Battery saver instellingen geconfigureerd

### Voorbereiding
- [ ] Portable charger/powerbank aanwezig (10.000+ mAh)
- [ ] Familie/vrienden ook ATAK geïnstalleerd?
- [ ] Noodcontacten opgeslagen in telefoon
- [ ] App getest (weet je hoe te navigeren?)

### Noodpakket
- [ ] Telefoon + charger
- [ ] Powerbank + kabel
- [ ] Papieren kaart (backup)
- [ ] Zaklamp
- [ ] Water + eten
- [ ] EHBO kit
- [ ] Belangrijke documenten (ID, paspoort, verzekering)

## 🎓 Training

**Oefen nu, niet tijdens calamiteit!**

### Oefening 1: Vind Noodopvang (5 min)

1. Open ATAK
2. Find nearest noodopvang
3. Navigate naar locatie
4. Bekijk route (volg NIET echt, alleen kijken)
5. Check faciliteiten (keuken, bedden, etc.)

### Oefening 2: Offline Test (10 min)

1. Download offline kaarten (als nog niet gedaan)
2. Zet **Airplane mode** aan
3. Open ATAK
4. Check brandweerkazernes nog zichtbaar?
5. Plan route (werkt het nog?)
6. Airplane mode uit

### Oefening 3: Familie Delen (15 min)

1. Vraag familielid ook ATAK te installeren
2. Deel jouw locatie met hen
3. Zie jij hun locatie?
4. Stuur chat bericht
5. Plan route naar elkaar

### Oefening 4: Evacuatie Simulatie (30 min)

**Scenario:** Je moet huis verlaten binnen 15 min

1. Start timer (15 min)
2. Pak noodpakket (zie checklist)
3. Open ATAK → Emergency Mode
4. Kies dichtstbijzijnde noodopvang
5. Plan route
6. Loop/fiets eerste 500m (test route)
7. Keer terug

**Evalueer:** Wat ging goed? Wat kan beter?

## 📱 Voor Gevorderden

### Custom Markers

Maak eigen markers voor:
- Jouw huis (HOME)
- Familie/vrienden woningen
- Veilige verzamelpunten
- Resources (water, benzinestation)

**How:**
1. Tap lange tijd op locatie
2. **Create Marker**
3. Kies type + kleur
4. Add naam/beschrijving
5. Save

### Team Communication

Als meerdere mensen ATAK hebben:

1. Create Team → Nodig leden uit
2. Share real-time locations
3. Draw on map (markeer routes, zones)
4. Voice chat (if TAK Server connected)
5. File sharing (foto's, documenten)

### Advanced Features

- **3D View:** Zie gebouwen in 3D
- **Weather Overlay:** KNMI data (plugin)
- **Traffic:** Vermijd drukte
- **Import KML/KMZ:** Externe data sources

## ✅ Je bent klaar!

**Samenvatting:**

1. ✅ ATAK + plugin geïnstalleerd
2. ✅ Offline kaarten gedownload
3. ✅ Settings geconfigureerd
4. ✅ Getest (vind noodopvang)
5. ✅ Noodpakket klaar

**Nu ben je voorbereid voor calamiteiten! 🎉**

**Vergeet niet:**
- 🔄 Sync data wekelijks
- 🔋 Houd batterij/powerbank geladen
- 👨‍👩‍👧‍👦 Familie ook installeren
- 🏃 Oefen regelmatig!

---

**Stay Safe! 🚨**

**Vragen?** support@itlusions.nl

**Website:** https://github.com/ITlusions/ITL.FMEServer/tree/main/examples/atak-calamiteiten-plugin
