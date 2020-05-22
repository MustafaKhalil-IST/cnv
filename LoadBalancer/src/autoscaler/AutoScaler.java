package src.autoscaler;

import src.loadbalancer.InstanceProxy;
import src.loadbalancer.InstancesManager;
import src.properties.PropertiesReader;

import java.util.*;

public class AutoScaler implements Runnable{
    static PropertiesReader reader = PropertiesReader.getSingleton();
    static Timer monitor = new Timer();
    public static ScalingPolicy INCREASE = new ScalingPolicy(
            Integer.parseInt(reader.getProperty("auto-scale.increase.load")),
            Integer.parseInt(reader.getProperty("auto-scale.increase.load.for.more.than")),
            Integer.parseInt(reader.getProperty("auto-scale.increase.max.instances")));

    public static ScalingPolicy DECREASE = new ScalingPolicy(
            Integer.parseInt(reader.getProperty("auto-scale.decrease.load")),
            Integer.parseInt(reader.getProperty("auto-scale.decrease.load.for.more.than")),
            Integer.parseInt(reader.getProperty("auto-scale.decrease.min.instances")));

    static final int CHECK_OVERLOADED_WORKERS_PERIOD = Integer.parseInt(reader.getProperty("auto-scale.check.overloaded.period"));
    static final int CHECK_DOWNLOADED_WORKERS_PERIOD = Integer.parseInt(reader.getProperty("auto-scale.check.downloaded.period"));

    public final static Integer CHECK_DOWNLOADED_WORKERS = 1;
    public final static Integer CHECK_OVERLOADED_WORKERS = 2;
    Map<String, Integer> downloadedInstances = new HashMap<>();

    @Override
    public void run() {
        monitor.schedule(new AutoScaleTask(CHECK_OVERLOADED_WORKERS), CHECK_OVERLOADED_WORKERS_PERIOD, CHECK_OVERLOADED_WORKERS_PERIOD);
        monitor.schedule(new AutoScaleTask(CHECK_DOWNLOADED_WORKERS), CHECK_DOWNLOADED_WORKERS_PERIOD, CHECK_DOWNLOADED_WORKERS_PERIOD);
    }

    public void updateDownloadedInstances() {
        for(InstanceProxy instance: InstancesManager.getSingleton().getInstances()) {
            if (instance.getLoadPercentage() < 0.4) {
                if (downloadedInstances.containsKey(instance.getAddress())) {
                    downloadedInstances.put(instance.getAddress(), downloadedInstances.get(instance.getAddress()) + 1);
                }
                else {
                    downloadedInstances.put(instance.getAddress(), 1);
                }
            }
        }
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
