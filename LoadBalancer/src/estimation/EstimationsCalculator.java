package src.estimation;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import storage.dynamo.Metrics;
import storage.dynamo.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class EstimationsCalculator {
    AmazonDynamoDB client;
    DynamoDBMapper mapper;
    private static Logger logger = Logger.getLogger(EstimationsCalculator.class.getName());
    private static EstimationsCalculator instance = null;

    private EstimationsCalculator() {
        init();
    }

    public static synchronized EstimationsCalculator getInstance() {
        if (instance == null) {
            instance = new EstimationsCalculator();
        }
        return instance;
    }

    public void init() {
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        } catch (Exception e) {
            throw new RuntimeException("Credentials not found or not correct!", e);
        }

        client = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build();

        mapper = new DynamoDBMapper(client);
    }

    public long calculateEstimatedCost(Request request) {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":zero", new AttributeValue().withN(String.valueOf(0)));
        PaginatedScanList<Metrics> reads =  mapper.scan(Metrics.class, new DynamoDBScanExpression()
                .withFilterExpression("numberOfCalls > :zero")
                .withExpressionAttributeValues(values));
        double cost = 0;
        double cnt = 0;
        for(Metrics read: reads) {
            if(read.getRequest().getDimension().equals(request.getDimension()) &&
                    Math.abs(read.getRequest().getMissed() - request.getMissed()) < 5 &&
                    read.getRequest().getStrategy().equals(request.getStrategy())) {
                cost += read.getNumberOfCalls();
                cnt += 1;
            }
        }
        long res;
        if (cnt == 0) {
            res = 1000; // TODO
        } else {
            res = Math.round(cost / cnt);
        }
        logger.info("estimated cost for " + request.getQuery() + " is " + res);
        return res;
    }

}
