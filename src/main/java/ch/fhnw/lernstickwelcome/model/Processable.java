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
