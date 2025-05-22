package clientes;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 *
 * @author daniela
 */

public class ClienteAuto {

    private static final String HOST = "localhost";
    private static final int PUERTO_SERVIDOR = 5000; // Cambia si usas otro puerto
    private static final int NUM_CLIENTES = 10;
    private static final int TRANSACCIONES_POR_CLIENTE = 5;

    public static void main(String[] args) {
        for (int i = 0; i < NUM_CLIENTES; i++) {
            final int clienteId = i + 1;
            new Thread(() -> simularCliente(clienteId)).start();
        }
    }

    private static void simularCliente(int clienteId) {
        Random rand = new Random();
        for (int t = 0; t < TRANSACCIONES_POR_CLIENTE; t++) {
            try (Socket socket = new Socket(HOST, PUERTO_SERVIDOR);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                String operacion;
                if (rand.nextBoolean()) {
                    // CONSULTAR_SALDO
                    int idCuenta = 100 + rand.nextInt(11); 
                    operacion = "CONSULTAR_SALDO|" + idCuenta;
                } else {
                    // TRANSFERIR_FONDOS
                    int origen = 100 + rand.nextInt(10);
                    int destino = 100 + rand.nextInt(10);
                    double monto = 50 + rand.nextInt(200); // entre 50 y 250
                    operacion = "TRANSFERIR_FONDOS|" + origen + "|" + destino + "|" + monto;
                }

                out.println(operacion);
                StringBuilder respuesta = new StringBuilder();
                String linea;
                String respuestaUtil = null;

                while ((linea = in.readLine()) != null) {
                    if (linea.startsWith("SALDO|") || linea.startsWith("CONFIRMACION|") || linea.startsWith("ERROR|")) {
                        respuestaUtil = linea;
                        break; // No leemos más, ya tenemos la respuesta
                    }
                }

                System.out.println("Cliente #" + clienteId + " → " + operacion);
                System.out.println(" ↪ " + (respuestaUtil != null ? respuestaUtil : "Sin respuesta útil"));
                System.out.println("");
                Thread.sleep(500 + rand.nextInt(1000)); // delay aleatorio entre 0.5 y 1.5 segundos

            } catch (IOException | InterruptedException e) {
                System.err.println("Cliente #" + clienteId + ": Error -> " + e.getMessage());
            }
        }
    }
}
