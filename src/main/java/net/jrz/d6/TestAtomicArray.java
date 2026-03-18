package net.jrz.d6;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j(topic = "c.TestAtomicArray")
public class TestAtomicArray {
    public static void main(String[] args) {
        // 使用普通数组，不安全
        demo(
                () -> new int[10],
                (array) -> array.length,
                (array, index) -> ++array[index],
                (array) -> System.out.println(Arrays.toString(array))
        );

        // 使用原子数组，安全
//        demo(
//                () -> new AtomicIntegerArray(10),
//                AtomicIntegerArray::length,
//                AtomicIntegerArray::getAndIncrement,
//                System.out::println
//        );
    }

    /*
     * Supplier 创建数组方法
     * Function 获取长度
     * BiConsumer 对数组指定位置进行操作
     * Consumer 打印
     * */
    // T 必须要是个对象类型，所以这里传进来int[] 和 AtomicIntegerArray是可以保证通用性的
    // 如果这里弄成<T[]>具有巨大的缺陷性，无法很好的兼容AtomicIntegerArray
    public static <T> void demo(
            Supplier<T> arraySupplier,
            Function<T, Integer> lengthFun,
            BiConsumer<T, Integer> putConsumer,
            Consumer<T> printConsumer) {
        List<Thread> ts = new ArrayList<>();
        T arr = arraySupplier.get();
        int len = lengthFun.apply(arr);
        for (int i = 0; i < len; ++i) {
            ts.add(new Thread(() -> {
                for (int j = 0; j < 10000; ++j) {
                    putConsumer.accept(arr, j % len);
                }
            }));
        }
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        });
        printConsumer.accept(arr);
    }

}
