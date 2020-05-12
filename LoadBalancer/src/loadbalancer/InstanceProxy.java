package loadbalancer;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import properties.PropertiesReader;
import store.Request;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class InstanceProxy {

    private final static Logger logger = Logger.getLogger(InstanceProxy.class.getName());

    public final static long MAX_LOAD = 10000000L;
    final static int STATUS_CHECK_INTERVAL = PropertiesReader.getInstance().getNumericalProperty("status.check.interval.ms");
    String address;
    String instanceID;
    Long currentLoad = 0L;
    InstanceStatus status;
    HashMap<String, Request> currentRequests = new HashMap<>();
    HashMap<String, Long> requestEstimatedComplexity = new HashMap<>();
    Timer checkStatus = new Timer();

    static final Comparator<InstanceProxy> LOAD_COMPARATOR = new Comparator<InstanceProxy>() {
        @Override
        public int compare(InstanceProxy o1, InstanceProxy o2) {
            return o1.getLoad().compareTo(o2.getLoad());
        }
    };

    public InstanceProxy(String ip, String instanceID) {
        setIP(ip);
        this.instanceID = instanceID;
        status = InstanceStatus.ACTIVE;
    }

    private InstanceProxy(String instanceID) {
        this.instanceID = instanceID;
        status = InstanceStatus.STARTING;
        checkStatus.schedule(new StartUpStatusTask(InstancesManager.ec2, this), STATUS_CHECK_INTERVAL, STATUS_CHECK_INTERVAL);
    }

    public synchronized void addRequest(Request request, long estimatedComplexity) {
        currentLoad += estimatedComplexity;
        requestEstimatedComplexity.put(request.getRequestID(), estimatedComplexity);
        currentRequests.put(request.getRequestID(), request);
    }

    public Long getLoad() {
        return currentLoad / MAX_LOAD * 100;
    }

    public synchronized void completeRequest(Request request) {
        long complexity = requestEstimatedComplexity.get(request.getRequestID());
        currentLoad -= complexity;
        requestEstimatedComplexity.remove(request.getRequestID());
        currentRequests.remove(request.getRequestID());
    }
    private synchronized void shutDown(AmazonEC2 client) {
        status = InstanceStatus.STOPPING;
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instanceID);
        client.terminateInstances(termInstanceReq);
    }

    public static InstanceProxy requestNewWorker(AmazonEC2 client) {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        PropertiesReader reader = PropertiesReader.getInstance();
        runInstancesRequest.withImageId(reader.getStringProperty("render.image.id"))
                .withInstanceType(reader.getStringProperty("render.instance.type"))
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(reader.getStringProperty("render.key.name"))
                .withSecurityGroups(reader.getStringProperty("render.security.group"))
                .withIamInstanceProfile(new IamInstanceProfileSpecification()
                        .withName(reader.getStringProperty("render.iam.role.name")));

        RunInstancesResult runInstancesResult = client.runInstances(runInstancesRequest);
        String newInstanceId = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();
        logger.info("made a new instance with ID " + newInstanceId);

        return new InstanceProxy(newInstanceId);
    }

    public void setIP(String ip) {
        address = ip + ":" + PropertiesReader.getInstance().getStringProperty("render.port");
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
            setIP(describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
            logger.info("Instance " + this.instanceID + " has started and has address" + this.address);
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
        this.checkStatus.schedule(new ShutDownStatusTask(InstancesManager.ec2, this), STATUS_CHECK_INTERVAL, STATUS_CHECK_INTERVAL);

    }

    class StartUpStatusTask extends TimerTask {

        AmazonEC2 client;
        InstanceProxy instance;

        public StartUpStatusTask(AmazonEC2 client, InstanceProxy instance){
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

    class ShutDownStatusTask extends TimerTask {
        AmazonEC2 client;
        InstanceProxy instance;

        public ShutDownStatusTask(AmazonEC2 ec2, InstanceProxy instance) {
            this.client = ec2;
            this.instance = instance;
        }

        @Override
        public void run() {
            if (currentRequests.isEmpty()) {
                instance.shutDown(client);
                InstancesManager.getInstance().removeInstance(instance);
                this.cancel();
            }
        }
    }

}
