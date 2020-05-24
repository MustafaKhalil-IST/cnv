# Cloud Computation and Virtualization
### We Are Group 13


## How to run?
### In Worker instances
Create An AWS instance and connect to it using a ssh client (like PUTTY ...)
1-  ```sh    
    $ sudo yum install java-1.7.0-openjdk-devel.x86_64 
    ```
2-  ```sh    
    $ sudo yum install git 
    ```
3-  ```sh    
    $ git clone https://github.com/MustafaKhalil-IST/cnv
    ```
4-  ```sh    
    $ cd ../../~root;   
    ```
5- ```sh    
    $ mkdir .aws  
    ```
6- ```sh
    $ nano .aws/credentials
    ```  
and paste and replace with your keys
[default]

aws_access_key_id=<aws_access_key_id>

aws_secret_access_key=<aws_secret_access_key>
7-  ```sh
    $ mkdir cnv/libs
    ```
8-  ```sh
    $ cd cnv/libs
    ```
9-  ```sh
    $ wget http://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip 
    ```
10- ```sh
    $ unzip aws-java-sdk.zip 
    ```
11- ```sh
    $ rm aws-java-sdk.zip 
    ```
12- ```sh
    $ cd ~/cnv/MetricsStorage/store
    ```
13- [Replace XXX with the downloaded version number, e.g 786] 
```sh
$ javac -cp ~/cnv/libs/aws-java-sdk-1.11.XXX/lib/aws-java-sdk-1.11.XXX.jar:~/cnv/libs/aws-java-sdk-1.11.XXX/third-party/lib/*:. *.java
```

14- ```sh
    $ cd /home/ec2-user/cnv/
    ```
15- ```sh
    $ javac -cp ~/cnv/MetricsStorage:/home/ec2-user/cnv/lib/BIT/BIT/ Instrumentation.java
    ```
16- ```sh
    $ mkdir /instrumented/; mkdir /instrumented/pt/; mkdir /instrumented/pt/ulisboa/; mkdir /instrumented/pt/ulisboa/tecnico/; mkdir /instrumented/pt/ulisboa/tecnico/cnv/; mkdir /instrumented/pt/ulisboa/tecnico/cnv/solver/;
    ```

17-  ```sh
     $ java -cp /home/ec2-user/cnv/lib/BIT/BIT/:/home/ec2-user/cnv/lib/BIT/BIT/samples/:/home/ec2-user/cnv/ -XX:-UseSplitVerifier Instrumentation project/pt/ulisboa/tecnico/cnv/solver/ instrumented/pt/ulisboa/tecnico/cnv/solver/
     ```

18- [NOT IMPORTANT] You can try this execution to make sure that everything work well
```sh
$ java -cp /home/ec2-user/cnv/instrumented/:/home/ec2-user/cnv/project/:/home/ec2-user/cnv/lib/BIT/BIT/:/home/ec2-user/cnv/lib/BIT/BIT/samples/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.784/lib/aws-java-sdk-1.11.784.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.784/third-party/lib/*:/home/ec2-user/cnv/ -XX:-UseSplitVerifier pt.ulisboa.tecnico.cnv.solver.SolverMain -d -n1 9 -n2 9 -un 81 -i SUDOKU_PUZZLE_9x19_101 -s DLX -b [[2,0,0,8,0,5,0,9,1],[9,0,8,0,7,1,2,0,6],[0,1,4,2,0,3,7,5,8],[5,0,1,0,8,7,9,2,4],[0,4,9,6,0,2,0,8,7],[7,0,2,1,4,9,3,0,5],[1,3,7,5,0,6,0,4,9],[4,2,5,0,1,8,6,0,3],[0,9,6,7,3,4,0,1,2]]
```
19- Run the server with this command:  
    ```sh
    $ java -cp /home/ec2-user/cnv/instrumented/:/home/ec2-user/cnv/project/:/home/ec2-user/cnv/lib/BIT/BIT/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.XXX/lib/aws-java-sdk-1.11.XXX.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.XXX/third-party/lib/*:/home/ec2-user/cnv/ -XX:-UseSplitVerifier pt.ulisboa.tecnico.cnv.server.WebServer
    ```
20- ```sh
    $ sudo chmod +x /etc/rc.local
    ```
21- ```sh
    $ sudo nano /etc/rc.local/
    ```
paste 
```sh
$ java -cp /home/ec2-user/cnv/instrumented/:/home/ec2-user/cnv/project/:/home/ec2-user/cnv/lib/BIT/BIT/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.XXX/lib/aws-java-sdk-1.11.XXX.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.XXX/third-party/lib/*:/home/ec2-user/cnv/ -XX:-UseSplitVerifier pt.ulisboa.tecnico.cnv.server.WebServer
```
and close it.
22- stop the instance then start it
23- Create an image of the instance


### In Load Balancer Instance

24- ```sh
        $ sudo yum install java-1.7.0-openjdk-devel.x86_64
        ```
25- ```sh
        $ sudo yum install git
        ```
26- ```sh
        $ git clone https://github.com/MustafaKhalil-IST/cnv
        ```
27- ```sh
        $ cd ../../~root;
        ```
28- ```sh
        $ mkdir .aws
        ```
29- ```sh
        $ nano .aws/credentials
        ```
30- paste 

[default]

aws_access_key_id=<aws_access_key_id>

aws_secret_access_key=<aws_secret_access_key>

31- ```sh
        $ cd /home/ec2-user/cnv/LoadBalancer/
        ```
32- ```sh
        $ sh clean_compile.sh
        ```
33- Run the server: 
```sh
$ java -cp /home/ec2-user/cnv/LoadBalancer/:/home/ec2-user/cnv/MetricsStorage/:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/lib/aws-java-sdk-1.11.786.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.786/third-party/lib/*  -XX:-UseSplitVerifier LoadBalancer
```

34- Create an image of the instance
35- You can change the configuration from the properties file.

### Development
1- Instrumentation Java Class: make your changes then compile it with 
```sh
$ javac -cp ~/cnv/MetricsStorage:/home/ec2-user/cnv/lib/BIT/BIT/ Instrumentation.java
```
Then you will need to reinstrument the files in project package using:
 ```sh
$ java -cp /home/ec2-user/cnv/BIT/BIT/:/home/ec2-user/cnv/BIT/BIT/samples/:/home/ec2-user/cnv/ -XX:-UseSplitVerifier Instrumentation project/pt/ulisboa/tecnico/cnv/solver/ instrumented/pt/ulisboa/tecnico/cnv/solver/
```

2- MetricsStorage package: make your changes then compile it with
```sh
$ javac -cp /home/ec2-user/cnv/libs/aws-java-sdk-1.11.XXX/lib/aws-java-sdk-1.11.XXX.jar:/home/ec2-user/cnv/libs/aws-java-sdk-1.11.XXX/third-party/lib/*:/home/ec2-user/cnv/ /home/ec2-user/cnv/MetricsStorage/storage/dynamo/*.java
```

3- LoadBalancer: make your changes then run:
```sh
$ sh /home/ec2-user/cnv/LoadBalancer/clean_compile.sh
```

PS: w e had some problems when trying to produce jars, so we used this way of using classes.
