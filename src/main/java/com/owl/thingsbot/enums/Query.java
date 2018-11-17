package com.owl.thingsbot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
@Getter
public enum Query {
    SITE(""),
    LIST("Список сайтов"),
    GROUP_INFORMATION("Скидки и акции"),
    START("/start"),
    COMMAND_NOT_FOUND(""),
    ADD_SITE("add_site"),
    ADMINISTRATION_PANEL("Панель администартора"),
    ADD_SITE_DESCRIPTION("/add_site_description"),
    DELETE_SITE("/delete_site"),
    DELETE_SITE_BY_ID("/delete_site_by_id"),
    EDIT_SITE_URL_LIST("/edit_site_url_list"),
    EDIT_SITE_URL("/edit_site_url"),
    EDIT_SITE_URL_BY_ID("/edit_site_url_by_id"),
    EDIT_SITE_DESCRIPTION_LIST("/edit_site_description_list"),
    EDIT_SITE_DESCRIPTION("/edit_site_description"),
    EDIT_SITE_DESCRIPTION_BY_ID("/edit_site_description_by_id"),
    HELP("Помощь");

    private String commandName;

    public static Query getByCommandName(String name) {
        return Arrays.stream(Query.values())
                .filter(type -> Objects.equals(type.commandName, name))
                .findFirst().orElse(COMMAND_NOT_FOUND);
    }
}
