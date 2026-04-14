package uz.freight.bot.userbot;

import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uz.freight.bot.config.UserbotConfig;

@Slf4j
@Component
public class UserbotClient {

    private final RestTemplate restTemplate;
    private final UserbotConfig userbotConfig;

    public UserbotClient(RestTemplateBuilder restTemplateBuilder, UserbotConfig userbotConfig) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.userbotConfig = userbotConfig;
    }

    public boolean forward(long fromChatId, int messageId, long toChatId) {
        String url = userbotConfig.getPythonServiceUrl() + "/forward";
        Map<String, Object> body = Map.of(
                "from_chat_id", fromChatId,
                "message_id", messageId,
                "to_chat_id", toChatId
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Userbot forward request failed with status={} for messageId={} toChatId={}",
                        response.getStatusCode(), messageId, toChatId);
                return false;
            }

            Map responseBody = response.getBody();
            boolean success = responseBody != null && "ok".equals(responseBody.get("status"));
            if (!success) {
                log.error("Userbot forward request returned non-ok response for messageId={} toChatId={}: {}",
                        messageId, toChatId, responseBody);
            }
            return success;
        } catch (Exception e) {
            log.error("Userbot forward request failed for messageId={} toChatId={}", messageId, toChatId, e);
            return false;
        }
    }

    public boolean isHealthy() {
        String url = userbotConfig.getPythonServiceUrl() + "/health";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Userbot health check failed", e);
            return false;
        }
    }
}
