#! /bin/bash

# at server
# workspace=/home/aarmas/workspace/
# RDFoxDistribution=/home/aarmas/Documents/RDFoxDistribution/RDFox/build/RDFox/

# at my macbook
workspace=/Users/Ana/Documents/Work/DatalogModules/
RDFoxDistribution=/Users/Ana/Documents/Work/RDFoxDistribution_newer_sampling/RDFox/build/release/




# at server
# projectDirs=$workspace"TailoredModuleExtractor/bin"
# at my macbook
projectDirs=$workspace"TailoredModuleExtractor/target/classes":$workspace"TailoredModuleExtractor/target/test-classes"

jars=$(echo $workspace"TailoredModuleExtractor/lib"/*.jar | tr ' ' ':')
PAGOdAjars=$workspace"TailoredModuleExtractor/lib/pagoda/PAGOdA.jar"
PAGOdAjars=$PAGOdAjars:$(echo $workspace"TailoredModuleExtractor/lib/pagoda/pagoda_lib"/*.jar | tr ' ' ':')
RDFox=$RDFoxDistribution"lib/JRDFox.jar"
RDFoxNative=$RDFoxDistribution"lib/libCppRDFox.so"
export CLASSPATH=$projectDirs:$jars:$PAGOdAjars:$RDFox
# echo $CLASSPATH


timeout="7200"
iterations=5
threads="8"
dataGeneratorPath=org/semanticweb/tmextractor/tests/DataRepresentationGenerator
testPath=org/semanticweb/tmextractor/tests/RandomSampleTest

# ontoDir="file:/home/aarmas/Documents/ontologies/"
ontoDir="http://www.cs.ox.ac.uk/isg/ontologies/UID/"

dataParentDir=$workspace"TailoredModuleExtractor/DataRepresentation/"
results="RandomSampleTest_results/6Mar_testResults_"
script=$workspace"TailoredModuleExtractor/DataRepresentation/script"

# for ontoName in {fma}
for ontoName in 00008
do
  echo $onto
  onto=$ontoDir$ontoName".owl" 
  dataDir=$dataParentDir$ontoName"/"
  dataFile=$ontoName".ttl"
  data=$dataDir$dataFile
  samplesDir=$dataDir"Samples/"
  mkdir $samplesDir
  

# first generate the data for the ontology
  # java -Xms2G -Xmx90G -DentityExpansionLimit=100000000 $dataGeneratorPath $onto $dataDir $dataFile

# then generate the samples

	echo set active "st100" >$script
	echo init seq-head >>$script
	echo import \"$data\" >>$script

	# for i in {1..5}
	for ((  i=1; i <= 5; i++ ))
	do
		echo set active "st"$i >>$script	
		echo init seq-head >>$script	
		echo sample 1 pm $i st100 >>$script	
		echo export \"$samplesDir"sample"$i".ttl"\" N-Triples >>$script
  		echo drop >>$script	
	done
	echo quit >>$script
	$RDFoxDistribution"lib/CppRDFox" -shell $script

# and finally run the tests
  java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $onto $timeout $threads &> $results$ontoName"_"$threads"threads.txt"
done

