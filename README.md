# Implementing a Search Engine using Page Rank for Big Data Processing

For this project I designed and developed my own page ranking algorithm to build a search engine for the Wikipedia dataset for the module 'Big Data Processing'.
As team leader of this project I divided the work load into subtasks and ensured the smooth collaboration amongst members.

See 'Report.pdf' for the full report of this project including the research that was conducted to develop a pank rank algorithm 
to outperform Wikipedia's existing search engine.

This project made use of: Hadoop, MapReduce and Java.

All requirments and bonus tasks were completed. 
This project was graded a score of 88/100.


Instructions to Run the Search Engine:

To run this Search Engine, the folder from which the terminal is running should contain the following:
1. src: contains all the .java files
2. dist: contains the jar that the command "ant clean dist" will create
3. lib: must contain the cloud9 library as a .jar file - "cloud9-2.0.2-fatjar.jar" 
4. classes: will be created when the .jar in the dist file is run
5. build.xml

To run the program first re-build using "ant clean dist" and then run the following example command: 

hadoop jar dist/IterateBFS.jar IterateBFS input newOut3 "bob the builder"

Currently, the input is hardcoded to the file "/data/enwiki-latest.block" in the QMUL cluster as it is the folder containing all the Wikipedia page sequence files with the articles. To change the input you must go into the IterateBFS.java file and change it to whichever file you would like; also, the output is hardcoded to newOut3. (We did this to save time in testing).

The output file is under “newOut3” and is in the QMUL cluster which needs to be copied somewhere local to be viewed. (The program also outputs outNew and outNew2, which are the corresponding outputs to Job1(1st Mapper - Inverted Index) and Job2(2nd Mapper - Page Rank).

For single term searches it isn't necessary to have quotation marks but for multiple term searches it is.

We have also provided examples of the outputs that we received when testing the algorithm along with the outputs we have posted in our results. These are all located in the “outputResults” folder. 

