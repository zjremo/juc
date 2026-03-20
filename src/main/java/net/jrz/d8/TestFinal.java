package net.jrz.d8;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.TestFinal")
public class TestFinal {
    /*
    * 1. 编译期常量，直接被替换，之后的值从运行时常量池获取
    * 2. 运行期才可以确定，从内存中读取
    * 3. 实例final: 从内存中读取
    * 4. 引用类型: 运行时才知道，从内存中读取
    * */
    public static void main(String[] args) {

    }
}
