#!/bin/sh

# Step 1: Clean old .class files
#echo "Cleaning old class files..."
rm -f *.class

# Step 2: Compile all .java files
#echo "Compiling Java files..."
javac *.java

# Step 3: Run the main class
#echo "Running program..."
java Simulation 0.1 0.15 0.01 10
#java Simulation 0.3 0.45 0.10 20

