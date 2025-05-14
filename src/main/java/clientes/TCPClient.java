package clientes;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author daniela
 */
public class TCPClient {
    private String serverMsg;
    public String SERVERIP;
    public static final int SERVERPORT = 5000;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    
    PrintWriter out;
    BufferedReader in;

    public TCPClient(String ip, OnMessageReceived listener) {
        SERVERIP = ip;
        mMessageListener = listener;
    }    
    
    public void sendMessage(String message) {
        if(out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }
    
    public void stopClient() {
        mRun = false;
    }
    
    public void run() {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            System.out.println("TCP Client"+ "C: Conectando...");
            Socket socket = new Socket(serverAddr, SERVERPORT);
            
            try {
                out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())), true);
                System.out.println("TCP Client"+ "C: Sent.");
                System.out.println("TCP Client"+ "C: Done.");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while(mRun) {
                    serverMsg = in.readLine();
                    if(serverMsg != null && mMessageListener != null) {
                        mMessageListener.messageReceived(serverMsg);
                    }
                    serverMsg = null;
                }
            } catch (Exception e) {
                System.out.println("TCP"+ "C: Error"+ e);
                
            } finally {
                socket.close();
            }
            
        } catch (Exception e) {
            System.out.println("TCP"+ "C: Error"+ e);
        }
    }
    
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
