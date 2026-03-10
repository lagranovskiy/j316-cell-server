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
      return "Zeitüberschreitung.. sorry";
    } catch (IOException ex) {
      log.warn("I/O error while pinging {}:{}", ip, port, ex);
      return "Couldn't get I/O for the connection to: " + ip;
    }
  }

  public String sendTxt(String msg) {
    byte[] frame = buildFrame(msg);
    String result = send(frame);

    if (!msg.isEmpty()) {
      result = result + "\n Text sent: " + msg;
    }

    return result;
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

  public String send(byte[] frame) {
    StringBuilder retVal = new StringBuilder();

    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(ip, port), timeout);
      retVal.append("\n Connection ok.");

      try (DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
        os.write(frame);
        os.write(STX);
        os.flush();
      }

      retVal.append("\n Fertig.");
    } catch (SocketTimeoutException ex) {
      log.warn("Timeout while sending data to {}:{}", ip, port, ex);
      retVal.append("\n Zeitüberschreitung.. sorry");
    } catch (IOException ex) {
      log.warn("I/O error while sending data to {}:{}", ip, port, ex);
      retVal.append("\n Couldn't get I/O for the connection to: ").append(ip);
    }

    return retVal.toString();
  }
}
