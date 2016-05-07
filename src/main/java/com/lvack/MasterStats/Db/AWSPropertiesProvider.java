package com.lvack.MasterStats.Db;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.lvack.MasterStats.Api.RiotApiFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AWSPropertiesProviderClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * class to read aws credentials and region from properties file
 */
class AWSPropertiesProvider {
    private final String credentialsFilePath;
    private String accessKey;
    private String secretKey;
    private Region region;

    AWSPropertiesProvider(String credentialsFilePath) {
        this.credentialsFilePath = credentialsFilePath;
        refresh();
    }

    /**
     * @return BasicAWSCredentials with the data from the properties file
     */
    AWSCredentials getCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    /**
     * load accessKey, secretKey and region from properties file given in constructor
     */
    private void refresh() {
        Properties properties = new Properties();
        InputStream in = RiotApiFactory.class.getClassLoader().getResourceAsStream(credentialsFilePath);
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        accessKey = properties.getProperty("accessKey");
        secretKey = properties.getProperty("secretKey");
        region = Region.getRegion(Regions.valueOf(properties.getProperty("region")));
    }

    /**
     * @return the region specified in the properties file
     */
    public Region getRegion() {
        return region;
    }
}
