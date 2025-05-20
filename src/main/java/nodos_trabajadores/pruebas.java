package nodos_trabajadores;

/**
 *
 * @author daniela
 */

public class pruebas {
    public static void main(String[] args) {
        NodoTrabajador nodo1 = new NodoTrabajador("nodo1");

        System.out.println(nodo1.procesarSolicitud("CONSULTAR_SALDO|101"));
        System.out.println(nodo1.procesarSolicitud("TRANSFERIR_FONDOS|101|102|200.00"));
        System.out.println(nodo1.procesarSolicitud("ARQUEO"));

    }
}
