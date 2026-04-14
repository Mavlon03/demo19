package uz.freight.bot.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import uz.freight.bot.config.BotConfig;

@Service
public class RegionDetectorService {

    private final Map<String, Long> regionGroups;
    private static final List<String> SUFFIXES = List.of(
            "дан", "га", "да", "даги", "лик", "нинг",
            "dan", "ga", "da", "dagi", "lik", "ning", "ni"
    );

    public RegionDetectorService(BotConfig botConfig) {
        Map<String, Long> map = new HashMap<>();
        BotConfig.RegionGroups groups = botConfig.getRegionGroups();
        map.put("тошкент шаҳар", groups.getToshkentCity());
        map.put("toshkent shahar", groups.getToshkentCity());
        map.put("toshkent shahri", groups.getToshkentCity());
        map.put("toshkent", groups.getToshkentCity());
        map.put("тошкент вилояти", groups.getToshkentRegion());
        map.put("toshkent viloyati", groups.getToshkentRegion());
        map.put("toshkent viloyat", groups.getToshkentRegion());
        map.put("самарқанд", groups.getSamarqand());
        map.put("samarqand", groups.getSamarqand());
        map.put("жиззах", groups.getJizzax());
        map.put("jizzax", groups.getJizzax());
        map.put("андижон", groups.getAndijon());
        map.put("andijon", groups.getAndijon());
        map.put("наманган", groups.getNamangan());
        map.put("namangan", groups.getNamangan());
        map.put("фарғона", groups.getFargona());
        map.put("farg'ona", groups.getFargona());
        map.put("fargona", groups.getFargona());
        map.put("фергана", groups.getFargona());
        map.put("бухоро", groups.getBuxoro());
        map.put("buxoro", groups.getBuxoro());
        map.put("хоразм", groups.getXorazm());
        map.put("xorazm", groups.getXorazm());
        map.put("навоий", groups.getNavoiy());
        map.put("navoiy", groups.getNavoiy());
        map.put("navoi", groups.getNavoiy());
        map.put("қашқадарё", groups.getQashqadaryo());
        map.put("qashqadaryo", groups.getQashqadaryo());
        map.put("сурхондарё", groups.getSurxondaryo());
        map.put("surxondaryo", groups.getSurxondaryo());
        map.put("сирдарё", groups.getSirdaryo());
        map.put("sirdaryo", groups.getSirdaryo());
        map.put("қорақалпоғ", groups.getQoraqalpoq());
        map.put("qoraqalp", groups.getQoraqalpoq());
        map.put("qoraqalpoq", groups.getQoraqalpoq());
        map.put("qoraqalpog", groups.getQoraqalpoq());
        this.regionGroups = Collections.unmodifiableMap(map);
    }

    public Set<Long> detect(String text) {
        if (text == null) {
            return Collections.emptySet();
        }

        String normalized = text.toLowerCase().trim();
        if (normalized.isBlank()) {
            return Collections.emptySet();
        }

        Set<Long> result = new HashSet<>();
        for (Map.Entry<String, Long> entry : regionGroups.entrySet()) {
            String keyword = entry.getKey();
            Long groupId = entry.getValue();
            if (groupId == null || groupId == 0L) {
                continue;
            }
            if (normalized.contains(keyword)) {
                result.add(groupId);
                continue;
            }
            for (String suffix : SUFFIXES) {
                if (normalized.contains(keyword + suffix)) {
                    result.add(groupId);
                    break;
                }
            }
        }

        return result;
    }
}
