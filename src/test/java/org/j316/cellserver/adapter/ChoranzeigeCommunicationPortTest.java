package org.j316.cellserver.adapter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ChoranzeigeCommunicationPortTest {

  @Test
  void pingReturnsConnectedWhenSocketAccepts() throws Exception {
    ChoranzeigeCommunicationPort port = new ChoranzeigeCommunicationPort();
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      configure(port, "127.0.0.1", serverSocket.getLocalPort(), 300);

      Thread acceptThread = new Thread(() -> {
        try (Socket ignored = serverSocket.accept()) {
        } catch (IOException ignored) {
        }
      });
      acceptThread.start();

      assertEquals("connected", port.ping());
      acceptThread.join(1000);
    }
  }

  @Test
  void clearSendsEmptyMessageFrame() throws Exception {
    ChoranzeigeCommunicationPort port = new ChoranzeigeCommunicationPort();
    AtomicReference<byte[]> received = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);

    try (ServerSocket serverSocket = new ServerSocket(0)) {
      configure(port, "127.0.0.1", serverSocket.getLocalPort(), 500);

      Thread serverThread = new Thread(() -> {
        try (Socket socket = serverSocket.accept()) {
          received.set(readAll(socket.getInputStream()));
          latch.countDown();
        } catch (IOException ignored) {
        }
      });
      serverThread.start();

      String result = port.clear();

      assertTrue(result.contains("Verbindung zur Choranzeige aufgebaut."));
      assertTrue(result.contains("Datenpaket erfolgreich übertragen."));
      assertTrue(latch.await(2, TimeUnit.SECONDS));
      assertArrayEquals(expectedFrame(""), received.get());
      serverThread.join(1000);
    }
  }

  @Test
  void sendTxtSendsFrameAndAppendsMessageInfo() throws Exception {
    ChoranzeigeCommunicationPort port = new ChoranzeigeCommunicationPort();
    AtomicReference<byte[]> received = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);

    try (ServerSocket serverSocket = new ServerSocket(0)) {
      configure(port, "127.0.0.1", serverSocket.getLocalPort(), 500);

      Thread serverThread = new Thread(() -> {
        try (Socket socket = serverSocket.accept()) {
          received.set(readAll(socket.getInputStream()));
          latch.countDown();
        } catch (IOException ignored) {
        }
      });
      serverThread.start();

      String result = port.sendTxt("HALLO");

      assertTrue(result.contains("Verbindung zur Choranzeige aufgebaut."));
      assertTrue(result.contains("Datenpaket erfolgreich übertragen."));
      assertTrue(result.contains("Übertragener Text: HALLO"));
      assertTrue(latch.await(2, TimeUnit.SECONDS));
      assertArrayEquals(expectedFrame("HALLO"), received.get());
      serverThread.join(1000);
    }
  }

  @Test
  void sendTxtDoesNotAppendTextOnConnectionFailure() {
    ChoranzeigeCommunicationPort port = new ChoranzeigeCommunicationPort();
    configure(port, "127.0.0.1", 1, 100);

    String result = port.sendTxt("HALLO");

    assertFalse(result.contains("Übertragener Text: HALLO"));
  }

  @Test
  void pingReturnsIoErrorMessageOnConnectionFailure() {
    ChoranzeigeCommunicationPort port = new ChoranzeigeCommunicationPort();
    configure(port, "127.0.0.1", 1, 100);

    String result = port.ping();

    assertEquals("Verbindungsprüfung fehlgeschlagen (I/O-Fehler). Ziel: 127.0.0.1", result);
  }

  private static void configure(ChoranzeigeCommunicationPort port, String ip, int endpointPort,
      int timeout) {
    ReflectionTestUtils.setField(port, "ip", ip);
    ReflectionTestUtils.setField(port, "port", endpointPort);
    ReflectionTestUtils.setField(port, "timeout", timeout);
  }

  private static byte[] readAll(InputStream inputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[256];
    int read;
    while ((read = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, read);
    }
    return outputStream.toByteArray();
  }

  private static byte[] expectedFrame(String message) {
    ByteArrayOutputStream expected = new ByteArrayOutputStream();
    expected.writeBytes(new byte[] {(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        0x0b, 0x01, (byte) 0xff});
    expected.writeBytes(new byte[] {0x01, 0x30, 0x31, 0x02, (byte) 0xef, (byte) 0xb0, (byte) 0xef,
        (byte) 0xa2});
    expected.writeBytes(message.getBytes(StandardCharsets.US_ASCII));
    expected.write((byte) 0xff);
    expected.write((byte) 0xff);
    expected.write((byte) 0x00);
    return expected.toByteArray();
  }
}
