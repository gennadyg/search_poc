package com.wordcounter;

import com.files.FileUtils;
import com.files.TaskResult;
import com.utils.Constants;
import lombok.Getter;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 *  *
 *
 * Receive few text com.files and will count (and later print) all the words that exist in these com.files (together) and the
 * number of times each one of them appears.
 * <br>
 * In order to get {@link WordsCounter} object use its builder as following:
 * <br>{@code
 * WordsCounter wordsCounter = new WordsCounter(timeout, TimeUnit.SECONDS );
 *}
 * <br>
 * Parameters example:
 * {@code
 * --input "input/input_Q1b.txt" --output "input/output_Q2b.txt"
 * }
 *
 *
 */
@Getter
public class WordsCounter {
  private static final Logger logger = LoggerFactory.getLogger( WordsCounter.class );
  // Initial censures a reasonably good number of elements before resizing happens.
  private int initialCapacity = 32;
  // Load factor 0.9 ensures a dense packaging inside ConcurrentHashMap which will optimize memory use
  private float loadFactor = 0.9f;
  private int maxTimeout = 0;
  public Map<String, WordMetaData> wordCounts;
  private TimeUnit unit;
  private ExecutorService executor;
  private int concurrency = 0;
  /**
   *
   * @param maxTimeOut - maximum timeout to wait for single file processor to finish
   */
  public WordsCounter( int maxTimeOut, TimeUnit unit ){

    this.maxTimeout = maxTimeOut;
    this.unit = unit;
    // Want to create concurrency that equals to number of com.files or number of cores on machine
    // each core could handle multiple threads, usually 2
    concurrency = Math.min( Constants.maxNumOfFilesInBatch, Runtime.getRuntime().availableProcessors()*2 );
    logger.info("Concurrency used - " + concurrency );

    wordCounts = new ConcurrentHashMap<>( initialCapacity, loadFactor, concurrency );
  }

  /**
   *  Create set of callables using SingleFileProcessors
   *
   * @param fileNames - array of file locations
   * @return set of callables of type Set<Callable>
   */
  private Set<Callable<TaskResult>> createTasks(String[] fileNames ) {
    Set<Callable<TaskResult>> callables = new HashSet<>();
    for( String fileName : fileNames ){
      SingleFileProcessor singleFileProcessor = new SingleFileProcessor( fileName, wordCounts );
      callables.add( singleFileProcessor );
    }
    return callables;
  }

  /**
   *  Validate input file names
   *
   * @param fileNames - comma separated file names
   * @throws Exception - if file name is invalid
   */
  private void validateInput( String ... fileNames ) throws FileNotFoundException {
    if( fileNames == null || fileNames.length == 0 ){
      throw new FileNotFoundException("Please provide comma separated file names, less than " + Constants.maxNumOfFilesInBatch );
    }
    for( String fileName : fileNames ){
      if( !FileUtils.checkThatFileExists( fileName )){
        throw new FileNotFoundException("File doesn't exists -" + fileName );
      }
    }
  }
  /**
   *  Receives file names and process all of them, count words occurrences.
   *
   * @param fileNames - variable list of file names
   */
  public boolean load( String ... fileNames ) throws FileNotFoundException, ExecutionException {
    boolean res = true;
    validateInput( fileNames );
    Set<Callable<TaskResult>> tasks = createTasks( fileNames );
    try {
      // Submit all tasks to executor
      executor = Executors.newFixedThreadPool( concurrency );
      List<Future<TaskResult>> futures = executor.invokeAll( tasks, maxTimeout, unit );
      // Iterate over all tasks and waiting for maximum timeout
      for( Future<TaskResult> future : futures){
        TaskResult taskResult = future.get();
        logger.info("Response from task id [{}]: processed {} -> {}", taskResult.getTaskId(), taskResult.getNumOfProcessed(), taskResult.getResultMessage());
      }
    }catch ( CancellationException cc ){
      throw new ExecutionException("Timed out to process files -" + StringUtils.join( fileNames, ","), cc );
    }catch ( InterruptedException | ExecutionException e ) {
      throw new ExecutionException("Failed to process files -" + StringUtils.join( fileNames, ","), e );
    }finally {
      executor.shutdown();
      try {
        executor.awaitTermination( maxTimeout, unit );
      } catch( InterruptedException e ) {
        logger.error("Failed to stop executor ", e );
      }
    }
    return res;
  }

