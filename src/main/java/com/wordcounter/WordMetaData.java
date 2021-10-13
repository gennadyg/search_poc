package com.wordcounter;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * Word metadata class
 */
@Getter
@Setter
public class WordMetaData {
  private Set<String> fileIdSet = new HashSet<>();
  private LongAdder occurencesNo = new LongAdder();
  /**
   *
   * @param fileId - file unique id
   */
  public WordMetaData(String fileId){
    fileIdSet.add(fileId);
  }
}
