import com.sun.net.httpserver.HttpServer;
import loadbalancer.InstanceCreationhandler;
import loadbalancer.InstancesManager;
import loadbalancer.LoadBalanceHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LoadBalancer  implements Runnable{
    private final static Logger logger = Logger.getLogger(LoadBalancer.class.getName());
    private final static int PORT    = Integer.getInteger("balancer.port", 8181);
    private LoadBalanceHandler loadBalancerHandler = new LoadBalanceHandler();
    private InstanceCreationhandler instanceCreationhandler = new InstanceCreationhandler();

    @Override
    public void run() {
        try {
            ExecutorService executor = Executors.newCachedThreadPool();

            HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/sudoku", loadBalancerHandler);
            httpServer.createContext("/instances", instanceCreationhandler);

            httpServer.setExecutor(executor);
            httpServer.start();

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
