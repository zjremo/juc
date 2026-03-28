package net.jrz.d6;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.concurrent.atomic.AtomicMarkableReference;

@Slf4j(topic = "c.TestAtomicMarkableRef")
public class TestAtomicMarkableRef {
    public static void main(String[] args) {
        GarbageBag bag = new GarbageBag("装满垃圾");
        // mark是一个标记，表示垃圾袋满了
        AtomicMarkableReference<GarbageBag> ref = new AtomicMarkableReference<>(bag, true);

        log.debug("main thread start...");
        GarbageBag prev = ref.getReference();
        log.debug(prev.toString());

        new Thread(() -> {
            log.debug("打扫卫生的线程 start...");
            bag.setDesc("空垃圾袋");
            while (!ref.compareAndSet(bag, bag, true, false)){}
            log.debug(bag.toString());
        }).start();

        Sleeper.sleep(1);
        log.debug("主线程想换一个新垃圾袋?");
        boolean success = ref.compareAndSet(prev, new GarbageBag("空垃圾袋"), true, false);
        log.debug("换了吗? {}", success);

        log.debug(ref.getReference().toString());
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GarbageBag{
    String desc;
}