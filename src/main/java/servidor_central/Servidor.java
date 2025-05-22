package servidor_central;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.*;
import org.json.*;

/**
 *
 * @author daniela
 */
public class Servidor {
    TCPServer mTCPServer;
    Scanner sc;
    private static final String CONFIG_PATH = "src/main/java/config/config_nodos.json";
    Map<String, List<Integer>> mapaParticiones = new HashMap<>();
    
    public static void main(String[] args) {
        Servidor objSer = new Servidor();
        objSer.init();
    }
    
    void init() {
        cargarAsignacionesDesdeJson();
        // Inicia el servidor TCP
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
        
        System.out.println("Servidor iniciado. Escribe 'stop' para detenerlo.");
        sc = new Scanner(System.in);
        String exit;
        
        while (true) {
            exit = sc.nextLine();
            if (exit.equalsIgnoreCase("stop")) {
                if (mTCPServer != null) {
                    mTCPServer.stopServer();
                }
                break; // rompe el bucle
            }
            SendServer(exit); // sigue funcionando para enviar a clientes
        }
    }
    
    void ReceiveServer(String message, int clientId) {
        TCPServerThread client = mTCPServer.getClients()[clientId];
        switch (client.state) {
            case 0:
                String opcion = message.trim();
                if (opcion.equals("1")) {
                    client.state = 1;
                    client.sendMessage("Ingrese el ID de su cuenta:");
                } else if (opcion.equals("2")) {
                    client.state = 2;
                    client.sendMessage("Ingrese: ID_ORIGEN,ID_DESTINO,MONTO (ej. 101,102,500.00)");
                } else if (opcion.equals("3")) {
                    client.sendMessage("Gracias por usar el banco. Cerrando sesión...");
                    client.stopClient();
                } else {
                    client.sendMessage("Opción inválida. Ingrese 1, 2 o 3:");
                }
                break;

            case 1:
                String respuesta = procesarOperacion("CONSULTAR_SALDO|" + message.trim());
                client.sendMessage(respuesta);
                client.state = 0;
                client.sendMessage("¿Desea realizar otra operación? Ingrese 1, 2 o 3:");
                break;
                
            case 2:
                String[] datos = message.split(",");
                if (datos.length == 3) {
                    try {
                        String origen = datos[0].trim();
                        String destino = datos[1].trim();
                        double monto = Double.parseDouble(datos[2].trim());
                        String operacion = "TRANSFERIR_FONDOS|" + origen + "|" + destino + "|" + monto;
                        respuesta = procesarOperacion(operacion);
                        client.sendMessage(respuesta);
                    } catch (NumberFormatException e) {
                        client.sendMessage("Monto inválido. Intente de nuevo.");
                    }
                } else {
                    client.sendMessage("Formato incorrecto. Use: ID_ORIGEN,ID_DESTINO,MONTO");
                }
                client.state = 0;
                client.sendMessage("¿Desea realizar otra operación? Ingrese 1, 2 o 3:");
                break;
        }
    }
    
    void SendServer(String message) {
        if(mTCPServer != null) {
            mTCPServer.sendMessageTCPServe(message);
        }
    }
    
     public void cargarAsignacionesDesdeJson() {
        try (InputStream is = new FileInputStream(CONFIG_PATH)) {
            JSONArray nodos = new JSONArray(new JSONTokener(is));
            for (int i = 0; i < nodos.length(); i++) {
                JSONObject nodo = nodos.getJSONObject(i);
                int puerto = nodo.getInt("puerto");

                for (int j = 0; j < 2; j++) {
                    String prefijo = (j == 0) ? "particionesClientes" : "particionesCuentas";
                    JSONArray partes = nodo.getJSONArray(prefijo);
                    for (int k = 0; k < partes.length(); k++) {
                        String clave = partes.getString(k);
                        if (!mapaParticiones.containsKey(clave)) {
                            mapaParticiones.put(clave, new ArrayList<Integer>());
                        }
                        mapaParticiones.get(clave).add(puerto);
                    }
                }
            }

            System.out.println("Configuración de nodos cargada correctamente.");
        } catch (IOException e) {
            System.err.println("Error cargando configuración: " + e.getMessage());
        }
    }

    public String procesarOperacion(String operacion) {
        try {
            if (operacion.startsWith("CONSULTAR_SALDO")) {
                String[] partes = operacion.split("\\|");
                String idCuenta = partes[1].trim();
                String claveParticion = obtenerClaveParticion("parte1", idCuenta);
                return reenviarAServidorDisponible(claveParticion, operacion);
            } else if (operacion.startsWith("TRANSFERIR_FONDOS")) {
                String[] partes = operacion.split("\\|");
                String idOrigen = partes[1].trim();
                String claveParticion = obtenerClaveParticion("parte2", idOrigen);
                return reenviarAServidorDisponible(claveParticion, operacion);
            }
        } catch (Exception e) {
            return "ERROR|Formato incorrecto: " + e.getMessage();
        }
        return "ERROR|Comando no reconocido";
    }

    public String reenviarAServidorDisponible(String claveParticion, String mensaje) {
        List<Integer> puertos = mapaParticiones.getOrDefault(claveParticion, Collections.emptyList());

        for (int i = 0; i < puertos.size(); i++) {
            int puerto = puertos.get(i);
            try (Socket socket = new Socket("localhost", puerto);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(mensaje);
                String respuesta = in.readLine();
                return (respuesta != null) ? respuesta : "ERROR|Nodo sin respuesta";

            } catch (IOException e) {
                System.out.println("Nodo en puerto " + puerto + " no disponible. Intentando siguiente...");
            }
        }
        return "ERROR|Ningún nodo respondió para " + claveParticion;
    }

    public String obtenerClaveParticion(String tabla, String id) {
        int numero = Integer.parseInt(id);
        int numParticiones = obtenerNumeroDeParticiones(tabla);
        int particion = (numero % numParticiones) + 1;
        return tabla + "." + particion;
    }

    public int obtenerNumeroDeParticiones(String tabla) {
        int count = 0;
        for (String key : mapaParticiones.keySet()) {
            if (key.startsWith(tabla + ".")) {
                count++;
            }
        }
        System.out.println("Total particiones para " + tabla + ": " + count);
        return count;
    }
    
}