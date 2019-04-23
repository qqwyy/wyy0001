package com;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    /**
     * 实例参考
     * https://blog.csdn.net/dingqinghu/article/details/46758671
     */
    public static void main(String[] args) throws Exception {
        //引入远程服务
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("people.xml");

        People p = (People)context.getBean("people1");
//        System.out.println(p.getId());
        System.out.println(p.getName());
        System.out.println(p.getAge());
    }


}
