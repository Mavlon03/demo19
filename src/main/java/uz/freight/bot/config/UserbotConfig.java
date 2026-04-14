package uz.freight.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "userbot")
public class UserbotConfig {
    private String pythonServiceUrl;
}
