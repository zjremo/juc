package net.jrz.scheme;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Slf4j(topic = "c.ShareSchemeConnectionPool")
public class ShareSchemeConnectionPool {
    public static void main(String[] args) {
        ConnectionPool pool = new ConnectionPool(2);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                Connection conn = pool.borrow();
                try {
                    Thread.sleep(new Random().nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
                pool.free(conn);
            }).start();
        }
    }
}

// Connection
record Connection(int id, String name){

}

// Connection Pool
/*
* borrow : 拿一个连接
* free : 归还一个连接
* */
@Slf4j(topic = "c.ConnectionPool")
class ConnectionPool{
    private final Connection[] connections;
    private final AtomicIntegerArray atomicArr;
    private final int capacity;
    private final ReentrantLock lock;
    private final Condition fullCondition;

    public ConnectionPool(int capacity) {
        this.capacity = capacity;
        atomicArr = new AtomicIntegerArray(new int[capacity]);
        connections = IntStream.range(0, capacity).mapToObj(v -> new Connection(v, "Conn" + v)).toArray(Connection[]::new);

        lock = new ReentrantLock();
        fullCondition = lock.newCondition();
    }

    public Connection borrow(){
        while (true){
            for (int i = 0; i < capacity; ++i){
                if (atomicArr.compareAndSet(i, 0, 1)){
                    // 找到连接
                    log.debug("borrow connection {}", connections[i].id());
                    return connections[i];
                }
            }
            // 找了一圈都没找到，让它阻塞
            lock.lock();
            try {
                fullCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            } finally {
                lock.unlock();
            }
        }
    }

    public void free(Connection connection){
        for (int i = 0; i < capacity; ++i){
            if (connection == connections[i]){
                lock.lock();
                try {
                    if (atomicArr.compareAndSet(i, 1, 0)){
                        // 归还连接
                        log.debug("归还连接 id : {}", connection.id());
                        fullCondition.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
                break;
            }
        }
    }
}
