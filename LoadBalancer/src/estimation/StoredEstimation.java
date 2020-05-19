package estimation;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import store.dynamo.Request;

@DynamoDBTable(tableName = StoredEstimation.TABLE)
public class StoredEstimation {
    public static final String TABLE = "Estimations";
    private Request request;

    public StoredEstimation() {

    }

    public StoredEstimation(Request request) {
        this.request = request;
    }

    @DynamoDBAttribute(attributeName = "request")
    public Request getRequest() {
        return request;
    }

    @DynamoDBAttribute(attributeName = "request")
    public void setRequest(Request request) {
        this.request = request;
    }
}
