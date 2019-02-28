package uk.gov.ida.notification.session.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import io.lettuce.core.codec.RedisCodec;
import uk.gov.ida.notification.exceptions.RedisSerializationException;
import uk.gov.ida.notification.session.GatewaySessionData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SessionRedisCodec implements RedisCodec<String, GatewaySessionData> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return new String(bytes.array(), StandardCharsets.UTF_8);
    }

    @Override
    public GatewaySessionData decodeValue(ByteBuffer bytes) {
        try (InputStream inputStream = new ByteBufferBackedInputStream(bytes)) {
            return objectMapper.readValue(inputStream, GatewaySessionData.class);
        } catch (IOException e) {
            throw new RedisSerializationException("Error decoding Gateway session data", e);
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return ByteBuffer.wrap(key.getBytes());
    }

    @Override
    public ByteBuffer encodeValue(GatewaySessionData value) {
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
            throw new RedisSerializationException("Error encoding Gateway session data", e);
        }
    }
}
