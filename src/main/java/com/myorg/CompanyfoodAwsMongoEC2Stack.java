package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.List;

public class CompanyfoodAwsMongoEC2Stack extends Stack {

    public CompanyfoodAwsMongoEC2Stack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public CompanyfoodAwsMongoEC2Stack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        //Definindo um grupo de segurança para permitir acesso ao MongoDB
        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "MySecurityGroup")
                .securityGroupName("MySecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        //Abrir a porta 27017 para o MongoDB
        securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(27017),
                "Allow MongoDB access"
        );

        //Importanto o par de chaves EC2 existente
        IKeyPair keyPair = software.amazon.awscdk.services.ec2.KeyPair
                .fromKeyPairName(this, "ExistingKeyPair", "foodcompany-ec2-key");

        //Criação da instância EC2 no Free Tier account access
        Instance mongoInstance = Instance.Builder.create(this, "MongoInstance")
                .instanceName("mongodb-instance")
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)) // t2.micro ou t3.micro
                .vpc(vpc)
                .machineImage(MachineImage.latestAmazonLinux2()) // Amazon linux free tier
                .securityGroup(securityGroup)
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(vpc.getPrivateSubnets())
                        .build())
                .blockDevices(List.of(BlockDevice.builder()
                        .deviceName("/dev/sdh")
                        .volume(BlockDeviceVolume.ebs(30)) // Volume EBS de 30 GB, dentro do Free Tier
                        .build()))
                .keyPair(keyPair)
                .userData(UserData.custom(
                "#!/bin/bash\n" +
                        "sudo yum update -y\n" +
                        "sudo yum install -y mongodb-org\n" +
                        "sudo systemctl start mongod\n" +
                        "sudo systemctl enable mongod\n" +
                        "mongo --eval 'db.createUser({user: \"admin\", pwd: \"mymongodbpassword\", roles: [\"root\"]})'\n"
                ))
                .build();

        //Exportando o endpoint do MongoDB (endereco IP da instância EC2)
        CfnOutput.Builder.create(this, "MongoDBInstanceEndpoint")
                .exportName("mongodb-instance-endpoint")
                .value(mongoInstance.getInstancePrivateIp()) //Obtém o IP privado da instância
                .build();

        CfnOutput.Builder.create(this, "MongoDBPassword")
                .exportName("mongodb-password")
                .value("mymongodbpassword") //MINHA SENHA DO MONGODB
                .build();
    }
}
