package src.autoscaler;

import src.loadbalancer.InstanceProxy;
import src.loadbalancer.InstancesManager;
import src.properties.PropertiesReader;

import java.util.*;

public class AutoScaler implements Runnable{
    static PropertiesReader reader = PropertiesReader.getSingleton();
    static Timer monitor = new Timer();
    public static ScalingPolicy INCREASE = new ScalingPolicy(
            Integer.parseInt(reader.getProperty("auto-scale.increase.load")) / InstanceProxy.MAX_LOAD,
            Integer.parseInt(reader.getProperty("auto-scale.increase.load.for.more.than")),
            Integer.parseInt(reader.getProperty("auto-scale.increase.max.instances")));

    public static ScalingPolicy DECREASE = new ScalingPolicy(
            Integer.parseInt(reader.getProperty("auto-scale.decrease.load")) / InstanceProxy.MAX_LOAD,
            Integer.parseInt(reader.getProperty("auto-scale.decrease.load.for.more.than")),
            Integer.parseInt(reader.getProperty("auto-scale.decrease.min.instances")));

    public final static Integer CHECK_DOWNLOADED_WORKERS = 1;
    public final static Integer CHECK_OVERLOADED_WORKERS = 2;

    @Override
    public void run() {
        monitor.schedule(new AutoScaleTask(CHECK_OVERLOADED_WORKERS), AutoScaler.INCREASE.getPeriodToAct(), AutoScaler.INCREASE.getPeriodToAct());
        monitor.schedule(new AutoScaleTask(CHECK_DOWNLOADED_WORKERS), AutoScaler.DECREASE.getPeriodToAct(), AutoScaler.DECREASE.getPeriodToAct());
    }

    public static Integer getNumberOfOverloadedWorkers() {
        Integer count = 0;
        for(InstanceProxy instance: InstancesManager.getSingleton().getInstances()) {
            if (instance.getLoadPercentage() > AutoScaler.INCREASE.getLoadPercentageToAct()) {
                count ++;
            }
        }
        return count;
    }

    public static Integer getNumberOfDownloadedWorkers() {
        System.out.println("Downloaded Percentage: " + AutoScaler.DECREASE.getLoadPercentageToAct());
        Integer count = 0;
        for(InstanceProxy instance: InstancesManager.getSingleton().getInstances()) {
            if (instance.getLoadPercentage() < AutoScaler.DECREASE.getLoadPercentageToAct()) {
                count ++;
            }
        }
        return count;
    }

}
