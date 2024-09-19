package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Collections;

public class CompanyfoodAwsRDSStack extends Stack {

    public CompanyfoodAwsRDSStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public CompanyfoodAwsRDSStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        //CFN Parameter: é um parâmetro do cloudFormation p/ se entrar com informações na hora que for realizar o deploy
        //CFN parameter irá construir a solicitação de senha para realização do deploy na base de dados RDS
        CfnParameter password = CfnParameter.Builder.create(this, "password")
                .type("String")
                .description("Password of the payment-ms database")
                .build();

        //Código para inclusão do Security Group
        ISecurityGroup iSecurityGroup = SecurityGroup
                .fromSecurityGroupId(this, "MySecurityGroup", vpc.getVpcDefaultSecurityGroup());
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306));

        //Criação da instância RDS
        DatabaseInstance databaseInstanceRDS = DatabaseInstance.Builder
                .create(this, "payment-rds")
                .instanceIdentifier("companyfood-aws-payment-db")
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder()
                        .version(MysqlEngineVersion.VER_8_0_35)
                        .build()))
                .vpc(vpc)
                .credentials(Credentials.fromUsername("admin",
                        CredentialsFromUsernameOptions.builder()
                                .password(SecretValue.unsafePlainText(password.getValueAsString()))
                                .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .multiAz(false)
                .allocatedStorage(10)
                .securityGroups(Collections.singletonList(iSecurityGroup))
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(vpc.getPrivateSubnets())
                        .build())
                .build();

        CfnOutput.Builder.create(this, "payment-db-endpoint")
                .exportName("payment-db-endpoint")
                .value(databaseInstanceRDS.getDbInstanceEndpointAddress())
                .build();

        CfnOutput.Builder.create(this, "payment-db-password")
                .exportName("payment-db-password")
                .value(password.getValueAsString())
                .build();
    }
}
