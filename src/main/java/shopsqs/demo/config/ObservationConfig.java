package shopsqs.demo.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservationConfig {

    @Bean
    public ObservationRegistry observationRegistry() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();

        // Configurar la observación para incluir todas las solicitudes HTTP
        observationRegistry.observationConfig().observationPredicate((name, context) -> true);
        
        return observationRegistry;
    }
}

