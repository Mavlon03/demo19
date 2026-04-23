package uz.freight.bot.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import uz.freight.bot.config.BotConfig;

@Service
public class RegionDetectorService {

    private static final List<String> SUFFIXES = List.of(
            "", "dan", "ga", "da", "dagi", "lik", "ning", "ni"
    );

    private final List<RegionRule> regionRules;

    public RegionDetectorService(BotConfig botConfig) {
        BotConfig.RegionGroups groups = botConfig.getRegionGroups();
        List<RegionRule> rules = new ArrayList<>();

        rules.add(rule(groups.getToshkentRegion(),
                "toshkent viloyati", "toshkent viloyat", "toshkent oblasti"));
        rules.add(rule(groups.getToshkentCity(),
                "toshkent shahri", "toshkent shahar", "toshkent city", "tashkent city"));
        rules.add(rule(groups.getSamarqand(),
                "samarqand", "samarkand"));
        rules.add(rule(groups.getJizzax(),
                "jizzax", "jizzakh", "jizax"));
        rules.add(rule(groups.getAndijon(),
                "andijon", "andijan"));
        rules.add(rule(groups.getNamangan(),
                "namangan"));
        rules.add(rule(groups.getFargona(),
                "fargona", "farg'ona", "farg ona", "fergana"));
        rules.add(rule(groups.getBuxoro(),
                "buxoro", "bukhoro", "buxara", "bukhara"));
        rules.add(rule(groups.getXorazm(),
                "xorazm", "khorazm", "horezm"));
        rules.add(rule(groups.getNavoiy(),
                "navoiy", "navoi"));
        rules.add(rule(groups.getQashqadaryo(),
                "qashqadaryo", "qashqadarya", "kashkadaryo", "kashkadarya"));
        rules.add(rule(groups.getSurxondaryo(),
                "surxondaryo", "surkhandaryo"));
        rules.add(rule(groups.getSirdaryo(),
                "sirdaryo", "syrdarya"));
        rules.add(rule(groups.getQoraqalpoq(),
                "qoraqalpoq", "qoraqalpog", "qoraqalpak", "karakalpak"));

        this.regionRules = Collections.unmodifiableList(rules);
    }

    public Set<Long> detect(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }

        String normalizedText = normalize(text);
        if (normalizedText.isBlank()) {
            return Collections.emptySet();
        }

        Set<Long> result = new LinkedHashSet<>();
        for (RegionRule regionRule : regionRules) {
            if (regionRule.matches(normalizedText)) {
                result.add(regionRule.groupId());
            }
        }

        return result;
    }

    private RegionRule rule(Long groupId, String... aliases) {
        return new RegionRule(groupId, buildPatterns(aliases));
    }

    private List<Pattern> buildPatterns(String... aliases) {
        List<Pattern> patterns = new ArrayList<>();
        for (String alias : aliases) {
            String normalizedAlias = normalize(alias);
            String compactAlias = compactWhitespace(normalizedAlias);
            for (String suffix : SUFFIXES) {
                String candidate = compactWhitespace(normalizedAlias + suffix);
                patterns.add(Pattern.compile("(^|\\W)" + Pattern.quote(candidate) + "(\\W|$)"));
            }

            String regexAlias = compactAlias.replace(" ", "\\\\W+");
            patterns.add(Pattern.compile("(^|\\W)" + regexAlias + "(\\W|$)"));
        }
        return patterns;
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace('\u02bb', '\'')
                .replace('\u2018', '\'')
                .replace('\u2019', '\'')
                .replace('`', '\'')
                .replace('\u0451', '\u0435')
                .replace('\u049b', 'q')
                .replace('\u0493', 'g')
                .replace('\u045e', 'o')
                .replace('\u04b3', 'h')
                .replace('\u0439', 'i');

        normalized = normalized
                .replace("shahri", "shahar")
                .replace("\u0433\u043e\u0440\u043e\u0434 \u0442\u0430\u0448\u043a\u0435\u043d\u0442", "toshkent shahar")
                .replace("\u0442\u0430\u0448\u043a\u0435\u043d\u0442", "toshkent")
                .replace("\u0442\u043e\u0448\u043a\u0435\u043d\u0442", "toshkent")
                .replace("\u0441\u0430\u043c\u0430\u0440\u043a\u0430\u043d\u0434", "samarqand")
                .replace("\u0431\u0443\u0445\u043e\u0440\u043e", "buxoro")
                .replace("\u0430\u043d\u0434\u0438\u0436\u043e\u043d", "andijon")
                .replace("\u043d\u0430\u043c\u0430\u043d\u0433\u0430\u043d", "namangan")
                .replace("\u0436\u0438\u0437\u0437\u0430\u0445", "jizzax")
                .replace("\u0441\u0438\u0440\u0434\u0430\u0440\u0435", "sirdaryo")
                .replace("\u0441\u0443\u0440\u0445\u043e\u043d\u0434\u0430\u0440\u0435", "surxondaryo")
                .replace("\u049b\u0430\u0448\u049b\u0430\u0434\u0430\u0440\u0435", "qashqadaryo")
                .replace("\u049b\u043e\u0440\u0430\u049b\u0430\u043b\u043f\u043e\u0493", "qoraqalpoq")
                .replace("\u049b\u043e\u0440\u0430\u049b\u0430\u043b\u043f\u043e\u049b", "qoraqalpoq")
                .replace("\u0444\u0430\u0440\u0433\u043e\u043d\u0430", "fargona")
                .replace("\u043d\u0430\u0432\u043e\u0438\u0438", "navoiy")
                .replace("\u0445\u043e\u0440\u0430\u0437\u043c", "xorazm");
        return compactWhitespace(normalized);
    }

    private String compactWhitespace(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private record RegionRule(Long groupId, List<Pattern> patterns) {
        private boolean matches(String normalizedText) {
            if (groupId == null || groupId == 0L) {
                return false;
            }
            for (Pattern pattern : patterns) {
                if (pattern.matcher(normalizedText).find()) {
                    return true;
                }
            }
            return false;
        }
    }
}
