package model;

/**
 *
 * @author daniela
 */
public class Transaccion {
    private String idTransaccion;
    private String idOrigen;
    private String idDestino;
    private double monto;
    private String fechaHora;
    private String estado;

    public Transaccion(String idTransaccion, String idOrigen, String idDestino,
                       double monto, String fechaHora, String estado) {
        this.idTransaccion = idTransaccion;
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.monto = monto;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    public String getIdTransaccion() { return idTransaccion; }
    public String getIdOrigen() { return idOrigen; }
    public String getIdDestino() { return idDestino; }
    public double getMonto() { return monto; }
    public String getFechaHora() { return fechaHora; }
    public String getEstado() { return estado; }

    @Override
    public String toString() {
        return "Transaccion{" +
                "id=" + idTransaccion +
                ", origen=" + idOrigen +
                ", destino=" + idDestino +
                ", monto=" + monto +
                ", fechaHora='" + fechaHora + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
