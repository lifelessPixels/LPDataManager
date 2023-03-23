package pl.lifelesspixels.lpdatamanager.data;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class Configuration {

    private static Configuration instance;

    private final String mongoConnectionString;
    private final MongoAuthenticationType mongoAuthenticationType;
    private final String mongoCertificatePath;
    private final String mongoDatabaseName;

    public static void loadFrom(FileConfiguration fileConfiguration) {
        new Configuration(fileConfiguration);
    }

    private Configuration(FileConfiguration fileConfiguration) {
        if(instance != null)
            throw new IllegalStateException("cannot create new configuration, instance is already here");

        // read values from config
        if(!fileConfiguration.contains("mongo-connection-string") || !fileConfiguration.isString("mongo-connection-string"))
            throw new IllegalArgumentException("missing or invalid mongo connection string found in config.yml");
        mongoConnectionString = fileConfiguration.getString("mongo-connection-string");

        if(!fileConfiguration.contains("mongo-auth-type") || !fileConfiguration.isString("mongo-auth-type"))
            throw new IllegalArgumentException("missing or invalid mongo authentication type found in config.yml");
        String authType = fileConfiguration.getString("mongo-auth-type");

        switch (Objects.requireNonNull(authType)) {
            case "pass" -> {
                mongoAuthenticationType = MongoAuthenticationType.Password;
                mongoCertificatePath = "";
            }
            case "cert" -> {
                if (!fileConfiguration.contains("mongo-cert-path") || !fileConfiguration.isString("mongo-cert-path"))
                    throw new IllegalArgumentException("missing or invalid mongo certificate path found in config.yml");

                mongoAuthenticationType = MongoAuthenticationType.Certificate;
                mongoCertificatePath = fileConfiguration.getString("mongo-cert-path");
            }
            default -> throw new IllegalArgumentException("invalid mongo authentication type found in config.yml " +
                    "(expected 'pass' or 'cert'");
        }

        if(!fileConfiguration.contains("mongo-db-name") || !fileConfiguration.isString("mongo-db-name"))
            throw new IllegalArgumentException("missing or invalid mongo database name found in config.yml");
        mongoDatabaseName = fileConfiguration.getString("mongo-db-name");

        // if everything was loaded correctly, just set the instance
        instance = this;
    }

    public String getMongoConnectionString() {
        return mongoConnectionString;
    }

    public MongoAuthenticationType getMongoAuthenticationType() {
        return mongoAuthenticationType;
    }

    public String getMongoCertificatePath() {
        return mongoCertificatePath;
    }

    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    public static Configuration getInstance() {
        return instance;
    }

}
