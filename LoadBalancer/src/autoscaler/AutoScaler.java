package autoscaler;

import properties.PropertiesReader;

import java.util.ArrayList;
import java.util.Timer;

public class AutoScaler implements Runnable{
    static PropertiesReader reader = PropertiesReader.getInstance();
    public static ScalingPolicy UPSCALE_POLICY = new ScalingPolicy(reader.getIntegerProperty("autoscale.upscale.load"),
            reader.getIntegerProperty("autoscale.upscale.seconds.over.load"),
            reader.getIntegerProperty("autoscale.max.workers"));
    public static ScalingPolicy DOWNSCALE_POLICY = new ScalingPolicy(reader.getIntegerProperty("autoscale.downscale.load"),
                    reader.getIntegerProperty("autoscale.downscale.seconds.below.load"),
                    reader.getIntegerProperty("autoscale.min.workers"));
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
        int upscaleEntries = UPSCALE_POLICY.secondsWithLoad / measurePeriod;
        int pos = 0;
        for (Double reading: load) {
            if (pos > upscaleEntries)
                totalLoad += reading;
            pos++;
        }
        return totalLoad;
    }


    public static Double getDownScaleLoad() {
        double totalLoad = 0;
        int downScaleEntries = DOWNSCALE_POLICY.secondsWithLoad / measurePeriod;
        int pos = 0;
        for (Double reading: load) {
            if (pos > downScaleEntries)
                totalLoad += reading;
            pos++;
        }
        return totalLoad;
    }

}
