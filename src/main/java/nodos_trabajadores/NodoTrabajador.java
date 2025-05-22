package nodos_trabajadores;

import model.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
/**
 *
 * @author daniela
 */
public class NodoTrabajador {
    private final int puerto;
    private final Map<String, Cuenta> cuentas = new HashMap<>();
    private final Map<String, Cliente> clientes = new HashMap<>();
    private final String rutaTransacciones = "src/main/java/data/transacciones.txt";

    public NodoTrabajador(int puerto) {
        this.puerto = puerto;
        cargarArchivosDesdeParticiones("clientes");
        cargarArchivosDesdeParticiones("cuentas");
    }

    private void cargarArchivosDesdeParticiones(String tipoArchivo) {
        int num = puerto - 6000;
        String carpeta = "src/main/java/partitions/" + tipoArchivo + "/nodo" + num + "/";
        File dir = new File(carpeta);

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("No existe la carpeta " + carpeta);
            return;
        }

        File[] archivos = dir.listFiles((d, nombre) -> nombre.endsWith(".txt"));
        if (archivos == null) return;

        for (File archivo : archivos) {
            System.out.println("Leyendo archivo: " + archivo.getName());
            if (tipoArchivo.equals("cuentas")) {
                Map<String, Cuenta> parciales = RepositorioDatos.cargarCuentas(archivo.getPath());
                cuentas.putAll(parciales);
                System.out.println("Total cuentas cargadas: " + cuentas.size());
            } else if (tipoArchivo.equals("clientes")) {
                Map<String, Cliente> parciales = RepositorioDatos.cargarClientes(archivo.getPath());
                clientes.putAll(parciales);
                System.out.println("Total clientes cargados: " + clientes.size());
            }
        }
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Nodo trabajador iniciado en el puerto " + puerto);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + socket.getInetAddress().getHostAddress());
                new Thread(() -> manejarConexion(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el nodo: " + e.getMessage());
        }
    }

    private void manejarConexion(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String mensaje = in.readLine();
            if (mensaje == null || mensaje.trim().isEmpty()) {
                out.println("ERROR|Solicitud vacía");
                return;
            }

            if (mensaje.startsWith("CONSULTAR_SALDO|")) {
                String[] partes = mensaje.split("\\|");
                if (partes.length != 2) {
                    out.println("ERROR|Formato incorrecto. Use: CONSULTAR_SALDO|ID");
                    return;
                }

                String id = partes[1].trim();
                Cuenta c = cuentas.get(id);
                if (c != null) {
                    out.println("SALDO|" + c.getSaldo());
                } else {
                    out.println("ERROR|Cuenta no encontrada");
                }

            } else if (mensaje.startsWith("TRANSFERIR_FONDOS|")) {
                String[] partes = mensaje.split("\\|");
                if (partes.length != 4) {
                    out.println("ERROR|Formato incorrecto. Use: TRANSFERIR_FONDOS|ID1|ID2|MONTO");
                    return;
                }

                String idOrigen = partes[1].trim();
                String idDestino = partes[2].trim();
                double monto;
                try {
                    monto = Double.parseDouble(partes[3].trim());
                } catch (NumberFormatException e) {
                    out.println("ERROR|Monto inválido");
                    return;
                }

                Cuenta origen = cuentas.get(idOrigen);
                Cuenta destino = cuentas.get(idDestino);

                if (origen == null) {
                    out.println("ERROR|Cuenta origen no encontrada");
                    return;
                }
                if (destino == null) {
                    out.println("ERROR|Cuenta destino no encontrada");
                    return;
                }

                synchronized (cuentas) {
                    if (!origen.retirar(monto)) {
                        out.println("ERROR|Saldo insuficiente");
                        return;
                    }
                    destino.depositar(monto);
                }

                // Guardar cuentas modificadas (copia local por nodo)
                //RepositorioDatos.guardarCuentas("src/main/java/data/cuentas_actualizadas_" + puerto + ".txt", cuentas);
                guardarCuentasPorArchivos("src/main/java/partitions/cuentas/nodo" + (puerto - 6000));

                // Registrar transacción
                registrarTransaccion(idOrigen, idDestino, monto);

                out.println("CONFIRMACION|Transferencia realizada de " + idOrigen + " a " + idDestino + " por " + monto);

            } else {
                out.println("ERROR|Operación no reconocida");
            }

        } catch (IOException e) {
            System.err.println("Error al manejar conexión: " + e.getMessage());
        }
    }

    private void registrarTransaccion(String idOrigen, String idDestino, double monto) {
        File archivo = new File(rutaTransacciones);
        int idTransaccion = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            while (br.readLine() != null) {
                idTransaccion++;
            }
        } catch (IOException e) {
            // Si no existe, se creará automáticamente
        }

        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String linea = idTransaccion + " | " + idOrigen + " | " + idDestino + " | " + monto + " | " + fechaHora + " | Confirmada";

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, true))) {
            pw.println(linea);
        } catch (IOException e) {
            System.err.println("Error al registrar transacción: " + e.getMessage());
        }
    }

    private void guardarCuentasPorArchivos(String carpetaNodo) {
        File dir = new File(carpetaNodo);
        File[] archivos = dir.listFiles((d, nombre) -> nombre.endsWith(".txt"));
        if (archivos == null) return;
    
        for (File archivo : archivos) {
            Map<String, Cuenta> cuentasParaEsteArchivo = new HashMap<>();
            List<String> ids = extraerIdsDesdeArchivo(archivo.getPath());
            for (String id : ids) {
                if (cuentas.containsKey(id)) {
                    cuentasParaEsteArchivo.put(id, cuentas.get(id));
                }
            }
            RepositorioDatos.guardarCuentas(archivo.getPath(), cuentasParaEsteArchivo);
        }
    }
    
    private List<String> extraerIdsDesdeArchivo(String ruta) {
        List<String> ids = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length >= 1) {
                    ids.add(partes[0].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo IDs desde archivo: " + e.getMessage());
        }
        return ids;
    }
    
}