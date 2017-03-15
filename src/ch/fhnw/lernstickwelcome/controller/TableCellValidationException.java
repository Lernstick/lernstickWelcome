/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

/**
 *
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
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
}
