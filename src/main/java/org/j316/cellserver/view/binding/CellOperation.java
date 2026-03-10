package org.j316.cellserver.view.binding;

import org.springframework.stereotype.Component;

@Component
public class CellOperation {

  private String sendValue;

  public CellOperation() {
    this.sendValue = "";
  }

  public String getSendValue() {
    return sendValue;
  }

  public void setSendValue(String sendValue) {
    this.sendValue = sendValue;
  }
}
