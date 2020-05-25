package storage.dynamo;


import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DynamoStore {
    AmazonDynamoDB client;
    DynamoDBMapper mapper;
    Map<Long, Request> requestInformation = new HashMap<>();
    protected final long MINIMUM_UPDATE = 1000L;
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
            throw new RuntimeException("Credentials not found", e);
        }

        client = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build();

        mapper = new DynamoDBMapper(client);

        CreateTableRequest req = mapper.generateCreateTableRequest(Metrics.class);

        req.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));

        TableUtils.createTableIfNotExists(client, req);
        try {
            TableUtils.waitUntilActive(client, Metrics.TABLE);
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
        }

    }

    public void setRequestInformation(long threadID, Request request) {
        this.requestInformation.put(threadID, request);
    }

    public Request getRequestInformation(long threadID) {
        return this.requestInformation.get(threadID);
    }

    public void updateCallsCount(long threadID, long currentMethodCount) {
        if ((currentMethodCount % MINIMUM_UPDATE) == 0) {
	    System.out.println("[DEBUG]: UPDATE");
            Request request = getRequestInformation(threadID);
            Metrics requestMetrics = mapper.load(Metrics.class, request.getRequestID());
            if (requestMetrics == null) {
                requestMetrics = new Metrics(request);
            } else {
                long currentMethods = requestMetrics.getCurrentNumberOfCalls();
                currentMethods += MINIMUM_UPDATE;
                requestMetrics.setCurrentNumberOfCalls(currentMethods);
            }
            mapper.save(requestMetrics);
        }
    }

    public void storeCallsCount(long threadID, long methodCount) {
        Request request = getRequestInformation(threadID);
        Metrics metrics = mapper.load(Metrics.class, request.getRequestID());
        if (metrics == null) {
            metrics = new Metrics(request);
        }
        metrics.setNumberOfCalls(methodCount);
        mapper.save(metrics);
    }

    public PaginatedScanList<Metrics> getRequestMetricsToProcess() {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":zero", new AttributeValue().withN(String.valueOf(0)));
        return mapper.scan(Metrics.class, new DynamoDBScanExpression()
                .withFilterExpression("numberOfCalls > :zero")
                .withExpressionAttributeValues(values));
    }

    public void deleteMetric(Metrics metrics) {
        mapper.delete(metrics);
    }
}
