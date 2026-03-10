package org.j316.cellserver.adapter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChoranzeigeCommunicationPort {

  private static final Logger log = LoggerFactory.getLogger(ChoranzeigeCommunicationPort.class);

  private static final byte STX = (byte) 0x00;
  private static final byte FTX = (byte) 0xff;

  private static final String PING_TIMEOUT_MESSAGE =
      "Verbindung zur Choranzeige konnte innerhalb des konfigurierten Timeouts nicht aufgebaut werden.";
  private static final String SEND_TIMEOUT_MESSAGE =
      "Senden abgebrochen: Verbindungsaufbau hat das konfigurierte Timeout überschritten.";
  private static final String CONNECTED_MESSAGE = "Verbindung zur Choranzeige aufgebaut.";
  private static final String SEND_SUCCESS_MESSAGE = "Datenpaket erfolgreich übertragen.";

  private static final byte[] INIT_PACKET = new byte[] {
      STX, FTX, FTX, (byte) 0x01,
      0x0b, 0x01, FTX
  };

  private static final byte[] TEXT_FILE = new byte[] {
      0x01,
      0x30, 0x31,
      0x02, (byte) 0xef, (byte) 0xb0, (byte) 0xef, (byte) 0xa2
  };

  @Value("${adapter.cell.ip}")
  private String ip;

  @Value("${adapter.cell.port}")
  private int port;

  @Value("${adapter.cell.connection.timeout}")
  private int timeout;

  public String clear() {
    return sendTxt("");
  }

  public String ping() {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(ip, port), timeout);
      return socket.isConnected() ? "connected" : "not connected";
    } catch (SocketTimeoutException ex) {
      log.warn("Timeout while pinging {}:{}", ip, port, ex);
      return PING_TIMEOUT_MESSAGE;
    } catch (IOException ex) {
      log.warn("I/O error while pinging {}:{}", ip, port, ex);
      return String.format("Verbindungsprüfung fehlgeschlagen (I/O-Fehler). Ziel: %s", ip);
    }
  }

  public String sendTxt(String msg) {
    SendResult sendResult = send(buildFrame(msg));

    if (!msg.isEmpty() && sendResult.success()) {
      return sendResult.message() + "\nÜbertragener Text: " + msg;
    }

    return sendResult.message();
  }

  private byte[] buildFrame(String msg) {
    ByteArrayOutputStream frame = new ByteArrayOutputStream();
    frame.writeBytes(INIT_PACKET);
    frame.writeBytes(TEXT_FILE);
    frame.writeBytes(msg.getBytes(StandardCharsets.US_ASCII));
    frame.write(FTX);
    frame.write(FTX);
    return frame.toByteArray();
  }

  public SendResult send(byte[] frame) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(ip, port), timeout);

      try (DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
        os.write(frame);
        os.write(STX);
        os.flush();
      }

      return new SendResult(true, CONNECTED_MESSAGE + "\n" + SEND_SUCCESS_MESSAGE);
    } catch (SocketTimeoutException ex) {
      log.warn("Timeout while sending data to {}:{}", ip, port, ex);
      return new SendResult(false, SEND_TIMEOUT_MESSAGE);
    } catch (IOException ex) {
      log.warn("I/O error while sending data to {}:{}", ip, port, ex);
      return new SendResult(false,
          String.format("Senden fehlgeschlagen (I/O-Fehler). Ziel: %s", ip));
    }
  }

  public record SendResult(boolean success, String message) {
  }
}
