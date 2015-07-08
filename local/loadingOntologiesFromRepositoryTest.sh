#! /bin/bash

projectDirs=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/target/classes:/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/target/test-classes:
jars=$(echo "/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib"/*.jar | tr ' ' ':')
PAGOdAjars=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/pagoda/PAGOdA.jar
PAGOdAjars=$PAGOdAjars:$(echo "/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/pagoda/pagoda_lib"/*.jar | tr ' ' ':')
RDFox=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/RDFox/JRDFox.jar 
RDFoxNative=/Users/Ana/Documents/Work/DatalogModules/TailoredModuleExtractor/lib/RDFox/libCppRDFox.dylib


export CLASSPATH=$projectDirs:$jars:$PAGOdAjars:$RDFox

    
ontoPath="http://www.cs.ox.ac.uk/isg/ontologies/UID/0000"

for i in {1..2}
do
  onto=$ontoPath$i".owl" 
  echo $onto 
  java -Xms2G -Xmx6G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative LoadingTest $onto &> testResults$i.txt
done

# for i in {10 .. 99}
# do
    
# done

# for i in {100 .. 797}
# do
    
# done




# i=0
# j=0
# one=1
# zero=0
# ont1=hey
# ont2=hey
# for item in $onts
# do
#     if [ $j -eq $zero ]
#     then
#         echo $i
#         ont1=$item
#         j=$one
#         i=$(($i+$one))
#     else
#         ont2=$item
#         j=$zero
#         echo $ont1
#         echo $ont2
#         java -Xms2G -Xmx6G -DentityExpansionLimit=100000000 prelimTests.CreatingNormalizedAndRewrittenOntologies file:$ont1 file:$ont2
#     fi
# done





# onts=("$(echo /Users/Ana/Documents/Work/workspace/MOReInverseRewriting/ontologies/nonInvRew_ok/*.owl)")
# #i=0
# j=0
# one=1
# zero=0
# ont1=hey
# ont2=hey
# for item in $onts
# do
#     if [ $j -eq $zero ]
#     then
#         echo $i
#         ont1=$item
#         j=$one
#         i=$(($i+$one))
#     else
#         ont2=$item
#         j=$zero
#         echo $ont1
#         echo $ont2
#         java -Xms2G -Xmx6G -DentityExpansionLimit=100000000 prelimTests.CreatingNormalizedAndRewrittenOntologies file:$ont1 file:$ont2
#     fi
# done