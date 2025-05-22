package servidor_central;

import java.io.*;

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
    private String message;
    TCPServerThread[] allCli;
    public int state = 0;   //0:menu, 1:constar saldo, 2: transferir dinero
    
    public TCPServerThread(Socket client, TCPServer tcpServer, int clientId, TCPServerThread[] allCli) {
        this.client = client;
        this.tcpServer = tcpServer;
        this.clientId = clientId;
        this.allCli = allCli;
    }

    public void run() {
        running = true;
        try {
            mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            mOut.println("""
                    ------------Bienvenido a su Banco------------
                    A continuación elija la operación que desea realizar
                    1) Consultar Saldo
                    2) Transferir Fondos
                    3) Salir
                    Ingrese una opción válida (1, 2 o 3):""");

            while (running && (message = in.readLine()) != null) {
                tcpServer.getMessageListener().messageReceived(message, clientId); 
            }

        } catch (IOException e) {
            System.out.println("Error en cliente #" + clientId + ": " + e.getMessage());
        } finally {
            stopClient();
        }
    }
    
    public void stopClient() {
        running = false;
        try {
            if (client != null) client.close();
        } catch (IOException e) {
            System.out.println("Error cerrando cliente #" + clientId);
        }
    }
    
    public void sendMessage(String message) {   //work function
        if(mOut != null && !mOut.checkError()) {
            mOut.println(message);
            mOut.flush();
        }
    }
}
