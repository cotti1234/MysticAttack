# Zauberstab-System – Konzeptdokument

## 1. Grundsystem – Zauberstäbe

### Stab-Typen (aktuell 3)

| Stab | Element | Beschreibung |
|---|---|---|
| Basic-Stab | Elementlos | Einfach farmbar/craftbar, Einstiegs-Stab, kein Bonus/Malus |
| Wasser-Stab | Wasser | Bonus auf Wasser-Fähigkeiten |
| Obsidian-Stab | Stein/Härte | Fähigkeit: Blindheit (offensiv). Passiv: Stärke/Resistance auf sich selbst (skillbar) |

### Slot-Anzahl nach Tier

| Tier | Slots |
|---|---|
| Niedrig | 2 |
| Mittel | 3 |
| Hoch (bestes Tier) | 5 |

### Element-Mechanik
- Jeder Stab hat ein Basis-Element, das automatisch als Extra-Gewicht in die Kombo-Berechnung einfließt (Wasser-Stab + 1x Feuer-Fähigkeit = wirkt wie 2x Wasser + 1x Feuer)
- Fähigkeiten, die zum Stab-Element passen, sind stärker (Element-Bonus)
- Zwei Stäbe mit gleichem Element (Mainhand + Offhand) stacken NICHT – bleibt beim einfachen Bonus, fördert bewusstes Mischen statt Doppel-Stapeln

---

## 2. Steuerung

| Input | Aktion |
|---|---|
| Linksklick | Fähigkeit/Aktion A |
| Rechtsklick | Fähigkeit/Aktion B |
| Sneak + Rechtsklick | Öffnet Ingame-Menü |

---

## 3. Ingame-Menü

- Auswahl, welche 2 Fähigkeiten auf Links-/Rechtsklick gelegt werden
- Zeigt Level, Kosten (Mana), Beschreibung pro Fähigkeit
- Dient der schnellen Combat-Vorbereitung

---

## 4. Custom Tisch (Fähigkeiten-Schmiede)

- Eigener Block, eigenes GUI (kein Standard-Enchanting)
- Stab + Materialien rein → Fähigkeit wird freigeschaltet oder geleveled
- **Erfolgschance:** 90%
- **Bei Fehlschlag (10%):** Stab nimmt Durability-Schaden und ist temporär schwächer
- **Reparatur:** möglich am Tisch, kostet ca. 50% der ursprünglichen Upgrade-Materialien

---

## 5. Kombinations-System

- **Auslösung:** Passiv/automatisch – belegte Slots (Element-Fähigkeiten + Tränke) ergeben automatisch die Haupt-Fähigkeit (Angriff/Buff/Debuff), kein manuelles Triggern nötig
- **Reichweite der Berechnung:** Nur Stäbe in Hotbar (Mainhand) + Offhand zählen mit
- **Tränke als dritter Einflussfaktor:**
  - Wirken doppelt: auf den Stab-Effekt UND direkt auf den Spieler (wie normaler Trank)
  - Können den Zauber-Typ grundlegend verändern (z.B. Nahkampf → Fernkampf/Reichweite)
  - Im GUI austauschbar, nutzen sich aber ab (Ladungssystem, z.B. 50 Nutzungen pro Zutat)
- **Berechnungsmethode:** Freies, YAML-gesteuertes Tag-System (keine hardgecodeten Kombos)
  - Jede Zutat hat Tags (damage, range, aoe, burn, ...)
  - Ergebnis wird zur Laufzeit aus der aktuellen Slot-Belegung berechnet
  - YAML dient gleichzeitig als kommentiertes Handbuch für Tag-Bedeutungen

### Balance-Caps (Vorschlag)

| Wert | Cap PvE | Cap PvP |
|---|---|---|
| Damage-Bonus | +150% Basiswert | +80% Basiswert |
| Range | max. 15 Blöcke | max. 10 Blöcke |
| AoE-Radius | max. 6 Blöcke | max. 4 Blöcke |
| Debuff-Dauer | max. 8 Sek | max. 4 Sek |
| Heilung pro Kombo | max. 20% Max-HP | max. 10% Max-HP |

*(PvP durchgängig schwächer als PvE)*

---

## 6. Cooldown-System

- Cooldown hängt an Spieler + Fähigkeit-Typ, nicht am Item
- Gleiche Fähigkeit auf mehreren Stäben teilt sich den Cooldown (kein Umgehen durch Stab-Wechsel)

---

## 7. Mana-System

- Fähigkeiten kosten Mana zusätzlich zum Cooldown
- Mana-Pool global pro Spieler

**Erweiterungswege:**
1. Custom Tisch (Materialien → mehr Max-Mana)
2. Craftbare Verzauberung (mehr Mana/Regeneration)
3. Magie-Nutzungslevel (XP durchs Casten → Skill-Tree-Punkte)

---

## 8. Skill-Tree (Spieler-Progression)

