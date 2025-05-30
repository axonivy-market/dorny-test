# DSGVO Utils

Die **DSGVO (Datenschutz-Grundverordnung)** ist eine EU-Verordnung, die den Schutz personenbezogener Daten innerhalb der **EU und des EWR** regelt. Wichtige Aspekte sind der erweiterte Geltungsbereich, strenge Einwilligungsanforderungen, Meldepflichten bei Datenschutzverletzungen und das **Prinzip der Speicherbegrenzung**, das vorschreibt, dass personenbezogene Daten nur so lange gespeichert werden dürfen, wie es für den ursprünglichen Zweck erforderlich ist. In diesem Zusammenhang ermöglicht das **"Recht auf Vergessenwerden" (Art. 17 DSGVO)** betroffenen Personen, die Löschung ihrer Daten zu beantragen, wenn sie nicht mehr benötigt werden. Dieses Tool unterstützt die Einhaltung dieser Vorschriften, indem es die automatische Löschung von Daten ermöglicht, die älter als ein definierter Zeitraum sind.  

- **Erstelle einen Job, der in festgelegten Intervallen an die Datenlöschung erinnert.**  
- **Definiere genau, welche Daten gelöscht werden sollen**, z. B. nur Daten mit einem bestimmten Status. Daten zu Aufgaben, die alt, aber noch in Bearbeitung sind, sollten nicht gelöscht werden. Lege außerdem das genaue Zeitintervall für die Datenlöschung fest.  
- **Business cases mit gelöschten Daten**: Der Geschäftsvorfall bleibt weiterhin verfügbar, es wird jedoch angezeigt, dass die verknüpften Daten gemäß der DSGVO gelöscht wurden.  
- **Erhalte eine Zusammenfassung des Löschprozesses**: In unserer Implementierung erfolgt die Datenlöschung manuell, um Systemadministratoren die Kontrolle zu geben – du erhältst einen kurzen Bericht über die Anzahl der gelöschten Datensätze.  

![data-deletion-page](images/data-deletion-page.png)
