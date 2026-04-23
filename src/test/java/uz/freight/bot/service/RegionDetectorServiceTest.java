package uz.freight.bot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import uz.freight.bot.config.BotConfig;

class RegionDetectorServiceTest {

    private final RegionDetectorService service = new RegionDetectorService(buildConfig());

    @Test
    void detectsMultipleRegionsFromLatinText() {
        Set<Long> result = service.detect("Toshkentdan Samarqandga yuk bor");

        assertThat(result).containsExactlyInAnyOrder(100L, 300L);
    }

    @Test
    void detectsCyrillicTextAfterNormalization() {
        Set<Long> result = service.detect("\u0422\u043e\u0448\u043a\u0435\u043d\u0442\u0434\u0430\u043d \u0424\u0430\u0440\u0493\u043e\u043d\u0430\u0433\u0430 yuk");

        assertThat(result).containsExactlyInAnyOrder(100L, 700L);
    }

    @Test
    void prefersRegionAliasForToshkentViloyati() {
        Set<Long> result = service.detect("Toshkent viloyatiga yuk kerak");

        assertThat(result).containsExactly(200L);
    }

    @Test
    void ignoresBlankText() {
        assertThat(service.detect("   ")).isEmpty();
    }

    private BotConfig buildConfig() {
        BotConfig config = new BotConfig();
        config.setSourceGroupId(-1001234567890L);

        BotConfig.RegionGroups groups = new BotConfig.RegionGroups();
        groups.setToshkentCity(100L);
        groups.setToshkentRegion(200L);
        groups.setSamarqand(300L);
        groups.setJizzax(400L);
        groups.setAndijon(500L);
        groups.setNamangan(600L);
        groups.setFargona(700L);
        groups.setBuxoro(800L);
        groups.setXorazm(900L);
        groups.setNavoiy(1000L);
        groups.setQashqadaryo(1100L);
        groups.setSurxondaryo(1200L);
        groups.setSirdaryo(1300L);
        groups.setQoraqalpoq(1400L);
        config.setRegionGroups(groups);
        return config;
    }
}
