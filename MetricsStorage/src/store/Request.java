package store;

import java.util.logging.Logger;

public class Request {
    private static Logger logger = Logger.getLogger(Request.class.getName());
    String requestID;
    Integer dimension;
    Integer missed;
    String strategy;

    public Request() {
    }

    public Request(String requestID, Integer dimension, Integer missed, String strategy) {
        this.requestID = requestID;
        this.dimension = dimension;
        this.missed = missed;
        this.strategy = strategy;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public Integer getMissed() {
        return missed;
    }

    public void setMissed(Integer missed) {
        this.missed = missed;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}


