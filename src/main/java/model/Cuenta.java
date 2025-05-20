package model;

/**
 *
 * @author daniela
 */
public class Cuenta {
    private String idCuenta;
    private String idCliente;
    private double saldo;
    private String tipo;

    public Cuenta(String idCuenta, String idCliente, double saldo, String tipo) {
        this.idCuenta = idCuenta;
        this.idCliente = idCliente;
        this.saldo = saldo;
        this.tipo = tipo;
    }

    public String getIdCuenta() { return idCuenta; }
    public String getIdCliente() { return idCliente; }
    public double getSaldo() { return saldo; }
    public String getTipo() { return tipo; }

    public void depositar(double monto) {
        this.saldo += monto;
    }

    public boolean retirar(double monto) {
        if (this.saldo >= monto) {
            this.saldo -= monto;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Cuenta{" +
                "idCuenta='" + idCuenta + '\'' +
                ", idCliente='" + idCliente + '\'' +
                ", saldo=" + saldo +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}
