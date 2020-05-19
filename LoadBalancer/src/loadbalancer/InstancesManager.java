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
    private static InstancesManager instance = new InstancesManager();
    private List<InstanceProxy> instances = new ArrayList<InstanceProxy>();

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

        queryForWorkers();
    }

    public void start() {
        init();
    }

    public static synchronized InstancesManager getInstance(){
        return instance;
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
            totalLoad += instance.getLoad();
        }
        return totalLoad / nrInstances;
    }

    public void createInstance(long complexity) {
        if (instances.size() < AutoScaler.UPSCALE.instances && complexity/InstanceProxy.MAX_LOAD > 0) {
            addInstance(InstanceProxy.requestNewWorker(ec2));
        }
    }

    // TODO
    public InstanceProxy getRandomInstance() {
        return instances.get(new Random().nextInt(instances.size()));
    }


    private void queryForWorkers() {
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        PropertiesReader reader = PropertiesReader.getInstance();
        for (Reservation reservation : reservations) {
            for(Instance instance : reservation.getInstances()){
                if (instance.getState().getName().equals(InstanceStateName.Running.toString()) &&
                        instance.getImageId().equals(reader.getStringProperty("render.image.id")) &&
                        instance.getInstanceType().equals(reader.getStringProperty("render.instance.type")) &&
                        instance.getIamInstanceProfile().getArn().equals(reader.getStringProperty("render.iam.role.arn"))) {

                    instances.add(new InstanceProxy(instance.getPublicIpAddress(), instance.getInstanceId()));

                    logger.info("Adding " + instance.getInstanceId() + " " + instance.getState().getName() + " " +
                            instance.getImageId() + " " +
                            instance.getInstanceType() + " " +
                            instance.getIamInstanceProfile().getArn());
                }
            }
        }
        if (instances.isEmpty()) {
            addInstance(InstanceProxy.requestNewWorker(ec2));
        }
    }

    public void shutDown() {
        for (InstanceProxy instance: instances) {
            if (instance.status.equals(InstanceStatus.ACTIVE)) {
                instance.startShutDown();
            }
        }
    }

    public void shutDownLaziestInstance() {
        if (instances.size() == AutoScaler.DOWNSCALE.instances) {
            return;
        }
        Collections.sort(instances, InstanceProxy.LOAD_COMPARATOR);
        int index = 0;
        InstanceProxy instance = instances.get(index);
        while (!instance.status.equals(InstanceStatus.ACTIVE)) {
            index++;
        }
        instance = instances.get(index);
        instance.startShutDown();
    }

}
