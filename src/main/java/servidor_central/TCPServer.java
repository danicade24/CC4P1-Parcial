package servidor_central;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.net.Socket;
/**
 *
 * @author daniela
 */
public class TCPServer {
    private String message;
    int nCli = 0;
    public static final int SERVERPORT = 5000;
    private OnMessageReceived messageListener = null;
    private boolean running = false;
    TCPServerThread[] sendCli = new TCPServerThread[10];
    PrintWriter mOut;
    BufferedReader in;
    ServerSocket serverSocket;

    public TCPServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }
    
    public OnMessageReceived getMessageLisstener() {
        return this.messageListener;
    }
    
    public void sendMessageTCPServe(String message) {
        for(int i = 0; i <= nCli; i++) {
            sendCli[i].sendMessage(message);
            System.out.println("SENDING TO CLIENT " + (i));
        }
    }
    
    public void run() {
        running = true;
        try {
            System.out.println("TCP Server "+"S : Connecting...");
            serverSocket = new ServerSocket(SERVERPORT);
            
            while(running) {
                Socket client = serverSocket.accept();
                System.out.println("TCP Server"+" S : Connecting...");
                System.out.println("Bienvenido a su Banco");
                nCli++;
                System.out.println("Connected Client #" + nCli);
                sendCli[nCli] = new TCPServerThread(client, this, nCli, sendCli);
                Thread t = new Thread(sendCli[nCli]);
                t.start();
                System.out.println("New client connected: " + nCli + " connected clients");
            }
        } catch(Exception e){
            System.out.println("TCP Server " + "S: Error " + e);
        } finally {
            
        }
    }
    
    public TCPServerThread[] getClients(){
        return sendCli;
    }
    
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
