package src.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class InstanceCreationhandler implements HttpHandler {
    private static Logger logger = Logger.getLogger(InstanceCreationhandler.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String ip = "";
        String id = "";
        String[] params = httpExchange.getRequestURI().getQuery().split("&");
        for(String p: params) {
            String[] param = p.split("=");
            if (param[0].equals("ip")) {
                ip = param[1];
            }
            if (param[0].equals("id")) {
                id = param[1];
            }
        }
        InstanceProxy instance = new InstanceProxy(ip, id);
        InstancesManager.getInstance().addInstance(instance);
        logger.info("added instance - " + instance.getAddress());
        String response = "Succeed";
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
