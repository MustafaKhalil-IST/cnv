package src.autoscaler;

import src.loadbalancer.InstanceProxy;
import src.loadbalancer.InstancesManager;

import java.util.ArrayList;
import java.util.TimerTask;

public class AutoScaleTask extends TimerTask {
    private int maxArraySize;
    private int minArraySize;
    private int position = 0;

    public AutoScaleTask(int period) {
        this.maxArraySize = Math.max(AutoScaler.INCREASE.getPeriodToAct(), AutoScaler.DECREASE.getPeriodToAct()) / period;
        this.minArraySize = Math.min(AutoScaler.INCREASE.getPeriodToAct(), AutoScaler.DECREASE.getPeriodToAct()) / period;
    }

    @Override
    public void run() {
        if (AutoScaler.loadReadings.size() >= minArraySize) {
            if (AutoScaler.loadReadings.size() >= maxArraySize) {
                AutoScaler.loadReadings.set(position, InstancesManager.getSingleton().getAverageLoad());
            } else {
                AutoScaler.loadReadings.add(InstancesManager.getSingleton().getAverageLoad());
            }
            if (AutoScaler.getIncreasedLoad() > AutoScaler.INCREASE.getLoadPercentageToAct()) {
                InstancesManager.getSingleton().createInstance(InstanceProxy.MAX_LOAD);
                AutoScaler.loadReadings = new ArrayList<>(0);
            } else if (AutoScaler.getDecreasedLoad() < AutoScaler.DECREASE.getLoadPercentageToAct()) {
                InstancesManager.getSingleton().shutDownLaziestInstance();
                AutoScaler.loadReadings = new ArrayList<>(0);
            }
        } else {
            AutoScaler.loadReadings.add(InstancesManager.getSingleton().getAverageLoad());
        }
        position = (position + 1) % maxArraySize;

    }
}
