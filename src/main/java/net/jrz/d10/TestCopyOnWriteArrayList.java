package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;
import net.jrz.util.Sleeper;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j(topic = "c.TestCopyOnWriteArrayList")
public class TestCopyOnWriteArrayList {
    public static void main(String[] args) {
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        Iterator<Integer> iter = list.iterator();
        new Thread(() -> {
            list.removeFirst();
            log.debug("list is {}", list);
        }).start();
        Sleeper.sleep(1);
        while (iter.hasNext()) {
            System.out.print(iter.next() + " ");
        }
    }
}

// 结果: 出现一致性问题，迭代器存在弱一致性问题
// 并发高和一致性是矛盾的，需要权衡
/*
*   2026-03-24 19:12:22.546 [Thread-0] DEBUG c.TestCopyOnWriteArrayList- list is [2, 3]
    1 2 3
* */
