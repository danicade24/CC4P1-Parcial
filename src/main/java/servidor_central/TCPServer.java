package servidor_central;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;
/**
 *
 * @author daniela
 */
public class TCPServer {
    int nCli = 0;
    public static final int SERVERPORT = 5000;
    private final OnMessageReceived messageListener;
    private boolean running = false;
    private final TCPServerThread[] sendCli = new TCPServerThread[50];
    private ServerSocket serverSocket;

    public TCPServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }
    
    public OnMessageReceived getMessageListener() {
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
//                System.out.println("TCP Server"+" S : Connecting...");
                System.out.println("Bienvenido a su Banco");
                nCli++;
                sendCli[nCli] = new TCPServerThread(client, this, nCli, sendCli);
                Thread t = new Thread(sendCli[nCli]);
                t.start();
                System.out.println("Nuevo cliente conectado: #" + nCli);
            }
        } catch(Exception e){
            System.out.println(" Error en servidor: " + e.getMessage());
        } 
    }
    
    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // fuerza salida del .accept()
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar el servidor: " + e.getMessage());
        }

        // Detener todos los hilos de clientes
        for (int i = 0; i <= nCli; i++) {
            if (sendCli[i] != null) {
                sendCli[i].stopClient(); // método ya existente
            }
        }
        System.out.println("Servidor detenido correctamente.");
    }
    
    public TCPServerThread[] getClients(){
        return sendCli;
    }
    
    public interface OnMessageReceived {
        public void messageReceived(String message, int clientId);
    }
}
