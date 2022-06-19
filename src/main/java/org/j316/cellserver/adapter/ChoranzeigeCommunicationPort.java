package org.j316.cellserver.adapter;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Component
public class ChoranzeigeCommunicationPort {

    @Value("${cell.ip}")
    private String ip;

    @Value("${cell.port}")
    private Integer port;

    @Value("${cell.connection.timeout}")
    private Integer timeout;

    private final static byte STX = (byte) 0x00;
    private final static byte FTX = (byte) 0xff;

    private final static byte[] INITPACKET = new byte[]{
            STX, FTX, FTX, (byte) 0x01, // Searial Address Paket,
            0x0b, 0x01, FTX// xpanded serial address packet
            //     0x10, 0x50 // Größe und Breite
    };


    public String clear() {

        return sendTxt("");
    }

    public String ping() {
        Socket rowSocket = new Socket();
        StringBuilder retVal = new StringBuilder();
        try {
            rowSocket.connect(new InetSocketAddress(ip, port), timeout);
            if (rowSocket.isConnected())
                retVal.append("connected");
            else
                retVal.append("not connected");

            rowSocket.close();
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout occured " + ip);
            retVal.append("\n Zeitüberschreitung.. sorry");
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + ip);
            retVal.append("\n Don't know about host: ").append(ip);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + ip);
            retVal.append("\n Couldn't get I/O for the connection to: ").append(ip);
            e.printStackTrace();
        }

        return retVal.toString();
    }

    public String sendTxt(String msg) {

        byte[] TEXTFILE = new byte[]{
                0x01, // Start of TXT
                0x30, 0x31, // Filename
                0x02, (byte) 0xef, (byte) 0xb0, (byte) 0xef, (byte) 0xa2 // Styles
        };

        byte[] sendMSG = ArrayUtils.addAll(INITPACKET, TEXTFILE);
        sendMSG = ArrayUtils.addAll(sendMSG, msg.getBytes(StandardCharsets.US_ASCII));
        sendMSG = ArrayUtils.addAll(sendMSG, FTX, FTX);
        String retVal = send(sendMSG);

        if (msg.length() > 0)
            retVal = retVal + ("\n Text sent: " + msg);

        return retVal;
    }


    public String send(byte[] frame) {
        StringBuilder retVal = new StringBuilder();
        Socket rowSocket = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            rowSocket = new Socket();
            rowSocket.connect(new InetSocketAddress(ip, port), timeout);

            os = new DataOutputStream(rowSocket.getOutputStream());
            is = new DataInputStream(rowSocket.getInputStream());

            retVal.append("\n Connection ok.");
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout occured " + ip);
            retVal.append("\n Zeitüberschreitung.. sorry");
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + ip);
            retVal.append("\n Don't know about host: ").append(ip);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + ip);
            retVal.append("\n Couldn't get I/O for the connection to: ").append(ip);
            e.printStackTrace();
        }

        if (os != null && is != null) {
            try {

                os.write(frame);
                os.write(STX);

                os.flush();

                os.close();
                is.close();
                rowSocket.close();
                retVal.append("\n Fertig.");
            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + ip);
                retVal.append("\n Trying to connect to unknown host.");
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
                retVal.append("\n IOException");
            }
        }

        return retVal.toString();

    }
}
