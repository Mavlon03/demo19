package uz.freight.bot.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.freight.bot.config.BotConfig;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final BotConfig botConfig;

    public HealthController(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        long configuredRegions = botConfig.asRegionGroupMap().values().stream()
                .filter(value -> value != null && value != 0L)
                .count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("legacyBotEnabled", botConfig.isEnableLegacyBot());
        response.put("sourceGroupId", botConfig.getSourceGroupId());
        response.put("configuredRegionCount", configuredRegions);
        return response;
    }
}
