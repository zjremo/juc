package net.jrz.d6;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

@Slf4j(topic = "c.TestAtomic")
public class TestAtomic {
    public static void main(String[] args) {
        AtomicInteger i = new AtomicInteger(5);

        System.out.println(i.getAndIncrement()); // i++ 5
        System.out.println(i.incrementAndGet()); // ++i 7

        System.out.println(i.getAndAdd(5)); // 7
        System.out.println(i.addAndGet(5)); // 12
        i.updateAndGet(v -> v * 10);
        System.out.println(i.get());
    }

    public static int updateAndGet(AtomicInteger i, IntUnaryOperator operator){
        while (true){
            int prev = i.get();
            int next = operator.applyAsInt(prev);
            if (i.compareAndSet(prev, next))
                return next;
        }
    }
}
