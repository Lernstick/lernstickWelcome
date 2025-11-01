/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.controller.exception;

import java.util.Arrays;

/**
 * Thrown if a backend process fails.
 *
 * @author sschw
 */
public class ProcessingException extends Exception {

    private final String titleKey;
    private Object[] details;

    public ProcessingException(String titleKey, String messageKey) {
        this(titleKey, messageKey, (Object) null);
    }

    public ProcessingException(String titleKey, String messageKey,
            Object... details) {

        super(messageKey);
        this.titleKey = titleKey;
        this.details = details;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public Object[] getMessageDetails() {
        if (details != null) {
            return Arrays.copyOf(details, details.length);
        }
        return new Object[0];
    }
}
