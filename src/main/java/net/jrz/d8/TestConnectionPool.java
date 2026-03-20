package net.jrz.d8;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

@Slf4j(topic = "c.TestConnectionPool")
public class TestConnectionPool {
    private final int poolSize;
    private Connection[] connections;
    private AtomicIntegerArray flags;

    public TestConnectionPool(int poolSize) {
        this.poolSize = poolSize;
        this.connections = new Connection[poolSize];
        this.flags = new AtomicIntegerArray(new int[poolSize]);
        for (int i = 0; i < poolSize; ++i){
            connections[i] = new ConnectionImpl("Connection " + i);
        }
    }

    // 获取连接
    public Connection borrow(){
        while (true){
            for (int i = 0; i < poolSize; ++i){
                int prev = flags.get(i); // 首先获取当前读
                if (prev == 0){ // 为0继续看
                    if (flags.compareAndSet(i, prev, 1)){ // 乐观锁CAS
                        log.debug("borrow {}", connections[i]);
                        return connections[i];
                    }
                }
            }
            // 尝试一圈没有，说明已经被借完了
            synchronized (this){
                log.debug("wait ...");
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    // 归还连接
    public void free(Connection conn){
        for (int i = 0; i < poolSize; ++i){
            if (connections[i] == conn) {
                flags.set(i, 0);
                synchronized (this){
                    log.debug("free {}", conn);
                    this.notifyAll(); // 唤醒wating线程
                }
                break;
            }
        }
    }

    public static void main(String[] args) {
        /* This is th test for Connection Pool */
        TestConnectionPool connectionPool = new TestConnectionPool(5);
        List<Thread> ts = new ArrayList<>(10);

        IntStream.range(0, 10).forEach( v -> {
            ts.add(
                    new Thread(() -> {
                        Connection conn = connectionPool.borrow();
                        Sleeper.sleep((int) (Math.random() * 10) + 1); // 休眠1到10秒
                        connectionPool.free(conn);
                    }, v + "")
            );
        });

        ts.forEach(Thread::start);

        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        });

        log.debug("Test is over");
    }
}

interface Connection {

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ConnectionImpl implements Connection{
    String name;
}