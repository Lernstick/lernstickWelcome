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
 * Thrown if a field on a GUI has an invalid value.
 * 
 * @author sschw
 */
public class ValidationException extends Exception {
    private Object[] details;
    
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Object... details) {
        super(message);
        this.details = details;
    }
    
    public Object[] getMessageDetails() {
        if(details != null)
            return Arrays.copyOf(details, details.length);
        return new Object[0];
    }
}
