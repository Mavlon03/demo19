package uz.freight.bot.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BotConfigSanityChecker implements ApplicationRunner {

    private final BotConfig botConfig;

    public BotConfigSanityChecker(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (botConfig.getSourceGroupId() == null || botConfig.getSourceGroupId() == 0L) {
            throw new IllegalStateException("SOURCE_GROUP_ID must be configured with a valid Telegram chat id");
        }

        List<String> configuredRegions = botConfig.asRegionGroupMap().entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() != 0L)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (configuredRegions.isEmpty()) {
            throw new IllegalStateException("At least one REGION_*_ID must be configured");
        }

        if (botConfig.isEnableLegacyBot()) {
            if (botConfig.getToken() == null || botConfig.getToken().isBlank()) {
                throw new IllegalStateException("BOT_TOKEN is required when BOT_ENABLE_LEGACY_BOT=true");
            }
            if (botConfig.getUsername() == null || botConfig.getUsername().isBlank()) {
                throw new IllegalStateException("BOT_USERNAME is required when BOT_ENABLE_LEGACY_BOT=true");
            }
        }
    }
}
