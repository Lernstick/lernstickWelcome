/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import javafx.concurrent.Task;

/**
 * Interface for a class that can be described by a Task.
 * <p>
 Subclasses of this class have to implement newTask which return a task, which
 are responsible to schedule the process of the processable class.
 * 
 * @author sschw
 */
public interface Processable<T> {
    /**
     * Return a new instance of a task representing the {@link Processable}.
     * @return new Instance of the Task representing the {@link Processable}
     */
    public abstract Task<T> newTask();
}
