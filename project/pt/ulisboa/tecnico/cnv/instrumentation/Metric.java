public class Metric {

    public long tid;
    public double count = 0;

    public Metric(long tid) {
        this.tid = tid;
        System.out.println("> [Instrumentation]: Created metrics for thread: " + tid);
    }

    public String toString() {
        return "" + count + "\n";
    }

    public void resetMetrics() {
        count = 0;
        System.out.println("> [Instrumentation]: Reset metrics for thread: " + this.tid);
    }
}




