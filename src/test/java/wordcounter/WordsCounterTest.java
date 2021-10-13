package wordcounter;

import com.utils.Constants;
import com.wordcounter.WordsCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertArrayEquals;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class WordsCounterTest {

  private WordsCounter wordsCounter = new WordsCounter( 60, TimeUnit.SECONDS );
  public Map<String, Long> wordsCountMap = new HashMap<>();

  @BeforeEach
  public void setUp(){
    wordsCountMap.put("and", 457L );
    wordsCountMap.put("file", 14L );
    wordsCountMap.put("first", 1L );
    wordsCountMap.put("software", 1L );

  }

  @Test
  public void testTimeoutWordCounts() throws Exception {
    WordsCounter wordsCounter1 = new WordsCounter( 1, TimeUnit.NANOSECONDS );
    assertThrows(ExecutionException.class, () -> {
      wordsCounter1.load(new String[]{"words/inputLarge1.txt"});
    });
  }

  @Test
  public void testCheckEmptyFiles(){
    assertThrows(FileNotFoundException.class, () -> {
      wordsCounter.load();
    });
  }
  @Test
  public void testCheckNotExistedFile(){
    assertThrows(FileNotFoundException.class, () -> {
      wordsCounter.load("input/777");
    });
  }

  @Test
  public void testProcessSmallFileAndCheckRandomWords() throws Exception {
    Map<String, Long> wordsCountMap = new HashMap<>();
    wordsCountMap.put("this", 1L );
    wordsCountMap.put("provided", 1L );
    wordsCountMap.put("holders", 1L );
    wordsCountMap.put("software", 1L );
    assertTrue( "Failed to load com.files", wordsCounter.load(new String[]{"words_small/inputSample1.txt"}) );
    for( String key: wordsCountMap.keySet() ){
      assertTrue("Failed to check " + key, wordsCounter.wordCounts.containsKey(key) );
      assertEquals("Failed to check " + key, wordsCountMap.get(key).longValue(), wordsCounter.wordCounts.get(key).getOccurencesNo().sum() );
      assertTrue("Failed to check " + key, wordsCounter.wordCounts.get(key).getFileIdSet().contains("words_small/inputSample1.txt"));
    }
  }

  @Test
  public void testCheckingThatStopWordSkipped() throws Exception {
    assertTrue( "Failed to load com.files", wordsCounter.load(new String[]{"words_small/inputSample1.txt"}) );

    Iterator<String> iterator = Constants.stopWords.iterator();
    while( iterator.hasNext() ){
      assertFalse("Failed to check stop words absense", wordsCounter.wordCounts.containsKey(iterator.next()) );
    }
  }

  @Test
  public void testParseLargFileAndCheckRandomWords() throws Exception {

    Map<String, Long> wordsCountMap = new HashMap<>();
    wordsCountMap.put("software", 360L );
    wordsCountMap.put("provided", 78L );
    wordsCountMap.put("copyright", 213L );
    wordsCountMap.put("contributors", 34L );

    assertTrue( "Failed to load files", wordsCounter.load(new String[]{"words/inputLarge1.txt"}) );
    for( String key: wordsCountMap.keySet() ){
      assertTrue("Failed to check " + key, wordsCounter.wordCounts.containsKey(key) );
      assertEquals("Failed to check " + key, wordsCountMap.get(key).longValue(), wordsCounter.wordCounts.get(key).getOccurencesNo().sum() );
      assertTrue("Failed to check " + key, wordsCounter.wordCounts.get(key).getFileIdSet().contains("words/inputLarge1.txt"));
    }
  }

  @Test
  public void testProcessTwoFilesAndCheckThatComplexSearchExpressionWorks() throws Exception {
    String[] filesToProcess = new String[]{"words_small/inputSample1.txt", "words_small/inputSample2.txt"};
    assertTrue( "Failed to load files", wordsCounter.load(filesToProcess));

    String[] foundFiles = wordsCounter.search("a computer science").stream().toArray(String[]::new);
    Arrays.sort(foundFiles);
    assertArrayEquals("Failed to check inverted file index", filesToProcess, foundFiles);
  }

  @Test
  public void testProcessGroupLargeFilesAndCheckThatComplexSearchExpressionWorks() throws Exception {
    String[] filesToProcess = new String[]{"words/inputLarge1.txt", "words/inputLarge2.txt", "words/inputLarge3.txt", "words/inputSample1.txt"};
    assertTrue( "Failed to load files", wordsCounter.load(filesToProcess));

    String[] foundFiles = wordsCounter.search("a computer science").stream().toArray(String[]::new);
    Arrays.sort(foundFiles);
    assertArrayEquals("Failed to check inverted file index", filesToProcess, foundFiles);
  }
}
