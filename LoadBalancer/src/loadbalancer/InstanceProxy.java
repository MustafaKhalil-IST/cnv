package src.loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import src.properties.PropertiesReader;
import storage.dynamo.Request;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.logging.Logger;

public class InstanceProxy {

    private final static Logger logger = Logger.getLogger(InstanceProxy.class.getName());

    public final static long MAX_LOAD = Integer.parseInt(PropertiesReader.getSingleton().getProperty("instance.max-load"));
    final static int STATUS_CHECK_PERIOD = Integer.parseInt(PropertiesReader.getSingleton().getProperty("status.check.interval"));
    String address;
    String instanceID;
    Long currentLoad = 0L;
    InstanceStatus status;
    HashMap<String, Request> currentRequests = new HashMap<>();
    HashMap<String, Long> estimatedRequestsLoads = new HashMap<>();
    Timer checkStatusMonitor = new Timer();

    static final Comparator<InstanceProxy> LOAD_COMPARATOR = new Comparator<InstanceProxy>() {
        @Override
        public int compare(InstanceProxy o1, InstanceProxy o2) {
            return o1.getLoadPercentage().compareTo(o2.getLoadPercentage());
        }
    };

    public InstanceProxy(String instanceID) {
        this.instanceID = instanceID;
        status = InstanceStatus.STARTING;
        checkStatusMonitor.schedule(new CheckStatusTask(InstancesManager.client, this), STATUS_CHECK_PERIOD, STATUS_CHECK_PERIOD);
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

    public String getAddress() {
        return address;
    }

    private DescribeInstancesResult describeInstance(AmazonEC2 client) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(this.instanceID);
        return client.describeInstances(describeInstancesRequest);
    }

    boolean updateStatus(AmazonEC2 client){
        DescribeInstancesResult describeInstancesResult = describeInstance(client);
        InstanceState state = describeInstancesResult.getReservations().get(0).getInstances().get(0).getState();
        if(state.getName().equals(InstanceStateName.Pending.toString())){
            logger.info("Instance " + this.instanceID + " is starting ...");
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
            logger.info("Instance " + this.instanceID + " has been terminated or stopped");
            this.status = InstanceStatus.STOPPED;
            return false;
        }
    }

    public synchronized void startShutDown() {
        status = InstanceStatus.STOPPED;
        checkStatusMonitor.schedule(new CheckStatusBeforeShutDown(InstancesManager.client, this), STATUS_CHECK_PERIOD, STATUS_CHECK_PERIOD);
    }

    synchronized void shutDown(AmazonEC2 client) {
        status = InstanceStatus.STOPPING;
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        terminateInstancesRequest.withInstanceIds(instanceID);
        client.terminateInstances(terminateInstancesRequest);
    }
}
