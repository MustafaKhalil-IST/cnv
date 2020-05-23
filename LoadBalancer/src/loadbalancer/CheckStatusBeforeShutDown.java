package src.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;

import java.util.TimerTask;
import java.util.logging.Logger;

public class CheckStatusBeforeShutDown extends TimerTask {
    private final static Logger logger = Logger.getLogger(CheckStatusBeforeShutDown.class.getName());
    AmazonEC2 client;
    InstanceProxy instance;

    public CheckStatusBeforeShutDown(AmazonEC2 client, InstanceProxy instance) {
        this.client = client;
        this.instance = instance;
    }

    @Override
    public void run() {
        logger.info("Instance " + instance.getAddress() + " has " + instance.currentRequests.size() + " requests .. ");
        // Shut down only if there is no requests left to be done by the instance
        if (instance.currentRequests.isEmpty()) {
            logger.info("Shutting down instance " + instance.getAddress());
            instance.shutDown(client);
            InstancesManager.getSingleton().removeInstance(instance);
            this.cancel();
        }
    }
}