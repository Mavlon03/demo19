package uz.freight.bot.config;

import java.util.LinkedHashMap;
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
        log.info("BotConfig loaded: token={}, username={}, sourceGroupId={}",
                mask(botConfig.getToken()),
                safe(botConfig.getUsername()),
                botConfig.getSourceGroupId());

        BotConfig.RegionGroups groups = botConfig.getRegionGroups();
        Map<String, Long> groupIds = new LinkedHashMap<>();
        groupIds.put("toshkentCity", groups.getToshkentCity());
//        groupIds.put("toshkentRegion", groups.getToshkentRegion());
//        groupIds.put("samarqand", groups.getSamarqand());
//        groupIds.put("jizzax", groups.getJizzax());
//        groupIds.put("andijon", groups.getAndijon());
//        groupIds.put("namangan", groups.getNamangan());
//        groupIds.put("fargona", groups.getFargona());
//        groupIds.put("buxoro", groups.getBuxoro());
//        groupIds.put("xorazm", groups.getXorazm());
//        groupIds.put("navoiy", groups.getNavoiy());
//        groupIds.put("qashqadaryo", groups.getQashqadaryo());
//        groupIds.put("surxondaryo", groups.getSurxondaryo());
//        groupIds.put("sirdaryo", groups.getSirdaryo());
//        groupIds.put("qoraqalpoq", groups.getQoraqalpoq());
//        REGION_TOSHKENT_REGION_ID=-1002222222222
//REGION_SAMARQAND_ID=-1003333333333
//REGION_JIZZAX_ID=-1004444444444
//REGION_ANDIJON_ID=-1005555555555
//REGION_NAMANGAN_ID=-1006666666666
//REGION_FARGONA_ID=-1007777777777
//REGION_BUXORO_ID=-1008888888888
//REGION_XORAZM_ID=-1009999999999
//REGION_NAVOIY_ID=-1000000000001
//REGION_QASHQADARYO_ID=-1000000000002
//REGION_SURXONDARYO_ID=-1000000000003
//REGION_SIRDARYO_ID=-1000000000004
//REGION_QORAQALPOQ_ID=-1000000000005

        for (Map.Entry<String, Long> entry : groupIds.entrySet()) {
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
