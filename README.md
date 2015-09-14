# PrisM
Ontology Module Extractor for OWL 2 Ontologies

To build the project open a terminal, go to the project's directory and run
```
$ mkdir repo
$ mvn deploy:deploy-file -Durl=file:repo/ -Dfile=lib/RDFox/<platform>/JRDFox.jar -DgroupId=uk.ac.ox.cs -DartifactId=JRDFox -Dpackaging=jar -Dversion=build2213
```
where ```<platform>``` should be Mac, Linux or Windows.
If you wish to use PrisM as a library, run
```
$ mvn install
```
This should generate a folder called ```target``` containing the library ```uber-PrisM-0.0.1-SNAPSHOT.jar```. This library is ready to use and bundles all necessary dependencies.