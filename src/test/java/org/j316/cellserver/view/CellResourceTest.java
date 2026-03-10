package org.j316.cellserver.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.j316.cellserver.adapter.ChoranzeigeCommunicationPort;
import org.j316.cellserver.view.binding.CellOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class CellResourceTest {

  private ChoranzeigeCommunicationPort communicationPort;
  private CellOperation sharedOperation;
  private CellResource resource;

  @BeforeEach
  void setUp() {
    communicationPort = mock(ChoranzeigeCommunicationPort.class);
    sharedOperation = new CellOperation();
    resource = new CellResource(communicationPort, sharedOperation);
  }

  @Test
  void initAddsModelAttributesAndReturnsIndex() {
    Model model = new ExtendedModelMap();
    when(communicationPort.ping()).thenReturn("connected");

    String viewName = resource.init(model);

    assertEquals("index", viewName);
    assertEquals(sharedOperation, model.getAttribute("cellOperation"));
    assertEquals("connected", model.getAttribute("result"));
    verify(communicationPort).ping();
  }

  @Test
  void sendMessageStoresValueAndResultInModel() {
    Model model = new ExtendedModelMap();
    CellOperation requestOperation = new CellOperation();
    requestOperation.setSendValue("Test");
    when(communicationPort.sendTxt("Test")).thenReturn("ok");

    String viewName = resource.sendMessage(requestOperation, model);

    assertEquals("index", viewName);
    assertEquals("Test", sharedOperation.getSendValue());
    assertEquals(sharedOperation, model.getAttribute("cellOperation"));
    assertEquals("ok", model.getAttribute("result"));
    verify(communicationPort).sendTxt("Test");
  }

  @Test
  void clearMessageResetsStateAndReturnsIndex() {
    Model model = new ExtendedModelMap();
    sharedOperation.setSendValue("Vorher");
    when(communicationPort.clear()).thenReturn("cleared");

    String viewName = resource.clearMessage(model);

    assertEquals("index", viewName);
    assertEquals("", sharedOperation.getSendValue());
    assertEquals(sharedOperation, model.getAttribute("cellOperation"));
    assertEquals("cleared", model.getAttribute("result"));
    verify(communicationPort).clear();
  }
}
