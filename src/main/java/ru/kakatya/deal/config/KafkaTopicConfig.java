package ru.kakatya.deal.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import ru.kakatya.deal.mappers.Mapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    @Autowired
    private Mapper mapper;
    @Value("${topics.finish-registr}")
    private String finishRegTopic;
    @Value("${topics.create-doc}")
    private String createDocTopic;
    @Value("${topics.send-doc}")
    private String sendDocTopic;
    @Value("${topics.send-ses}")
    private String sendSesTopic;
    @Value("${topics.credit-issd}")
    private String creditIssuedTopic;
    @Value("${topics.appl-denied}")
    private String applDenied;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic finishRegistrationTopic() {
        return new NewTopic(finishRegTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createDocumentsTopic() {
        return new NewTopic(createDocTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic sendDocumentsTopic() {
        return new NewTopic(sendDocTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic sendSesTopic() {
        return new NewTopic(sendSesTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic creditIssuedTopic() {
        return new NewTopic(creditIssuedTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic applicationDeniedTopic() {
        return new NewTopic(applDenied, 1, (short) 1);
    }
}
