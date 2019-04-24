package com.ares.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ArrayBlockingQueueDemo {
    public static void main(String[] args) {
        ArrayBlockingQueue<Cookie> arrayBlockingQueue= new ArrayBlockingQueue<Cookie>(10);//生产者和消费者共用这一个队列，队列容量为10
        Produce produce = new Produce(arrayBlockingQueue);
        produce.start();
//        一个生产者，5个消费者
        new Thread(new Consume(arrayBlockingQueue)).start();
        new Thread(new Consume(arrayBlockingQueue)).start();
        new Thread(new Consume(arrayBlockingQueue)).start();
        new Thread(new Consume(arrayBlockingQueue)).start();
        new Thread(new Consume(arrayBlockingQueue)).start();

    }
}
class Produce extends Thread{
    private static int i=0;
    private ArrayBlockingQueue<Cookie> arrayBlockingQueue;
    public Produce(ArrayBlockingQueue<Cookie> arrayBlockingQueue){
        this.arrayBlockingQueue=arrayBlockingQueue;
    }
    public void run(){
        try {
            while (i<1000) {
                arrayBlockingQueue.put(new Cookie("cookie"+i));
                if (++i%100==0){//每生产100个，休息10s
                    Thread.sleep(10000);
                }
            }
        }catch (InterruptedException e){
            System.out.println("produce queue InterruptedException");
        }
    }
}
class Consume implements Runnable{
    private ArrayBlockingQueue<Cookie> arrayBlockingQueue;
    public Consume(ArrayBlockingQueue<Cookie> arrayBlockingQueue){
        this.arrayBlockingQueue=arrayBlockingQueue;
    }
    public void run(){
        try{
            while (true){
                Cookie poll = arrayBlockingQueue.poll(5, TimeUnit.SECONDS);//如果queue为null，那么5秒之后再去队列中取数据
                if (poll!=null){
                    System.out.println(Thread.currentThread().getName()+"--consume --"+poll);
                }else{
                    System.out.println(Thread.currentThread().getName()+"--consume is null ");
                }
            }
        }catch (InterruptedException e){
            System.out.println("consume queue InterruptedException");
        }
    }
}

class Cookie{
    private String number;
    public Cookie(String number){
        this.number=number;
    }
    @Override
    public String toString() {
        return number+"";
    }
}