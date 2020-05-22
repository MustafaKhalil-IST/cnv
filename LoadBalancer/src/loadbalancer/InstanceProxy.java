package src.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import src.properties.PropertiesReader;
import storage.dynamo.Request;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class InstanceProxy {

    private final static Logger logger = Logger.getLogger(InstanceProxy.class.getName());

    public final static long MAX_LOAD = Integer.parseInt(PropertiesReader.getSingleton().getProperty("instance.max-load"));
    final static int STATUS_CHECK_PERIOD = Integer.parseInt(PropertiesReader.getSingleton().getProperty("status.check.interval"));
    String address;
    String instanceID;
    Long currentLoad = 0L;
    InstanceStatus status;
    HashMap<String, Request> currentRequests = new HashMap<>(); // TODO
    HashMap<String, Long> estimatedRequestsLoads = new HashMap<>(); // TODO
    Timer checkStatus = new Timer();

    static final Comparator<InstanceProxy> LOAD_COMPARATOR = new Comparator<InstanceProxy>() {
        @Override
        public int compare(InstanceProxy o1, InstanceProxy o2) {
            return o1.getLoadPercentage().compareTo(o2.getLoadPercentage());
        }
    };

    public InstanceProxy(String ip, String instanceID) {
        updateAddress(ip);
        this.instanceID = instanceID;
        status = InstanceStatus.ACTIVE;
    }

    public InstanceProxy(String instanceID) {
        this.instanceID = instanceID;
        status = InstanceStatus.STARTING;
        checkStatus.schedule(new CheckStatusTask(InstancesManager.client, this), STATUS_CHECK_PERIOD, STATUS_CHECK_PERIOD);
    }

    public InstanceStatus getStatus(){
        return status;
    }

    public synchronized void addRequest(Request request, long estimatedCost) {
        currentLoad += estimatedCost;
        estimatedRequestsLoads.put(request.getRequestID(), estimatedCost);
        currentRequests.put(request.getRequestID(), request);
        logger.info("Instance " + getAddress() + " current load is " + currentLoad);
    }

    public Long getLoadPercentage() {
        return currentLoad / MAX_LOAD * 100;
    }

    public synchronized void updateInstanceLoad(Request request) {
        long estimatedLoad = estimatedRequestsLoads.get(request.getRequestID());
        currentLoad -= estimatedLoad;
        estimatedRequestsLoads.remove(request.getRequestID());
        currentRequests.remove(request.getRequestID());
        logger.info("Instance " + getAddress() + " current load is " + currentLoad);
    }

    private synchronized void shutDown(AmazonEC2 client) {
        status = InstanceStatus.STOPPING;
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        terminateInstancesRequest.withInstanceIds(instanceID);
        client.terminateInstances(terminateInstancesRequest);
    }

    public static InstanceProxy connectToAnInstance(AmazonEC2 client) {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        PropertiesReader reader = PropertiesReader.getSingleton();
        runInstancesRequest.withImageId(reader.getProperty("image.id"))
                .withInstanceType(reader.getProperty("instance.type"))
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(reader.getProperty("key.name"))
                .withSecurityGroups(reader.getProperty("security.group"));

        RunInstancesResult runInstancesResult = client.runInstances(runInstancesRequest);
        String newInstanceId = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();

        logger.info("A new Instance has been connected: " + newInstanceId);

        return new InstanceProxy(newInstanceId);
    }

    public void updateAddress(String newAddress) {
        address = newAddress + ":" + PropertiesReader.getSingleton().getProperty("instance.port");
    }

    private DescribeInstancesResult describeInstance(AmazonEC2 client) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(this.instanceID);
        return client.describeInstances(describeInstancesRequest);
    }

    private boolean updateState(AmazonEC2 client){
        DescribeInstancesResult describeInstancesResult = describeInstance(client);
        InstanceState state = describeInstancesResult.getReservations().get(0).getInstances().get(0).getState();
        if(state.getName().equals(InstanceStateName.Pending.toString())){
            this.status = InstanceStatus.STARTING;
            return false;
        }
        else if(state.getName().equals(InstanceStateName.Running.toString())){
            updateAddress(describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
            logger.info("Instance " + this.instanceID + " has started with the address " + this.address);
            this.status = InstanceStatus.STARTED;
            return true;
        }
        else {
            return false;
        }
    }

    public String getAddress() {
        return address;
    }

    public synchronized void startShutDown() {
        this.status = InstanceStatus.STOPPED;
        this.checkStatus.schedule(new CheckStatusBeforeShutDownTask(InstancesManager.client, this), STATUS_CHECK_PERIOD, STATUS_CHECK_PERIOD);
    }

    static class CheckStatusTask extends TimerTask {

        AmazonEC2 client;
        InstanceProxy instance;

        public CheckStatusTask(AmazonEC2 client, InstanceProxy instance){
            this.client = client;
            this.instance = instance;
        }

        public void run() {
            logger.info("Checking Status of Instance " + instance.instanceID);
            if(instance.status.equals(InstanceStatus.STARTING)){
                if (instance.updateState(client)) {
                    this.cancel();
                }
            } else {
                this.cancel();
            }
        }
    }

    class CheckStatusBeforeShutDownTask extends TimerTask {
        AmazonEC2 client;
        InstanceProxy instance;

        public CheckStatusBeforeShutDownTask(AmazonEC2 client, InstanceProxy instance) {
            this.client = client;
            this.instance = instance;
        }

        @Override
        public void run() {
            if (currentRequests.isEmpty()) {
                instance.shutDown(client);
                InstancesManager.getSingleton().removeInstance(instance);
                this.cancel();
            }
        }
    }

}
