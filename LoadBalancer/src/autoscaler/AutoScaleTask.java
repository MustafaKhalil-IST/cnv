package src.autoscaler;

import src.loadbalancer.InstancesManager;
import java.util.TimerTask;

public class AutoScaleTask extends TimerTask {
    public AutoScaleTask() {
    }

    @Override
    public void run() {
        Integer overloadedNumber = AutoScaler.getNumberOfOverloadedWorkers();
        Integer downloadedNumber = AutoScaler.getNumberOfDownloadedWorkers();
        if (overloadedNumber > 1) {
            InstancesManager.getSingleton().createInstance();
        }
        if (downloadedNumber < 1) {
            InstancesManager.getSingleton().shutDownInstanceWithLeastLoad();
        }
    }
}
