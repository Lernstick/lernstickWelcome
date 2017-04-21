/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 * This class takes {@link Processable} objects and runs them in a new thread 
 * sequentially.
 * <br>
 * The TaskProcessor binds its properties (progress, title, message and exception)
 * to the corrently processing {@link Task} of the {@link Processable}.
 * <br>
 * If the TaskProcessor is finished, the finished Property is set to true.
 * <br>
 * If a processable throws an error the whole process stops and finished is set to true.
 * 
 * @author sschw
 */
public class TaskProcessor {
    private final static Logger LOGGER = Logger.getLogger(TaskProcessor.class.getName());
    private final List<Processable> tasks;
    /**
     * Value represents progress by binding it to the values of the tasks.<br>
     * Tasks need to use progress.
     */
    private final DoubleProperty progress = new SimpleDoubleProperty();

    private final BooleanProperty finished = new SimpleBooleanProperty();
    
    private final ObjectProperty<Exception> exception = new SimpleObjectProperty<>();

    private final StringProperty title = new SimpleStringProperty();

    private final StringProperty message = new SimpleStringProperty();

    public TaskProcessor(List<Processable> tasks) {
        this.tasks = tasks;
    }

    /**
     * Run the TaskProcessor.
     */
    public void run() {
        // Reset all values
        resetTaskProcessor();
        
        // Get Tasks from Processable
        List<Task> taskList = tasks.stream().map(t -> t.newTask()).collect(Collectors.toList());
        
        // Binding progress to tasks
        progress.bind(Bindings.createDoubleBinding(
                () -> taskList.stream().mapToDouble(
                        t -> t.getProgress() // Add progress to bind
                ).sum() / tasks.size(),
                taskList.stream().map(t -> t.progressProperty()).toArray(s -> new ReadOnlyDoubleProperty[s])));

        new Thread(() -> {
            Iterator<Task> iterator = taskList.iterator();
            
            // Running a task might throw an exception
            // If an exception is thrown the while has to be interrupted
            try {
                // As long we have tasks and there is no exception.
                while(iterator.hasNext() && exception.getValue() == null) {
                    Task t = iterator.next();
                    // Bind the values in GUI Thread.
                    Platform.runLater(() -> {
                        title.bind(t.titleProperty());
                        message.bind(t.messageProperty());
                        exception.bind(t.exceptionProperty());
                    });
                    // Run the task
                    t.run();
                    // Ensure the task is finished
                    // If the task had an exception, throw it.
                    t.get();
                    // Unbind the values in GUI Thread.
                    Platform.runLater(() -> {
                        title.unbind();
                        message.unbind();
                        exception.unbind();
                    });
                }
                Platform.runLater(() -> {
                    title.set("TaskProcessor.finishedTitle");
                    message.set("TaskProcessor.finishedMessage");
                });
            } catch(ExecutionException ex) {
                LOGGER.log(Level.INFO, "Task throwed an exception", ex);
            } catch(InterruptedException ex) {
                LOGGER.log(Level.WARNING, "Save task got interrupted", ex);
            } finally {
                // If leaving this method, the task processor has finished its work.
                Platform.runLater(() -> finished.set(true));
            }
        }).start();
    }

    /**
     * Resets the TaskProcessor Properties.
     */
    private void resetTaskProcessor() {
        finished.set(false);
        title.unbind();
        message.unbind();
        exception.unbind();
        exception.set(null);
    }

    public DoubleProperty progressProperty() {
        return progress;
    }
    
    public BooleanProperty finishedProperty() {
        return finished;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public ObjectProperty<Exception> exceptionProperty() {
        return exception;
    }
}
