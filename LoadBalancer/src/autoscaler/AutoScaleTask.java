package src.autoscaler;

import src.loadbalancer.InstanceProxy;
import src.loadbalancer.InstancesManager;
import java.util.TimerTask;
import java.util.logging.Logger;

public class AutoScaleTask extends TimerTask {
    private final static Logger logger = Logger.getLogger(AutoScaleTask.class.getName());
    public AutoScaleTask() {
    }

    @Override
    public void run() {
        Integer overloadedNumber = AutoScaler.getNumberOfOverloadedWorkers();
        Integer downloadedNumber = AutoScaler.getNumberOfDownloadedWorkers();
        logger.info("Autoscaling check: over: " + overloadedNumber + " - down: " + downloadedNumber);
        for(InstanceProxy instance: InstancesManager.getSingleton().getInstances()) {
            System.out.println(instance.getAddress() + " - " + instance.getStatus());
        }
        if (overloadedNumber >= 1) {
            InstancesManager.getSingleton().createInstance();
        }
        if (downloadedNumber >= 1) {
            InstancesManager.getSingleton().shutDownInstanceWithLeastLoad();
        }
    }
}
