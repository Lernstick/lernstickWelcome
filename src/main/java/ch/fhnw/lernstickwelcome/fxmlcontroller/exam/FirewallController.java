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
package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

import ch.fhnw.lernstickwelcome.controller.exception.TableCellValidationException;
import ch.fhnw.lernstickwelcome.controller.exception.ValidationException;
import ch.fhnw.lernstickwelcome.model.firewall.IpFilter;
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
 * FXML Controller class
 *
 * @author user
 */
public class FirewallController implements Initializable {

    @FXML
    private Button helpButton;
    @FXML
    private ToggleSwitch monitoringToggleSwitch;
    @FXML
    private Label monitoringLabel;
    @FXML
    private TableView<WebsiteFilter> tvAllowedSites;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter.SearchPattern> tcSitesPattern;
    @FXML
    private TableColumn<WebsiteFilter, String> tcSitesCriteria;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tcSitesEdit;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tcSitesDelete;
    @FXML
    private ComboBox<WebsiteFilter.SearchPattern> cbAddEditPattern;
    @FXML
    private TextField tfAddEditCriteria;
    @FXML
    private Button btAddEditSite;
    @FXML
    private Button btCheckForDep;
    @FXML
    private TableView<IpFilter> tvAllowedServers;
    @FXML
    private TableColumn<IpFilter, IpFilter.Protocol> tcServerProtocol;
    @FXML
    private TableColumn<IpFilter, String> tcServerIp;
    @FXML
    private TableColumn<IpFilter, String> tcServerPort;
    @FXML
    private TableColumn<IpFilter, String> tcServerDesc;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tcServerEdit;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tcServerDelete;
    @FXML
    private ComboBox<IpFilter.Protocol> cbAddEditProtocol;
    @FXML
    private TextField tfAddEditIp;
    @FXML
    private TextField tfAddEditPort;
    @FXML
    private TextField tfAddEditDesc;
    @FXML
    private Button btAddEditServer;

    private ResourceBundle rb;

    private int indexSaveWebsiteFilter;
    private int indexSaveIpFilter;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.rb = rb;

