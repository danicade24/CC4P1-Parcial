package nodos_trabajadores;

import model.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.io.*;

/**
 *
 * @author daniela
 */
public class NodoTrabajador {
    private final int puerto;  
    private final String rutaCuentas;
    private final String rutaTransacciones;
    
    private Map<String, Cuenta> cuentas;
    private List<Transaccion> transacciones;

    public NodoTrabajador(int puerto, String archivoCuentas, String archivoTransacciones) {
        this.puerto = puerto;
        this.rutaCuentas = archivoCuentas;
        this.rutaTransacciones = archivoTransacciones;
        
        this.cuentas = RepositorioDatos.cargarCuentas(rutaCuentas);
        this.transacciones = RepositorioDatos.cargarTransacciones(rutaTransacciones);
    }
        
    public void iniciarNodo() {
        System.out.println("Nodo iniciado en el puerto " + puerto);
        System.out.println("Usando el archivov " + rutaCuentas);
        
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String solicitud = in.readLine();
                System.out.println("Solicitud recibida: " + solicitud);

                String respuesta = procesarSolicitud(solicitud);
                out.println(respuesta);

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String procesarSolicitud(String solicitud) {
        if (solicitud.startsWith("CONSULTAR_SALDO")) {
            String[] partes = solicitud.split("\\|");
            return consultarSaldo(partes[1].trim());
        } else if (solicitud.startsWith("TRANSFERIR_FONDOS")) {
            String[] partes = solicitud.split("\\|");
            return transferir(partes[1].trim(), partes[2].trim(), Double.parseDouble(partes[3].trim()));
        }
        return "ERROR|Operación no reconocida";
    }

    private String consultarSaldo(String idCuentaBuscada) {
        Cuenta c = cuentas.get(idCuentaBuscada);
        return (c != null) ? "SALDO | " + c.getSaldo() : "ERROR | CUENTA NO ENCONTRADA";
    }
    
    private String transferir(String origen, String destino, double monto) {
        Cuenta cOrigen = cuentas.get(origen);
        Cuenta cDestino = cuentas.get(destino);

        if (cOrigen == null)
            return "ERROR|Cuenta origen no encontrada";

        if (cDestino == null)
            return "ERROR|Cuenta destino no encontrada";

        if (!cOrigen.retirar(monto))
            return "ERROR|Saldo insuficiente";

        cDestino.depositar(monto);

        String idTrans = String.valueOf(transacciones.size() + 1);
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        transacciones.add(new Transaccion(idTrans, origen, destino, monto, fechaHora, "Confirmada"));

        // Guardar los cambios
        RepositorioDatos.guardarCuentas(rutaCuentas, cuentas);
        RepositorioDatos.guardarTransacciones(rutaTransacciones, transacciones);

        return "CONFIRMACION | Transferencia realizada de " + origen + " a " + destino + " por " + monto;
    }
    
    public static void main(String[] args) {
        int puerto = 6001;
        String archivoCuentas = "src/main/java/partitions/cuentas/parte2.1_r1.txt";
        String archivoTransacciones = "src/main/java/data/transacciones.txt";

        NodoTrabajador nodo = new NodoTrabajador(puerto, archivoCuentas, archivoTransacciones);
        nodo.iniciarNodo();
    }

}