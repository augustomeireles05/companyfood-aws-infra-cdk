package com.myorg;

import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class CompanyfoodAwsServiceStack extends Stack {

    public CompanyfoodAwsServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public CompanyfoodAwsServiceStack(final Construct scope, final String id,
                                      final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        Map<String, String> authentication = new HashMap<>();

        /*

        //Configuração de credenciais para RDS:
        authentication.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" +
                Fn.importValue("payment-db-endpoint" +
                        ":3306/foodcompanyorder?createDatabaseIfNotExist=true"));
        authentication.put("SPRING_DATASOURCE_USERNAME", "admin");
        authentication.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("payment-db-password"));

         */

        //Configurações de credenciais do MongoDB
        authentication.put(
                "SPRING_DATA_MONGODB_URI", "mongodb://" +
                Fn.importValue("mongodb-instance-endpoint") +
                ":27017/foodcompanyorder");
        authentication.put("SPRING_DATA_MONGODB_USERNAME", "admin");
        authentication.put("SPRING_DATA_MONGODB_PASSWORD", Fn.importValue("mongodb-password"));

        // Create a load-balanced Fargate service and make it public
        ApplicationLoadBalancedFargateService.Builder.create(this, "CompanyFargateService")
                .serviceName("company-fargate-service")
                .cluster(cluster)           // Required
                .cpu(512)                   // Default is 256
                .desiredCount(2)            // Default is 1
                .assignPublicIp(true)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("augustomeireles/order-ms")) //Image do docker hub
                                .containerPort(8080)                      //Porta do container
                                .containerName("order-ms-container")      //Nome do container
                                .environment(authentication)              //Variáveis de ambiente
                                .build())
                .memoryLimitMiB(1024)       // Default is 512
                .publicLoadBalancer(true)   // Default is true
                .build();


    }
}
