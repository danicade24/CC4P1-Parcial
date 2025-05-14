package servidor_central;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.Scanner;
/**
 *
 * @author daniela
 */
public class Servidor {
    TCPServer mTCPServer;
    Scanner sc;
    
    public static void main(String[] args) {
        Servidor objSer = new Servidor();
        objSer.init();
    }
    
    void init() {
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    mTCPServer = new TCPServer(
                        new TCPServer.OnMessageReceived(){
                            @Override
                            public void messageReceived(String message){
                                ReceiveServer(message);
                            }
                        }
                    );
                    mTCPServer.run();
                }

            }
        ).start();
        
        String exit = "n";
        sc = new Scanner(System.in);
        System.out.println("Flag Server 01");
        while (!exit.equals("s")) {            
            exit = sc.nextLine();
            SendServer(exit);
        }
        System.out.println("Flag Server 02");
    }
    
    void ReceiveServer(String message) {
        System.out.println("Client chose option " + message);
        if(message.equals("1")){
            System.out.println("ACÁ IMPLEMENTARE LA LOGICA DE LA PRIMERA OPCION");
            Consultar_Saldo("1");
        } else if(message.equals("2")){
            System.out.println("ACÁ IMPLEMENTARE LA LOGICA DE LA SEGUNDA OPCION");
            Transferir_Fondos("", "", 0);
        } else {
            System.out.println("OPCION INVALIDA");
        }
    }
    
    void SendServer(String message) {
        if(mTCPServer != null) {
            mTCPServer.sendMessageTCPServe(message);
        }
    }
    
    void Consultar_Saldo(String id_cuenta) {
        System.out.println("Saldo restante: ");
    }
    
    void Transferir_Fondos(String id_cuenta, String id_cuenta_destino, double monto){
        
    }
}
