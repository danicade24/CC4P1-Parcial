package servidor_central;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.*;

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
                            public void messageReceived(String message, int clientId){
                                ReceiveServer(message, clientId);
                            }
                        }
                    );
                    mTCPServer.run();
                }

            }
        ).start();
        inicializarMapa();
        System.out.println("Servidor iniciado. Escribe 'stop' para detenerlo.");
        
        String exit = "n";
        sc = new Scanner(System.in);
        System.out.println("Flag Server 01");
        while (!exit.equals("s")) {
            exit = sc.nextLine();

            if (exit.equals("stop")) {
                if (mTCPServer != null) {
                    mTCPServer.stopServer();
                }
                break; // rompe el bucle
            }

            SendServer(exit); // sigue funcionando para enviar a clientes
        }
        System.out.println("Flag Server 02");
    }
    
    void ReceiveServer(String message, int clientId) {
//        System.out.println("Client chose option " + message);
//        if(message.equals("1")){
//            System.out.println("ACÁ IMPLEMENTARE LA LOGICA DE LA PRIMERA OPCION");
//            Consultar_Saldo("1");
//        } else if(message.equals("2")){
//            System.out.println("ACÁ IMPLEMENTARE LA LOGICA DE LA SEGUNDA OPCION");
//            Transferir_Fondos("", "", 0);
//        } else {
//            System.out.println("OPCION INVALIDA");
//        }
        TCPServerThread client = mTCPServer.getClients()[clientId];
        switch (client.state) {
            case 0:
                if(message.equals("1")){
                    client.state = 1;
                    client.sendMessage("Ingrese el ID de su cuenta:");
                } else if(message.equals("2")){
                    client.state = 2;
                    client.sendMessage("Ingrese: ID_ORIGEN,ID_DESTINO,MONTO (ej. 101,102,500.00)");
                } else if(message.equals("3")) {
                    client.sendMessage("Gracias por usar el banco. Cerrando sesión...");
                    client.stopClient();
                } else {
                    client.sendMessage("OPCION INVALIDA");
                }
                break;
            case 1:
                Consultar_Saldo(message, client);
                client.state = 0;
                client.sendMessage("¿Desea realizar otra operación? Ingrese 1 o 2 o 3:");
                break;
            case 2:
                String[] datos = message.split(",");
                if (datos.length == 3) {
                    try {
                        String origen = datos[0].trim();
                        String destino = datos[1].trim();
                        double monto = Double.parseDouble(datos[2].trim());
                        Transferir_Fondos(origen, destino, monto, client);
                    } catch (NumberFormatException e) {
                        client.sendMessage("Monto inválido. Intente de nuevo.");
                    }
                } else {
                    client.sendMessage("Formato incorrecto. Use: ID_ORIGEN,ID_DESTINO,MONTO");
                }
                client.state = 0;
                client.sendMessage("¿Desea realizar otra operación? Ingrese 1 o 2 o 3:");
                break;
        }
    }
    
    void SendServer(String message) {
        if(mTCPServer != null) {
            mTCPServer.sendMessageTCPServe(message);
        }
    }
    
    void Consultar_Saldo(String id_cuenta_str, TCPServerThread client) {
        try {
            int idCuenta = Integer.parseInt(id_cuenta_str);
            int puertoNodo = obtenerPuertoNodo(idCuenta);

            if (puertoNodo == -1) {
                client.sendMessage("ERROR | Cuenta fuera de rango");
                return;
            }

            try (Socket nodo = new Socket("localhost", puertoNodo);
                 PrintWriter out = new PrintWriter(nodo.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(nodo.getInputStream()))) {

                out.println("CONSULTAR_SALDO|" + idCuenta);
                String respuesta = in.readLine();
                client.sendMessage(respuesta);

            } catch (IOException e) {
                client.sendMessage("ERROR | Nodo inalcanzable: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            client.sendMessage("ERROR | ID de cuenta inválido.");
        }
    }
    
    int obtenerPuertoNodo(int idCuenta) {
        for (Map.Entry<Integer, Integer> entry : mapaNodos.entrySet()) {
            int rangoInicio = entry.getKey();
            if (idCuenta >= rangoInicio && idCuenta < rangoInicio + 100) {
                return entry.getValue();
            }
        }
        return -1; // No encontrado
    }

    
    void Transferir_Fondos(String id_cuenta_origen, String id_cuenta_destino, double monto, TCPServerThread client){
        try {
            int idOrigen = Integer.parseInt(id_cuenta_origen);
            int puertoNodo = obtenerPuertoNodo(idOrigen);
            
            if (puertoNodo == -1) {
                client.sendMessage("ERROR | No se encuentra el nodo para cuenta de origen");
            }
            
            try (Socket nodo = new Socket("localhost", puertoNodo);
                 PrintWriter out = new PrintWriter(nodo.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(nodo.getInputStream()))) {
                
                out.println("TRANSFERIR_FONDOS|" + id_cuenta_origen + "|" + id_cuenta_destino + "|" + monto);
                String respuesta = in.readLine();
                client.sendMessage(respuesta);
            } catch (IOException e) {
                client.sendMessage("ERROR | No se puede alcanzar el nodo : " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            client.sendMessage("ERROR | ID inválido");
        }
    }
    
    Map<Integer, Integer> mapaNodos = new HashMap<>();
    
    void inicializarMapa() {
        mapaNodos.put(101, 6001);
        mapaNodos.put(105, 6002);
        mapaNodos.put(108, 6003);
    }
    
}
