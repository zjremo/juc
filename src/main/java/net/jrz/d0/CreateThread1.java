package net.jrz.d0;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "test CreateThread1")
public class CreateThread1 {
    public static void main(String[] args) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Current running thread is {}", Thread.currentThread().getName());
            }
        });
        t1.start();
    }
}
