package com.techie.shoppingstore.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.io.Streams;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@JsonComponent
public class ResponseSerializer extends JsonSerializer<Response> {

    @Override
    public void serialize(Response response, JsonGenerator gen, SerializerProvider provider) throws IOException {
        try (Reader in = new InputStreamReader(response.getEntity().getContent())) {
            gen.writeRaw(Streams.copyToString(in));
        }
    }
}
