package ru.kakatya.deal.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import ru.kakatya.deal.dtos.Theme;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic finishRegistrationTopic() {
        return new NewTopic(Theme.FINISH_REGISTRATION.name(), 1, (short) 1);
    }

    @Bean
    public NewTopic createDocumentsTopic() {
        return new NewTopic(Theme.CREATE_DOCUMENTS.name(), 1, (short) 1);
    }

    @Bean
    public NewTopic sendDocumentsTopic() {
        return new NewTopic(Theme.SEND_DOCUMENTS.name(), 1, (short) 1);
    }

    @Bean
    public NewTopic sendSesTopic() {
        return new NewTopic(Theme.SEND_SES.name(), 1, (short) 1);
    }
    @Bean
    public NewTopic creditIssuedTopic() {
        return new NewTopic(Theme.CREDIT_ISSUED.name(), 1, (short) 1);
    }
    @Bean
    public NewTopic applicationDeniedTopic() {
        return new NewTopic(Theme.APPLICATION_DENIED.name(), 1, (short) 1);
    }
}
