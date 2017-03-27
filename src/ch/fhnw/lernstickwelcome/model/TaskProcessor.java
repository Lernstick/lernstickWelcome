/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import java.util.Iterator;
import java.util.List;
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
 *
 * @author user
 */
public class TaskProcessor {

    private final List<ResetableTask> tasks;
    /**
     * Value represents progress by binding it to the values of the tasks.<br>
     * Tasks need to use progress.
     */
    private final DoubleProperty progress = new SimpleDoubleProperty();

    private final BooleanProperty finished = new SimpleBooleanProperty();
    
    private final ObjectProperty<Exception> exception = new SimpleObjectProperty<>();

    private final StringProperty title = new SimpleStringProperty();

    private final StringProperty message = new SimpleStringProperty();

    public TaskProcessor(List<ResetableTask> tasks) {
        this.tasks = tasks;
        // Binding progress to tasks
        progress.bind(Bindings.createDoubleBinding(
                () -> tasks.stream().mapToDouble(
                        t -> t.getProgress() // Add progress to bind
                ).sum() / tasks.size(),
                tasks.stream().map(t -> t.progressProperty()).toArray(s -> new ReadOnlyDoubleProperty[s])));
    }

    public void run() {
        finished.set(false);
        tasks.forEach(t -> t.reset());
        new Thread(() -> {
            Iterator<ResetableTask> iterator = tasks.iterator();
            while(iterator.hasNext() && exception.getValue() == null) {
                Task t = iterator.next();
                Platform.runLater(() -> {
                    title.bind(t.titleProperty());
                    message.bind(t.messageProperty());
                    exception.bind(t.exceptionProperty());
                });
                t.run();
                Platform.runLater(() -> {
                    title.unbind();
                    message.unbind();
                    exception.unbind();
                });
            }
            if(exception.getValue() == null) {
                // If there was a rounding error still set the value to 100%.
                Platform.runLater(() -> {
                    progress.unbind();
                    progress.set(1);
                    finished.set(true);
                });
            } else {
                // Set progress to a undefined state.
                Platform.runLater(() -> {
                    progress.unbind();
                    progress.set(-1);
                    finished.set(true);
                });
            }
        }).start();
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
