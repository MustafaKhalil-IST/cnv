echo "update xxx version of aws sdk"
javac -cp ~/cnv/lib/aws-java-sdk-1.11.XXX/lib/aws-java-sdk-1.11.XXX.jar:~/cnv/lib/aws-java-sdk-1.11.XXX/third-party/lib/*:. /home/ec2-user/MetricsStorage/storage/dynamo/*.java

javac -cp ~/cnv/MetricsStorage:/home/ec2-user/cnv/lib/BIT/BIT/ /home/ec2-user/cnv/Instrumentation.java
java -cp /home/ec2-user/cnv/lib/BIT/BIT/:/home/ec2-user/cnv/lib/BIT/BIT/samples/:/home/ec2-user/cnv/ -XX:-UseSplitVerifier Instrumentation project/pt/ulisboa/tecnico/cnv/solver/ instrumented/pt/ulisboa/tecnico/cnv/solver/

