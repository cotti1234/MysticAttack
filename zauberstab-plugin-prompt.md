# Auftrag: Zauberstab-System als vollständiges Minecraft-Java-Plugin

## Rolle

Du bist ein erfahrener Senior-Minecraft-Plugin-Entwickler (Paper/Spigot, Java 21). Du entwickelst auf Basis des beigefügten Konzeptdokuments ("Zauberstab-System – Konzeptdokument") ein **vollständiges, produktionsreifes Plugin**. Du arbeitest **eigenständig eine feste Phasen-Reihenfolge ab** (siehe unten), lieferst nach jeder Phase lauffähigen, kompilierbaren Code und fasst am Ende jeder Phase kurz zusammen, was fertig ist und was als Nächstes kommt. Du überspringst keine Phase und fasst keine Phasen zusammen, außer ich sage es dir explizit.

Wenn an einer Stelle eine Designentscheidung nicht eindeutig aus dem Konzeptdokument hervorgeht, triffst du eine sinnvolle, begründete Default-Entscheidung und dokumentierst sie in einem `DECISIONS.md` – du fragst nur nach, wenn eine Entscheidung das gesamte Projekt blockieren würde (z. B. "welche DB-Engine genau").

---

## Technischer Rahmen (verbindlich)

- **Server-Software:** PaperMC (aktuelle stabile 1.21.x API), Java 21, Build-Tool: Maven
- **Einsatzort:** Das Plugin läuft **ausschließlich auf dem Hinter-Server** (dem eigentlichen Gameplay-Backend-Server), nicht auf dem Vor-Proxy oder Hinter-Proxy. Es ist als eigenständiges Server-Plugin zu entwickeln, **ohne** proxy-übergreifende State-Synchronisation über Redis – aller Spielerzustand (Skill-Tree, Mana, Cooldowns etc.) wird ausschließlich lokal auf diesem Server bzw. in der angebundenen Datenbank verwaltet. Keine Abhängigkeit zur bestehenden Vor-Proxy/Hinter-Proxy-Infrastruktur einbauen.
- **Datenbank:** Externer Speicher primär über einen **Redis-Server**, sofern konfiguriert. Ist kein Redis-Server in der `config.yml` hinterlegt bzw. erreichbar, fällt das Plugin automatisch auf eine **interne Datenbank** zurück (umschaltbar über `storage.type: REDIS | INTERNAL`).
  - **Redis-Anbindung:** über einen etablierten Java-Redis-Client (z. B. Lettuce oder Jedis), Verbindungs-Pooling, Reconnect-Handling, klare Fehlerbehandlung bei Verbindungsabbruch (Plugin läuft weiter, loggt den Ausfall, fällt nicht einfach in einen inkonsistenten Zustand).
  - **Interne DB (Fallback):** keine Flatfile-/YAML-Lösung für Spielerdaten, da das bei vielen gleichzeitigen Spielern nicht performant und crash-sicher genug ist. Stattdessen eine **eingebettete, echte Datenbank** (z. B. SQLite über eine Connection-Pool-Bibliothek, oder alternativ H2 im Embedded-Modus) mit Indizes auf den relevanten Spalten (z. B. Spieler-UUID). Ziel: Auch bei vielen gleichzeitigen Spielern (Zugriffe im Combat-Takt, Cooldown-/Mana-Checks etc.) darf der Server weder laggen noch abstürzen.
  - **Gemeinsame Anforderung für beide Varianten:** Alle Zugriffe **grundsätzlich asynchron** (kein I/O im Main-Thread), Caching der aktiv genutzten Spielerdaten im Arbeitsspeicher (z. B. beim Login laden, im Hintergrund periodisch/batched persistieren statt bei jeder Kleinigkeit synchron zu schreiben), damit häufige Lese-Operationen im Kampf (Cooldown/Mana-Check) nicht bei jedem Aufruf einen externen Request auslösen. Sauberes Connection-/Pool-Handling, Migrationssystem für Schema-Updates der internen DB.
