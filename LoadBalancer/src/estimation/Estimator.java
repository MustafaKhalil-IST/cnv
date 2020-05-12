package estimation;

import properties.PropertiesReader;

import java.util.Timer;

public class Estimator implements Runnable {

    static Timer estimator = new Timer();
    static int PERIOD = PropertiesReader.getInstance().getNumericalProperty("estimate.period.milliseconds");
    @Override
    public void run() {
        estimator.schedule(new Estimation(), PERIOD, PERIOD);
    }
}
