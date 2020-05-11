package estimation;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.logging.Logger;

public class EstimationsStore {
    AmazonDynamoDB client;
    DynamoDBMapper mapper;
    private static final double TOLERANCE = 0.1;
    private static Logger logger = Logger.getLogger(EstimationsStore.class.getName());
    private static EstimationsStore instance = null;

    private EstimationsStore() {
        init();
    }

    public static synchronized EstimationsStore getStore() {
        if (instance == null) {
            instance = new EstimationsStore();
        }
        return instance;
    }

    public void init() {
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        } catch (Exception e) {
            throw new RuntimeException("Error loading credentials", e);
        }

        client = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build();

        mapper = new DynamoDBMapper(client);
        CreateTableRequest req = mapper.generateCreateTableRequest(StoredEstimation.class);
        req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        TableUtils.createTableIfNotExists(client, req);
        try {
            TableUtils.waitUntilActive(client, StoredEstimation.TABLE);
        } catch (InterruptedException e) {
            logger.warning("Could not wait for table to be active.");
            logger.warning(e.getMessage());
        }
    }

    public void storeEstimate(StoredEstimation estimation) {
        mapper.save(estimation);
    }

}
