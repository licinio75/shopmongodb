package shopsqs.demo.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.event.CommandListener;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsCommandListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoDBMetricsConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoClientSettings());
    }

    @Bean
    public MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(mongoUri))
                .addCommandListener(commandListener())
                .build();
    }

    @Bean
    public CommandListener commandListener() {
        return new MongoMetricsCommandListener(meterRegistry);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
