package net.jrz.d7;

import net.jrz.d6.Teacher;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class TestVarHandle {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        // 1. 获取varHandle, varHandle 像是直接加在字段上的钩子
        VarHandle idHandle = MethodHandles.lookup().findVarHandle(Teacher.class, "id", int.class);
        VarHandle nameHandle = MethodHandles.lookup().findVarHandle(Teacher.class, "name", String.class);

        Teacher t = new Teacher();
        // 2. 执行cas操作，来对字段信息进行修改
        idHandle.compareAndSet(t, 0, 1);
        nameHandle.compareAndSet(t, null, "张三");

        // 3. 验证
        System.out.println(t);
    }
}