        // Set TableView WebsiteFilter cell properties and implement edit functionality
        tcSitesPattern.setCellValueFactory(p -> p.getValue().searchPatternProperty());
        tcSitesPattern.setCellFactory(ComboBoxTableCell.forTableColumn(getSearchPatternStringConverter(), WebsiteFilter.SearchPattern.values()));
        tcSitesPattern.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                .searchPatternProperty().setValue(e.getNewValue()));
        tcSitesCriteria.setCellValueFactory(p -> p.getValue().searchCriteriaProperty());
        tcSitesCriteria.setCellFactory(TextFieldTableCell.forTableColumn());
        tcSitesCriteria.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                .searchCriteriaProperty().setValue(e.getNewValue()));
        tcSitesEdit.setCellFactory(c -> new FirewallEditButtonCell(this, c.getTableView()));
        tcSitesDelete.setCellFactory(p -> new FirewallDeleteButtonCell(this, p.getTableView()));

        // Set TableView IpFilter cell properties and implement edit functionality
        tcServerProtocol.setCellValueFactory(p -> p.getValue().protocolProperty());
        tcServerProtocol.setCellFactory(ComboBoxTableCell.forTableColumn(IpFilter.Protocol.values()));
        tcServerProtocol.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                .protocolProperty().setValue(e.getNewValue()));
        tcServerIp.setCellFactory(ValidatableTextFieldCell.forTableColumn((s, i) -> {
            try {
                WelcomeUtil.checkTarget((String) s, i);
                return null;
            } catch(TableCellValidationException ex) {
                return ex;
            }
        }, rb));
        tcServerIp.setOnEditCommit(e -> 
                e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .ipAddressProperty().setValue(e.getNewValue()));
        tcServerIp.setCellValueFactory(p -> p.getValue().ipAddressProperty());
        tcServerPort.setCellFactory(ValidatableTextFieldCell.forTableColumn((s, i) -> {
            try {
                WelcomeUtil.checkPortRange((String) s, i);
                return null;
            } catch(TableCellValidationException ex) {
                return ex;
            }
        }, rb));
        tcServerPort.setCellValueFactory(p -> p.getValue().portProperty());
        tcServerPort.setOnEditCommit(e -> 
                e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .portProperty().setValue(e.getNewValue()));
        tcServerDesc.setCellValueFactory(p -> p.getValue().descriptionProperty());
        tcServerDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        tcServerDesc.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                .descriptionProperty().setValue(e.getNewValue()));
        tcServerEdit.setCellFactory(c -> new FirewallEditButtonCell(this, c.getTableView()));
        tcServerDelete.setCellFactory(p -> new FirewallDeleteButtonCell(this, p.getTableView()));

        // Load ComboBox data
        cbAddEditPattern.setConverter(getSearchPatternStringConverter());
        cbAddEditPattern.getItems().addAll(WebsiteFilter.SearchPattern.values());
        cbAddEditProtocol.getItems().addAll(IpFilter.Protocol.values());
    }

    private StringConverter<SearchPattern> getSearchPatternStringConverter() {
        return new StringConverter<SearchPattern>() {
            @Override
            public String toString(SearchPattern t) {
                return rb.getString(t.toString());
            }

            @Override
            public SearchPattern fromString(String string) {
                if(rb.getString(SearchPattern.Exact.toString()).equals(string))
                    return SearchPattern.Exact;
                if(rb.getString(SearchPattern.StartsWith.toString()).equals(string))
                    return SearchPattern.StartsWith;
                if(rb.getString(SearchPattern.Contains.toString()).equals(string))
                    return SearchPattern.Contains;
                return SearchPattern.Custom;
            }

        };
    }

    @FXML
    private void onClickShowHelp(MouseEvent event) {
    }

    @FXML
    private void onClickNewWebsiteRule(MouseEvent event) {
        if (validateSitesFields()) {
            // Add to table
            if (btAddEditSite.getStyleClass().contains("btn_add")) {
                tvAllowedSites.getItems().add(new WebsiteFilter(
                        cbAddEditPattern.getValue(),
                        tfAddEditCriteria.getText()));
            }
            // Edit
            else {
                WebsiteFilter element = (WebsiteFilter) tvAllowedSites.getItems().get(indexSaveWebsiteFilter);
                element.searchPatternProperty().set(cbAddEditPattern.getValue());
                element.searchCriteriaProperty().set(tfAddEditCriteria.getText());
                btAddEditSite.getStyleClass().remove("btn_save");
                btAddEditSite.getStyleClass().add("btn_add");
            }
            // Clear fields
            cbAddEditPattern.setValue(null);
            tfAddEditCriteria.setText("");
        }
    }

    @FXML
    private void onClickNewServerRule(MouseEvent event) {
        if(validateServerFields()) {
            // Add to table
            if (btAddEditServer.getStyleClass().contains("btn_add")) {
                tvAllowedServers.getItems().add(new IpFilter(
                        cbAddEditProtocol.getValue(),
                        tfAddEditIp.getText(),
                        tfAddEditPort.getText(),
                        tfAddEditDesc.getText()));
            }
            // Edit
            else {
                IpFilter element = (IpFilter) tvAllowedServers.getItems().get(indexSaveIpFilter);
                element.protocolProperty().set(cbAddEditProtocol.getValue());
                element.ipAddressProperty().set(tfAddEditIp.getText());
                element.portProperty().set(tfAddEditPort.getText());
                element.descriptionProperty().set(tfAddEditDesc.getText());
                btAddEditServer.getStyleClass().remove("btn_save");
                btAddEditServer.getStyleClass().add("btn_add");
            }
            // Clear fields
            cbAddEditProtocol.setValue(null);
            tfAddEditIp.setText("");
            tfAddEditPort.setText("");
            tfAddEditDesc.setText("");
        }
    }

    public TableView<WebsiteFilter> getTvAllowedSites() {
        return tvAllowedSites;
    }

    public TableView<IpFilter> getTvAllowedServers() {
        return tvAllowedServers;
    }

    public ToggleSwitch getTsAllowMonitoring() {
        return monitoringToggleSwitch;
    }

    public ComboBox<WebsiteFilter.SearchPattern> getCbAddEditPattern() {
        return cbAddEditPattern;
    }

    public TextField getTfAddEditCriteria() {
        return tfAddEditCriteria;
    }

    public Button getBtAddEditSite() {
        return btAddEditSite;
    }

    public Button getBtAddEditServer() {
        return btAddEditServer;
    }

    public TextField getTfAddEditDesc() {
        return tfAddEditDesc;
    }

    public TextField getTfAddEditPort() {
        return tfAddEditPort;
    }

    public TextField getTfAddEditIp() {
        return tfAddEditIp;
    }

    public Button getBtCheckForDep() {
        return btCheckForDep;
    }

    public ComboBox<IpFilter.Protocol> getCbAddEditProtocol() {
        return cbAddEditProtocol;
    }

    public Label getLbAllowMonitoring() {
        return monitoringLabel;
    }

    private boolean validateSitesFields() {
        boolean result = true;
        if (!cbAddEditPattern.getSelectionModel().isEmpty()) {
            cbAddEditPattern.getStyleClass().remove("error");
        } else {
            cbAddEditPattern.getStyleClass().add("error");
            result = false;
        }
        if (tfAddEditCriteria.getText().length() > 0) {
            tfAddEditCriteria.getStyleClass().remove("error");
        } else {
            tfAddEditCriteria.getStyleClass().add("error");
            result = false;
        }
        return result;
    }

    private boolean validateServerFields() {
        boolean result = true;
        if (!cbAddEditProtocol.getSelectionModel().isEmpty()) {
            cbAddEditProtocol.getStyleClass().remove("error");
        } else {
            cbAddEditProtocol.getStyleClass().add("error");
            result = false;
        }
        try {
            WelcomeUtil.checkTarget(tfAddEditIp.getText(), -1);
            tfAddEditIp.getStyleClass().remove("error");
        } catch(ValidationException ex) {
            tfAddEditIp.setPromptText(MessageFormat.format(rb.getString(ex.getMessage()), ex.getMessageDetails()));
            tfAddEditIp.getStyleClass().add("error");
            result = false;
        }
        try {
            WelcomeUtil.checkPortRange(tfAddEditPort.getText(), -1);
            tfAddEditPort.getStyleClass().remove("error");
        } catch(ValidationException ex) {
            tfAddEditPort.setPromptText(MessageFormat.format(rb.getString(ex.getMessage()), ex.getMessageDetails()));
            tfAddEditPort.getStyleClass().add("error");
            result = false;
        }
        return result;
    }

    public Button getHelpButton() {
        return helpButton;
    }


    public int getIndexSaveWebsiteFilter() { return indexSaveWebsiteFilter; }
    public int getIndexSaveIpFilter() { return indexSaveIpFilter; }

    public void setIndexSaveWebsiteFilter(int i) {
        indexSaveWebsiteFilter = i;
    }

    public void setIndexSaveIpFilter(int i) {
        indexSaveIpFilter = i;
    }
}
