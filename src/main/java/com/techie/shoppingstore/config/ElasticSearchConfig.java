package com.techie.shoppingstore.config;

import com.techie.shoppingstore.exceptions.SpringStoreException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableElasticsearchRepositories
public class ElasticSearchConfig {

    @Bean
    public Client client() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        try {
            return new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

        } catch (UnknownHostException e) {
            throw new SpringStoreException("An error occured when configuring Elastic Search");
        }
    }
}
