import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LoadBalancer  implements Runnable{
    private final static Logger logger = Logger.getLogger(LoadBalancer.class.getName());
    private final static int PORT    = Integer.getInteger("balancer.port", 8181);

    @Override
    public void run() {
        try {
            ExecutorService executor = Executors.newCachedThreadPool();

            HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            logger.info("Creating LoadBalancer at port: " + PORT);
            // httpServer.createContext(RENDER_ROUTE, handler);
            // httpServer.createContext("/register", registerWorkerHandler);
            // logger.info("Setup route: " + RENDER_ROUTE + " with handler " + LoadBalancerHandler.class.getName());
            httpServer.setExecutor(executor);
            httpServer.start();
            logger.info("Started loadbalancer.LoadBalancer!");

            InstancesManager.getInstance().start();

            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception e) {
                    logger.warning("There was an exception waiting for shutdown, check the stacktrace for more errors.");
                    logger.warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException t) {
            logger.warning("There was an exception handling, check the stacktrace for more errors.");
            logger.warning(t.getMessage());
            t.printStackTrace();
        }
    }
}
