package net.jrz.d10;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j(topic = "c.TestConcurrentHashMap")
public class TestConcurrentHashMap {
    static final String ALPHA = "abcedfghijklmnopqrstuvwxyz";

    static final String TEST_DIR = "/home/jrz/codes/juc/tmp/";

    public static void main(String[] args) {
        test2();
    }

    // 使用普通的map，存在问题
    public static void test(){
        // 1. 生成测试数据
//        generateData();

        // 2. 测试
        demo(
                () -> new HashMap<String, Integer>(),
                (map, words) -> {
                    for (String word : words){
                        map.put(word, map.getOrDefault(word, 0) + 1);
                    }
                }
        );
    }

    // 安全的两种做法
    public static void test2(){
        // Method 1: 引入原子变量
//        demo(
//                () -> new ConcurrentHashMap<String, AtomicInteger>(),
//                (map, words) -> {
//                    for (String word : words){
//                        map.computeIfAbsent(word, key -> new AtomicInteger()).getAndIncrement();
//                    }
//                }
//        );

        // Method 2: 函数式编程，无需原子变量
        demo(
                () -> new ConcurrentHashMap<String, Integer>(),
                (map, words) -> {
                    for (String word : words){
                        // 函数式编程，无需原子变量
                        map.merge(word, 1, Integer::sum);
                    }
                }
        );
    }

    // 生成测试数据
    public static void generateData() {
        int length = ALPHA.length();
        int cnt = 200;
        List<String> list = new ArrayList<>(length * cnt);

        for (int i = 0; i < length; ++i) {
            char ch = ALPHA.charAt(i);

            for (int j = 0; j < cnt; ++j) {
                list.add(String.valueOf(ch));
            }
        }

        Collections.shuffle(list);
        for (int i = 0; i < 26; ++i) {
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(TEST_DIR + (i + 1) + ".txt")))) {
                String collect = list.subList(i * cnt, (i + 1) * cnt).stream().collect(Collectors.joining("\n"));
                out.print(collect);
            } catch (FileNotFoundException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    // 从文件中读取数据
    public static List<String> readFromFile(int i) {
        List<String> words = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(TEST_DIR
                + i + ".txt")))) {
            while (true){
                String word = in.readLine();
                if (word == null)
                    break;
                words.add(word);
            }
            return words;
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }

    public static <V> void demo(Supplier<Map<String, V>> supplier, BiConsumer<Map<String, V>, List<String>> consumer){
        Map<String, V> counterMap = supplier.get();
        List<Thread> ts = new ArrayList<>();
        for (int i = 1; i <= 26; ++i){
            int idx = i;
            Thread t = new Thread(() -> {
                List<String> words = readFromFile(idx);
                consumer.accept(counterMap, words);
            });
            ts.add(t);
        }
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
                throw new RuntimeException(e);
            }
        });
        log.debug("counterMap: {}", counterMap);
    }
}




