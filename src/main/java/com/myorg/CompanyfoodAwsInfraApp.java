package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class CompanyfoodAwsInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        //CRIANDO A VPC
        var vpcStack = new CompanyfoodAwsVpcStack(app, "VpcId");

        //CRIANDO O CLUSTER
        var clusterStack = new CompanyfoodAwsClusterStack(app, "ClusterId", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack); // cluster depende de VPC criada

        //Criando a stack do MongoDB
        var mongoStack = new CompanyfoodAwsMongoEC2Stack(app, "MongoStackId", vpcStack.getVpc());
        mongoStack.addDependency(vpcStack); // Mongo depende da VPC

        //Criando a instância do RDS
        CompanyfoodAwsRDSStack rdsStack = new CompanyfoodAwsRDSStack(app, "RdsStackId", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack); // RDS depende da VPC

        //Criando a stack de serviço(s) após o MongoDB e/ou RDS.
        var companyServiceStack = new CompanyfoodAwsServiceStack(app, "ServiceId", clusterStack.getCluster());
        companyServiceStack.addDependency(clusterStack); //Serviço depende do cluster
        //companyServiceStack.addDependency(rdsStack);     //Serviço depende do banco
        companyServiceStack.addDependency(mongoStack);   //Serviço depende do banco


        app.synth();
    }
}

