package model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.*;
import java.util.*;
/**
 *
 * @author daniela
 */

public class ParticionadorDistribuido {

    static List<Integer> cargarNodos(String rutaJson) throws IOException {
        File file = new File(rutaJson);
        if (!file.exists() || file.length() == 0) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("{ \"nodos\": [1, 2, 3] }");
            }
            System.out.println("Archivo de nodos no encontrado. Se creó uno por defecto con [1, 2, 3]");
        }
        
        try (InputStream is = new FileInputStream(rutaJson)) {
            JSONObject obj = new JSONObject(new JSONTokener(is));
            JSONArray nodosArray = obj.getJSONArray("nodos");
            List<Integer> nodos = new ArrayList<>();
            for (int i = 0; i < nodosArray.length(); i++) {
                nodos.add(nodosArray.getInt(i));
            }
            return nodos;
        }
    }

    static void particionarYReplicar(String archivoClientes, String archivoCuentas, int numParticiones, String rutaJsonNodos) throws IOException {
        List<Integer> nodosDisponibles = cargarNodos(rutaJsonNodos);
        if (nodosDisponibles.size() < 3) throw new IllegalArgumentException("Se requieren al menos 3 nodos disponibles");

        Map<Integer, List<String>> particionesClientes = particionarArchivo(archivoClientes, numParticiones);
        Map<Integer, List<String>> particionesCuentas = particionarArchivo(archivoCuentas, numParticiones);

        generarArchivos(particionesClientes, "clientes", "parte1", nodosDisponibles);
        generarArchivos(particionesCuentas, "cuentas", "parte2", nodosDisponibles);
        
        generarConfigNodos("src/main/java/partitions", 6000);

    }

    static Map<Integer, List<String>> particionarArchivo(String ruta, int numParticiones) throws IOException {
        Map<Integer, List<String>> particiones = new HashMap<>();
        for (int i = 0; i < numParticiones; i++) {
            particiones.put(i, new ArrayList<>());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                int id = Integer.parseInt(partes[0].trim());
                int idx = id % numParticiones;
                particiones.get(idx).add(linea);
            }
        }
        return particiones;
    }

    static void generarArchivos(Map<Integer, List<String>> particiones, String carpeta, String prefijo, List<Integer> nodos) throws IOException {
        String rutaBase = "src/main/java/partitions/" + carpeta;

        Random random = new Random();
        for (Map.Entry<Integer, List<String>> entry : particiones.entrySet()) {
            int particionNum = entry.getKey() + 1;
            List<String> lineas = entry.getValue();

            // Elegir 3 nodos distintos aleatoriamente
            List<Integer> nodosElegidos = new ArrayList<>(nodos);
            Collections.shuffle(nodosElegidos, random);
            nodosElegidos = nodosElegidos.subList(0, 3);

            for (int nodo : nodosElegidos) {
                // Carpeta por nodo
                Path carpetaNodo = Paths.get(rutaBase, "nodo" + nodo);
                Files.createDirectories(carpetaNodo);

                // Mismo nombre de archivo para todas las réplicas
                String nombreArchivo = String.format("%s.%d.txt", prefijo, particionNum);
                Path path = carpetaNodo.resolve(nombreArchivo);

                try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                    for (String linea : lineas) {
                        bw.write(linea);
                        bw.newLine();
                    }
                }
            }
        }
    }
    
    static void generarConfigNodos(String rutaBase, int puertoBase) throws IOException {
        File clientesBase = new File(rutaBase + "/clientes");
        File cuentasBase = new File(rutaBase + "/cuentas");

        Map<Integer, JSONObject> nodosJson = new HashMap<>();

        for (File tipo : new File[]{clientesBase, cuentasBase}) {
            String tipoNombre = tipo.getName(); // "clientes" o "cuentas"
            for (File carpetaNodo : tipo.listFiles()) {
                if (!carpetaNodo.isDirectory()) continue;

                int nodoId = Integer.parseInt(carpetaNodo.getName().replace("nodo", ""));
                int puerto = puertoBase + nodoId;

                JSONObject nodo = nodosJson.computeIfAbsent(puerto, k -> {
                    JSONObject o = new JSONObject();
                    o.put("puerto", k);
                    o.put("particionesClientes", new JSONArray());
                    o.put("particionesCuentas", new JSONArray());
                    return o;
                });

                JSONArray destino = tipoNombre.equals("clientes") ?
                    nodo.getJSONArray("particionesClientes") :
                    nodo.getJSONArray("particionesCuentas");

                for (File archivo : carpetaNodo.listFiles()) {
                    String nombreSinExt = archivo.getName().replace(".txt", "");
                    if (!destino.toList().contains(nombreSinExt)) {
                        destino.put(nombreSinExt);
                    }
                }
            }
        }

        JSONArray jsonFinal = new JSONArray(nodosJson.values());
        try (PrintWriter pw = new PrintWriter("src/main/java/config/config_nodos.json")) {
            pw.println(jsonFinal.toString(4));
        }
    }


    public static void main(String[] args) {
        try {
            particionarYReplicar(
                "src/main/java/data/clientes.txt",
                "src/main/java/data/cuentas.txt",
                3,
                "src/main/java/config/config_nodos.json"
            );
            System.out.println("Particiones y réplicas generadas correctamente.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
