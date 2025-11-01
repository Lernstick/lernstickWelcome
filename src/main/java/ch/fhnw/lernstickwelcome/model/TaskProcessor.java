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

import ch.fhnw.lernstickwelcome.controller.WelcomeApplication;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import javafx.stage.Stage;

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

    private static final Logger LOGGER
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

    private final StringProperty image = new SimpleStringProperty();

    private Stage progressStage;

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
                () -> taskList.stream()
                        .mapToDouble(
                                t -> t.getProgress() > 0 ? t.getProgress() : 0)
                        .sum() / tasks.size(),
                taskList.stream()
                        .map(t -> t.progressProperty())
                        .toArray(s -> new ReadOnlyDoubleProperty[s])));

        new Thread(() -> {
            Iterator<Task<String>> iterator = taskList.iterator();

            // Running a task might throw an exception.
            // If an exception is thrown the while loop has to be interrupted.
            try {
                // As long we have tasks and there is no exception.
                while (iterator.hasNext() && exception.getValue() == null) {
                    Task<String> task = iterator.next();
                    // Bind the values in GUI Thread.
                    Platform.runLater(() -> {
                        title.bind(task.titleProperty());
                        message.bind(task.messageProperty());
                        image.bind(task.valueProperty());
                        exception.bind(task.exceptionProperty());
                    });
                    // Run the task
                    task.run();
                    // Ensure the task is finished
                    // If the task had an exception, throw it.
                    task.get();
                    // Unbind the values in GUI Thread.
                    Platform.runLater(() -> {
                        title.unbind();
                        message.unbind();
                        image.unbind();
                        image.set(null);
                        exception.unbind();
                    });
                }

                WelcomeApplication.playNotifySound();

                Platform.runLater(() -> {
                    image.set(null);
                    title.set("TaskProcessor.finishedTitle");
                    message.set("TaskProcessor.finishedMessage");
                });

                // show the finished message for a short while
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "", ex);
                }

            } catch (Exception ex) {
                WelcomeApplication.showThrowable(ex);

            } finally {
                Platform.runLater(() -> progressStage.close());
            }

        }).start();
    }

    public void setProgressStage(Stage progressStage) {
        this.progressStage = progressStage;
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

    public StringProperty imageProperty() {
        return image;
    }

    public ObjectProperty<Throwable> exceptionProperty() {
        return exception;
    }

    private void resetTaskProcessor() {
        finished.set(false);
        title.unbind();
        message.unbind();
        image.unbind();
        exception.unbind();
        exception.set(null);
    }
}