- **Bedrock-Kompatibilität:** Volle Nutzbarkeit für Bedrock-Spieler über **GeyserMC/Floodgate**, ohne dass Java-Spieler benachteiligt werden und ohne dass Bedrock-Spieler eingeschränkte Funktionalität erhalten. Das heißt konkret:
  - Erkennung von Bedrock-Spielern über die Floodgate-API
  - Alle Interaktionen (Linksklick/Rechtsklick/Sneak+Rechtsklick) müssen 1:1 auf Bedrock funktionieren – dort, wo Bedrock-Input technisch abweicht (z. B. Sneak-Verhalten, Doppelinteraktionen), eine funktional gleichwertige Alternative implementieren statt sie einfach wegzulassen
  - GUIs ausschließlich als **Inventory-basierte Menüs** umsetzen (kein reines Bedrock-Form-UI, kein reines Java-Chat-UI) – Inventories funktionieren nativ auf beiden Plattformen und sind damit die gemeinsame Basis
  - Kein Feature, das nur auf einer Plattform nutzbar ist – wenn technisch etwas auf Bedrock nicht 1:1 geht, wird das explizit in `BEDROCK-PARITY.md` dokumentiert samt gewählter Lösung
- **Konfiguration:** YAML-getriebenes Tag-System für die Kombinations-Engine wie im Konzeptdokument beschrieben – keine hardcodierten Kombinationen. YAML-Dateien dienen gleichzeitig als kommentiertes Handbuch.
- **Persistenz von Item-Daten:** PersistentDataContainer (PDC) für Stab-Metadaten auf dem Item selbst (Stab-Typ, Slot-Belegung), Spieler-Fortschritt (Skill-Tree, Mana, Cooldowns, Statistiken) vollständig in der Datenbank.

---

## Funktionsumfang (aus dem Konzeptdokument, siehe Anhang)

1. Stab-Grundsystem: 3 Stab-Typen (Basic, Wasser, Obsidian), Element-Mechanik, Slot-Anzahl nach Tier (2/3/5)
2. Steuerung: Links-/Rechtsklick für Fähigkeiten, Sneak+Rechtsklick für Ingame-Menü
3. Ingame-Menü zur Fähigkeiten-Zuweisung
4. Custom Tisch ("Fähigkeiten-Schmiede") mit eigenem Block/GUI, 90% Erfolgschance, Durability-Schaden bei Fehlschlag, Reparatur-Funktion
5. Kombinations-System: passive/automatische Berechnung aus Slot-Belegung (Stab + Offhand), Tränke als dritter Einflussfaktor mit Ladungssystem, freies YAML-Tag-System, Balance-Caps getrennt für PvE/PvP
6. Cooldown-System: spielerbasiert statt itembasiert, geteilt über gleiche Fähigkeiten
7. Mana-System: globaler Pool pro Spieler, drei Erweiterungswege (Tisch, Verzauberung, Skill-Tree-XP)
8. Skill-Tree: max. 3 gleichzeitig wählbare Äste (aktuell 8 Äste vorgeschlagen – als konfigurierbares Limit umsetzen, nicht hardcoded), 5 Skills/Ast, 5 Level/Skill, Reset über "Erinnerungskristall", versteckter Ast "Die verborgene Kraft" mit Freischaltbedingung
9. Animationen: Particle-/Sound-Signaturen pro Fähigkeit, Wind-up + Impact, Skalierung mit Level

Vollständige Details siehe angehängtes Konzeptdokument – dieses ist die verbindliche Spezifikationsquelle, dieser Prompt fasst nur zusammen.

---

## Qualitätsanforderungen

- Sauberes Package-Design (z. B. `de.cottisystem.zauberstab.{db, items, combat, gui, skilltree, config, listener, util}`)
- Repository/DAO-Pattern für alle DB-Zugriffe, keine SQL-Strings verstreut im Code
- Vollständige Fehlerbehandlung bei DB-Ausfall (Plugin darf nicht abstürzen, sinnvolles Degradation-Verhalten, Logging)
- Config-Validierung beim Start (fehlerhafte YAML-Configs führen zu klarer Fehlermeldung, nicht zu stillem Fehlverhalten)
- Keine synchronen DB- oder I/O-Zugriffe im Main-Thread
- Kommentierter Code, insbesondere in der Tag-/Kombinations-Engine
- Am Ende jeder Phase: kurze Testanleitung, wie ich das Ergebnis manuell prüfen kann

---

## Arbeitsweise: verbindliche Phasen-Reihenfolge

Arbeite die folgenden Phasen **strikt nacheinander** ab. Liefere nach jeder Phase den vollständigen Code-Stand dieser Phase, bevor du zur nächsten übergehst. Warte nach jeder Phase kurz auf mein "weiter", außer ich sage dir vorher, dass du durchlaufen sollst.

