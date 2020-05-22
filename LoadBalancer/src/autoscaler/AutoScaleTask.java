package src.autoscaler;

import src.loadbalancer.InstanceProxy;
import src.loadbalancer.InstancesManager;
import java.util.TimerTask;
import java.util.logging.Logger;

public class AutoScaleTask extends TimerTask {
    private final static Logger logger = Logger.getLogger(AutoScaleTask.class.getName());
    private Integer task;

    public AutoScaleTask(int task) {
        this.task = task;
    }

    @Override
    public void run() {
        if(task.equals(AutoScaler.CHECK_DOWNLOADED_WORKERS)) {
            Integer downloadedNumber = AutoScaler.getNumberOfDownloadedWorkers();
            logger.info("Checking downloaded workers ...: # = " + downloadedNumber);
            if (downloadedNumber >= 2) {
                InstancesManager.getSingleton().shutDownInstanceWithLeastLoad();
            }
        } else if (task.equals(AutoScaler.CHECK_OVERLOADED_WORKERS)){
            Integer overloadedNumber = AutoScaler.getNumberOfOverloadedWorkers();
            logger.info("Checking overloaded workers ...: # = " + overloadedNumber);
            if (overloadedNumber >= 1) {
                InstancesManager.getSingleton().createInstance();
            }
        }
    }
}
