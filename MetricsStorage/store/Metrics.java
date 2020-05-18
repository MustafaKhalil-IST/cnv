package store;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

@DynamoDBTable(tableName = Metrics.TABLE)
public class Metrics {
    public static final String TABLE = "Metrics";

    private String requestID;
    private Request request;
    private long numberOfCalls;
    private long estimatedNumberOfCalls;
    private long currentNumberOfCalls;

    public Metrics() {
    }

    public Metrics(Request request) {
        this.requestID = request.getRequestID();
        this.request = request;
    }

    @DynamoDBHashKey(attributeName = "requestID")
    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    @DynamoDBTypeConverted(converter = RequestConverter.class)
    @DynamoDBAttribute(attributeName = "request")
    public Request getRequest() {
        return request;
    }


    @DynamoDBAttribute(attributeName = "request")
    public void setRequest(Request request) {
        this.request = request;
    }

    @DynamoDBAttribute(attributeName = "numberOfCalls")
    public long getNumberOfCalls() {
        return numberOfCalls;
    }

    public void setNumberOfCalls(long numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    @DynamoDBAttribute(attributeName = "estimatedNumberOfCalls")
    public long getEstimatedNumberOfCalls() {
        return estimatedNumberOfCalls;
    }

    public void setEstimatedNumberOfCalls(long estimatedNumberOfCalls) {
        this.estimatedNumberOfCalls = estimatedNumberOfCalls;
    }

    @DynamoDBAttribute(attributeName = "currentNumberOfCalls")
    public long getCurrentNumberOfCalls() {
        return currentNumberOfCalls;
    }

    public void setCurrentNumberOfCalls(long currentNumberOfCalls) {
        this.currentNumberOfCalls = currentNumberOfCalls;
    }
}
