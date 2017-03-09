/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

/**
 * Thrown if a field on a GUI has an invalid value.
 * 
 * @author sschw
 */
public class ValidationException extends Exception {
    private String[] details;
    
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String... details) {
        super(message);
        this.details = details;
    }
    
    public String[] getMessageDetails() {
        return details;
    }
}
