# Barrierefreie Fußnoten mit Apache FOP

Ziel dieser Bemühungen ist es, die Fußnoten in Apache FOP 2.5 dahingehend zu verbessern, als dass diese in einer PDF/UA-konformen Weise ins PDF gerendert werden. Das ursprüngliche Problem ist unter [Gitlab-Issue #1](https://gitlab.tollwerk.net/tollwerk/tollwerk-asciidoc/-/issues/1) erfasst. Zum Prüfen von PDFs auf PDF/UA-Konformität wird der [PDF Accessibility Checker (PAC 3)](https://access-for-all.ch/ch/pdf-werkstatt/pdf-accessibility-checker-pac.html) verwendet.

 ## Problematik
 
 PAC 3 akzeptiert Dokumente mit Fußnoten nicht als vollständig konform, weil die beteiligten `/S /Note`-Objekte im PDF nicht über eine eindeutige `/ID` verfügen. Stattdessen müsste es bspw. heißen:
 
 ```
 29 0 obj
 <<
   /S /Note
   /ID(1)
   /P 28 0 R
   /K [8 0 R 30 0 R]
 >>
 ```
 
 Das Problem sowie eine Lösung mit Arcobat wird hier beschrieben: https://taggedpdf.com/508-pdf-help-center/id-missing-in-note-structure-element/
 
 Vermutlich ist eine Anpassung von FOP notwendig, um das Problem zu beheben. Gleichzeitig hat der Fehler offenbar keinen Einfluss auf die Barrierefreiheit des Dokuments. Lediglich das Matterhorn-Protokoll rebelliert ...
 
 ## Kompilieren von FOP
 
 Die FOP-Distribution kann mithilfe des Docker-Images `hub.tollwerk.net/tollwer/ant`. Es ist dazu nur der Start von [docker-compose](docker-compose.yml) notwendig. Das Kompilieren von FOP kann mehrere Minuten dauern.
 
 ## Erzeugen von Test- und Referenz-PDFs
 
 Sobald FOP kompiliert wurde, können die enthaltenen Test- und Referenz-PDFs erzeugt werden. Dazu auf der Kommandozeile im obersten Projektverzeichnis (unter Windows):
 
 ```bash
# Einfaches Standard-PDF mit vollständiger PDF/UA-Konformität
.\fop\fop.bat -q -c .\a11y\fop.xml -fo .\a11y\fo\simple.fo .\a11y\build\simple.pdf

# Minimaler Test mit Fußnote
.\fop\fop.bat -q -c .\a11y\fop.xml -fo .\a11y\fo\footnote.fo .\a11y\build\footnote.pdf
```
 
 