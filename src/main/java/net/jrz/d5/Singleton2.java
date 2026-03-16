package net.jrz.d5;

import java.io.Serial;
import java.io.Serializable;

// 饿汉单例（实现了序列化接口的情况）
public final class Singleton2 implements Serializable {
    private Singleton2(){}

    private static final Singleton2 INSTANCE = new Singleton2();

    public static Singleton2 getInstance(){
        return INSTANCE;
    }

    @Serial
    public Object readResolve(){
        return INSTANCE;
    }
}

