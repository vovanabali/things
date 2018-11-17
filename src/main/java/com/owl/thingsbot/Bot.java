package com.owl.thingsbot;

import com.owl.thingsbot.entity.SiteInformation;
import com.owl.thingsbot.enums.Query;
import com.owl.thingsbot.service.SiteInformationService;
import com.owl.thingsbot.utils.Command;
import com.owl.thingsbot.utils.Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

import static com.owl.thingsbot.enums.Query.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botUserName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${botInformation}")
    private String botInformation;
    @Value("${channel}")
    private String channel;
    @Value("${adminName}")
    private String adminName;

    private Command lastAdminCommand = null;

    private final SiteInformationService informationService;
    private SiteInformation information = new SiteInformation();


    @Override
    public void onUpdateReceived(Update update) {
        Command command = new Command(update);
        deleteMessage(command);
        try {
            if (isAdmin(command.getFrom())) {
                adminActions(command);
            } else {
                userActions(command);
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка обработки данный");
            sendButtons(command);
        }
    }

    private void deleteMessage(final Command command) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(command.getMessageId());
        deleteMessage.setChatId(command.getChatId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.info("Filed to delete message by id");
        }
    }

    private void callbackQueryProcessing(Command command) throws TelegramApiException {
        if (isAdmin(command.getFrom())) {
            if (Objects.nonNull(lastAdminCommand)) {
                switch (lastAdminCommand.getQuery()) {
                    case ADD_SITE: {
                        if (Objects.nonNull(informationService.getSiteByURL(command.getSiteUrl()))) {
                            sendMessage(command, "Такой URL уже есть в базе данных, поробуйте другой или внесите изменения в старый");
                            return;
                        }
                        information.setUri(command.getSiteUrl());
                        lastAdminCommand.setQuery(ADD_SITE_DESCRIPTION);
                        sendMessage(command, "Отправте описание сайта");
                        break;
                    }
                    case ADD_SITE_DESCRIPTION: {
                        information.setDescription(command.getUserMessage());
                        informationService.save(information);
                        information = new SiteInformation();
                        sendMessage(command, "Запись успешно добавлена");
                        lastAdminCommand = null;
                        getAdminButtons(command);
                        break;
                    }
                    case DELETE_SITE_BY_ID: {
                        informationService.deleteById(Utils.getIdFromCommand(command));
                        lastAdminCommand = null;
                        sendMessage(command, "Запись успешно удалена");
                        getAdminButtons(command);
                        break;
                    }
                    case EDIT_SITE_URL_BY_ID: {
                        SiteInformation siteInformation = informationService.getById(Utils.getIdFromCommand(lastAdminCommand));
                        if (Objects.nonNull(siteInformation)) {
                            siteInformation.setUri(command.getSiteUrl());
                            informationService.save(siteInformation);
                            sendMessage(command, "URL сайта успешно изменён");
                            lastAdminCommand = null;
                            getAdminButtons(command);
                            break;
                        }
                        sendMessage(command, "Не удалось изменить URL сайта");
                        lastAdminCommand = null;
                        getAdminButtons(command);
                        break;
                    }
                    case EDIT_SITE_DESCRIPTION_BY_ID: {
                        SiteInformation siteInformation = informationService.getById(Utils.getIdFromCommand(lastAdminCommand));
                        if (Objects.nonNull(siteInformation)) {
                            siteInformation.setDescription(command.getUserMessage());
                            informationService.save(siteInformation);
                            sendMessage(command, "Описание сайта успешно изменёно");
                            lastAdminCommand = null;
                            getAdminButtons(command);
                            break;
                        }
                        sendMessage(command, "Не удалось изменить описание сайта");
                        getAdminButtons(command);
                        lastAdminCommand = null;
                        break;
                    }
                    default:
                        break;
                }
                return;
            }
            lastAdminCommand = null;
            switch (command.getQuery()) {
                case ADMINISTRATION_PANEL:
                    getAdminButtons(command);
                    deleteMessage(command);
                    break;
                case ADD_SITE:
                    sendMessage(command, "Отправте URL сайта");
                    lastAdminCommand = command;
                    lastAdminCommand.setQuery(ADD_SITE);
                    break;
                case LIST:
                    String sendTest = Utils.getListToResponse(informationService.getInformationsList());
                    sendMessage(command, sendTest.isEmpty() ? "База в данный момент пуста" : sendTest);
                    break;
                case DELETE_SITE:
                    if (Utils.emptyIfNull(informationService.getAll()).isEmpty()) {
                        sendMessage(command, "Базу пуста");
                    } else {
                        sendSiteListButtons(command, DELETE_SITE, "Выберите сайт для удаления");
                        lastAdminCommand = command;
                        lastAdminCommand.setQuery(DELETE_SITE_BY_ID);
                    }
                    break;
                case EDIT_SITE_URL_LIST:
                    sendSiteListButtons(command, EDIT_SITE_URL, "Выберите URL для изменения");
                    break;
                case EDIT_SITE_DESCRIPTION_LIST:
                    sendSiteListButtons(command, EDIT_SITE_DESCRIPTION, "Выберите URL для изменения описания");
                    break;
                case EDIT_SITE_URL:
                    updateLastAdminMessage(command, "Отправте новый URL сайта", EDIT_SITE_URL_BY_ID);
                    break;
                case EDIT_SITE_DESCRIPTION:
                    updateLastAdminMessage(command, "Отправте новое описание сайта", EDIT_SITE_DESCRIPTION_BY_ID);
                    break;
                default:
                    sendButtons(command);
                    break;
            }
        }
    }

    private void updateLastAdminMessage(Command command, String message, Query newQuery) throws TelegramApiException {
        sendMessage(command, message);
        lastAdminCommand = command;
        lastAdminCommand.setQuery(newQuery);
    }

    private void adminActions(Command command) throws TelegramApiException {
        if (Objects.nonNull(lastAdminCommand)) {
            callbackQueryProcessing(command);
        } else {
            switch (command.getQueryType()) {
                case CALLBACK_QUERY:
                    callbackQueryProcessing(command);
                    break;
                case MESSAGE:
                    messageProcessing(command);
                    break;
                default:
                    sendButtons(command);
                    break;
            }
        }
    }

    private void userActions(Command command) {
        messageProcessing(command);
    }

    private void sendSiteListButtons(Command command, Query query, String message) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(command.getChatId())
                .setText(message);
        sendMessage.enableMarkdown(true);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        Utils.emptyIfNull(informationService.getAll()).forEach(information1 -> {
            rowsInline.add(Collections.singletonList(
                    new InlineKeyboardButton()
                            .setText(information1.getUri())
                            .setCallbackData(
                                    query.getCommandName()
                                            .concat(" ")
                                            .concat(String.valueOf(information1.getId()))
                            )
            ));
        });
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошыбка отправки панели упарвления");
        }
    }

    private void messageProcessing(Command command) {
        try {
            final String urlnfo = "Для получения информации о сайте отправте его URL адресс";
            switch (command.getQuery()) {
                case START:
                    sendMessage(command , urlnfo, getSettingsKeyboard(command));
                    break;
                case LIST:
                    String sendTest = Utils.getListToResponse(informationService.getInformationsList());
                    sendMessage(command, sendTest.isEmpty() ? "База в данный момент пуста" : sendTest);
                    sendMessage(command , urlnfo, getSettingsKeyboard(command));
                    break;
                case SITE:
                    List<String> siteInformation = Utils.emptyIfNull(informationService.getSitesByUrl(command.getUserMessage())).stream().map(Objects::toString).collect(Collectors.toList());
                    sendMessage(command, siteInformation.isEmpty() ? "Данный сайт отсутствует в нашей базе, попроуйте позже" : Utils.getListToResponse(siteInformation));
                    break;
                case HELP: {
                    sendMessage(command , urlnfo, getSettingsKeyboard(command));
                    sendButtons(command);
                    break;
                }
                case ADMINISTRATION_PANEL:
                    if (isAdmin(command.getFrom())) {
                        getAdminButtons(command);
                        return;
                    }
                    sendMessage(command , urlnfo, getSettingsKeyboard(command));
                    break;
                default:
                    sendButtons(command);
                    sendMessage(command , urlnfo, getSettingsKeyboard(command));
                    break;
            }
        } catch (TelegramApiException e) {
            log.error("Error while replying to userMessage: ", command.getMessage(), e);
        }
    }

    private void sendButtons(Command command) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(command.getChatId())
                .setText(botInformation);
        sendMessage.enableMarkdown(true);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Список сайтов").setCallbackData(LIST.getCommandName())));
        rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Группа с акциями").setUrl("tg://resolve?domain=".concat(channel))));
        if (isAdmin(command.getFrom())) {
            rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Панель администатора").setCallbackData(ADMINISTRATION_PANEL.getCommandName())));
        }
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошыбка отправки панели упарвления");
        }
    }

    private void getAdminButtons(Command command) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage()
                .setChatId(command.getChatId())
                .setText("Для управления ботом используйте следующие команды");
        sendMessage.enableMarkdown(true);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Добавит сайт").setCallbackData(ADD_SITE.getCommandName())));
        rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Изменить URL сайта").setCallbackData(EDIT_SITE_URL_LIST.getCommandName())));
        rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Изменить описание сайта").setCallbackData(EDIT_SITE_DESCRIPTION_LIST.getCommandName())));
        rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Список всех сайтов").setCallbackData(LIST.getCommandName())));
        rowsInline.add(Collections.singletonList(new InlineKeyboardButton().setText("Удалить сайт и информацию о нём").setCallbackData(DELETE_SITE.getCommandName())));
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
        execute(sendMessage);
    }

    private ReplyKeyboardMarkup getSettingsKeyboard(Command command) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(HELP.getCommandName());
        if (isAdmin(command.getFrom())) {
            keyboardFirstRow.add(ADMINISTRATION_PANEL.getCommandName());
        }
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private void sendMessage(Command command, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(command.getChatId());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(text);
        sendMessage.disableWebPagePreview();
        execute(sendMessage);
    }

    private void sendMessage(Command command, String text, ReplyKeyboardMarkup keyboard) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(command.getChatId());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.disableWebPagePreview();
        execute(sendMessage);
    }

    private boolean isAdmin(User user) {
        return Objects.equals(adminName, user.getUserName());
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
