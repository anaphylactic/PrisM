#! /bin/bash

projectDirs=/home/aarmas/workspace/TailoredModuleExtractor/bin
jars=$(echo "/home/aarmas/workspace/TailoredModuleExtractor/lib"/*.jar | tr ' ' ':')
PAGOdAjars=/home/aarmas/workspace/TailoredModuleExtractor/lib/pagoda/PAGOdA.jar
PAGOdAjars=$PAGOdAjars:$(echo "/home/aarmas/workspace/TailoredModuleExtractor/lib/pagoda/pagoda_lib"/*.jar | tr ' ' ':')
RDFox=/home/aarmas/Documents/RDFoxDistribution/RDFox/build/RDFox/lib/JRDFox.jar 

RDFoxNative=/home/aarmas/Documents/RDFoxDistribution/RDFox/build/RDFox/lib/libCppRDFox.so

timeout="7200"
iterations="5"
threads="8"
testPath=org/semanticweb/tmextractor/tests/GenuineSignaturesTest

export CLASSPATH=$projectDirs:$jars:$PAGOdAjars:$RDFox
echo $CLASSPATH
    

ontoPath="file:/home/aarmas/Documents/ontologies/"

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"biomodels-21.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_biomodels-21.txt

 java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"fma.owl" $timeout $iterations 10 &> GenuineSignaturesTest_results/6Mar_testResults_fma_10threads.txt

 java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"fma.owl" $timeout $iterations 16 &> GenuineSignaturesTest_results/6Mar_testResults_fma_16threads.txt


ontoPath="http://www.cs.ox.ac.uk/isg/ontologies/UID/"

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00001.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00001.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00024.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00024.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00029.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00029.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00032.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00032.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00347.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00347.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00350.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00350.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00351.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00351.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00354.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00354.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00463.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00463.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00471.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00471.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00477.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00477.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00512.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00512.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00545.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00545.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00774.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00774.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00775.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00775.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00778.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00778.txt

 java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00026.owl" $timeout $iterations 10 &> GenuineSignaturesTest_results/6Mar_testResults_00026_10threads.txt

 java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00026.owl" $timeout $iterations 16 &> GenuineSignaturesTest_results/6Mar_testResults_00026_16threads.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00786.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/testResults_00786.txt

 java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00004.owl" $timeout $iterations 10 &> GenuineSignaturesTest_results/6Mar_testResults_00004_10threads.txt

 java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00004.owl" $timeout $iterations 16 &> GenuineSignaturesTest_results/6Mar_testResults_00004_16threads.txt

# java -Xms2G -Xmx90G -DentityExpansionLimit=100000000  -Djava.libarary.path=RDFoxNative $testPath $ontoPath"00285.owl" $timeout $iterations $threads &> GenuineSignaturesTest_results/4Mar_testResults_00285_8threads.txt



