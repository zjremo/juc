package net.jrz.d0.method;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

@Slf4j
public class StartAndRun {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1"){
            @Override
            public void run() {
               log.debug("running...");
                IntStream.range(1, 10).forEach(v -> {
                    System.out.print(v + " ");
                });
                System.out.println();
            }
        };

        log.debug("t1 state is {}", t1.getState());
        t1.start();
        log.debug("t1 state is {}", t1.getState());
        log.debug("do other things ... ");
    }
}
