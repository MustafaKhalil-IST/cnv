package store;

import java.util.logging.Logger;

public class Request {
    private static Logger logger = Logger.getLogger(Request.class.getName());
    String requestID;
    Integer dimension;
    Integer missed;
    String strategy;
    String puzzle;

    public Request() {
    }

    public Request(String requestID, Integer dimension, Integer missed, String strategy, String puzzle) {
        this.requestID = requestID;
        this.dimension = dimension;
        this.missed = missed;
        this.strategy = strategy;
        this.puzzle = puzzle;
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

    public String getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(String puzzle) {
        this.puzzle = puzzle;
    }

    public String getQuery() {
        return "s={strategy}&un={missed}&n1={dimension}&n2={dimension}&i={puzzle}".replace("{strategy}", this.strategy)
                .replace("{missed}", this.missed.toString())
                .replace("{dimension}", this.dimension.toString())
                .replace("{puzzle}", this.puzzle);
    }
}


