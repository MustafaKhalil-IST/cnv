cd /home/ec2-user/cnv/LoadBalancer/src/properties/;
rm *.class;
cd /home/ec2-user/cnv/LoadBalancer/src/loadbalancer/;
rm *.class;
cd /home/ec2-user/cnv/LoadBalancer/src/estimation/;
rm *.class;
cd /home/ec2-user/cnv/LoadBalancer/src/autoscaler/;
rm *.class;

cd /home/ec2-user/cnv/LoadBalancer/src/properties/;
javac -cp /home/ec2-user/cnv/LoadBalancer/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/lib/aws-java-sdk-1.11.786.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/third-party/lib/* *.java
cd /home/ec2-user/cnv/LoadBalancer/src/estimation/;
javac -cp /home/ec2-user/cnv/LoadBalancer/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/lib/aws-java-sdk-1.11.786.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/third-party/lib/* *.java
cd /home/ec2-user/cnv/LoadBalancer/src/autoscaler/;
javac -cp /home/ec2-user/cnv/LoadBalancer/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/lib/aws-java-sdk-1.11.786.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/third-party/lib/* *.java
cd /home/ec2-user/cnv/LoadBalancer/src/loadbalancer/;
javac -cp /home/ec2-user/cnv/LoadBalancer/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/lib/aws-java-sdk-1.11.786.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/third-party/lib/* *.java
cd /home/ec2-user/cnv/LoadBalancer/;
javac -cp /home/ec2-user/cnv/LoadBalancer/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/lib/aws-java-sdk-1.11.786.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/third-party/lib/* *.java