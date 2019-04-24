package com.ares.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.avro.data.Json;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ZKClientDemo {

    ZkClient zkClient = null;

    @Before
    public void beforeTestMethod() {
        String zkServers = "ss1:2181,ss2:2181,ss3:2181";
        int connectionTimeout = 3000;
        zkClient = new ZkClient(zkServers, connectionTimeout);
    }

    /**
     * 监听节点数据的变化
     */
    @Test
    public void test1() throws Exception{
        String path = "/config_register/wyytest2";
        if (zkClient.exists(path)) {
            zkClient.deleteRecursive(path);
        }
        zkClient.createPersistent(path,true);
        //节点写入数据
        zkClient.writeData(path, "testdata1");
        //节点读取数据
        String data = zkClient.<String>readData(path, true);
        System.out.println("获取数据为："+data);
        //注册监听器,监听数据变化
        zkClient.subscribeDataChanges(path, new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                System.out.println("监听到修改数据,dataPath:" + dataPath + " data:" + data);
            }
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                System.out.println("监听到删除数据,dataPath:" + dataPath);
            }
        });
        //修改数据
        zkClient.writeData(path, "testdata2");
        Thread.sleep(1000);
        //删除节点
        zkClient.delete(path);
        Thread.sleep(1000);
    }


    /**
     * 监听子节点的新增和删除
     */
    @Test
    public void test2() throws Exception{
        String path = "/config_register/wyytest";
        if (zkClient.exists(path)) {
            zkClient.deleteRecursive(path);
        }
        zkClient.createPersistent(path,true);
        zkClient.subscribeChildChanges(path, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds){
                System.out.println("path:"+parentPath + "    "+Json.toString(currentChilds));
            }
        });
        zkClient.createPersistent(path+"/aa");
        Thread.sleep(1000);
        zkClient.createPersistent(path+"/bb");
        Thread.sleep(1000);
        zkClient.delete(path+"/bb");
        Thread.sleep(1000);
        zkClient.createPersistent(path+"/dd");
        Thread.sleep(1000);
    }

}
