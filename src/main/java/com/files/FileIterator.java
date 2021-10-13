package com.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 *  File content iterator, to read file
 */
public class FileIterator implements Iterator<String>, Closeable {

  private static final Logger logger = LoggerFactory.getLogger( FileIterator.class );
  public String nextLine = null;
  private BufferedReader in = null;
  /**
   *  File iterator wrapper
   *
   * @param fileName - file name
   * @throws FileNotFoundException
   */
  public FileIterator(String fileName ) throws FileNotFoundException {
    try{
      in = new BufferedReader( new FileReader( fileName ));
    }catch ( FileNotFoundException ex ){
      logger.error("Failed to open file " + fileName, ex );
      throw ex;
    }
  }

  /**
   *  Checks if file has a next line
   *
   * @return true if has next line, false otherwise
   */
  @Override
  public boolean hasNext() {

    if (nextLine != null) {
      return true;
    } else {
      try {
        nextLine = in.readLine();
        return (nextLine != null);
      } catch ( IOException  e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  /**
   *  Iterator that
   *
   * @return string - next line in file
   */
  @Override
  public String next() {
    if (nextLine != null || hasNext()) {
      String line = nextLine;
      nextLine = null;
      return line;
    } else {
      throw new NoSuchElementException();
    }
  }

  /**
   *  Close file if open, used as part of autocloseable interface,
   *  to make file closing managed by try
   */
  @Override
  public void close() {
    try{
      if( in != null ){
        in.close();
      }
    }catch ( Exception ex ){
      logger.error("Failed to close file", ex );
    }
  }
}
