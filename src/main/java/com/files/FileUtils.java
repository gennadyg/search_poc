package com.files;

import org.apache.log4j.Logger;

import java.io.File;

/**
 *
 */
public class FileUtils {

  private static final Logger logger = Logger.getLogger( FileUtils.class );

  /**
   *  Check whether file exist
   *
   * @param fileName - file name
   *
   * @return true / false
   */
  public static boolean checkThatFileExists( String fileName ){
    File file = new File( fileName );
    if( file.exists() && !file.isDirectory() && file.length() > 0 ) {
      return true;
    }else
      return false;
  }
}
