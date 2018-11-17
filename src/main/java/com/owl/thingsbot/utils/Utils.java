package com.owl.thingsbot.utils;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.logging.log4j.util.Strings.EMPTY;

public class Utils {

    private Utils(){}

    public static String getListToResponse (List<String> strings) {
        StringBuilder response = new StringBuilder();
        emptyIfNull(strings).forEach(s -> {
            response.append(s);
            response.append("\n");
        });
        return response.toString().isEmpty() ? "На данный момент записей нет" : response.toString().concat("\n\n") ;
    }

    public static <T> List<T> emptyIfNull(List<T> collection) {
        return Objects.isNull(collection) ? new ArrayList() : collection;
    }

    public static Long getIdFromCommand(Command command) {
        if (Objects.nonNull(command)) {
            String[] splitMessage = command.getUserMessage().split(command.getQuery().getCommandName());
            if (splitMessage.length > 0) {
                return Long.valueOf(splitMessage[0].trim());
            }
            return null;
        }
        return null;
    }

    public static String getMessageFromCommand(Command command) {
        if (Objects.nonNull(command)) {
            String[] splitMessage = command.getUserMessage().split(command.getQuery().getCommandName());
            if (splitMessage.length > 0) {
                return splitMessage[1];
            }
            return EMPTY;
        }
        return EMPTY;
    }
}
