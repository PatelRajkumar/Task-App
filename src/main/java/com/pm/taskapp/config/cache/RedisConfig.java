package com.pm.taskapp.config.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration.
 * Configures Redis connection, serialization strategy, and cache managers with
 * custom TTLs.
 * 
 * <p>
 * This configuration enables:
 * <ul>
 * <li>Spring Cache abstraction (@Cacheable, @CacheEvict, @CachePut)</li>
 * <li>JSON serialization for cached objects</li>
 * <li>Custom TTL per cache name</li>
 * <li>Transaction-aware caching (synchronized with DB transactions)</li>
 * </ul>
 * 
 * <p>
 * Connection details are configured in application-dev.yml:
 * 
 * <pre>
 * spring.data.redis:
 *   host: localhost
 *   port: 6379
 *   password: ${REDIS_PASSWORD}
 * </pre>
 * 
 * @author TaskApp Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

        /**
         * Configure RedisTemplate with JSON serialization.
         * Used for manual Redis operations if needed (not used for @Cacheable
         * annotations).
         * 
         * @param connectionFactory Redis connection factory (auto-configured by Spring
         *                          Boot)
         * @param objectMapper      Jackson ObjectMapper for JSON serialization
         * @return Configured RedisTemplate
         */
        @Bean
        public RedisTemplate<String, Object> redisTemplate(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper objectMapper) {

                log.info("Configuring RedisTemplate with JSON serialization");

                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // String serializer for keys (e.g., "taskapp::user:details:123")
                StringRedisSerializer stringSerializer = new StringRedisSerializer();
                template.setKeySerializer(stringSerializer);
                template.setHashKeySerializer(stringSerializer);

                // JSON serializer for values (with type information for polymorphic objects)
                GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer(objectMapper);
                template.setValueSerializer(jsonSerializer);
                template.setHashValueSerializer(jsonSerializer);

                template.afterPropertiesSet();

                log.debug("RedisTemplate configured successfully");
                return template;
        }

        /**
         * Configure RedisCacheManager with custom TTLs per cache name.
         * This is the primary configuration for Spring Cache abstraction.
         * 
         * @param connectionFactory Redis connection factory
         * @param objectMapper      Jackson ObjectMapper for JSON serialization
         * @return Configured RedisCacheManager
         */
        @Bean
        public RedisCacheManager cacheManager(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper objectMapper) {

                log.info("Configuring RedisCacheManager with custom TTLs");

                // Default cache configuration (5 minutes fallback)
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5))
                                .disableCachingNullValues() // Never cache null values
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                                new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                                createJsonSerializer(objectMapper)));

                // Custom TTLs per cache name
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // User details cache: 15 minutes (1/4 of JWT expiration: 3600000ms)
                cacheConfigurations.put(
                                CacheNames.USER_DETAILS,
                                defaultConfig.entryTtl(Duration.ofMinutes(15)));

                // Project member cache: 5 minutes (faster propagation for authorization
                // changes)
                cacheConfigurations.put(
                                CacheNames.PROJECT_MEMBER,
                                defaultConfig.entryTtl(Duration.ofMinutes(5)));

                log.info("Cache TTL configuration: {} = 15 min, {} = 5 min",
                                CacheNames.USER_DETAILS, CacheNames.PROJECT_MEMBER);

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .transactionAware() // Synchronize cache operations with DB transactions
                                .build();
        }

        /**
         * Create JSON serializer with type information for polymorphic objects.
         * This allows proper deserialization of complex objects like UserPrincipal.
         * 
         * @param objectMapper Source ObjectMapper to copy configuration from
         * @return Configured JSON serializer
         */
        private GenericJackson2JsonRedisSerializer createJsonSerializer(ObjectMapper objectMapper) {
                // Create a copy of the application's ObjectMapper to avoid side effects
                ObjectMapper cachingObjectMapper = objectMapper.copy();

                // Register Spring Security Jackson modules to handle Security classes
                // This fixes deserialization of SimpleGrantedAuthority and other Security types
                cachingObjectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));

                // Enable type information for polymorphic deserialization
                // This adds "@class" field to JSON, allowing reconstruction of exact types
                cachingObjectMapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                return new GenericJackson2JsonRedisSerializer(cachingObjectMapper);
        }
}