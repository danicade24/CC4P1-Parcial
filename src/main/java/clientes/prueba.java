/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clientes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author daniela
 */
public class prueba {
    public static void main(String[] args) {
        String file = "src/main/java/data/clientes.txt" ;
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while((line = br.readLine()) != null) {
                String[] partes = line.split("\\|");
//                String[] partes = linea.split("\\s*\\|\\s*");  // Perfecto para tu caso
//                System.out.println(line);
                String idCliente = partes[0].trim(); 
                String nombreCliente = partes[1].trim(); 
                String emailCliente = partes[2].trim();
                String telfCliente = partes[3].trim();
                System.out.println("Id: " + idCliente + ", Nombre: " + nombreCliente +
                                   ", email: " + emailCliente + ", Celular: " + telfCliente);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
