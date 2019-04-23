package ares.remoting.framework.spring;

import ares.remoting.framework.provider.ProviderFactoryBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author liyebing created on 17/2/12.
 * @version $Id$
 */
public class ProviderFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final Logger logger = LoggerFactory.getLogger(ProviderFactoryBeanDefinitionParser.class);

    @Override
    protected Class getBeanClass(Element element) {
        return ProviderFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        /**
         *     <!-- 发布远程服务 -->
         *     <bean id="helloService" class="ares.remoting.test.HelloServiceImpl"/>
         *
         *     <AresServer:service id="helloServiceRegister"
         *                         interface="ares.remoting.test.HelloService"
         *                         ref="helloService"
         *                         groupName="default"
         *                         weight="2"
         *                         appKey="ares"
         *                         workerThreads="100"
         *                         serverPort="8081"
         *                         timeout="600"/>
         */
        try {
            String serviceItf = element.getAttribute("interface");
            String ref = element.getAttribute("ref");
            String groupName = element.getAttribute("groupName");
            String weight = element.getAttribute("weight");
            String appKey = element.getAttribute("appKey");
            String workerThreads = element.getAttribute("workerThreads");
            String serverPort = element.getAttribute("serverPort");
            String timeOut = element.getAttribute("timeout");

            bean.addPropertyValue("serverPort", Integer.parseInt(serverPort));
            bean.addPropertyValue("timeout", Integer.parseInt(timeOut));
            bean.addPropertyValue("serviceItf", Class.forName(serviceItf));
            bean.addPropertyReference("serviceObject", ref);
            bean.addPropertyValue("appKey", appKey);

            if (NumberUtils.isNumber(weight)) {
                bean.addPropertyValue("weight", Integer.parseInt(weight));
            }
            if (NumberUtils.isNumber(workerThreads)) {
                bean.addPropertyValue("workerThreads", Integer.parseInt(workerThreads));
            }
            if (StringUtils.isNotBlank(groupName)) {
                bean.addPropertyValue("groupName", groupName);
            }
        } catch (Exception e) {
            logger.error("ProviderFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }

    }


}
