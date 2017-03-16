/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

/**
 * Thrown if a backend process fails.
 *
 * @author sschw
 */
public class ProcessingException extends Exception {
    private Object[] details;
    
    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Object... details) {
        super(message);
        this.details = details;
    }
    
    public Object[] getMessageDetails() {
        return details;
    }
}
