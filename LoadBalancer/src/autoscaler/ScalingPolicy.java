package src.autoscaler;

public class ScalingPolicy {
    private int periodToAct;
    private double loadPercentageToAct;
    private int numberOfWorkers;

    public ScalingPolicy(double loadPercentage, int periodToAct, int numberOfWorkers) {
        this.periodToAct = periodToAct;
        this.loadPercentageToAct = loadPercentage;
        this.numberOfWorkers = numberOfWorkers;
    }

    public int getPeriodToAct() {
        return periodToAct;
    }

    public void setPeriodToAct(int periodToAct) {
        this.periodToAct = periodToAct;
    }

    public double getLoadPercentageToAct() {
        return loadPercentageToAct;
    }

    public void setLoadPercentageToAct(int loadPercentageToAct) {
        this.loadPercentageToAct = loadPercentageToAct;
    }

    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }
}
