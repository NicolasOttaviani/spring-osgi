package spring.osgi.services;

import org.springframework.aop.framework.ProxyFactoryBean;
import spring.osgi.utils.SingleServiceTracker;

/**
 * Created by nico.
 */
public class SingleServiceTrackerProxyFactoryBean extends ProxyFactoryBean {

    public SingleServiceTrackerProxyFactoryBean(SingleServiceTracker<?> singleServiceTracker) {
        setInterfaces(singleServiceTracker.getClazz());
        setTargetSource(new SingleServiceTrackerTargetSource(singleServiceTracker));
    }
}
