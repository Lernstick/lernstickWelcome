/*
 * Copyright (C) 2020 Ronny Standtke <ronny.standtke@gmx.net>
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
package ch.fhnw.lernstickwelcome.view.impl;

import javafx.scene.Node;

/**
 * An item in the main menu of the welcome application
 * 
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class MainMenuItem {

    private Node node;
    private String text;
    private String imagePath;

    public MainMenuItem(String imagePath, String displayText, Node node) {
        if (imagePath != null) {
            this.imagePath = getClass().getResource(imagePath).toExternalForm();
        }
        this.text = displayText;
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public String getText() {
        return text;
    }

    public String getImagePath() {
        return imagePath;
    }
}
