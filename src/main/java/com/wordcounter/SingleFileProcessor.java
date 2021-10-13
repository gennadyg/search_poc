package com.wordcounter;

import com.files.FileIterator;
import com.files.TaskResult;
import com.utils.Constants;
import com.utils.Constants.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 *   Process single file of text, map all words and count their appearances using map
 */
public class SingleFileProcessor implements Callable<TaskResult> {
  private static final Logger logger = LoggerFactory.getLogger( SingleFileProcessor.class );
  private final String fileName;
  private Map<String, WordMetaData> wordCount;
  /**
   *  Creates text processor for a single file
   *
   * @param fileName - file location
   * @param wordCount - reference to concurrent hash map
   */
  public SingleFileProcessor( String fileName, Map<String, WordMetaData> wordCount ){
    this.fileName = fileName;
    this.wordCount = wordCount;
  }
  /**
   *  Map each line of text to array of strings using regex
   *
   * @param line - line of text from file
   * @return array of strings
   */
  private String[] map( String line ){
    return line.split(Constants.regexToSplitWords);
  }
  /**
   *  Take words and update their counter in words shared map
   *
   * @param words - array of words
   */
  private void reduce( String[] words, String fileId ){
    Arrays.stream(words)
         .filter( word -> !word.isEmpty())
         .filter( word -> !Constants.stopWords.contains(word.toLowerCase()))
         .forEach( currentWord ->  {
           wordCount.computeIfAbsent( currentWord.toLowerCase(), k -> new WordMetaData(fileId)).getFileIdSet().add(fileId);
           wordCount.get(currentWord.toLowerCase()).getOccurencesNo().increment();
         });
  }
  /**
   *  Main task function that called by executor service
   *
   * @return result of type TaskResult, that contains results
   */
  @Override
  public TaskResult call(){
    TaskResult taskResult = new TaskResult( Thread.currentThread().getId(), fileName );
    int numOfWordsProcessed = 0;
    try( FileIterator fileIterator = new FileIterator( fileName )){
      while( fileIterator.hasNext() ){
        String[] words = map( fileIterator.next() );
        numOfWordsProcessed += words.length;
        reduce( words, fileName );
      }
      taskResult.setNumOfProcessed(numOfWordsProcessed);
      logger.info("Finished processing words: {}", numOfWordsProcessed );
    }catch( FileNotFoundException fne ){
      logger.error("File not exist {}", fileName, fne );
      taskResult.setResultMessage( ResultType.FileNotExist.getName() );
    }catch ( Exception ex ){
      logger.error("Failed to process file [{}]", fileName, ex );
      taskResult.setResultMessage( ex.getMessage() );
    }
    return taskResult;
  }

  /**
   *  Main for testing purposes
   *
   * @param args
   */
  public static void main(String[] args) {
    Map<String, LongAdder> map = new ConcurrentHashMap<String, LongAdder>();
    map.computeIfAbsent( "word", k -> new LongAdder()).increment();
    System.out.println( map.get("word"));
    // SingleFileProcessor singleFileProcessor = new SingleFileProcessor("words/input1.txt", map );
    // singleFileProcessor.call();
    SortedMap<String, Long> sortedKeys = new TreeMap<>();

    for( Map.Entry<String, LongAdder> entry : map.entrySet()){
    //for( Map.Entry<String, LongAdder> entry : sortedKeys.keySet()){
      sortedKeys.put(entry.getKey(), entry.getValue().sum());
      //logger.info( entry.getKey() + " -> " +  entry.getValue().sumThenReset());
    }
    logger.info(sortedKeys.toString());
  }
}
