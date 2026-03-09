package net.jrz.d0;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.io.IOException;

@Slf4j
public class TestState {
    public static void main(String[] args) throws IOException {
        Thread t1 = new Thread("t1"){
            @Override
            public void run() {
                log.debug("running ... ");
            }
        };

        Thread t2 = new Thread("t2"){
            @Override
            public void run() {
                while (true){ // runnable

                }
            }
        };
        t2.start();

        Thread t3 = new Thread("t3"){
            @Override
            public void run() {
                log.debug("running ...");
            }
        };
        t3.start();

        Thread t4 = new Thread("t4"){
            @Override
            public void run() {
                synchronized (TestState.class){
                    Sleeper.sleep(1000);
                }
            }
        };
        t4.start();

        Thread t5 = new Thread("t5"){
            @Override
            public void run() {
                try {
                    t2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        };
        t5.start();

        Thread t6 = new Thread("t6"){
            @Override
            public void run() {
                synchronized (TestState.class){ // blocked
                    Sleeper.sleep(1000);
                }
            }
        };
        t6.start();

        Sleeper.sleep(0.5);

        log.debug("t1 state {}", t1.getState());
        log.debug("t2 state {}", t2.getState());
        log.debug("t3 state {}", t3.getState());
        log.debug("t4 state {}", t4.getState());
        log.debug("t5 state {}", t5.getState());
        log.debug("t6 state {}", t6.getState());
        System.in.read(); // 标准输入流，读取一个字节的数据
    }
}

/*
Result :
2026-03-07 21:55:38.815 [t3] DEBUG net.jrz.d0.TestState-running ...
2026-03-07 21:55:39.316 [main] DEBUG net.jrz.d0.TestState-t1 state NEW
2026-03-07 21:55:39.319 [main] DEBUG net.jrz.d0.TestState-t2 state RUNNABLE
2026-03-07 21:55:39.319 [main] DEBUG net.jrz.d0.TestState-t3 state TERMINATED
2026-03-07 21:55:39.319 [main] DEBUG net.jrz.d0.TestState-t4 state TIMED_WAITING
2026-03-07 21:55:39.319 [main] DEBUG net.jrz.d0.TestState-t5 state WAITING
2026-03-07 21:55:39.319 [main] DEBUG net.jrz.d0.TestState-t6 state BLOCKED
 */