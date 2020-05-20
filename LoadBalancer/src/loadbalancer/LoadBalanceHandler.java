package src.loadbalancer;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import storage.dynamo.Request;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
        InputStreamReader isr =  new InputStreamReader(is,"utf-8");
        BufferedReader br = new BufferedReader(isr);

        // From now on, the right way of moving from bytes to utf-8 characters:

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
        queries.put(t, request);
        long complexity  = estimateComplexity(request);
        byte[] buffer = redirectAndProcessRequestByWorker(request, complexity, body);
        if (buffer != null) {
            logger.info("The response is " + Arrays.toString(buffer));

            final Headers hdrs = t.getResponseHeaders();

            hdrs.add("Content-Type", "application/json");

            hdrs.add("Access-Control-Allow-Origin", "*");

            hdrs.add("Access-Control-Allow-Credentials", "true");
            hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
            hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

            logger.info("headers are set");

            t.sendResponseHeaders(200, buffer.length);

            logger.info("response start to send");

            final OutputStream os = t.getResponseBody();

            logger.info("response start to send");

            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

            logger.info("osw");

            osw.write(Arrays.toString(buffer));

            logger.info("written");
            osw.flush();

            logger.info("flush");
            osw.close();

            os.close();

            logger.info("response is written");

            logger.info("> Sent response to " + t.getRemoteAddress().toString());

        } else {
            logger.warning("empty buffer");
        }
    }

    private byte[] redirectAndProcessRequestByWorker(Request request, long complexity, String body) {
        HttpURLConnection connection = null;
        try {
            InstanceProxy instance = InstancesManager.getSingleton().getRandomInstance(); // TODO
            instance.addRequest(request, complexity);

            logger.info("The request will be redirected to: " + instance.getAddress());
            URL url = new URL("http://" + instance.getAddress() + "/sudoku?" + request.getQuery());
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", "application/" + "POST");
            if (body != null) {
                connection.setRequestProperty("Content-Length", Integer.toString(body.length()));
                connection.getOutputStream().write(body.getBytes("UTF8"));
            }


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
