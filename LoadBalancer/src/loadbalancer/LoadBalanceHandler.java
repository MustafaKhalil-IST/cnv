package src.loadbalancer;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import src.estimation.EstimationsCalculator;
import storage.dynamo.Request;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class LoadBalanceHandler implements HttpHandler {
    private static Logger logger = Logger.getLogger(LoadBalanceHandler.class.getName());
    static AmazonEC2 ec2;

    public LoadBalanceHandler(){
        super();
        init();
    }

    private void init(){
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }
        catch (Exception e) {
            throw new RuntimeException("Credentials not found or not correct", e);
        }
        ec2 = AmazonEC2ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(credentialsProvider)
                .build();
        logger.info("Load Balancer Handler is initialized");
    }

    private Request getRequestFromQuery(String query) {
        String[] queries = query.split("&");
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

    private static String parseRequestBody(InputStream is) throws IOException {
        InputStreamReader isr =  new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }

        br.close();
        isr.close();

        return buf.toString();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        logger.info("A request " + t.getRequestURI().getQuery() + " has been received");
        Request request = getRequestFromQuery(t.getRequestURI().getQuery());
        String body = parseRequestBody(t.getRequestBody());
        long cost  = estimateRequestCost(request);
        String buffer = redirectAndProcessRequestByWorker(request, cost, body);
        if (buffer != null) {
            logger.info("The response is " + buffer);

            final Headers hdrs = t.getResponseHeaders();

            hdrs.add("Content-Type", "application/json");

            hdrs.add("Access-Control-Allow-Origin", "*");

            hdrs.add("Access-Control-Allow-Credentials", "true");
            hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
            hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

            t.sendResponseHeaders(200, buffer.length());

            final OutputStream os = t.getResponseBody();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);

            osw.write(buffer);
            osw.flush();
            osw.close();
            os.close();

            logger.info("> Sent response to " + t.getRemoteAddress().toString());

        } else {
            logger.warning("empty buffer");
        }
    }

    private String redirectAndProcessRequestByWorker(Request request, long cost, String body) {
        HttpURLConnection connection = null;
        try {
            InstanceProxy instance = InstancesManager.getSingleton().getBestInstance(cost);
            instance.addRequest(request, cost);

            logger.info("The request will be redirected to: " + instance.getAddress());
            URL url = new URL("http://" + instance.getAddress() + "/sudoku?" + request.getQuery());
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", "application/" + "POST");
            if (body != null) {
                connection.setRequestProperty("Content-Length", Integer.toString(body.length()));
                try {
                    connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                } catch (ConnectException ce) {
                    logger.warning(ce.getMessage());
                    Thread.sleep(5000); // TODO
                    connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);
            String buffer = in.readLine();
            in.close();

            instance.updateInstanceLoad(request);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private long estimateRequestCost(Request request) {
        return EstimationsCalculator.getInstance().calculateEstimatedCost(request);
    }
}
