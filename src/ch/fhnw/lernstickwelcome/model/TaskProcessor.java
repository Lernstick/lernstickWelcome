/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 *
 * @author user
 */
public class TaskProcessor {
    private final List<Task> tasks;
    /**
     * Value represents progress by binding it to the values of the tasks.<br>
     * Tasks need to use progress.
     */
    private final DoubleProperty progress = new SimpleDoubleProperty();
    
    private final StringProperty title = new SimpleStringProperty();
    
    private final StringProperty message = new SimpleStringProperty();
    
    public TaskProcessor(List<Task> tasks) {
        this.tasks = tasks;
        // Binding progress to tasks
        progress.bind(Bindings.createDoubleBinding(
                () -> tasks.stream().mapToDouble(
                        t -> t.getProgress() // Add progress to bind
                ).sum()/tasks.size(), 
                tasks.stream().map(t -> t.progressProperty()).toArray(s -> new ReadOnlyDoubleProperty[s])));
    }
    
    public void install() {
        new Thread(() -> {
            for(Task t : tasks) {
                title.bind(t.titleProperty());
                message.bind(t.messageProperty());
                t.run();
                title.unbind();
                message.unbind();
            }
        }).start();
    }
    
    public DoubleProperty progressProperty() {
        return progress;
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public StringProperty messageProperty() {
        return message;
    }
}
