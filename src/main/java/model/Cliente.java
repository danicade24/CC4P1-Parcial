package model;

/**
 *
 * @author daniela
 */
public class Cliente {
    private String idCliente;
    private String nombre;
    private String email;
    private String telefono;

    public Cliente(String idCliente, String nombre, String email, String telefono) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    public String getIdCliente() { return idCliente; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }

    @Override
    public String toString() {
        return "Cliente {" +
                "id = '" + idCliente + '\'' + 
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }    
}
