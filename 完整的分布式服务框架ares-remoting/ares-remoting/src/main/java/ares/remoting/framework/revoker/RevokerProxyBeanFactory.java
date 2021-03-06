package ares.remoting.framework.revoker;

import ares.remoting.framework.cluster.ClusterStrategy;
import ares.remoting.framework.cluster.engine.ClusterEngine;
import ares.remoting.framework.helper.PropertyConfigeHelper;
import ares.remoting.framework.model.AresRequest;
import ares.remoting.framework.model.AresResponse;
import ares.remoting.framework.model.ProviderService;
import ares.remoting.framework.zookeeper.IRegisterCenter4Consumer;
import ares.remoting.framework.zookeeper.RegisterCenter;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 消费端bean代理工厂
 *
 * @author liyebing created on 16/10/3.
 * @version $Id$
 */
public class RevokerProxyBeanFactory implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RevokerProxyBeanFactory.class);
    private ExecutorService fixedThreadPool = null;

    //todo  不建议使用成员变量

    //服务接口
    private Class<?> targetInterface;//todo  这里有问题  当多个服务的时候，这里只初始化一次  为单例
    //超时时间
    private int consumeTimeout;
    //调用者线程数
    private static int threadWorkerNumber = 10;
    //负载均衡策略
    private String clusterStrategy;


    public RevokerProxyBeanFactory(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) {
        logger.info("RevokerProxyBeanFactory: "+ (this.targetInterface==null?null:this.targetInterface.getName()) +"  " + targetInterface.getName());
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.clusterStrategy = clusterStrategy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //服务接口名称
        String serviceKey = targetInterface.getName();
        logger.info("invoke "+serviceKey +"  "+method.getName() );
        //获取某个接口的服务提供者列表
        IRegisterCenter4Consumer registerCenter4Consumer = RegisterCenter.singleton();
        List<ProviderService> providerServices = registerCenter4Consumer.getServiceMetaDataMap4Consume().get(serviceKey);
        //根据软负载策略,从服务提供者列表选取本次调用的服务提供者
        ClusterStrategy clusterStrategyService = ClusterEngine.queryClusterStrategy(clusterStrategy);
        ProviderService providerService = clusterStrategyService.select(providerServices);
        //复制一份服务提供者信息
        ProviderService newProviderService = providerService.copy();
        //设置本次调用服务的方法以及接口
        newProviderService.setServiceMethod(method);
        newProviderService.setServiceItf(targetInterface);

        //声明调用AresRequest对象,AresRequest表示发起一次调用所包含的信息
        final AresRequest request = new AresRequest();
        //设置本次调用的唯一标识
        request.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
        //设置本次调用的服务提供者信息
        request.setProviderService(newProviderService);
        //设置本次调用的超时时间
        request.setInvokeTimeout(consumeTimeout);
        //设置本次调用的方法名称
        request.setInvokedMethodName(method.getName());
        //设置本次调用的方法参数信息
        request.setArgs(args);

        try {
            //构建用来发起调用的线程池
            if (fixedThreadPool == null) { //todo  提取为公共客户端调用线程
                synchronized (RevokerProxyBeanFactory.class) {
                    if (null == fixedThreadPool) {
                        fixedThreadPool = Executors.newFixedThreadPool(threadWorkerNumber);
                    }
                }
            }
            //根据服务提供者的ip,port,构建InetSocketAddress对象,标识服务提供者地址
            String serverIp = request.getProviderService().getServerIp();
            int serverPort = request.getProviderService().getServerPort();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
            //提交本次调用信息到线程池fixedThreadPool,发起调用
            Future<AresResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(inetSocketAddress, request));//其实也不一定要用RevokerServiceCallable 期内的函数逻辑封装为调用工具类
            //获取调用的返回结果
            AresResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);//阻塞，等待返回，知道超时为止
            if (response != null) {
                return response.getResult();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    //todo 该函数需要外迁

//    public Object getProxy() {
//        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this);
//    }


//    private static volatile RevokerProxyBeanFactory singleton;
//
//    public static RevokerProxyBeanFactory singleton(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) throws Exception {
//        if (null == singleton) {
//            synchronized (RevokerProxyBeanFactory.class) {
//                if (null == singleton) {
//                    singleton = new RevokerProxyBeanFactory(targetInterface, consumeTimeout, clusterStrategy);
//                }
//            }
//        }
//        return singleton;
//    }


}
