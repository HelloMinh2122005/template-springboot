package org.minh.template.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.database}")
    private String database;

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.timeout}")
    private String timeout;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(); // Định nghĩa một bean trả về LettuceConnectionFactory, dùng để tạo kết nối tới Redis.
        config.setDatabase(Integer.parseInt(database));
        config.setHostName(host);
        config.setPort(Integer.parseInt(port));
        config.setPassword(password);

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder() // Xây dựng cấu hình client Lettuce, ở đây thiết lập thời gian timeout cho các lệnh gửi tới Redis.
                .commandTimeout(Duration.ofMillis(Long.parseLong(timeout)))
                .build();

        // Trả về factory kết nối Redis sử dụng cấu hình trên, để Spring sử dụng khi thao tác với Redis.
        return new LettuceConnectionFactory(config, lettuceClientConfiguration);
    }

    @Bean
    // Bean này giúp thao tác (CRUD) với Redis một cách dễ dàng trong Spring Boot
    // Định nghĩa bean kiểu RedisTemplate với key là String, value là Object.
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>(); // Tạo mới một instance của RedisTemplate.
        template.setConnectionFactory(redisConnectionFactory); // Thiết lập factory kết nối Redis cho template, để nó có thể sử dụng kết nối này khi thực hiện các thao tác với Redis.

        return template; // Trả về template đã cấu hình để sử dụng trong ứng dụng.
    }
}