package net.jrz.d1;

import java.util.ArrayList;

public class ThreadSafe {
    static final int THREAD_NUM = 2;
    static final int LOOP_NUM = 200;

    public void method1(int loopNumber){
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < loopNumber; ++i){
            method2(list);
            method3(list);
        }
    }

    public void method2(ArrayList<String> list){
        list.add("1");
    }

    public void method3(ArrayList<String> list){
        list.removeFirst();
    }

    public static void main(String[] args) {
        ThreadSafe threadSafe = new ThreadSafe();
        for (int i = 0; i < THREAD_NUM; ++i){
            new Thread(() -> {
                threadSafe.method1(LOOP_NUM);
            }, "Thread" + i).start();
        }
    }
}

// 这种情况会出问题，因为它新开了一个线程，原来的局部变量list跨范围作用变成了共享变量
// 要防止这种问题，可以将method3设置为private，不允许重写。这也侧面说明了private的提供的安全意义所在
class ThreadSafeSubClass extends ThreadSafe{
    @Override
    public void method3(ArrayList<String> list) {
        new Thread(list::removeFirst).start();
    }
}