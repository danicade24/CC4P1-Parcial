package nodos_trabajadores;

import model.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 *
 * @author daniela
 */
public class NodoTrabajador {
    private Map<String, Cuenta> cuentas;
    private List<Transaccion> transacciones;
    private String rutaCuentas;
    private String rutaTransacciones;

    public NodoTrabajador(String carpeta) {
        this.rutaCuentas = "src/main/java/data/cuentas.txt";
        this.rutaTransacciones = "src/main/java/data/transacciones.txt";

        this.cuentas = RepositorioDatos.cargarCuentas(rutaCuentas);
        this.transacciones = RepositorioDatos.cargarTransacciones(rutaTransacciones);
    }

    public String procesarSolicitud(String solicitud) {
        String[] partes = solicitud.split("\\s*\\|\\s*");

        switch (partes[0]) {
            case "CONSULTAR_SALDO":
                System.out.println("-----------OPERACIÓN : CONSULTAR SALDO----------");
                return consultarSaldo(partes[1]);

            case "TRANSFERIR_FONDOS":
                System.out.println("-----------OPERACIÓN : TRANSFERIR FONDOS----------");
                return transferir(partes[1], partes[2], Double.parseDouble(partes[3]));

            case "ARQUEO":
                System.out.println("-----------OPERACIÓN : ARQUEO----------");
                return calcularTotal();

            default:
                return "ERROR|Comando no reconocido";
        }
    }

    private String consultarSaldo(String idCuenta) {
        Cuenta c = cuentas.get(idCuenta);
        return (c != null) ? "SALDO |" + c.getSaldo() : "ERROR | Cuenta no encontrada";
    }

    private String transferir(String origen, String destino, double monto) {
        Cuenta cOrigen = cuentas.get(origen);
        Cuenta cDestino = cuentas.get(destino);

        if (cOrigen == null || cDestino == null)
            return "ERROR | Cuenta no encontrada";

        if (!cOrigen.retirar(monto))
            return "ERROR | Saldo insuficiente";

        cDestino.depositar(monto);

        String idTrans = String.valueOf(transacciones.size() + 1);
        String fechaHora = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        transacciones.add(new Transaccion(idTrans, origen, destino, monto, fechaHora, "Confirmada"));

        RepositorioDatos.guardarCuentas(rutaCuentas, cuentas);
        RepositorioDatos.guardarTransacciones(rutaTransacciones, transacciones);

        return "CONFIRMACION | Transferencia realizada";
    }

    private String saldoRestante(String origen) {
        Cuenta cOrigen = cuentas.get(origen);
        double sRestante = cOrigen.getSaldo();
        return "SALDO RESTANTE | " + sRestante;
    }
    
    private String calcularTotal() {
        double total = cuentas.values().stream().mapToDouble(Cuenta::getSaldo).sum();
        return "TOTAL |" + total;
    }
}

