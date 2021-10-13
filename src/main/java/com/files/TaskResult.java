package com.files;

import lombok.Getter;
import lombok.Setter;
import com.utils.Constants.ResultType;

/**
 *  Object that contains results of each callable task
 */
@Getter
@Setter
public class TaskResult {

  protected String resultMessage;
  protected long taskId;
  protected int numOfProcessed = 0;
  protected String fileName;

  public TaskResult( long id, String fileName ){
    resultMessage = ResultType.Ok.getName();
    taskId = id;
    this.fileName = fileName;
  }

  public long getTaskId() {
    return taskId;
  }

  public int getNumOfProcessed() {
    return numOfProcessed;
  }

  public void setResultMessage(String resultMessage) {
    this.resultMessage = resultMessage;
  }

  public void setNumOfProcessed(int numOfProcessed) {
    this.numOfProcessed = numOfProcessed;
  }

  public String getResultMessage() {
    return resultMessage;
  }
}
