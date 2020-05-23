package src.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;

import java.util.TimerTask;

public class CheckStatusBeforeShutDown extends TimerTask {
    AmazonEC2 client;
    InstanceProxy instance;

    public CheckStatusBeforeShutDown(AmazonEC2 client, InstanceProxy instance) {
        this.client = client;
        this.instance = instance;
    }

    @Override
    public void run() {
        // Shut down only if there is no requests left to be done by the instance
        if (instance.currentRequests.isEmpty()) {
            instance.shutDown(client);
            InstancesManager.getSingleton().removeInstance(instance);
            this.cancel();
        }
    }
}