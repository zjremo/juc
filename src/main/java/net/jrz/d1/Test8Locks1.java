package net.jrz.d1;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test8Locks")
public class Test8Locks1 {
    public static void main(String[] args) {
        Number n1 = new Number();
//        new Thread(n1::b, "t1").start();
        new Thread(Number::d, "t2").start();
        new Thread(Number::e, "t3").start();
    }
}