  /**
   *  Print all numbers and their occurrences count
   */
  public void displayStatus(){
    wordCounts.forEach( ( word, meta ) -> logger.info( word + " " + meta.getFileIdSet() ));
    logger.info("**Total:" + wordCounts.size() );
  }

  /**
   *  Prints all words sorted
   */
  public void displayStatusSorted(){
    TreeMap<String, WordMetaData> treeMap = new TreeMap<>();
    treeMap.putAll( wordCounts );
    treeMap.forEach( ( word, meta ) -> logger.info( word + " " + meta.getOccurencesNo() + " " + String.join(",", meta.getFileIdSet()) ));
    logger.info("**Total:" + wordCounts.size() );
  }
  /**
   * Arguments processing logic
   *
   * @return options - of type Options
   */
  public static Options buildArguments(){
    // create the Options
    Option input = Option.builder()
        .required()
        .hasArg()
        .longOpt(Constants.input)
        .build();
    Option timeout = Option.builder()
        .hasArg()
        .longOpt(Constants.timeout)
        .build();
    input.setArgs( Option.UNLIMITED_VALUES );
    Options options = new Options();
    options.addOption( input );
    options.addOption( timeout );
    return options;
  }
  public Set<String> search(String searchExpression){
    Set<String> results = new HashSet<>();
    try{
      List<String> searchWords = Arrays.stream(searchExpression.split(Constants.regexToSplitWords))
          .filter(word -> !Constants.stopWords.contains(word))
          .collect(Collectors.toList());
      for(String currentWord : searchWords){
        if(wordCounts.containsKey(currentWord)){
          results.addAll(wordCounts.get(currentWord).getFileIdSet());
        }
      }
    }catch (Exception ex){
      logger.error("Failed to make search", ex);
    }

    return results;
  }

  /**
   *  Main entry
   *
   * @param args - program arguments, for example --input "words\" --timeout 60
   *        --input folder that contains files
   *        --timeout timeout for each file in seconds
   * @throws {@link IllegalArgumentException}
   */
  public static void main(String[] args) throws IllegalArgumentException, ExecutionException {
    CommandLineParser parser = new DefaultParser();
    long executionStartTime = System.currentTimeMillis();
    try {
      // parse the command line arguments
      CommandLine line = parser.parse( buildArguments(), args );
      Integer timeout = Integer.valueOf( line.getOptionValue(Constants.timeout));
      WordsCounter wordsCounter = new WordsCounter(timeout, TimeUnit.SECONDS );
      String in = line.getOptionValue(Constants.input);

      // Make files pagination and load by chunks, to deal with folder that contains huge amount of files
      Path dir = FileSystems.getDefault().getPath( in );
      try(DirectoryStream<Path> stream = Files.newDirectoryStream( dir )){
        List<String> filesToProcess = new ArrayList<>();
        int numOfReadFiles = 0;
        for (Path path : stream) {
          filesToProcess.add(path.toString());
          logger.info( "Found file: " + path.getFileName() );
          if (++numOfReadFiles % Constants.maxNumOfFilesInBatch == 0){
            logger.info( "Processing chunk id: " + numOfReadFiles / Constants.maxNumOfFilesInBatch );
            if( wordsCounter.load( filesToProcess.toArray(new String[0]) )){
              logger.info( "Finished to process chunk id: " + numOfReadFiles / Constants.maxNumOfFilesInBatch );
              filesToProcess.clear();
            }
          }
        }
        if(!filesToProcess.isEmpty()){
          if( wordsCounter.load( filesToProcess.toArray(new String[0]) )){
            logger.info( "Finished to process leftovers");
          }
        }

        wordsCounter.displayStatusSorted();
      }
      logger.info("Word Counting took {} milliseconds", System.currentTimeMillis() - executionStartTime );

    } catch(ParseException pe) {
      logger.error("Failed to to iterate folder", pe);
      throw new IllegalArgumentException("Failed to parse arguments, example that should be: --input \"words\\inputSuperLarge1.txt\" --timeout 60");
    }catch (IOException exp){
      logger.error("Failed to to iterate folder", exp);
      throw new IllegalArgumentException("Failed to to iterate folder");
    }
  }

}
