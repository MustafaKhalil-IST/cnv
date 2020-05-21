package src.autoscaler;

import src.loadbalancer.InstanceProxy;
import src.loadbalancer.InstancesManager;
import src.properties.PropertiesReader;

import java.util.ArrayList;
import java.util.Timer;

public class AutoScaler implements Runnable{
    static PropertiesReader reader = PropertiesReader.getInstance();
    static Timer monitor = new Timer();
    public static ScalingPolicy INCREASE = new ScalingPolicy(
            Integer.parseInt(reader.getProperty("auto-scale.increase.load")),
            Integer.parseInt(reader.getProperty("auto-scale.increase.load.for.more.than")),
            Integer.parseInt(reader.getProperty("auto-scale.increase.max.instances")));
    public static ScalingPolicy DECREASE = new ScalingPolicy(
            Integer.parseInt(reader.getProperty("auto-scale.decrease.load")),
            Integer.parseInt(reader.getProperty("auto-scale.decrease.load.for.more.than")),
            Integer.parseInt(reader.getProperty("auto-scale.decrease.min.instances")));
    static final int period = Integer.parseInt(reader.getProperty("auto-scale.check.period"));

    @Override
    public void run() {
        monitor.schedule(new AutoScaleTask(), period, period);
    }

    public static Integer getNumberOfOverloadedWorkers() {
        Integer count = 0;
        for(InstanceProxy instance: InstancesManager.getSingleton().getInstances()) {
            if (instance.getLoadPercentage() > 1) {
                count ++;
            }
        }
        return count;
    }


    public static Integer getNumberOfDownloadedWorkers() {
        Integer count = 0;
        for(InstanceProxy instance: InstancesManager.getSingleton().getInstances()) {
            if (instance.getLoadPercentage() < 0.4) {
                count ++;
            }
        }
        return count;
    }

}
