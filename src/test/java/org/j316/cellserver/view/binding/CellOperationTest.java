package org.j316.cellserver.view.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CellOperationTest {

  @Test
  void defaultsToEmptySendValue() {
    CellOperation operation = new CellOperation();

    assertEquals("", operation.getSendValue());
  }

  @Test
  void updatesSendValue() {
    CellOperation operation = new CellOperation();

    operation.setSendValue("Hallo");

    assertEquals("Hallo", operation.getSendValue());
  }
}
