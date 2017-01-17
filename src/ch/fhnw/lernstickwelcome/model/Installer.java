/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import java.util.List;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;

/**
 *
 * @author user
 */
public class Installer {
    private final List<Task> tasks;
    /**
     * Value represents progress by binding it to the values of the tasks.<br>
     * Tasks need to use progress.<br>
     * It's also possible to use progress besides workDone and totalWork.<br>
     * <i>This allows to set a task with multiple subtasks which update the progress</i>
     */
    private final DoubleProperty progress = new SimpleDoubleProperty();
    
    public Installer(List<Task> tasks) {
        this.tasks = tasks;
        // Binding progress to tasks
        progress.bind(Bindings.createDoubleBinding(
                () -> tasks.stream().mapToDouble(
                        t -> t.getProgress()/t.getTotalWork() + // Add progress to bind
                                (t.getWorkDone()-1)/t.getTotalWork() // Add work to bind
                ).sum()/tasks.size(), 
                tasks.stream().map(t -> Stream.of(t.progressProperty(), t.workDoneProperty(), t.totalWorkProperty())).toArray(s -> new ReadOnlyDoubleProperty[s])));
    }
    
    public void install() {
        new Thread(() -> {
            for(Task t : tasks) {
                t.run();
            }
        }).start();
    }
    
    public DoubleProperty progressProperty() {
        return progress;
    }
}
