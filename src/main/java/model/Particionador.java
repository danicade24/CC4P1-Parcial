package model;

import java.io.*;
import java.nio.file.*;

/**
 *
 * @author daniela
 */
public class Particionador {
    
    public static void crearCarpetaSiNoExiste(String ruta) {
        File carpeta = new File(ruta);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }
    
    public static void particionarClientes(String archivoOriginal, int numParticiones) throws IOException {
        String ruta = "src/main/java/partitions/clientes";
        crearCarpetaSiNoExiste(ruta);
        BufferedReader br = new BufferedReader(new FileReader(archivoOriginal));
        BufferedWriter[] writers = new BufferedWriter[numParticiones];
        for(int i = 0; i < numParticiones; i++) {
            writers[i] = new BufferedWriter(new FileWriter(ruta + "/parte1."+ (i+1) + ".txt"));
        }
        
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split("\\|");
            int id = Integer.parseInt(partes[0].trim());
            int index = id % numParticiones;
            writers[index].write(linea + "\n");
        }
        
        for (BufferedWriter bw : writers) bw.close();
        br.close();
    }
    
    public static void particionarCuentas(String archivoOriginal, int numParticiones) throws IOException {
        String ruta = "src/main/java/partitions/cuentas";
        crearCarpetaSiNoExiste(ruta);
        BufferedReader br = new BufferedReader(new FileReader(archivoOriginal));
        BufferedWriter[] writers = new BufferedWriter[numParticiones];
        for(int i = 0; i < numParticiones; i++) {
            writers[i] = new BufferedWriter(new FileWriter(ruta + "/parte2."+ (i+1) + ".txt"));
        }
        
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split("\\|");
            int id = Integer.parseInt(partes[0].trim());
            int index = id % numParticiones;
            writers[index].write(linea + "\n");
        }
        
        for (BufferedWriter bw : writers) bw.close();
        br.close();
    }
    
    public static void replicar(String carpeta, String nombreBase) throws IOException {
        for (int r = 1; r < 3; r++) {
            Path original = Paths.get("src/main/java/partitions/" + carpeta + "/" + nombreBase + ".txt");
            Path destino = Paths.get("src/main/java/partitions/" + carpeta + "/" + nombreBase + "_r" + r + ".txt");
            Files.copy(original, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    public static void particionarYReplicarTodo(int numParticiones) throws IOException {
        particionarClientes("src/main/java/data/clientes.txt", numParticiones);
        particionarCuentas("src/main/java/data/cuentas.txt", numParticiones);

        for (int i = 1; i <= numParticiones; i++) {
            replicar("clientes", "parte1." + i);
            replicar("cuentas", "parte2." + i);
        }
    }

    public static void main(String[] args) {
        try {
            particionarYReplicarTodo(5);  // O 4, 5, lo que quieras
            System.out.println("Particiones y réplicas generadas correctamente.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
