package net.jrz.d4;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

// 交替输出 abcabcabc 三个线程
// park和unpark方法实现
@Slf4j(topic = "c.TestAlternateOutput3")
public class TestAlternateOutput3 {
    public static void main(String[] args) {
        int loopNumber = 3;
        SyncPark syncPark = new SyncPark(loopNumber);

        int thread_num = 3;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < thread_num; ++i) {
            int tmp_i = i;
            threads.add(new Thread(() -> {
                syncPark.print("" + (char) ('a' + tmp_i), (tmp_i + 1) % thread_num);
            }));
        }
        syncPark.setThreads(threads.toArray(new Thread[0]));
        syncPark.start();
    }
}

class SyncPark {
    private Thread[] threads;
    private final int loopNumber;

    public SyncPark(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    public void setThreads(Thread... threads) {
        this.threads = threads;
    }

    public void print(String str, int next) {
        for (int i = 0; i < loopNumber; ++i) {
            LockSupport.park();
            System.out.print(str);
            LockSupport.unpark(threads[next]);
        }
    }

    public void start(){
        for (Thread thread: threads){
            thread.start();
        }
        LockSupport.unpark(threads[0]);
    }
}
