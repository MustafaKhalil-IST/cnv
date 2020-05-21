package src.autoscaler;

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
    static ArrayList<Double> loadReadings = new ArrayList<>();
    static final int period = 5000;

    @Override
    public void run() {
        monitor.schedule(new AutoScaleTask(period / 1000), period, period);
    }

    public static Double getOverLoad() {
        double totalLoad = 0;
        int upscaleEntries = INCREASE.getPeriodToAct() / period;
        for(int i = upscaleEntries + 1; i < loadReadings.size(); i++) {
            totalLoad += loadReadings.get(i);
        }
        return totalLoad;
    }


    public static Double getDownLoad() {
        double totalLoad = 0;
        int downScaleEntries = DECREASE.getPeriodToAct() / period;
        for(int i = downScaleEntries + 1; i < loadReadings.size(); i++) {
            totalLoad += loadReadings.get(i);
        }
        return totalLoad;
    }

}
