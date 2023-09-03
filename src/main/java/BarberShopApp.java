import java.util.concurrent.locks.*;

class BarberShop {
    private int numChairs;
    private int numCustomers;
    private Lock lock;
    private Condition barberCondition;
    private Condition customerCondition;

    public BarberShop(int numChairs) {
        this.numChairs = numChairs;
        this.numCustomers = 0;
        this.lock = new ReentrantLock();
        this.barberCondition = lock.newCondition();
        this.customerCondition = lock.newCondition();
    }

    public void enterShop(int customerId) throws InterruptedException {
        lock.lock();
        try {
            if (numCustomers < numChairs) {
                numCustomers++;
                System.out.println("Cliente " + customerId + " entra a la barbería.");
                customerCondition.signal(); // Despierta al barbero si está durmiendo.
            } else {
                System.out.println("Cliente " + customerId + " no encuentra silla y se va.");
            }
        } finally {
            lock.unlock();
        }
    }

    public void getHaircut(int customerId) throws InterruptedException {
        lock.lock();
        try {
            while (numCustomers == 0) {
                System.out.println("Barbero está durmiendo. Cliente " + customerId + " lo despierta.");
                barberCondition.await();
            }
            numCustomers--;
            System.out.println("Cliente " + customerId + " está recibiendo un corte de pelo.");
            customerCondition.signal(); // Despierta a otro cliente si hay más esperando.
        } finally {
            lock.unlock();
        }
    }

    public void cutHair() throws InterruptedException {
        lock.lock();
        try {
            while (numCustomers == 0) {
                System.out.println("Barbero está durmiendo. No hay clientes.");
                barberCondition.await();
            }
            System.out.println("Barbero está cortando el pelo de un cliente.");
            customerCondition.signal(); // Despierta a otro cliente si hay más esperando.
        } finally {
            lock.unlock();
        }
    }
}

class Barber implements Runnable {
    private BarberShop shop;

    public Barber(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            while (true) {
                shop.cutHair();
                Thread.sleep((long) (Math.random() * 2000)); // Simula el tiempo que lleva cortar el pelo.
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Customer implements Runnable {
    private BarberShop shop;
    private int id;

    public Customer(BarberShop shop, int id) {
        this.shop = shop;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            shop.enterShop(id);
            shop.getHaircut(id);
            Thread.sleep((long) (Math.random() * 2000)); // Simula el tiempo que lleva el servicio al cliente.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class BarberShopApp {
    public static void main(String[] args) {
        int numChairs = 5;
        BarberShop shop = new BarberShop(numChairs);
        Thread barberThread = new Thread(new Barber(shop));
        barberThread.start();
        int numCustomers = 300
                ;
        for (int i = 1; i <= numCustomers; i++) {
            Thread customerThread = new Thread(new Customer(shop, i));
            customerThread.start();
        }
    }
}
