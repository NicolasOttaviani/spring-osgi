package spring.osgi.services;

import org.springframework.aop.TargetSource;
import spring.osgi.utils.SingleServiceTracker;

/**
 * Created by nico.
 */
public class SingleServiceTrackerTargetSource implements TargetSource {

    private final SingleServiceTracker<?> singleServiceTracker;

    public SingleServiceTrackerTargetSource(SingleServiceTracker<?> singleServiceTracker) {
        this.singleServiceTracker = singleServiceTracker;
    }

    @Override
    public Class<?> getTargetClass() {
        return singleServiceTracker.getClazz();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public Object getTarget() throws Exception {
        return singleServiceTracker.getService();
    }

    @Override
    public void releaseTarget(Object target) throws Exception {

    }
}
