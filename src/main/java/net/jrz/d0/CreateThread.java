package net.jrz.d0;

class MyThread extends Thread{
    @Override
    public void run() {
        System.out.println("Current running thread is " + Thread.currentThread().getName());
    }
}

public class CreateThread {
    public static void main(String[] args) {
        Thread t1 = new MyThread();
        t1.start();
    }
}
