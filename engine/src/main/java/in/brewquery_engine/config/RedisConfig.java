package in.brewquery_engine.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url:redis://redis:6379}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() throws Exception {
        URI uri = new URI(redisUrl);

        String userInfo = uri.getUserInfo();
        String password = (userInfo != null && userInfo.contains(":"))
                ? userInfo.substring(userInfo.indexOf(':') + 1)
                : null;

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(uri.getHost());
        config.setPort(uri.getPort());
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfig =
                LettuceClientConfiguration.builder();
        if (redisUrl.startsWith("rediss://")) {
            clientConfig.useSsl();
        }

        return new LettuceConnectionFactory(config, clientConfig.build());
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
