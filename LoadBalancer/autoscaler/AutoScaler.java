package autoscaler;

import properties.PropertiesReader;

import java.util.ArrayList;
import java.util.Timer;

public class AutoScaler implements Runnable{
    static PropertiesReader reader = PropertiesReader.getInstance();
    static Timer monitor = new Timer();
    public static ScalingPolicy UPSCALE = new ScalingPolicy(reader.getNumericalProperty("autoscale.upscale.load"),
            reader.getNumericalProperty("autoscale.upscale.seconds.over.load"),
            reader.getNumericalProperty("autoscale.max.instances"));
    public static ScalingPolicy DOWNSCALE = new ScalingPolicy(reader.getNumericalProperty("autoscale.downscale.load"),
                    reader.getNumericalProperty("autoscale.downscale.seconds.below.load"),
                    reader.getNumericalProperty("autoscale.min.instances"));
    static ArrayList<Double> loadReadings = new ArrayList<>();
    static final int period = 5000;

    @Override
    public void run() {
        monitor.schedule(new Monitor(period / 1000), period, period);
    }

    public static Double getTotalLoad() {
        double totalLoad = 0;
        for (Double reading: loadReadings) {
            totalLoad += reading;
        }
        return totalLoad;
    }

    public static Double getUpscaleLoad() {
        double totalLoad = 0;
        int upscaleEntries = UPSCALE.secondsWithLoad / period;
        for(int i = 0; i < loadReadings.size(); i++) {
            if (i > upscaleEntries) {
                totalLoad += loadReadings.get(i);
            }
        }
        return totalLoad;
    }


    public static Double getDownScaleLoad() {
        double totalLoad = 0;
        int downScaleEntries = DOWNSCALE.secondsWithLoad / period;
        for(int i = 0; i < loadReadings.size(); i++) {
            if (i > downScaleEntries) {
                totalLoad += loadReadings.get(i);
            }
        }
        return totalLoad;
    }

}
