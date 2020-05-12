package autoscaler;

import properties.PropertiesReader;

import java.util.ArrayList;
import java.util.Timer;

public class AutoScaler implements Runnable{
    static PropertiesReader reader = PropertiesReader.getInstance();
    public static ScalingPolicy UPSCALE = new ScalingPolicy(reader.getNumericalProperty("autoscale.upscale.load"),
            reader.getNumericalProperty("autoscale.upscale.seconds.over.load"),
            reader.getNumericalProperty("autoscale.max.instances"));
    public static ScalingPolicy DOWNSCALE = new ScalingPolicy(reader.getNumericalProperty("autoscale.downscale.load"),
                    reader.getNumericalProperty("autoscale.downscale.seconds.below.load"),
                    reader.getNumericalProperty("autoscale.min.instances"));
    static Timer monitor = new Timer();
    static ArrayList<Double> load = new ArrayList<>();
    static final int measurePeriod = 5000;

    @Override
    public void run() {
        monitor.schedule(new Monitor(measurePeriod /1000), measurePeriod, measurePeriod);
    }

    public static Double getTotalLoad() {
        double totalLoad = 0;
        for (Double reading: load) {
            totalLoad += reading;
        }
        return totalLoad;
    }

    public static Double getUpscaleLoad() {
        double totalLoad = 0;
        int upscaleEntries = UPSCALE.secondsWithLoad / measurePeriod;
        int index = 0;
        for (Double reading: load) {
            if (index > upscaleEntries)
                totalLoad += reading;
            index++;
        }
        return totalLoad;
    }


    public static Double getDownScaleLoad() {
        double totalLoad = 0;
        int downScaleEntries = DOWNSCALE.secondsWithLoad / measurePeriod;
        int index = 0;
        for (Double reading: load) {
            if (index > downScaleEntries)
                totalLoad += reading;
            index++;
        }
        return totalLoad;
    }

}
