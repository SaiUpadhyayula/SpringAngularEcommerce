package com.techie.shoppingstore.config;

import com.techie.shoppingstore.exceptions.SpringStoreException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableElasticsearchRepositories
public class ElasticSearchConfig {

    @SuppressWarnings("WeakerAccess")
    @Bean
    public Client client() {
        Settings elasticsearchSettings = Settings.builder()
                .put("client.transport.sniff", true).build();
        TransportClient client = new PreBuiltTransportClient(elasticsearchSettings);
        try {
            client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        } catch (UnknownHostException e) {
            throw new SpringStoreException("An error occured when configuring Elastic Search");
        }
        return client;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(client());
    }
}
