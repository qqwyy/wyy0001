package ares.remoting.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Random;
import java.util.concurrent.*;

public class MainClientThread {

    private static final Logger logger = LoggerFactory.getLogger(MainClientThread.class);

    class Task implements Callable<String> {

        private int times;
        private HelloService helloService;
        public Task(int times,HelloService helloService) {
            this.times = times;
            this.helloService =helloService;
        }

        @Override
        public String call() throws Exception{
            String result = helloService.sayHello(""+times);
            return result;
        }
    }


    class WorkTask implements Callable<String> {

        private int times;
        private WorkService workService;
        public WorkTask(int times,WorkService workService) {
            this.times = times;
            this.workService =workService;
        }

        @Override
        public String call() throws Exception{
            String result = workService.work(times);
            return result;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //引入远程服务
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ares-client.xml");
        //获取远程服务
        final HelloService helloService = (HelloService) context.getBean("remoteHelloService");
        final WorkService workService = (WorkService) context.getBean("remoteWorkService");
        MainClientThread mainClientThread = new MainClientThread();
        ExecutorService pool = Executors.newCachedThreadPool();//模拟客户端并发线程
        int count = 100;
        Future[] futures = new Future[count];
        Future[] workfutures = new Future[count];
        for (int i = 0; i < count; i++) {
            futures[i]= pool.submit(mainClientThread.new Task(i, helloService));
            workfutures[i] = pool.submit(mainClientThread.new WorkTask(i, workService));
        }
        Thread.sleep(10000);
        for (int i = 0; i < count; i++) {
            logger.info("序号："+i+"      结果"+(String)futures[i].get());
        }

        for (int i = 0; i < count; i++) {
            logger.info("work序号："+i+"      结果"+(String)workfutures[i].get());
        }




        Thread.sleep(1000000);

        pool.shutdown();
        //关闭jvm
        System.exit(0);
    }
}
