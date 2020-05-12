package estimation;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import store.Metrics;
import store.Store;
import java.util.TimerTask;
import java.util.logging.Logger;

public class Estimation extends TimerTask {
    private static Logger logger = Logger.getLogger(Estimation.class.getName());
    double acceptableAmount = 0.1;

    public Estimation() {
    }

    @Override
    public void run() {
        logger.info("Estimating metrics.");
        PaginatedScanList<Metrics> metricsToWorkOn = Store.getStore().getRequestMetricsToProcess();
        if (metricsToWorkOn == null || metricsToWorkOn.isEmpty()) {
            logger.info("There are no metrics to process.");
            return;
        }
        for (Metrics metric: metricsToWorkOn) {
            double guess = metric.getEstimatedNumberOfCalls();
            double real = metric.getNumberOfCalls();
            double ratio = guess / real;
            if (ratio >= 1 + acceptableAmount || ratio <= 1 - acceptableAmount) {
                EstimationsStore.getStore().storeEstimation(metricToStoredEstimation(metric));
            }
            Store.getStore().deleteMetric(metric);
        }
    }

    public StoredEstimation metricToStoredEstimation(Metrics metrics) {
        return new StoredEstimation(metrics.getRequest());
    }
}
