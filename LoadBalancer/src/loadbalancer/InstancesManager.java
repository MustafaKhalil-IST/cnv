package src.loadbalancer;

import src.autoscaler.AutoScaler;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Reservation;
import src.properties.PropertiesReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class InstancesManager {
    private final static Logger logger = Logger.getLogger(InstancesManager.class.getName());
    static AmazonEC2 ec2 = null;
    private static InstancesManager singleton = new InstancesManager();
    private List<InstanceProxy> instances = new ArrayList<InstanceProxy>();
    final Integer TOLERANCE = 500;

    private InstancesManager(){
    }

    private void init(){
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }
        catch (Exception e) {
            throw new RuntimeException("Error loading credentials", e);
        }
        ec2 = AmazonEC2ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(credentialsProvider)
                .build();

        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        PropertiesReader reader = PropertiesReader.getInstance();
        for (Reservation reservation : reservations) {
            for(Instance instance : reservation.getInstances()){
                if (instance.getState().getName().equals(InstanceStateName.Running.toString()) &&
                        instance.getImageId().equals(reader.getProperty("image.id")) &&
                        instance.getInstanceType().equals(reader.getProperty("instance.type"))) {

                    instances.add(new InstanceProxy(instance.getPublicIpAddress(), instance.getInstanceId()));

                    logger.info("Adding " + instance.getInstanceId() + " " + instance.getState().getName() + " " +
                            instance.getImageId() + " " + instance.getInstanceType());
                }
            }
        }
        if (instances.isEmpty()) {
            addInstance(InstanceProxy.connectToAnInstance(ec2));
        }
    }

    public void start() {
        init();
    }

    public static synchronized InstancesManager getSingleton(){
        return singleton;
    }

    public List<InstanceProxy> getInstances() {
        return instances;
    }

    public void addInstance(InstanceProxy instance) {
        instances.add(instance);
    }

    public void removeInstance(InstanceProxy instance) {
        instances.remove(instance);
    }

    public Double getAverageLoad() {
        double nrInstances = 0;
        double totalLoad = 0;
        for (InstanceProxy instance: instances) {
            nrInstances++;
            totalLoad += instance.getLoadPercentage();
        }
        return totalLoad / nrInstances;
    }

    public void createInstance() {
        if (instances.size() < AutoScaler.INCREASE.getNumberOfWorkers()) {
            logger.info("Creating a new Instance");
            addInstance(InstanceProxy.connectToAnInstance(ec2));
        }
    }

    public InstanceProxy getBestInstance(long cost) {
        for (InstanceProxy instance: instances) {
            if (instance.currentLoad + cost < InstanceProxy.MAX_LOAD + TOLERANCE && instance.status.equals(InstanceStatus.STARTED)) {
                return instance;
            }
        }
        return null;
    }

    public void shutDown() {
        for (InstanceProxy instance: instances) {
            if (instance.status.equals(InstanceStatus.ACTIVE)) {
                instance.startShutDown();
            }
        }
    }

    public void shutDownInstanceWithLeastLoad() {
        if (instances.size() == AutoScaler.DECREASE.getNumberOfWorkers()) {
            return;
        }
        Collections.sort(instances, InstanceProxy.LOAD_COMPARATOR);
        InstanceProxy instance = instances.get(instances.size() - 1);
        instance.startShutDown();
        logger.info("The instance " + instance.getAddress() + " is being shutdown");
    }

}
