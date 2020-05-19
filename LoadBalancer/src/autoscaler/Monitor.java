package src.autoscaler;

import src.loadbalancer.InstanceProxy;
import src.loadbalancer.InstancesManager;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Logger;

public class Monitor  extends TimerTask {
    private final static Logger logger = Logger.getLogger(Monitor.class.getName());

    private int maxArraySize;
    private int minArraySize;
    private int position = 0;

    public Monitor(int period) {
        this.maxArraySize = Math.max(AutoScaler.UPSCALE.secondsWithLoad, AutoScaler.DOWNSCALE.secondsWithLoad) / period;
        this.minArraySize = Math.min(AutoScaler.UPSCALE.secondsWithLoad, AutoScaler.DOWNSCALE.secondsWithLoad) / period;
    }

    @Override
    public void run() {
        if (AutoScaler.loadReadings.size() >= minArraySize) {
            if (AutoScaler.loadReadings.size() >= maxArraySize) {
                AutoScaler.loadReadings.set(position, InstancesManager.getInstance().getAverageLoad());
            } else {
                AutoScaler.loadReadings.add(InstancesManager.getInstance().getAverageLoad());
            }
            if (AutoScaler.getUpscaleLoad() > AutoScaler.UPSCALE.loadPercentage) {
                InstancesManager.getInstance().createInstance(InstanceProxy.MAX_LOAD);
                AutoScaler.loadReadings = new ArrayList<>(0);
            } else if (AutoScaler.getDownScaleLoad() < AutoScaler.DOWNSCALE.loadPercentage) {
                InstancesManager.getInstance().shutDownLaziestInstance();
                AutoScaler.loadReadings = new ArrayList<>(0);
            }
        } else {
            AutoScaler.loadReadings.add(InstancesManager.getInstance().getAverageLoad());
        }
        position = (position + 1) % maxArraySize;

    }
}
