package net.jrz.d0;

public class ThreadDemo {
    public static void main(String[] args) {
        Thread t1 = new Thread() {
            @Override
            public void run() {
               method1(20);
            }
        };
        t1.setName("t1");
        t1.start();
        method1(10);
    }

    public static void method1(int x) {
        int y = x + 1;
        Object m = method2();
        System.out.println(m);
    }

    public static Object method2() {
        Object n = new Object();
        return n;
    }
}
