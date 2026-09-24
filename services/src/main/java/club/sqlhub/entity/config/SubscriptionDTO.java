package club.sqlhub.entity.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SubscriptionDTO {
    private String planId;
    private LocalDateTime expiresAt;
}
