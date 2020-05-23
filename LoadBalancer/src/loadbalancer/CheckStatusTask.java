package src.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import storage.dynamo.Request;

import java.util.TimerTask;
import java.util.logging.Logger;

class CheckStatusTask extends TimerTask {
    private final static Logger logger = Logger.getLogger(InstanceProxy.class.getName());
    AmazonEC2 client;
    InstanceProxy instance;

    public CheckStatusTask(AmazonEC2 client, InstanceProxy instance){
        this.client = client;
        this.instance = instance;
    }

    public void run() {
        logger.info("Checking Status of Instance " + instance.instanceID);
        instance.updateStatus(client);
        if (instance.status.equals(InstanceStatus.STOPPED)) {
            InstancesManager.getSingleton().removeInstance(instance);
        }
    }
}