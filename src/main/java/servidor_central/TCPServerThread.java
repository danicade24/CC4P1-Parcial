package servidor_central;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.Socket;

/**
 *
 * @author daniela
 */
class TCPServerThread extends Thread{
    private Socket client;
    private TCPServer tcpServer;
    private int clientId;
    private boolean running = false;
    public PrintWriter mOut;
    public BufferedReader in;
    private TCPServer.OnMessageReceived messageListener = null;
    private String message;
    TCPServerThread[] allCli;
    public int state = 0;   //0:menu, 1:constar saldo, 2: transferir dinero
    
    public TCPServerThread(Socket client, TCPServer tcpServer, int clientId, TCPServerThread[] allCli) {
        this.client = client;
        this.tcpServer = tcpServer;
        this.clientId = clientId;
        this.allCli = allCli;
    }
    
    public void work(int cli) {
        mOut.println("Work [" + cli + "]...");
    }
    
    public void run() {
        running = true;
        try {
            try {
//                boolean 
                mOut = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(client.getOutputStream())), true);
//                System.out.println("TCP Server " + "C: Sent");
                mOut.println("------------Bienvenido a su Banco------------\n"
                        + "A continuación elija la operación que desa realizar\n"
                        + "1) Consultar Saldo\n"
                        + "2) Transferir Fondos\n"
                        + "3) Salir\n"
                        + "Ingrese una opcion valida (1, 2 o 3)");
                messageListener = tcpServer.getMessageLisstener();
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                while (running) {  
                    message = in.readLine();
                    
                    if(message != null && messageListener != null) {
                        messageListener.messageReceived(message, clientId);
                    }
                    
                    message = null;
                }
                System.out.println("RESPONSE FROM CLIENT " + "S: Received message: '" + message + "'");
            } catch(Exception e){
                System.out.println("TCP Server " + "S: Error " + e);
            } finally {
                client.close();
            }
        } catch(Exception e) {
            System.out.println("TCP Server " + "S: Error " + e);
        }
    }
    
    public void stopClient() {
        running = false;
    }
    
    public void sendMessage(String message) {   //work function
        if(mOut != null && !mOut.checkError()) {
            mOut.println(message);
            mOut.flush();
        }
    }
}
