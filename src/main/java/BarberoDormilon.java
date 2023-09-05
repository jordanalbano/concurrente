import java.util.concurrent.locks.*;

class Barberia {
    private int numeroDeSillas;
    private int numeroDeClientes;
    private Lock lock;
    private Condition condicionBarbero;
    private Condition condicionCliente;

    public Barberia(int numChairs) {
        this.numeroDeSillas = numChairs;
        this.numeroDeClientes = 0;
        this.lock = new ReentrantLock();
        this.condicionBarbero = lock.newCondition();
        this.condicionCliente = lock.newCondition();
    }

    public void entrarABarberia(int nroCliente)
            throws InterruptedException {
        lock.lock();
        try {
            if (numeroDeClientes < numeroDeSillas) {
                numeroDeClientes++;
                System.out.println("Cliente " +nroCliente+
                        " entra a la barbería.");
                condicionCliente.signal();
            } else {
                System.out.println("Cliente " +nroCliente+
                        " no encuentra silla y se va.");
            }
        } finally {
            lock.unlock();
        }
    }

    public void pedirCorteDePelo(int nroCliente)
            throws InterruptedException {
        lock.lock();
        try {
            while (numeroDeClientes == 0) {
                System.out.println("Barbero está durmiendo. " +
                        "Cliente " + nroCliente + " lo despierta.");
                condicionBarbero.await();
            }
            numeroDeClientes--;
            System.out.println("Cliente " + nroCliente +
                    " está recibiendo un corte de pelo.");
            condicionCliente.signal();
        } finally {
            lock.unlock();
            System.out.println("El cliente " + nroCliente
                    + " se fue");
        }
    }

    public void cortarPelo() throws InterruptedException {
        lock.lock();
        try {
            while (numeroDeClientes == 0) {
                System.out.println("Barbero está durmiendo. No hay clientes.");
                condicionBarbero.await();
            }
            System.out.println("Barbero está cortando el pelo de un cliente.");
            condicionCliente.signal();
        } finally {
            lock.unlock();
            System.out.println("No hay mas clientes");
        }
    }
}

class Barbero implements Runnable {
    private Barberia barberia;
    public Barbero(Barberia shop) {
        this.barberia = shop;
    }
    @Override
    public void run() {
        try {
            while (true) {
                barberia.cortarPelo();
                Thread.sleep((long) (Math.random() * 500));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Cliente implements Runnable {
    private Barberia shop;
    private int id;
    public Cliente(Barberia shop, int id) {
        this.shop = shop;
        this.id = id;
    }
    @Override
    public void run() {
        try {
            shop.entrarABarberia(id);
            shop.pedirCorteDePelo(id);
            Thread.sleep((long) (Math.random() * 8000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class BarberoDormilon {
    public static void main(String[] args) {
        int numeroDeSillas = 5;
        Barberia barberia = new Barberia(numeroDeSillas);
        Thread hiloDeBarbero = new Thread(new Barbero(barberia));
        hiloDeBarbero.start();
        int numeroClientes = 15;
        for (int i = 1; i <= numeroClientes; i++) {
            Thread hiloCliente = new Thread(new Cliente(barberia, i));
            hiloCliente.start();
        }
    }
}
