/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import javafx.concurrent.Task;

/**
 * Abstract class for tasks that can be reseted.
 * 
 * @author sschw
 */
public abstract class ResetableTask<T> {
    
    public abstract Task<T> getTask();
}
