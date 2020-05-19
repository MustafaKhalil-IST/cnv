package store.dynamo;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import java.util.logging.Logger;
import java.util.UUID;

public class RequestConverter implements DynamoDBTypeConverter<String, Request> {

    private static Logger logger = Logger.getLogger(RequestConverter.class.getName());

    @Override
    public String convert(Request object) {
        Request request = object;
        String requestString = null;
        try {
            if (request != null) {
                requestString = request.getQuery();
            }
        }
        catch (Exception e) {
            logger.warning("Couldn't convert request to String.");
            logger.warning(e.getMessage());
        }
        return requestString;
    }

    @Override
    public Request unconvert(String s) {
        String[] queries = s.split("&");
        String strategy = "";
        int dimension = 0;
        int missed = 0;
        String puzzle = "";
        for (String p : queries) {
            String[] param = p.split("=");
            if (param[0].equals("s")) {
                strategy = param[1];
            }
            if (param[0].equals("un")) {
                missed = Integer.parseInt(param[1]);
            }
            if (param[0].equals("n1")) {
                dimension = Integer.parseInt(param[1]);
            }
            if (param[0].equals("i")) {
                puzzle = param[1];
            }
        }
        return new Request(UUID.randomUUID().toString(), dimension, missed, strategy, puzzle);
    }
}

