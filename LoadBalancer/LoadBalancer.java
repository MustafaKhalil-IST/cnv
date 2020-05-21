import src.autoscaler.AutoScaler;
import com.sun.net.httpserver.HttpServer;
import src.loadbalancer.InstancesManager;
import src.loadbalancer.LoadBalanceHandler;
import src.properties.PropertiesReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LoadBalancer  implements Runnable {
    private final static Logger logger = Logger.getLogger(LoadBalancer.class.getName());
    private final static int PORT = Integer.parseInt(PropertiesReader.getInstance().getProperty("load-balance.port"));
    private final static LoadBalancer balancer = new LoadBalancer();
    private LoadBalanceHandler loadBalanceHandler = new LoadBalanceHandler();
    private HttpServer httpServer;

    @Override
    public void run() {
        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/sudoku", loadBalanceHandler);

            httpServer.setExecutor(executor);
            httpServer.start();

            InstancesManager.getSingleton().start();

            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception e) {
                    logger.warning("Error !!");
                    logger.warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException t) {
            logger.warning("Server error !!");
            logger.warning(t.getMessage());
            t.printStackTrace();
        }
    }

    static void shutdown() {
        try {
            logger.info("Shutting down the Load Balancer!");
            balancer.httpServer.stop(0);
        } catch (Exception e) {
            logger.warning("Error while Shutting down !!");
            logger.warning(e.getMessage());
            e.printStackTrace();
        } finally {
            logger.info("Server shut down!");
        }

        synchronized (balancer) {
            balancer.notifyAll();
        }
    }

    public static void main(String[] args) {
        Thread balancerThread = new Thread(new LoadBalancer());
        balancerThread.start();
        Thread autoScalerThread = new Thread(new AutoScaler());
        autoScalerThread.start();
        Runtime.getRuntime().addShutdownHook(new OnShutdown());
        new Shutdown().start();
        try {
            balancerThread.join();
            autoScalerThread.interrupt();
            logger.info("Load Balancer has been terminated");
        } catch (Exception e) {
            logger.warning("Load Balancer Exception");
            logger.warning(e.getMessage());
        }
    }

}


class OnShutdown extends Thread {
    public void run() {
        LoadBalancer.shutdown();
    }
}

class Shutdown extends Thread {
    public void run() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InstancesManager.getSingleton().shutDown();
        LoadBalancer.shutdown();
        Runtime.getRuntime().exit(0);
    }
}
