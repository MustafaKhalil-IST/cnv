import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

@DynamoDBTable(tableName = RequestMetrics.TABLE)
public class RequestMetrics {
    public static final String TABLE = "DynamoStore";
    private String requestID;
    private Request request;
    private long metric;

    public RequestMetrics() {

    }

    public RequestMetrics(Request request) {
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

    @DynamoDBTypeConverted(converter = QueryTypeConverter.class)
    @DynamoDBAttribute(attributeName = "request")
    public Request getRequest() {
        return request;
    }

    @DynamoDBAttribute(attributeName = "request")
    public void setRequest(Request request) {
        this.request = request;
    }

    @DynamoDBAttribute(attributeName = "metric")
    public long getMetric() {
        return metric;
    }

    public void setMetric(long metric) {
        this.metric = metric;
    }

}
