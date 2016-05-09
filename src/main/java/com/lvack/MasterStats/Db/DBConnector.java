package com.lvack.MasterStats.Db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import lombok.Getter;

/**
 * DBConnectorClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * singleton class to provide dynamoDB and mapper
 */
public class DBConnector {
    @Getter
    private static final DBConnector instance = new DBConnector();
    @Getter
    private DynamoDB dynamoDB;
    @Getter
    private DynamoDBMapper dynamoDBMapper;

    /**
     * generate dynamo db mapper etc. to connect to database
     */
    private DBConnector() {
        AWSPropertiesProvider awsPropertiesProvider = new AWSPropertiesProvider("dynamoDb.properties");
        AmazonDynamoDBClient dbClient = new AmazonDynamoDBClient(awsPropertiesProvider.getCredentials());
        dbClient.setRegion(awsPropertiesProvider.getRegion());
        dynamoDB = new DynamoDB(dbClient);
        dynamoDBMapper = new DynamoDBMapper(dbClient);
    }
}
