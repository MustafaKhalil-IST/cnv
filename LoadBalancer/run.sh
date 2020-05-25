echo "You might need to update aws sdk version in this script .. "
java -cp /home/ec2-user/cnv/LoadBalancer/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/lib/aws-java-sdk-1.11.786.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/third-party/lib/*  -XX:-UseSplitVerifier LoadBalancer
