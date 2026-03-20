package net.jrz.d7;

import lombok.extern.slf4j.Slf4j;
import net.jrz.d6.Teacher;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@Slf4j(topic = "c.TestUnsafe")
public class TestUnsafe { // java21 中unsafe其实已经算废弃了，现在一般用VarHandle，这个是对unsafe的一个更好的封装
    // 获取Unsafe对象
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true); // 这个字段是私有的
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        System.out.println(unsafe);

        // 1. 获取域的偏移地址
        long idOffset = unsafe.objectFieldOffset(Teacher.class.getDeclaredField("id"));
        long nameOffset = unsafe.objectFieldOffset(Teacher.class.getDeclaredField("name"));

        Teacher t = new Teacher();
        // 2. 执行cas操作
        unsafe.compareAndSwapInt(t, idOffset, 0, 1);
        unsafe.compareAndSwapObject(t, nameOffset, null, "张三");

        // 3. 验证
        System.out.println(t);
    }
}