package ares.remoting.test;

public class WorkServiceImpl implements WorkService {


    @Override
    public String work(int x) {
        return "work工作 " + x + "";
    }


}
