import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Buffer {
    private Queue<Integer> buffer;
    private int capacity;
    private Lock lock;
    private Condition notFull;
    private Condition notEmpty;

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
    }

    public void produce(int item, int producerId) throws InterruptedException {
        lock.lock();
        try {
            while (buffer.size() == capacity) {
                System.out.println("Productor " + producerId + " está esperando. buffer lleno");
                notFull.await();
            }
            buffer.add(item);
            System.out.println("Productor " + producerId + " produce: " + item);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public int consume(int consumerId) throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty()) {
                System.out.println("Consumidor " + consumerId + " está esperando. no hay nada para consumir en el buffer");
                notEmpty.await();
            }
            int item = buffer.poll();
            System.out.println("Consumidor " + consumerId + " consume: " + item);
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }
}

class Producer implements Runnable {
    private Buffer buffer;
    private int id;

    public Producer(Buffer buffer, int id) {
        this.buffer = buffer;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                int item = (int) (Math.random() * 100);
                buffer.produce(item, id);
                Thread.sleep((long) (Math.random() * 1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer implements Runnable {
    private Buffer buffer;
    private int id;

    public Consumer(Buffer buffer, int id) {
        this.buffer = buffer;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                int item = buffer.consume(id);
                Thread.sleep((long) (Math.random() * 1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ProducerConsumer {
    public static void main(String[] args) {
        Buffer buffer = new Buffer(5);

        Thread[] producers = new Thread[3];
        Thread[] consumers = new Thread[3];

        for (int i = 0; i < 3; i++) {
            producers[i] = new Thread(new Producer(buffer, i));
            consumers[i] = new Thread(new Consumer(buffer, i));
            producers[i].start();
            consumers[i].start();
        }
    }
}