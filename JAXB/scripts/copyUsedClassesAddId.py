#!/usr/bin/env python
###########################################################################
# copyUsedClassesAddId.py
#
# @Author Tim Grunshaw
# @Version 24.10.13
#
# This is the copyUsedClasses.sh bash script ported to python, combined with the
# fixAllIDs.sh and fixSingleID.py scripts. It makes sense to have them all in
# one as they have to be run together. 
# Part of the JAXB component of TradeIntel.
#
###########################################################################
import subprocess
import os
import shutil
import re

print("copyUsedClassesAddId.py python script started")

print("Running ClassHierarchyPrinter java program...")
# Run the ClassHierarchyPrinter, store the filenames in classes
classFileNames = subprocess.Popen(["java","-jar","../target/jaxb-1.0.0-SNAPSHOT-jar-with-dependencies.jar"],shell=True,stdout=subprocess.PIPE).stdout.read().decode("utf-8").splitlines()
print(str(len(classFileNames)) + " classes identified as being needed.")

# Delete target directory
targetDirectory = "../target/relevantClasses/"
if os.path.exists(targetDirectory):
    shutil.rmtree(targetDirectory)

# Create again, but empty
os.makedirs(targetDirectory)

# Copy the relevant files to the target directory
print("Copying files to target directory: " + targetDirectory)
srcDirectory = "../target/generated-sources/xjc/nz/co/trademe/api/v1/"
for line in classFileNames:
    shutil.copy(srcDirectory+line, targetDirectory)

# Create a jaxb.index file in the target directory & write the class names in it (no .java)
print("Creating jaxb.index in target directory")
f = open(targetDirectory+'jaxb.index', 'w')
for line in classFileNames:
    f.write(line.rstrip('.java')+"\n")
f.close()

# Add the package-info.java file to the target directory
print("Adding package-info.java to target directory")
shutil.copy(srcDirectory+"package-info.java", targetDirectory)

# Add the id to files matching the pattern
print("Adding id annotation to classes that need it")
patternToMatch = "// jaxb-custom-generation"
for line in classFileNames:
    with open(targetDirectory+line) as f:
        contents = f.read()
        if patternToMatch in contents:
            # Process this file
            r = re.compile(r'",\s*"id"\s*\}\)')
            if r.search(contents):
                print("The annotation has already been inserted: " + line)
                continue
            index = contents.find('"\n})')
            contentsList = list(contents)
            contentsList[index+1:index+2] = ',\n    "id"\n'
            contents = "".join(contentsList)
            classFile = open(targetDirectory+line, 'w')
            classFile.write(contents)
            classFile.close()
            print("Added annotation: " + line)

print("Finished copyUsedClassesAddId.py")
               

