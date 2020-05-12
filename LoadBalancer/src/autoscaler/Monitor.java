package autoscaler;

import loadbalancer.InstanceProxy;
import loadbalancer.InstancesManager;

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
        if (AutoScaler.load.size() >= minArraySize) {
            if (AutoScaler.load.size() >= maxArraySize) {
                AutoScaler.load.set(position, InstancesManager.getInstance().getAverageLoad());
            } else {
                AutoScaler.load.add(InstancesManager.getInstance().getAverageLoad());
            }
            if (AutoScaler.getUpscaleLoad() > AutoScaler.UPSCALE.loadPercentage) {
                InstancesManager.getInstance().createInstance(InstanceProxy.MAX_LOAD);
                AutoScaler.load = new ArrayList<>(0);
            } else if (AutoScaler.getDownScaleLoad() < AutoScaler.DOWNSCALE.loadPercentage) {
                InstancesManager.getInstance().shutDownLaziestInstance();
                AutoScaler.load = new ArrayList<>(0);
            }
        } else {
            AutoScaler.load.add(InstancesManager.getInstance().getAverageLoad());
        }
        position = (position + 1) % maxArraySize;

    }
}
