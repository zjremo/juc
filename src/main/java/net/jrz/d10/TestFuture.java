package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "c.TestFuture")
public class TestFuture {
    public static void test(){
        AtomicInteger id = new AtomicInteger(1);
        ExecutorService service = Executors.newFixedThreadPool(3, (r) -> new Thread(r, "" + id.getAndIncrement()));

        Future<Map<String, String>> f1 = service.submit(() -> {
            Map<String, String> map = new HashMap<>();
            map.put("1", "hello");
            map.put("2", "world");

            return map;
        });

        Future<Map<String, String>> f2 = service.submit(() -> {
            Map<String, String> map = new HashMap<>();
            map.put("1", "hi");
            map.put("2", "Xiao Hong");

            return map;
        });

        Future<Map<String, String>> f3 = service.submit(() -> {
            Map<String, String> map = new HashMap<>();
            map.put("1", "my");
            map.put("2", "batis");

            return map;
        });

        try {
            log.debug("{}", f1.get());
            log.debug("{}", f2.get());
            log.debug("{}", f3.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }

        service.shutdown();
    }

    public static void main(String[] args) {
        test();
    }
}
