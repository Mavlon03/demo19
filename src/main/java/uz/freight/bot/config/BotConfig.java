package uz.freight.bot.config;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.stereotype.Component;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private boolean enableLegacyBot;
    private String token;
    private String username;
    @NotNull
    private Long sourceGroupId;
    @Valid
    private RegionGroups regionGroups = new RegionGroups();

    public Map<String, Long> asRegionGroupMap() {
        Map<String, Long> groups = new LinkedHashMap<>();
        groups.put("toshkentCity", regionGroups.getToshkentCity());
        groups.put("toshkentRegion", regionGroups.getToshkentRegion());
        groups.put("samarqand", regionGroups.getSamarqand());
        groups.put("jizzax", regionGroups.getJizzax());
        groups.put("andijon", regionGroups.getAndijon());
        groups.put("namangan", regionGroups.getNamangan());
        groups.put("fargona", regionGroups.getFargona());
        groups.put("buxoro", regionGroups.getBuxoro());
        groups.put("xorazm", regionGroups.getXorazm());
        groups.put("navoiy", regionGroups.getNavoiy());
        groups.put("qashqadaryo", regionGroups.getQashqadaryo());
        groups.put("surxondaryo", regionGroups.getSurxondaryo());
        groups.put("sirdaryo", regionGroups.getSirdaryo());
        groups.put("qoraqalpoq", regionGroups.getQoraqalpoq());
        return groups;
    }

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
