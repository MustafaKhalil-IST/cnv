package src.loadbalancer;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import storage.dynamo.Request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class LoadBalanceHandler implements HttpHandler {
    private static Logger logger = Logger.getLogger(LoadBalanceHandler.class.getName());
    private static HashMap<HttpExchange , Request> queries = new HashMap<>();
    public static ArrayList<String> instanceIP = new ArrayList<String>();
    private static ArrayList<String> instanceIds = new ArrayList<String>();
    private static HashMap<String, Long> currentComplexity = new HashMap<>();

    static AmazonEC2 ec2;


    public LoadBalanceHandler(){
        super();
        init();
    }

    private void init(){
        // instanceIP.add("localhost");
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

    @Override
    public void handle(HttpExchange t) throws IOException {
        Request request = getRequestFromQuery(t.getRequestURI().getQuery());
        queries.put(t, request);
        long complexity  = estimateComplexity(request);
        byte[] buffer = redirectAndProcessRequestByWorker(request, complexity);
        if (buffer != null) {
            t.sendResponseHeaders(200, buffer.length);
            OutputStream outputStream = t.getResponseBody();
            outputStream.write(buffer);
            outputStream.close();
        } else {
            System.out.println("empty buffer");
        }
    }

    private byte[] redirectAndProcessRequestByWorker(Request request, long complexity) {
        HttpURLConnection connection = null;
        try {
            InstanceProxy instance = InstancesManager.getSingleton().getRandomInstance(); // TODO
            instance.addRequest(request, complexity);

            URL url = new URL("http://" + instance.getAddress() + "/sudoku?" + request.getQuery());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);

            DataInputStream is = new DataInputStream((connection.getInputStream()));
            byte[] buffer = new byte[connection.getContentLength()];
            is.readFully(buffer);

            instance.processRequest(request);
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

    private long estimateComplexity(Request request) {
        // TODO get all requests with the same thump and calc avg
        /*
        long estimate = EstimationsStore.getStore().requestEstimation(request);
        Store.getStore().storeEstimate(request, estimate);
        return estimate;
        */
        return 0;
    }
}
