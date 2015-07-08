#! /bin/bash

projectDirs=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/target/classes:/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/target/test-classes:
jars=$(echo "/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib"/*.jar | tr ' ' ':')
PAGOdAjars=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/pagoda/PAGOdA.jar
PAGOdAjars=$PAGOdAjars:$(echo "/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/pagoda/pagoda_lib"/*.jar | tr ' ' ':')
RDFox=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/RDFox/JRDFox.jar 
RDFoxNative=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/RDFox/libCppRDFox.dylib


export CLASSPATH=$projectDirs:$jars:$PAGOdAjars:$RDFox

    
ontoPath="http://www.cs.ox.ac.uk/isg/ontologies/UID/000"

# DOLCE
for i in {13..24}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done


# GALEN
for i in {26..39}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done


# GO extensions
for i in {40..48}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done


# Gardiner Corpus part1
for i in {53..99}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done

ontoPath="http://www.cs.ox.ac.uk/isg/ontologies/UID/00"

# Gardiner Corpus part2
for i in {100..345}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done


# OBO
for i in {351..697}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done


# Phenoscape
for i in {700..770}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done


# Others 
# 1
4
347
350
774
775
778
786
for i in {1,4,347,350,774,775,778,786}
do
  onto=$ontoPath$i".owl" 
  echo $onto
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative GenuineSignaturesTest $onto
done

# FMA is 285
# FLY_XP is 463	
#don't forget  biomodels, cell cycle ontology 1 and 2