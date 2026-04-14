package uz.freight.bot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.freight.bot.bot.FreightBot;

@Slf4j
@Component
@ConditionalOnProperty(name = "bot.enable-legacy-bot", havingValue = "true", matchIfMissing = false)
public class TelegramBotRegistration {

    private final FreightBot freightBot;

    public TelegramBotRegistration(FreightBot freightBot) {
        this.freightBot = freightBot;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(freightBot);
            log.info("Telegram bot registered successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot", e);
        }
    }
}
