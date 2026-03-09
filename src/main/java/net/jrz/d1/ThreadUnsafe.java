package net.jrz.d1;

import java.util.ArrayList;

public class ThreadUnsafe {
    private final ArrayList<String> list = new ArrayList<>();
    static final int THREAD_NUM = 2;
    static final int LOOP_NUM = 200;

    public void method1(int loopNumber){
        for (int i = 0; i < loopNumber; ++i){
            method2();
            method3();
        }
    }

    public void method2(){
        list.add("1");
    }

    public void method3(){
        list.removeFirst();
    }

    public static void main(String[] args) {
        ThreadUnsafe threadUnsafe = new ThreadUnsafe();
        for (int i = 0; i < THREAD_NUM; ++i){
            new Thread(() -> {
                threadUnsafe.method1(LOOP_NUM);
            }, "Thread" + i).start();
        }
    }
}
