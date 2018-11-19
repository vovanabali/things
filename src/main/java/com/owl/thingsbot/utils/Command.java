package com.owl.thingsbot.utils;

import com.owl.thingsbot.enums.Query;
import com.owl.thingsbot.enums.QueryType;
import lombok.Data;
import org.apache.commons.validator.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

import static com.owl.thingsbot.enums.Query.COMMAND_NOT_FOUND;
import static com.owl.thingsbot.enums.QueryType.*;

/**
 * Bot userMessage
 * Starts with /
 * Arguments are separated by space
 */
@Data
public class Command {

    private String userMessage;
    private String siteUrl;
    private Query query;
    private Long chatId;
    private Update update;
    private CallbackQuery callbackQuery;
    private Message message;
    private User from;
    private QueryType queryType;
    private Integer messageId;

    public Command(String command) {
        commandValidation(command);
    }

    public Command(Update update) {
        if (Objects.nonNull(update.getMessage())) {
            setChatId(update.getMessage().getChatId());
            setMessage(update.getMessage());
            setFrom(update.getMessage().getFrom());
            setQueryType(MESSAGE);
            setMessageId(update.getMessage().getMessageId());
            commandValidation(update.getMessage().getText());
        } else {
            setChatId(update.getCallbackQuery().getMessage().getChatId());
            setCallbackQuery(update.getCallbackQuery());
            setFrom(update.getCallbackQuery().getFrom());
            setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            setQueryType(CALLBACK_QUERY);
            commandValidation(update.getCallbackQuery().getData());
        }
        setUpdate(update);
    }

    private void commandValidation(String command) {
        UrlValidator urlValidator = new UrlValidator();
        if (urlValidator.isValid(command) || urlValidator.isValid("https://" + command)) {
            setSiteUrl(command);
            setQuery(Query.SITE);
            setUserMessage(command);
        } else {
            Query userQuery = Query.getByCommandName(command);
            if (userQuery.equals(COMMAND_NOT_FOUND)) {
                String[] args = command.split(" ");
                if (args.length > 1) {
                    userQuery = Query.getByCommandName(args[0]);
                    if (userQuery.equals(COMMAND_NOT_FOUND)) {
                        setUserMessage(command);
                    } else {
                        setUserMessage(command.split(args[0])[1]);
                    }
                } else {
                    setUserMessage(command);
                }
            } else {
                setUserMessage(command);
            }
            setQuery(userQuery);
        }
    }
}
