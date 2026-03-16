package net.jrz.d5;

// 用枚举类实现单例，由于每个成员是static的，在类加载时JVM会自动完成创建，JVM本身就会保证线程安全
// 但是这个是饿汉式的，类加载时就会完成
public enum Singleton3 {
    INSTANCE;
}
