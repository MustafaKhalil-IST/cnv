package autoscaler;

public class ScalingPolicy {
    public int secondsWithLoad;
    public int loadPercentage;
    public int instances;

    public ScalingPolicy(int loadPercentage, int secondsWithLoad, int instances) {
        this.secondsWithLoad = secondsWithLoad;
        this.loadPercentage = loadPercentage;
        this.instances = instances;
    }
}
