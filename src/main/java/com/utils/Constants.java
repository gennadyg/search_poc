package com.utils;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;


public class Constants {

  public static Set<String> stopWords = new HashSet<String>(){{
    add("an");
    add("is");
    add("the");
    add("a");
    add("and");
  }};
  public final static int maxNumOfFilesInBatch = 2;

  public final static String input = "input";
  public final static String timeout = "timeout";
  public final static String numOfThreads = "numOfThreads";

  public final static String regexToSplitWords = "\\W+";

  @Getter
  public static enum ResultType
  {
    Ok( "File processed successfully", 0 ),
    Fail( "Failed to process file", 3 ),
    BadContent( "Content is empty", 1 ),
    FileNotExist( "File not exist", 2 );

    private final String name;
    private int code = 0;

    ResultType( String name, int code )
    {
      this.name = name;
      this.code = code;
    }

    public String getName() {
      return name;
    }

    public int getCode() {
      return code;
    }
  }
}

