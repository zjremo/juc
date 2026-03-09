package net.jrz.d1;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.TestSynchronized1")
public class TestSynchronized1 {
    // 这个是利用面向对象的思想来对TestSynchronized.java进行修改
    public static void main(String[] args) throws InterruptedException {
        Room room = new Room();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; ++i)
                room.increment();
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; ++i)
                room.decrease();
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("{}", room.getCounter());
    }
}

// 封装互斥等逻辑到对象内部
class Room{
    private int counter = 0;

//    public void increment(){
//        synchronized (this){
//            ++counter;
//        }
//    }

    // synchronized锁在方法上其实就是锁住方法所在的对象
    public synchronized void increment(){
        ++counter;
    }

//    public void decrease(){
//        synchronized (this){
//            --counter;
//        }
//    }

    public synchronized void decrease(){
        --counter;
    }

    public synchronized int getCounter() {
        return counter;
    }
}