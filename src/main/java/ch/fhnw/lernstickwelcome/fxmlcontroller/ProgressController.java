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
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author user
 */
public class ProgressController implements Initializable {

    @FXML
    private ProgressBar pbInstBar;
    @FXML
    private Label lbInstalltitle;
    @FXML
    private Label lbInstPrc;
    @FXML
    private Label lbMesage;
    @FXML
    private ImageView ivValue;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }   

    public ProgressBar getPbInstBar() {
        return pbInstBar;
    }

    public Label getLbInstalltitle() {
        return lbInstalltitle;
    }

    public Label getLbInstPrc() {
        return lbInstPrc;
    }

    public Label getLbMesage() {
        return lbMesage;
    }

    public ImageView getIvValue() {
        return ivValue;
    }
}
