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
 * The TaskProcessor binds its properties (progress, title, message and
 * exception) to the corrently processing {@link Task} of the
 * {@link Processable}.
 * <br>
 * If the TaskProcessor is finished, the finished Property is set to true.
 * <br>
 * If a processable throws an error the whole process stops and finished is set
 * to true.
 *
 * @author sschw
 */
public class TaskProcessor {

    private final static Logger LOGGER
            = Logger.getLogger(TaskProcessor.class.getName());

    private final List<Processable<String>> tasks;
    /**
     * Value represents progress by binding it to the values of the tasks.<br>
     * Tasks need to use progress.
     */
    private final DoubleProperty progress = new SimpleDoubleProperty();

    private final BooleanProperty finished = new SimpleBooleanProperty();

    private final ObjectProperty<Throwable> exception
            = new SimpleObjectProperty<>();

    private final StringProperty title = new SimpleStringProperty();

    private final StringProperty message = new SimpleStringProperty();

    private final StringProperty value = new SimpleStringProperty();

    public TaskProcessor(List<Processable<String>> tasks) {
        this.tasks = tasks;
    }

    /**
     * Run the TaskProcessor.
     */
    public void run() {
        // Reset all values
        resetTaskProcessor();

        // Get Tasks from Processable
        List<Task<String>> taskList = tasks.stream()
                .map(t -> t.newTask())
                .collect(Collectors.toList());

        // Binding progress to tasks
        progress.bind(Bindings.createDoubleBinding(
                () -> taskList.stream().mapToDouble(
                        t -> t.getProgress() > 0 ? t.getProgress() : 0).sum()
                / tasks.size(),
                taskList.stream()
                        .map(t -> t.progressProperty())
                        .toArray(s -> new ReadOnlyDoubleProperty[s])));

        new Thread(() -> {
            Iterator<Task<String>> iterator = taskList.iterator();

            // Running a task might throw an exception
            // If an exception is thrown the while has to be interrupted
            try {
                // As long we have tasks and there is no exception.
                while (iterator.hasNext() && exception.getValue() == null) {
                    Task<String> t = iterator.next();
                    // Bind the values in GUI Thread.
                    Platform.runLater(() -> {
                        title.bind(t.titleProperty());
                        message.bind(t.messageProperty());
                        value.bind(t.valueProperty());
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
                        value.unbind();
                        value.set(null);
                        exception.unbind();
                    });
                }
                Platform.runLater(() -> {
                    value.set(null);
                    title.set("TaskProcessor.finishedTitle");
                    message.set("TaskProcessor.finishedMessage");
                });
            } catch (ExecutionException ex) {
                LOGGER.log(Level.INFO, "Task throwed an exception", ex);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, "Save task got interrupted", ex);
            } finally {
                // If leaving this method, the task processor has finished its
                // work.
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
        value.unbind();
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

    public StringProperty valueProperty() {
        return value;
    }

    public ObjectProperty<Throwable> exceptionProperty() {
        return exception;
    }
}
