# PrisM
Ontology Module Extractor for OWL 2 Ontologies

To build the project, modify the pom.xml file by replacing the line 
```
<systemPath>${basedir}/lib/RDFox/JRDFox.jar</systemPath>
```
with
```
<systemPath>${basedir}/lib/RDFox/Mac/JRDFox.jar</systemPath>
```
or
```
<systemPath>${basedir}/lib/RDFox/Linux/JRDFox.jar</systemPath>
```
or 
```
<systemPath>${basedir}/lib/RDFox/Windows/JRDFox.jar</systemPath>
```
according to your operating system, and then run maven.