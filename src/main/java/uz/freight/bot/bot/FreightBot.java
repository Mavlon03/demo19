package uz.freight.bot.bot;

import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.freight.bot.config.BotConfig;
import uz.freight.bot.service.RegionDetectorService;
import uz.freight.bot.userbot.UserbotClient;

@Slf4j
@Component
public class FreightBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final RegionDetectorService detectorService;
    private final UserbotClient userbotClient;

    public FreightBot(BotConfig botConfig, RegionDetectorService detectorService, UserbotClient userbotClient) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.detectorService = detectorService;
        this.userbotClient = userbotClient;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) {
            log.warn("Update is null");
            return;
        }
        if (!update.hasMessage()) {
            log.info("Update ignored: no message payload");
            return;
        }

        Message message = update.getMessage();
        log.info("Incoming message: chatId={}, messageId={}, fromUser={}",
                message.getChatId(),
                message.getMessageId(),
                message.getFrom() != null ? message.getFrom().getId() : null);
        if (!Objects.equals(message.getChatId(), botConfig.getSourceGroupId())) {
            log.info("Message ignored: chatId {} != sourceGroupId {}",
                    message.getChatId(),
                    botConfig.getSourceGroupId());
            return;
        }

        String text = message.getText();
        if (text == null || text.isBlank()) {
            text = message.getCaption();
        }
        if (text == null || text.isBlank()) {
            log.info("Message ignored: empty text/caption");
            return;
        }

        Set<Long> targetGroups = detectorService.detect(text);
        if (targetGroups.isEmpty()) {
            log.warn("No region detected for messageId={}", message.getMessageId());
            return;
        }

        for (Long groupId : targetGroups) {
            forwardMessage(message, groupId);
        }
    }

    private void forwardMessage(Message message, Long groupId) {
        boolean forwarded = userbotClient.forward(message.getChatId(), message.getMessageId(), groupId);
        if (forwarded) {
            log.info("Forwarded messageId={} to groupId={}", message.getMessageId(), groupId);
        } else {
            log.error("Failed to forward messageId={} to groupId={}", message.getMessageId(), groupId);
        }
    }
}
