package clientes;

import java.util.Scanner;
/**
 *
 * @author daniela
 */
public class Client {
    TCPClient mTcpClient;
    Scanner sc;
    public static void main(String[] args)  {
        Client objcli = new Client();
        objcli.iniciar();
    }
    void iniciar(){
       new Thread(
            new Runnable() {

                @Override
                public void run() {
                    mTcpClient = new TCPClient("127.0.0.1",
                        new TCPClient.OnMessageReceived(){
                            @Override
                            public void messageReceived(String message){
                                ClientReceive(message);
                            }
                        }
                    );
                    mTcpClient.run();                   
                }
            }
        ).start();
        //---------------------------
        String option = "", m = "";
        sc = new Scanner(System.in);
        while (!option.equals("s")) {        
            option = sc.nextLine();
            
            m = "Escogio la opcion " + option;

            System.out.println(m);
            ClientSend(option);
        }
        
//        String salir = "n";
//        sc = new Scanner(System.in);
//        System.out.println("Flag Client 01");
//        while( !salir.equals("s")){
//            salir = sc.nextLine();
//            ClienteEnvia(salir);
//        }
//        System.out.println("Flag Client 02");
    
    }
    void ClientReceive(String message){
        System.out.println(message);

    }
    void ClientSend(String envia){
        if (mTcpClient != null) {
            mTcpClient.sendMessage(envia);
        }
    }
}