**Phase 0 – Projekt-Setup**
Maven-Projekt, `pom.xml` mit allen nötigen Dependencies (Paper API, Floodgate API, Redis-Client wie Lettuce/Jedis, Connection-Pool-Bibliothek für die interne DB, SnakeYAML falls nicht schon in Paper enthalten), `plugin.yml`, Grundgerüst der Package-Struktur, leere Main-Class mit Lifecycle-Hooks.

**Phase 1 – Datenbank-Layer**
Schema-/Datenmodell-Design (Spieler-Profile, Skill-Tree-Fortschritt, Stab-Statistiken/Trank-Ladungen sofern nötig, Cooldowns, Mana), abstrahierte Storage-Schicht mit zwei Implementierungen (Redis und interne DB) hinter einem gemeinsamen Interface, automatische Fallback-Logik (Redis konfiguriert & erreichbar → nutzen, sonst interne DB), Migrationssystem für die interne DB, In-Memory-Caching aktiver Spieler mit periodischer/gebatchter Persistierung, Repository-Klassen, durchgängig async.

**Phase 2 – Core-Datenmodelle**
Java-Klassen für Stab, Fähigkeit, Element, Slot, Spieler-Profil – reine Datenmodelle, noch ohne Logik.

**Phase 3 – Stab-Grundsystem**
Die 3 Stab-Typen als Custom Items via PDC, Erstellung/Erkennung, Slot-Anzahl je Tier, Element-Zuordnung.

**Phase 4 – Steuerung & Input-Handling (inkl. Bedrock-Anpassung)**
Links-/Rechtsklick-Listener, Sneak+Rechtsklick fürs Menü, Floodgate-Erkennung, plattformspezifische Input-Angleichung dokumentiert in `BEDROCK-PARITY.md`.

**Phase 5 – Ingame-Menü (GUI)**
Inventory-basiertes Menü zur Fähigkeiten-Zuweisung auf Links-/Rechtsklick, inkl. Level/Kosten/Beschreibung-Anzeige.

**Phase 6 – Custom Tisch (Fähigkeiten-Schmiede)**
Eigener Block, eigenes GUI, Upgrade-/Freischalt-Logik, 90%-Erfolgschance, Fehlschlag-Konsequenz, Reparatur-Funktion.

**Phase 7 – YAML-Tag-System & Kombinations-Engine**
Parser für Tag-YAML, Laufzeit-Berechnung aus aktueller Slot-Belegung (Stab + Offhand + Tränke), Trank-Ladungssystem, Balance-Caps PvE/PvP konfigurierbar.

**Phase 8 – Cooldown-System**
Spielerbasierter Cooldown je Fähigkeit-Typ, geteilt über mehrere Stäbe.

**Phase 9 – Mana-System**
Globaler Pool pro Spieler, Kosten pro Fähigkeit, drei Erweiterungswege (Tisch, Verzauberung, XP).

**Phase 10 – Skill-Tree**
Äste, Skills, Level, konfigurierbares Limit gleichzeitig wählbarer Äste, Reset via Erinnerungskristall, versteckter Ast mit Freischaltbedingung. MVP-GUI als Inventory-Layout mit Glass-Panes.

**Phase 11 – Animationen**
Particle-/Sound-Signaturen pro Fähigkeit, Wind-up + Impact, Skalierung mit Level.

**Phase 12 – Balancing-Config**
Zentrale, im Betrieb änderbare Config für alle Cap-Werte, Abnutzungsraten, Freischalt-Schwellen aus Abschnitt 10 des Konzeptdokuments.

**Phase 13 – Tests & Bedrock/Java-Parity-Check**
Manuelle Test-Checkliste je Feature, expliziter Parity-Check Java vs. Bedrock, Performance-Check (kein synchrones I/O im Main-Thread, Combo-Berechnung performant genug für Kampfsituationen).

**Phase 14 – Dokumentation**
`README.md` (Setup, Config-Optionen, DB-Schema), `BEDROCK-PARITY.md`, `DECISIONS.md` finalisieren.

---

## Start

Beginne mit **Phase 0**. Bestätige zuerst kurz dein Verständnis der Kernanforderungen in 3–5 Sätzen, dann lege los.
