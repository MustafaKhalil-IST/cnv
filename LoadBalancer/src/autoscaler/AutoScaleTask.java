package src.autoscaler;

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
        if (overloadedNumber > 1) {
            InstancesManager.getSingleton().createInstance();
        }
        if (downloadedNumber < 1) {
            InstancesManager.getSingleton().shutDownInstanceWithLeastLoad();
        }
    }
}
