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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class InstancesManager {
    private final static Logger logger = Logger.getLogger(InstancesManager.class.getName());
    static AmazonEC2 client = null;
    private static InstancesManager singleton = new InstancesManager();
    private List<InstanceProxy> instances = new ArrayList<InstanceProxy>();
    private AtomicInteger nextInstance = new AtomicInteger(0);
    final Integer TOLERANCE = Integer.parseInt(PropertiesReader.getSingleton().getProperty("instance.tolerance"));

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

        client = AmazonEC2ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(credentialsProvider)
                .build();

        DescribeInstancesResult describeInstancesRequest = client.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        PropertiesReader reader = PropertiesReader.getSingleton();
        for (Reservation reservation : reservations) {
            for(Instance instance : reservation.getInstances()){
                if (instance.getState().getName().equals(InstanceStateName.Running.toString()) &&
                        instance.getImageId().equals(reader.getProperty("image.id")) &&
                        instance.getInstanceType().equals(reader.getProperty("instance.type"))) {

                    InstanceProxy instanceProxy = new InstanceProxy(instance.getInstanceId());
                    instanceProxy.updateAddress(instance.getPublicIpAddress());
                    instances.add(instanceProxy);

                    logger.info("Adding " + instance.getInstanceId() + " " + instance.getState().getName() + " " +
                            instance.getImageId() + " " + instance.getInstanceType());
                }
            }
        }

        int minimumNumberOfInstances = Integer.parseInt(PropertiesReader.getSingleton().getProperty("auto-scale.decrease.min.instances"));
        int currentNumberOfInstances = instances.size();
        logger.info("Adding " + (minimumNumberOfInstances - currentNumberOfInstances) + " instances ... ");
        for(int i = 0; i < minimumNumberOfInstances - currentNumberOfInstances; i++) {
            addInstance(InstanceProxy.connectToAnInstance(client));
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

    public void createInstance() {
        if (instances.size() < AutoScaler.INCREASE.getNumberOfWorkers()) {
            logger.info("Creating a new Instance");
            addInstance(InstanceProxy.connectToAnInstance(client));
        }
    }

    private Boolean isInstanceReadyToLoadCost(InstanceProxy instance, long cost) {
        return instance.currentLoad + cost < InstanceProxy.MAX_LOAD + TOLERANCE && instance.status.equals(InstanceStatus.STARTED);
    }

    public InstanceProxy getBestInstance(long cost) {
        if (nextInstance.get() >= instances.size()) {
            nextInstance.set(nextInstance.get() % instances.size());
        }

        nextInstance.getAndIncrement();
        nextInstance.set(nextInstance.get() % instances.size());
        InstanceProxy instance  = instances.get(nextInstance.get());
        while (!isInstanceReadyToLoadCost(instance, cost)) {
            logger.warning("There is no ready instance to execute the request - Waiting ... ");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nextInstance.getAndIncrement();
            nextInstance.set(nextInstance.get() % instances.size());
            instance  = instances.get(nextInstance.get());
        }
        logger.warning("Next instance is " + nextInstance.get());
        return instance;
    }

    public void shutDown() {
        for (InstanceProxy instance: instances) {
            if (instance.status.equals(InstanceStatus.STARTED)) {
                instance.startShutDown();
            }
        }
    }

    public void shutDownInstanceWithLeastLoad() {
        if (instances.size() == AutoScaler.DECREASE.getNumberOfWorkers()) {
            return;
        }
        Collections.sort(instances, InstanceProxy.LOAD_COMPARATOR);
        int index = instances.size() - 1;
        while (instances.get(index).status.equals(InstanceStatus.STARTING)) {
            index = (index - 1) % instances.size();
        }
        InstanceProxy instance = instances.get(index);
        instance.startShutDown();
        logger.info("The instance " + instance.getAddress() + " is being shutdown");
    }

}
