package com.utils;

import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *  Helper class to write/read content to com.files
 */
public class FileAppender {
  private static final Logger logger = Logger.getLogger( FileAppender.class );

  /**
   * Write to file some content
   *
   * @param fileName - file name
   * @param textToAppend - text to write
   * @param append - true if append, false otherwize
   *
   */
  public static void appendToFile(String fileName, String textToAppend, boolean append  ){
    try( FileOutputStream outputStream = new FileOutputStream(fileName, append )){
      outputStream.write( textToAppend.getBytes("UTF-8") );
    }catch ( Exception ex ){
      logger.error("Failed to appand to file - " +fileName, ex );
    }
  }

  /**
   *
   * @param fileName - file name
   */
  public static String readFileContent(String fileName ) throws IOException {
    return new String(Files.readAllBytes(Paths.get(fileName)));
  }
}
