package uz.freight.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String token;
    private String username;
    private Long sourceGroupId;
    private RegionGroups regionGroups = new RegionGroups();

    @Data
    public static class RegionGroups {
        private Long toshkentCity;
        private Long toshkentRegion;
        private Long samarqand;
        private Long jizzax;
        private Long andijon;
        private Long namangan;
        private Long fargona;
        private Long buxoro;
        private Long xorazm;
        private Long navoiy;
        private Long qashqadaryo;
        private Long surxondaryo;
        private Long sirdaryo;
        private Long qoraqalpoq;
    }
}
