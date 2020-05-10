import java.util.logging.Logger;

public class Request {
    private static Logger logger = Logger.getLogger(Request.class.getName());
    String requestID;
    int dim;
    int missed;
    String strategy;

    public Request () {
    }

    public Request(String requestID, int dim, int missed, String strategy) {
        this.requestID = requestID;
        this.dim = dim;
        this.missed = missed;
        this.strategy = strategy;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public int getMissed() {
        return missed;
    }

    public void setMissed(int missed) {
        this.missed = missed;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
