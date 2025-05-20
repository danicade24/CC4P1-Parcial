package model;

/**
 *
 * @author daniela
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RepositorioDatos {
    
    public static Map<String, Cliente> cargarClientes(String ruta) {
        Map<String, Cliente> clientes = new HashMap<>();
        
        try(BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while((linea = br.readLine()) != null) {
                String[] p = linea.split("\\s*\\|\\s*");
                if(p.length == 4) {
                    Cliente c = new Cliente(
                            p[0].trim(), 
                            p[1].trim(), 
                            p[2].trim(), 
                            p[3].trim());
                    clientes.put(c.getIdCliente(), c);
                }
            }
        } catch (IOException e) {
            System.out.println("Error cargando clientes desde " + ruta + ": " + e.getMessage());
        }
        return clientes;
    }
    
    public static Map<String, Cuenta> cargarCuentas(String ruta) {
        Map<String, Cuenta> cuentas = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split("\\s*\\|\\s*");
                if (p.length == 4) {
                    Cuenta c = new Cuenta(
                        p[0].trim(), // ID_CUENTA
                        p[1].trim(), // ID_CLIENTE
                        Double.parseDouble(p[2].trim()), // SALDO
                        p[3].trim()  // TIPO
                    );
                    cuentas.put(c.getIdCuenta(), c);
                }
            }
        } catch (IOException e) {
            System.out.println("Error cargando cuentas desde " + ruta + ": " + e.getMessage());
        }

        return cuentas;
    }

    public static List<Transaccion> cargarTransacciones(String ruta) {
        List<Transaccion> transacciones = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split("\\s*\\|\\s*");
                if (p.length == 6) {
                    Transaccion t = new Transaccion(
                        p[0].trim(),  // ID_TRANSACC
                        p[1].trim(),  // ID_ORIG
                        p[2].trim(),  // ID_DEST
                        Double.parseDouble(p[3].trim()), // MONTO
                        p[4].trim(),  // FECHA_HORA
                        p[5].trim()   // ESTADO
                    );
                    transacciones.add(t);
                }
            }
        } catch (IOException e) {
            System.out.println("Error cargando transacciones desde " + ruta + ": " + e.getMessage());
        }

        return transacciones;
    }
    
    public static void guardarCuentas(String ruta, Map<String, Cuenta> cuentas) {
        try (PrintWriter pw = new PrintWriter(ruta)) {
            for (Cuenta c : cuentas.values()) {
                pw.println(c.getIdCuenta() + " | " +
                           c.getIdCliente() + " | " +
                           c.getSaldo() + " | " +
                           c.getTipo());
            }
        } catch (IOException e) {
            System.out.println("Error guardando cuentas en " + ruta + ": " + e.getMessage());
        }
    }
    
    public static void guardarTransacciones(String ruta, List<Transaccion> transacciones) {
        try (PrintWriter pw = new PrintWriter(ruta)) {
            for (Transaccion t : transacciones) {
                pw.println(t.getIdTransaccion() + " | " +
                           t.getIdOrigen() + " | " +
                           t.getIdDestino() + " | " +
                           t.getMonto() + " | " +
                           t.getFechaHora() + " | " +
                           t.getEstado());
            }
        } catch (IOException e) {
            System.out.println("Error guardando transacciones en " + ruta + ": " + e.getMessage());
        }
    }
}
