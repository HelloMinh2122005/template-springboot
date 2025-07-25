package org.minh.template.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Builder
@Getter
@Setter
@RedisHash(value = "jwtTokens") // Đánh dấu class này sẽ được lưu trữ dưới dạng hash trong Redis với tên là jwtTokens.
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Indexed // @Indexed: Đánh dấu trường sẽ được tạo chỉ mục (index) trong Redis để tìm kiếm nhanh hơn.
    private UUID userId;

    @Indexed
    private String token;

    @Indexed
    private String refreshToken;

    @Indexed
    private Boolean rememberMe;

    @Indexed
    private String ipAddress;

    @Indexed
    private String userAgent;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long tokenTimeToLive;
}
