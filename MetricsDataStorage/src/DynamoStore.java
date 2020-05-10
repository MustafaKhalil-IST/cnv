import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class DynamoStore {

    Map<Long, Request> requestInformation = new HashMap<>();
    AmazonDynamoDB client;
    DynamoDBMapper mapper;
    private static Logger logger = Logger.getLogger(DynamoStore.class.getName());

    public DynamoStore() {
        init();
    }

    public void init() {
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }
        catch (Exception e) {
            throw new RuntimeException("Error loading credentials", e);
        }

        client = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build();
        mapper = new DynamoDBMapper(client);
        CreateTableRequest req = mapper.generateCreateTableRequest(RequestMetrics.class);
        req.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
        TableUtils.createTableIfNotExists(client, req);
        try {
            TableUtils.waitUntilActive(client, RequestMetrics.TABLE);
        } catch (InterruptedException e) {
            logger.warning("Could not wait for table to be active.");
            logger.warning(e.getMessage());
        }

    }

    public void setRequestInformation(long threadID, Request request) {
        this.requestInformation.put(threadID, request);
    }

    public Request getRequestInformation(long threadID) {
        return this.requestInformation.get(threadID);
    }

    public void updateMetric(long threadID, long metric) {
        Request request = getRequestInformation(threadID);
        RequestMetrics requestMetrics = mapper.load(RequestMetrics.class, request.getRequestID());
        if (requestMetrics == null) {
            requestMetrics = new RequestMetrics(request);
        } else {
            requestMetrics.setMetric(metric);
        }
        mapper.save(requestMetrics);
    }

    public void storeMetric(long threadID, long metric) {
        Request request = getRequestInformation(threadID);
        RequestMetrics requestMetrics = mapper.load(RequestMetrics.class, request.getRequestID());
        if (requestMetrics == null) {
            requestMetrics = new RequestMetrics(request);
        }
        requestMetrics.setMetric(metric);
        mapper.save(requestMetrics);
    }

    public void deleteMetric(RequestMetrics request) {
        mapper.delete(request);
    }
}
