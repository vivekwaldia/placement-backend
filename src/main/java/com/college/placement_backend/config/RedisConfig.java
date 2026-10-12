// By default, spring boot saves Redis data as unreadable binary blobs and never expires the cache
// We need to force JSON serialization and 10 minute TTL (time-to-live)
// JSON serialization: Process of converting in-memory DS to lightweight JSON
// TTL: How long cached data remains valid before expiring

package com.college.placement_backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig{
    // Handles object control over to spring
    // Creates a singleton instance for the whole app (exactly one object copy in the RAM)
    // Allows us to manage 3rd party classes like Redis, Swagger
    @Bean
    public RedisCacheConfiguration cacheConfiguration(){
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)).serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
    }
}