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

/**
 * Thrown if a validation fails inside a table.
 * 
 * @see ValidationException
 * @author sschw
 */
public class TableCellValidationException extends ValidationException {
    int row;
    int col;
    
    public TableCellValidationException(String message, int row, int col) {
        super(message);
        this.row = row;
        this.col = col;
    }
    
    public TableCellValidationException(String message, int row, int col, Object... details) {
        super(message, details);
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
}
