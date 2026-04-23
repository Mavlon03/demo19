package uz.freight.bot.config;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BotConfigLogger implements ApplicationRunner {

    private final BotConfig botConfig;

    public BotConfigLogger(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("BotConfig loaded: legacyBotEnabled={}, token={}, username={}, sourceGroupId={}",
                botConfig.isEnableLegacyBot(),
                mask(botConfig.getToken()),
                safe(botConfig.getUsername()),
                botConfig.getSourceGroupId());

        for (Map.Entry<String, Long> entry : botConfig.asRegionGroupMap().entrySet()) {
            log.info("Region group {}={}", entry.getKey(), entry.getValue());
        }
    }

    private String mask(String token) {
        if (token == null || token.isBlank()) {
            return "<empty>";
        }
        int keep = Math.min(6, token.length());
        return token.substring(0, keep) + "***";
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }
        return value;
    }
}
