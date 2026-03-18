package net.jrz.d6;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.atomic.AtomicStampedReference;

@Slf4j(topic = "c.TestAtomicStampedRef")
public class TestAtomicStampedRef {
    static AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 1);

    public static void main(String[] args) {
        log.debug("main thread start ...");
        // get (A,1)
        String prevRef = ref.getReference();
        int prevStamp = ref.getStamp();
        other();
        Sleeper.sleep(1);
//        DEBUG c.TestAtomicStampedRef- change A->C false
        log.debug("change A->C {}", ref.compareAndSet(prevRef, "C", prevStamp, 2));
    }

    public static void other(){
        new Thread(() -> {
            log.debug("change A->B {}", ref.compareAndSet(ref.getReference(), "B", ref.getStamp(), 2));
        }, "t1").start();

        new Thread(() -> {
            log.debug("change B->C {}", ref.compareAndSet(ref.getReference(), "C", ref.getStamp(), 3));
        }, "t2").start();
    }
}
