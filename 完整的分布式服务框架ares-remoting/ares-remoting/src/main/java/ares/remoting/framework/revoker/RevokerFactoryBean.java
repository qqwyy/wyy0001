package ares.remoting.framework.revoker;

import ares.remoting.framework.model.InvokerService;
import ares.remoting.framework.model.ProviderService;
import ares.remoting.framework.zookeeper.IRegisterCenter4Consumer;
import ares.remoting.framework.zookeeper.RegisterCenter;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * 服务bean引入入口
 *
 * @author liyebing created on 16/10/3.
 * @version $Id$
 */
public class RevokerFactoryBean implements FactoryBean, InitializingBean {

    //服务接口
    private Class<?> targetInterface;
    //超时时间
    private int timeout;
    //服务bean
    private Object serviceObject;
    //负载均衡策略
    private String clusterStrategy;
    //服务提供者唯一标识
    private String remoteAppKey;
    //服务分组组名
    private String groupName = "default";

    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }


    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //获取服务注册中心
        IRegisterCenter4Consumer registerCenter4Consumer = RegisterCenter.singleton();
        //初始化服务提供者列表到本地缓存
        registerCenter4Consumer.initProviderMap(remoteAppKey, groupName);

        //初始化Netty Channel
        Map<String, List<ProviderService>> providerMap = registerCenter4Consumer.getServiceMetaDataMap4Consume();
        if (MapUtils.isEmpty(providerMap)) {
            throw new RuntimeException("service provider list is empty.");
        }
        NettyChannelPoolFactory.channelPoolFactoryInstance().initChannelPoolFactory(providerMap);

        //获取服务提供者代理对象
//        RevokerProxyBeanFactory proxyFactory = RevokerProxyBeanFactory.singleton(targetInterface, timeout, clusterStrategy);
        RevokerProxyBeanFactory proxyFactory = new RevokerProxyBeanFactory(targetInterface, timeout, clusterStrategy);   //没一个服务 实例化一个具体的InvocationHandler

        //设置代理对象
//        this.serviceObject = proxyFactory.getProxy();
        this.serviceObject =  Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, proxyFactory);

        //将消费者信息注册到注册中心
        InvokerService invoker = new InvokerService();
        invoker.setServiceItf(targetInterface);
        invoker.setRemoteAppKey(remoteAppKey);
        invoker.setGroupName(groupName);
        registerCenter4Consumer.registerInvoker(invoker);

        /**
         * zk数据：
         * config_register
         * ares
         *   default
         *      ares.remoting.test.HelloService
         *         provider
         *             192.168.xx.xx|8081|2|100|default
         *         consumer
         *             192.168.xx.xx
         */
    }


    public Class<?> getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getClusterStrategy() {
        return clusterStrategy;
    }

    public void setClusterStrategy(String clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
