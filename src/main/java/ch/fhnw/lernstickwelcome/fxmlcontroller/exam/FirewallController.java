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
package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

import ch.fhnw.lernstickwelcome.controller.exception.TableCellValidationException;
import ch.fhnw.lernstickwelcome.controller.exception.ValidationException;
import ch.fhnw.lernstickwelcome.model.firewall.HostFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter.SearchPattern;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.lernstickwelcome.view.impl.FirewallDeleteButtonCell;
import ch.fhnw.lernstickwelcome.view.impl.FirewallEditButtonCell;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import ch.fhnw.lernstickwelcome.view.impl.ValidatableTextFieldCell;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

/**
 * Firewall controller class
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class FirewallController implements Initializable {

    @FXML
    private Button helpButton;
    @FXML
    private ToggleSwitch monitoringToggleSwitch;
    @FXML
    private Label monitoringLabel;
    @FXML
    private TableView<WebsiteFilter> allowedSitesTableView;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter.SearchPattern> sitesPatternTableColumn;
    @FXML
    private TableColumn<WebsiteFilter, String> sitesCriteriaTableColumn;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> editSitesTableColumn;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> deleteSitesTableColumn;
    @FXML
    private ComboBox<WebsiteFilter.SearchPattern> addEditPatternComboBox;
    @FXML
    private TextField addEditCriteriaTextField;
    @FXML
    private Button addEditSiteButton;
    @FXML
    private Button checkForDependenciesButton;
    @FXML
    private TableView<HostFilter> allowedServersTableView;
    @FXML
    private TableColumn<HostFilter, HostFilter.Protocol> serverProtocolTableColumn;
    @FXML
    private TableColumn<HostFilter, String> serverHostTableColumn;
    @FXML
    private TableColumn<HostFilter, String> serverPortRangeTableColumn;
    @FXML
    private TableColumn<HostFilter, String> serverDescriptionTableColumn;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> editServerTableColumn;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> deleteServerTableColumn;
    @FXML
    private ComboBox<HostFilter.Protocol> addEditProtocolComboBox;
    @FXML
    private TextField addEditHostTextField;
    @FXML
    private TextField addEditPortTextField;
    @FXML
    private TextField addEditDescriptionTextField;
    @FXML
    private Button addEditServerButton;

    private ResourceBundle resourceBundle;

    private int indexSaveWebsiteFilter;
    private int indexSaveHostFilter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.resourceBundle = resourceBundle;

        sitesPatternTableColumn.setCellValueFactory(
                p -> p.getValue().searchPatternProperty());

        sitesPatternTableColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                new SearchPatternStringConverter(resourceBundle),
                WebsiteFilter.SearchPattern.values()));

        sitesPatternTableColumn.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            WebsiteFilter websiteFilter = e.getTableView().getItems().get(row);
            websiteFilter.searchPatternProperty().setValue(e.getNewValue());
        });

        sitesCriteriaTableColumn.setCellValueFactory(
                p -> p.getValue().searchCriteriaProperty());

        sitesCriteriaTableColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        sitesCriteriaTableColumn.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            WebsiteFilter websiteFilter = e.getTableView().getItems().get(row);
            websiteFilter.searchCriteriaProperty().setValue(e.getNewValue());
        });

        editSitesTableColumn.setCellFactory(
                c -> new FirewallEditButtonCell(this, c.getTableView()));
        deleteSitesTableColumn.setCellFactory(
                p -> new FirewallDeleteButtonCell(this, p.getTableView()));

        serverProtocolTableColumn.setCellValueFactory(
                p -> p.getValue().protocolProperty());

        serverProtocolTableColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(HostFilter.Protocol.values()));

        serverProtocolTableColumn.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            HostFilter hostFilter = e.getTableView().getItems().get(row);
            hostFilter.protocolProperty().setValue(e.getNewValue());
        });

        serverHostTableColumn.setCellFactory(
                ValidatableTextFieldCell.forTableColumn((s, i) -> {
                    try {
                        WelcomeUtil.checkTarget((String) s, i);
                        return null;
                    } catch (TableCellValidationException ex) {
                        return ex;
                    }
                }, resourceBundle));

        serverHostTableColumn.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            HostFilter hostFilter = e.getTableView().getItems().get(row);
            hostFilter.hostProperty().setValue(e.getNewValue());
        });

        serverHostTableColumn.setCellValueFactory(
                p -> p.getValue().hostProperty());

        serverPortRangeTableColumn.setCellFactory(
                ValidatableTextFieldCell.forTableColumn((s, i) -> {
                    try {
                        WelcomeUtil.checkPortRange((String) s, i);
                        return null;
                    } catch (TableCellValidationException ex) {
                        return ex;
                    }
                }, resourceBundle));

        serverPortRangeTableColumn.setCellValueFactory(
                p -> p.getValue().portRangeProperty());

        serverPortRangeTableColumn.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            HostFilter hostFilter = e.getTableView().getItems().get(row);
            hostFilter.portRangeProperty().setValue(e.getNewValue());
        });

        serverDescriptionTableColumn.setCellValueFactory(
                p -> p.getValue().descriptionProperty());

        serverDescriptionTableColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        serverDescriptionTableColumn.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            HostFilter hostFilter = e.getTableView().getItems().get(row);
            hostFilter.descriptionProperty().setValue(e.getNewValue());
        });

        editServerTableColumn.setCellFactory(
                c -> new FirewallEditButtonCell(this, c.getTableView()));
        deleteServerTableColumn.setCellFactory(
                p -> new FirewallDeleteButtonCell(this, p.getTableView()));

        // load ComboBox data
        addEditPatternComboBox.setConverter(
                new SearchPatternStringConverter(resourceBundle));
        addEditPatternComboBox.getItems().addAll(
                WebsiteFilter.SearchPattern.values());
        
        addEditProtocolComboBox.getItems().addAll(HostFilter.Protocol.values());
    }

    public TableView<WebsiteFilter> getAllowedSitesTableView() {
        return allowedSitesTableView;
    }

    public TableView<HostFilter> getAllowedServersTableView() {
        return allowedServersTableView;
    }

    public ToggleSwitch getAllowMonitoringToggleSwitch() {
        return monitoringToggleSwitch;
    }

    public ComboBox<WebsiteFilter.SearchPattern> getAddEditPatternComboBox() {
        return addEditPatternComboBox;
    }

    public TextField getAddEditCriteriaTextField() {
        return addEditCriteriaTextField;
    }

    public Button getAddEditSiteButton() {
        return addEditSiteButton;
    }

    public Button getAddEditServerButton() {
        return addEditServerButton;
    }

    public TextField getAddEditDescriptionTextField() {
        return addEditDescriptionTextField;
    }

    public TextField getAddEditPortTextField() {
        return addEditPortTextField;
    }

    public TextField getAddEditHostTextField() {
        return addEditHostTextField;
    }

    public Button getCheckForDependenciesButton() {
        return checkForDependenciesButton;
    }

    public ComboBox<HostFilter.Protocol> getAddEditProtocolComboBox() {
        return addEditProtocolComboBox;
    }

    public Label getMonitoringLabel() {
        return monitoringLabel;
    }

    public Button getHelpButton() {
        return helpButton;
    }

    public int getIndexSaveWebsiteFilter() {
        return indexSaveWebsiteFilter;
    }

    public int getIndexSaveIpFilter() {
        return indexSaveHostFilter;
    }

    public void setIndexSaveWebsiteFilter(int i) {
        indexSaveWebsiteFilter = i;
    }

    public void setIndexSaveHostFilter(int i) {
        indexSaveHostFilter = i;
    }

    @FXML
    private void onClickNewWebsiteRule(MouseEvent event) {
        if (validateSitesFields()) {
            // Add to table
            if (addEditSiteButton.getStyleClass().contains("btn_add")) {
                allowedSitesTableView.getItems().add(new WebsiteFilter(
                        addEditPatternComboBox.getValue(),
                        addEditCriteriaTextField.getText()));
            } // Edit
            else {
                WebsiteFilter element = 
                        (WebsiteFilter) allowedSitesTableView.getItems().get(
                                indexSaveWebsiteFilter);
                
                element.searchPatternProperty().set(
                        addEditPatternComboBox.getValue());
                
                element.searchCriteriaProperty().set(
                        addEditCriteriaTextField.getText());
                
                addEditSiteButton.getStyleClass().remove("btn_save");
                addEditSiteButton.getStyleClass().add("btn_add");
            }
            // Clear fields
            addEditPatternComboBox.setValue(null);
            addEditCriteriaTextField.setText("");
        }
    }

    @FXML
    private void onClickNewServerRule(MouseEvent event) {
        if (validateServerFields()) {
            // Add to table
            if (addEditServerButton.getStyleClass().contains("btn_add")) {
                allowedServersTableView.getItems().add(new HostFilter(
                        addEditProtocolComboBox.getValue(),
                        addEditHostTextField.getText(),
                        addEditPortTextField.getText(),
                        addEditDescriptionTextField.getText()));
            } // Edit
            else {
                HostFilter element = 
                        (HostFilter) allowedServersTableView.getItems().get(
                                indexSaveHostFilter);
                
                element.protocolProperty().set(
                        addEditProtocolComboBox.getValue());
                
                element.hostProperty().set(addEditHostTextField.getText());
                element.portRangeProperty().set(addEditPortTextField.getText());
                
                element.descriptionProperty().set(
                        addEditDescriptionTextField.getText());
                
                addEditServerButton.getStyleClass().remove("btn_save");
                addEditServerButton.getStyleClass().add("btn_add");
            }
            // Clear fields
            addEditProtocolComboBox.setValue(null);
            addEditHostTextField.setText("");
            addEditPortTextField.setText("");
            addEditDescriptionTextField.setText("");
        }
    }

    private boolean validateSitesFields() {
        boolean result = true;
        if (!addEditPatternComboBox.getSelectionModel().isEmpty()) {
            addEditPatternComboBox.getStyleClass().remove("error");
        } else {
            addEditPatternComboBox.getStyleClass().add("error");
            result = false;
        }
        if (addEditCriteriaTextField.getText().length() > 0) {
            addEditCriteriaTextField.getStyleClass().remove("error");
        } else {
            addEditCriteriaTextField.getStyleClass().add("error");
            result = false;
        }
        return result;
    }

    private boolean validateServerFields() {
        boolean result = true;
        if (!addEditProtocolComboBox.getSelectionModel().isEmpty()) {
            addEditProtocolComboBox.getStyleClass().remove("error");
        } else {
            addEditProtocolComboBox.getStyleClass().add("error");
            result = false;
        }
        try {
            WelcomeUtil.checkTarget(addEditHostTextField.getText(), -1);
            addEditHostTextField.getStyleClass().remove("error");
        } catch (ValidationException ex) {
            addEditHostTextField.setPromptText(MessageFormat.format(
                    resourceBundle.getString(ex.getMessage()),
                    ex.getMessageDetails()));
            addEditHostTextField.getStyleClass().add("error");
            result = false;
        }
        try {
            WelcomeUtil.checkPortRange(addEditPortTextField.getText(), -1);
            addEditPortTextField.getStyleClass().remove("error");
        } catch (ValidationException ex) {
            addEditPortTextField.setPromptText(MessageFormat.format(
                    resourceBundle.getString(ex.getMessage()),
                    ex.getMessageDetails()));
            addEditPortTextField.getStyleClass().add("error");
            result = false;
        }
        return result;
    }

    private static class SearchPatternStringConverter
            extends StringConverter<SearchPattern> {

        private final ResourceBundle resourceBundle;

        private SearchPatternStringConverter(ResourceBundle resourceBundle) {
            this.resourceBundle = resourceBundle;
        }

        @Override
        public String toString(SearchPattern searchPattern) {
            return resourceBundle.getString(searchPattern.toString());
        }

        @Override
        public SearchPattern fromString(String string) {

            if (string.equals(resourceBundle.getString(
                    SearchPattern.Exact.toString()))) {
                return SearchPattern.Exact;
            }

            if (string.equals(resourceBundle.getString(
                    SearchPattern.StartsWith.toString()))) {
                return SearchPattern.StartsWith;
            }

            if (string.equals(resourceBundle.getString(
                    SearchPattern.Contains.toString()))) {
                return SearchPattern.Contains;
            }

            return SearchPattern.Custom;
        }
    }
}