- Verzweigtes System, **max. 3 Wege gleichzeitig wählbar**
- Jeder Ast hat 5 Skills, jeder Skill hat 5 Level (stufenweise ausbaubar statt Ein-Klick-Freischaltung)
- **Reset:** Erst nach Mindest-Investment möglich, Punkte werden erstattet, kostet einen **"Erinnerungskristall"** (custom Item, seltenes Material/Boss-Drop)
- **GUI:** MVP als cleveres Inventory-Layout (Glass-Panes als Linien-Ersatz), später evtl. Web-Dashboard über bestehendes `bot.cott-i.de` FastAPI-Setup

### Bestehende Äste

**Mana**
1. Mana-Pool – +10 Max-Mana / Level
2. Mana-Regeneration – +5% Regen-Speed / Level
3. Effizienz – −3% Mana-Kosten / Level
4. Notreserve – Burst-Regen bei <10% Mana, Cooldown 5 Min (skaliert)
5. Ultimate – einmalig +25% Max-Mana

**Stärke der Alten** *(Effektstärke + Element-Bonus)*
1. Rohschaden – +4% Effektstärke / Level
2. Element-Fokus – +3% zusätzlicher Stab-Element-Bonus / Level
3. Kombo-Verstärkung – +3% Stärke auf Kombo-Ergebnisse / Level
4. Durchschlag – ignoriert % Resistance / Level (PvE stärker als PvP)
5. Ultimate – +5% Chance auf verdoppelten Kombo-Effekt

**Ruhezeit** *(Cooldown)*
1. Grundreduktion – −3% Cooldown / Level
2. Fokussierung – Kill reduziert Cooldown auf aktiven Zauber (skaliert)
3. Zweitwind – Chance, Cooldown komplett zu ignorieren (skaliert)
4. Tisch-Vorteil – schnellere Tisch-Vorgänge
5. Ultimate – einmalig −15% globaler Cooldown

**Fokus der Natur** *(Regeneration/Heilung)*
1. Lebensregeneration – passiver HP-Regen / Level
2. Heilende Kombos – Kombo-Effekte heilen % des angerichteten Schadens zurück
3. Sättigung – Hunger regeneriert langsamer
4. Naturverbindung – Regen-Buff verstärkt in Wasser-/Wald-Biomen
5. Ultimate – Heilungs-Explosion (AoE-Heal) bei Kombo-Kill

**Die verborgene Kraft** *(versteckter Ast)*
- Freischaltung: mind. Level 3 in 2 verschiedenen Ästen gleichzeitig
- 3 Mini-Skills, thematisch = Kombination der 2 gewählten Ursprungs-Äste
- Beispiel: Mana + Ruhezeit → "Zeitlose Magie" (kein Cooldown, aber doppelte Mana-Kosten)

### Neue Ast-Vorschläge

**Wandlung** *(Elementwandel)*
1. Flexibler Wechsel – reduzierte Kosten/Cooldown beim Stab-/Fähigkeiten-Wechsel im Kampf
2. Mischbonus – Bonus auf Kombos aus nicht-reinen Elementen
3. Anpassung – kurzzeitiger Element-Bonus-Wechsel auf Zuruf
4. Katalysator – Trank-Wirkung im Slot verstärkt
5. Ultimate – einmaliger "Element-Swap" ohne Tisch, begrenzte Nutzung/Tag

**Beherrschung** *(Kontrolle, passt zu Obsidian-Thema)*
1. Griff – erhöhte Debuff-Dauer / Level (Blindheit, Slowness, etc.)
2. Zwang – erhöhte Chance auf CC-Effekte / Level
3. Unterwerfung – CC-Effekte wirken auch leicht auf Bosse/Mobs mit Resistance
4. Ketten – CC-Effekt kann sich unter bestimmten Bedingungen auf nahe Gegner ausbreiten
5. Ultimate – kurzer garantierter Stun-artiger Effekt (stark limitierter Cooldown)

**Schattenpfad** *(Mobilität/Überleben)*
1. Sprung – kurzer Dash/Blink beim Cast
2. Fluchtinstinkt – Bonus-Speed bei niedrigem HP
3. Tarnung – kurzzeitige Unsichtbarkeit/Transparenz nach Kombo-Treffer
4. Ausweichen – Chance, eingehenden Schaden zu reduzieren nach Cast
5. Ultimate – kurzzeitige Unverwundbarkeit gegen den nächsten Treffer (langer Cooldown)

*(Hinweis: Bei 8 Ästen und weiterhin max. 3 wählbaren Wegen wird die Build-Vielfalt deutlich größer – lohnt sich zu prüfen, ob "max. 3" dabei bleibt oder auf 4 angehoben wird.)*

---

## 9. Animationen

- Jede Fähigkeit hat eigene Particle-/Sound-Signatur
- Wind-up-Animation (Vorbereitung) + Impact-Animation (Treffer)
- Intensität skaliert mit Fähigkeits-Level

---

## 10. Offene Detailfragen (Balancing, kein Konzept-Blocker)

- Genaue Schwelle "Verborgene Kraft" (aktuell: Level 3 in 2 Ästen – Vorschlag, anpassbar)
- Exakte Abnutzungsrate der Trank-Ladungen
- Exakte Cap-Werte final festlegen (aktuell Platzhalter-Vorschläge)
- Ob 8 Äste bei "max. 3 wählbar" bleiben oder Limit angepasst wird
