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
package ch.fhnw.lernstickwelcome.view.impl;

import javafx.scene.Parent;

/**
 *
 * @author sschw
 */
public class MenuPaneItem {
    private Parent parentScene;
    private String displayText;
    private String imagePath;

    public MenuPaneItem(Parent parentScene, String displayText, String imagePath) {
        this.parentScene = parentScene;
        this.displayText = displayText;
        if (imagePath != null) {
            this.imagePath = MenuPaneItem.class.getResource(imagePath).toExternalForm();
        }
    }

    public Parent getParentScene() {
        return parentScene;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getImagePath() {
        return imagePath;
    }
}
