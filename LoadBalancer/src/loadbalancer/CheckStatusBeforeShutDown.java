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
        if (instance.currentRequests.isEmpty()) {
            instance.shutDown(client);
            InstancesManager.getSingleton().removeInstance(instance);
            this.cancel();
        }
    }
}