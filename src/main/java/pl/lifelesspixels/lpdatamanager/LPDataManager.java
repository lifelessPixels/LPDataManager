package pl.lifelesspixels.lpdatamanager;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.lifelesspixels.lpdatamanager.data.Configuration;
import pl.lifelesspixels.lpdatamanager.data.DatabaseEntry;
import pl.lifelesspixels.lpdatamanager.data.MongoAuthenticationType;
import pl.lifelesspixels.lpdatamanager.data.MongoUtils;

public class LPDataManager extends JavaPlugin {

    private MongoClient mongoClient = null;
    private MongoDatabase mongoDatabase = null;

    @Override
    public void onEnable() {
        // load config
        saveDefaultConfig();
        Configuration.loadFrom(getConfig());
        if(Configuration.getInstance().getMongoAuthenticationType() == MongoAuthenticationType.Certificate) {
            getLogger().severe("LPDataManager does not support certificate authentication for the moment");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // connect to MongoDB
        Configuration configuration = Configuration.getInstance();
        ConnectionString connectionString = new ConnectionString(configuration.getMongoConnectionString());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();

        try {
            mongoClient = MongoClients.create(settings);
        } catch (Exception e) {
            getLogger().severe("could not connect to Mongo server, aborting...");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // log successful connection
        getLogger().info("successfully connected to Mongo server");

        // open database connection
        String dbName = configuration.getMongoDatabaseName();
        try {
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(pojoCodecProvider));
            mongoDatabase = mongoClient.getDatabase(configuration.getMongoDatabaseName())
                    .withCodecRegistry(pojoCodecRegistry);
        } catch (Exception e) {
            getLogger().severe("database name '" + dbName + "' is not valid");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("successfully connected to Mongo database with name '" + dbName + "'");
    }

    public <T extends DatabaseEntry> T getPlayerData(Player player, String resourceIdentifier, Class<T> dataClass) {
        if(mongoDatabase == null)
            throw new IllegalStateException("cannot get player data, connection is null");

        return MongoUtils.getData(mongoDatabase, resourceIdentifier, player.getUniqueId(), dataClass);
    }

    public <T extends DatabaseEntry> void setPlayerData(String resourceIdentifier, T data, Class<T> dataClass) {
        if(mongoDatabase == null)
            throw new IllegalStateException("cannot set player data, connection is null");

        MongoUtils.setData(mongoDatabase, resourceIdentifier, data, dataClass);
    }

    public void removePlayerData(Player player, String resourceIdentifier) {
        if(mongoDatabase == null)
            throw new IllegalStateException("cannot remove player data, connection is null");

        MongoUtils.removeData(mongoDatabase, resourceIdentifier, player.getUniqueId());
    }

    @Override
    public void onDisable() {
        // disconnect from MongoDB
        if(mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            mongoDatabase = null;
        }
    }

}
