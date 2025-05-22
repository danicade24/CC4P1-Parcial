package nodos_trabajadores;

public class LanzadorDeNodos {
    public static void main(String[] args) {
        int[] puertos = {6001, 6002, 6003}; 

        for (int i = 0; i < puertos.length; i++) {
            final int puerto = puertos[i];

            Thread hiloNodo = new Thread(new Runnable() {
                @Override
                public void run() {
                    NodoTrabajador nodo = new NodoTrabajador(puerto);
                    nodo.iniciar();
                }
            });

            hiloNodo.start();
        }

        System.out.println("Lanzados los nodos en puertos:");
        for (int i = 0; i < puertos.length; i++) {
            System.out.println("Nodo -> " + puertos[i]);
        }
    }
}
