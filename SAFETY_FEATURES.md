# WeatherWise - Safety Features (what was added)

WeatherWise is now a **Weather + Safety** app. A normal weather app tells you the weather;
WeatherWise tells you **what to do when the weather becomes dangerous**.

## The 5 features
| Feature | What it does | Needs internet? |
|---|---|---|
| **Alert bar** (Home screen) | Green "All clear" or orange/red alert summary. Tap = full list. | Yes |
| **Live Alerts** | Official warnings, forecast risks (heavy rain, heat, cold, wind, lightning), air quality, earthquakes nearby | Yes |
| **SOS button** | Sends an SMS with a Google Maps link of your location to emergency contacts | No (SMS) |
| **Safety Guides** | 7 guides: what to do before / during / after a disaster | **No (offline)** |
| **Helplines** | One-tap dialer for 112, 100, 101, 102, 108, 1078, 1091, 1098 | No |

## Where is the code?
```
data/SafetyAlert.kt            alert models (severity, type)
data/RemoteEarthquakeData.kt   earthquake JSON model
data/EmergencyContact.kt       contact model
safety/AlertGenerator.kt       ALL alert rules + thresholds (change numbers here)
safety/SafetyGuides.kt         offline guide text (add/edit guides here)
safety/Helplines.kt            emergency numbers (India)
safety/SosHelper.kt            builds + sends the SOS SMS
network/api/EarthquakeAPI.kt   USGS earthquake API (free, no key)
network/repository/SafetyRepository.kt   gets weather + earthquakes, builds alerts, 10-min cache
fragments/home/                alert bar added to Home
fragments/safety/              Safety Center + SOS logic
fragments/alerts/              alerts list screen
fragments/contacts/            add / remove emergency contacts
fragments/guides/, helplines/  guide and helpline screens
fragments/common/              shared list row + small helpers
```
Data flow (same MVVM pattern as the original project):
`Fragment -> ViewModel -> Repository -> API`, with Koin providing the objects.

## How the alerts work
1. **Official warnings** - WeatherAPI `alerts=yes` (available only in some regions).
2. **Forecast risks** - we read the 3-day forecast and apply simple rules
   (e.g. rain >= 64.5 mm = "Heavy rain", max temp >= 40 C = "Heatwave").
   This works everywhere, so the app is useful even where official alerts are missing.
3. **Air quality** - US-EPA index from WeatherAPI (`aqi=yes`).
4. **Earthquakes** - USGS feed, last 7 days, within 500 km, magnitude 2.5+.

## Robustness fixes made to the original code
- App **crashed when the phone was offline** (network exception was not caught). Fixed.
- App **crashed if GPS returned no location**. Fixed.
- Partial failures never show a false "All clear".

## 5-minute test checklist (run once)
1. Sync Gradle, run the app, pick a location. Alert bar should turn green/orange/red.
2. Tap the bar -> Alerts screen opens. Tap an alert -> details + "Safety guide" button.
3. Home -> **Safety** -> Emergency Contacts -> Add a contact (use your own number) -> it appears.
4. Safety Center -> **SOS** -> confirm -> allow Location + SMS -> your own phone receives the SMS.
5. Safety Guides and Helplines open. Turn on airplane mode: guides still work.
6. Airplane mode + open the app: no crash, bar says "Couldn't check safety alerts".

Tip: to *see* a red alert in a demo, temporarily lower a threshold in `AlertGenerator.kt`
(e.g. `HEAT_WARNING_C = 20f`), run the app, then set it back.

## Automated tests
`app/src/test/.../AlertGeneratorTest.kt` - 14 unit tests for the alert rules.
Run: right-click the file in Android Studio > Run.

## Future improvements (just mention these in the presentation)
- Background push notifications for new alerts (WorkManager / Firebase)
- Nearby shelters, hospitals and safe places on a map
- Community incident reporting (flood / road blocked)
- Automatic SOS on strong earthquake shaking or phone fall detection
- Multi-language support and country-based helpline numbers
- Offline SMS-based alerts for areas without internet
- AI-based personal risk score (age, health, location)
- Flood/landslide risk maps, tsunami and cyclone track alerts

## Notes
- Helpline numbers are for India; please verify them before your final demo.
- The WeatherAPI key is stored in `WeatherAPI.kt`. Before publishing the code publicly
  (GitHub), move it to `local.properties` so nobody can misuse it.
