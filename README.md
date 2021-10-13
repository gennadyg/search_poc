#### Words Counter
Inverted Index & Map Reduce
Given a potentially very large set of documents. Some of the documents are also very large and cannot fit 
into the memory of a single machine. Design and implement a search engine. 
Example 1: Suppose you have a local copy of Wikipedia which comes in the form of a ZIP file. Each file in
the ZIP archive corresponds to the Wikipedia article and file names correspond to article names. Given a search query
‘a computer science’ , the designed engine will return all documents containing both the words: ‘computer’ and ‘science’.

![img.png](img.png)

##### Alghorithm:
Basically for each file created callable task, that submitted to executor pool. Each task read line by line text file,
parse to words( map function ) and updates words counters( reduce function ) in shared concurrent map.   
Main is located in WordsCounter.
Concurrency level, used by to define executor pool size and concurency level in shared concurent hash map.
To assure that no race condition will occur, for updates in in concurrent map is used computeIfAbsent method. Also used
LongAdder as counter.
##### Main classes involved:
com.wordscounter.SingleFileProcessor
com.wordscounter.WordsCounter
com.files.FileIterator
input files in inpout/* directory
```
gradlew wordsCounterSmall // to run three small files with data from assignment

gradlew wordsCounterLarge // to run three bigger files, in input directory
```
Tests:
com.ascii.WordsCounterTest
com.ascii.SingleFileProcessorTest
```
gradlew tests 