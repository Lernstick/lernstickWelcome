/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.controller.exception.TableCellValidationException;
import ch.fhnw.lernstickwelcome.controller.exception.ValidationException;
import ch.fhnw.lernstickwelcome.model.firewall.IpFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter.SearchPattern;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.lernstickwelcome.view.impl.ButtonCell;
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
    private Button btn_fw_help;
    @FXML
    private ToggleSwitch tsAllowMonitoring;
    @FXML
    private TableView<WebsiteFilter> tv_fw_allowed_sites;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter.SearchPattern> tab_fw_search_pattern;
    @FXML
    private TableColumn<WebsiteFilter, String> tab_fw_search_criteria;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tab_fw_search_btn_edit;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tab_fw_search_btn_delete;
    @FXML
    private ComboBox<WebsiteFilter.SearchPattern> choice_fw_search_pattern;
    @FXML
    private TextField txt_fw_search_criteria;
    @FXML
    private Button btn_fw_new_rule;
    @FXML
    private TableView<IpFilter> tv_fw_allowed_servers;
    @FXML
    private TableColumn<IpFilter, IpFilter.Protocol> tab_fw_server_protocol;
    @FXML
    private TableColumn<IpFilter, String> tab_fw_server_ip;
    @FXML
    private TableColumn<IpFilter, String> tab_fw_server_port;
    @FXML
    private TableColumn<IpFilter, String> tab_fw_server_desc;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tab_fw_server_btn_edit;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tab_fw_server_btn_delete;
    @FXML
    private Button btn_fw_add_new_server;
    @FXML
    private TextField txt_fw_new_desc;
    @FXML
    private TextField txt_fw_new_port;
    @FXML
    private TextField txt_fw_new_ip;
    @FXML
    private ComboBox<IpFilter.Protocol> choice_fw_protocol;
    @FXML
    private Label lbl_fw_allow_monitoring;
    
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
        tab_fw_search_pattern.setCellValueFactory(p -> p.getValue().searchPatternProperty());
        tab_fw_search_pattern.setCellFactory(ComboBoxTableCell.forTableColumn(getSearchPatternStringConverter(), WebsiteFilter.SearchPattern.values()));
        tab_fw_search_pattern.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .searchPatternProperty().setValue(e.getNewValue()));
        tab_fw_search_criteria.setCellValueFactory(p -> p.getValue().searchCriteriaProperty());
        tab_fw_search_criteria.setCellFactory(TextFieldTableCell.forTableColumn());
        tab_fw_search_criteria.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .searchCriteriaProperty().setValue(e.getNewValue()));
        tab_fw_search_btn_edit.setCellFactory(c -> new ButtonCell(ButtonCell.Type.EDIT, this, c.getTableView()));
        tab_fw_search_btn_delete.setCellFactory(p -> new ButtonCell(ButtonCell.Type.DELETE, this, p.getTableView()));
        
        // Set TableView IpFilter cell properties and implement edit functionality
        tab_fw_server_protocol.setCellValueFactory(p -> p.getValue().protocolProperty());
        tab_fw_server_protocol.setCellFactory(ComboBoxTableCell.forTableColumn(IpFilter.Protocol.values()));
        tab_fw_server_protocol.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .protocolProperty().setValue(e.getNewValue()));
        tab_fw_server_ip.setCellFactory(ValidatableTextFieldCell.forTableColumn((s, i) -> {
            try {
                WelcomeUtil.checkTarget((String) s, i);
                return null;
            } catch(TableCellValidationException ex) {
                return ex;
            }
        }, rb));
        tab_fw_server_ip.setOnEditCommit(e -> 
                e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .ipAddressProperty().setValue(e.getNewValue()));
        tab_fw_server_ip.setCellValueFactory(p -> p.getValue().ipAddressProperty());
        tab_fw_server_port.setCellFactory(ValidatableTextFieldCell.forTableColumn((s, i) -> {
            try {
                WelcomeUtil.checkPortRange((String) s, i);
                return null;
            } catch(TableCellValidationException ex) {
                return ex;
            }
        }, rb));
        tab_fw_server_port.setCellValueFactory(p -> p.getValue().portProperty());
        tab_fw_server_port.setOnEditCommit(e -> 
                e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .portProperty().setValue(e.getNewValue()));
        tab_fw_server_desc.setCellValueFactory(p -> p.getValue().descriptionProperty());
        tab_fw_server_desc.setCellFactory(TextFieldTableCell.forTableColumn());
        tab_fw_server_desc.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow())
                        .descriptionProperty().setValue(e.getNewValue()));
        tab_fw_server_btn_edit.setCellFactory(c -> new ButtonCell(ButtonCell.Type.EDIT, this, c.getTableView()));
        tab_fw_server_btn_delete.setCellFactory(p -> new ButtonCell(ButtonCell.Type.DELETE, this, p.getTableView()));
        
        // Load ComboBox data
        choice_fw_search_pattern.setConverter(getSearchPatternStringConverter());
        choice_fw_search_pattern.getItems().addAll(WebsiteFilter.SearchPattern.values());
        choice_fw_protocol.getItems().addAll(IpFilter.Protocol.values());
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
            if (btn_fw_new_rule.getStyleClass().contains("btn_add")) {
                tv_fw_allowed_sites.getItems().add(new WebsiteFilter(
                    choice_fw_search_pattern.getValue(),
                    txt_fw_search_criteria.getText()));
            }
            // Edit
            else {
                WebsiteFilter element = (WebsiteFilter) tv_fw_allowed_sites.getItems().get(indexSaveWebsiteFilter);
                element.searchPatternProperty().set(choice_fw_search_pattern.getValue());
                element.searchCriteriaProperty().set(txt_fw_search_criteria.getText());
                btn_fw_new_rule.getStyleClass().remove("btn_save");
                btn_fw_new_rule.getStyleClass().add("btn_add");
            }
            // Clear fields
            choice_fw_search_pattern.setValue(null);
            txt_fw_search_criteria.setText("");
        }
    }

    @FXML
    private void onClickNewServerRule(MouseEvent event) {
        if(validateServerFields()) {
            // Add to table
            if (btn_fw_add_new_server.getStyleClass().contains("btn_add")) {
                tv_fw_allowed_servers.getItems().add(new IpFilter(
                        choice_fw_protocol.getValue(), 
                        txt_fw_new_ip.getText(), 
                        txt_fw_new_port.getText(), 
                        txt_fw_new_desc.getText()));
            }
            // Edit
            else {
                IpFilter element = (IpFilter) tv_fw_allowed_servers.getItems().get(indexSaveIpFilter);
                element.protocolProperty().set(choice_fw_protocol.getValue());
                element.ipAddressProperty().set(txt_fw_new_ip.getText());
                element.portProperty().set(txt_fw_new_port.getText());
                element.descriptionProperty().set(txt_fw_new_desc.getText());
                btn_fw_add_new_server.getStyleClass().remove("btn_save");
                btn_fw_add_new_server.getStyleClass().add("btn_add");
            }
            // Clear fields
            choice_fw_protocol.setValue(null);
            txt_fw_new_ip.setText("");
            txt_fw_new_port.setText("");
            txt_fw_new_desc.setText("");
        }
    }

    public TableView getTv_fw_allowed_sites() {
        return tv_fw_allowed_sites;
    }

    public TableView getTv_fw_allowed_servers() {
        return tv_fw_allowed_servers;
    }

    public ToggleSwitch getCb_fw_allow_monitoring() {
        return tsAllowMonitoring;
    }

    public ComboBox<WebsiteFilter.SearchPattern> getChoice_fw_search_pattern() {
        return choice_fw_search_pattern;
    }

    public TextField getTxt_fw_search_criteria() {
        return txt_fw_search_criteria;
    }

    public Button getBtn_fw_new_rule() {
        return btn_fw_new_rule;
    }

    public Button getBtn_fw_add_new_server() {
        return btn_fw_add_new_server;
    }

    public TextField getTxt_fw_new_desc() {
        return txt_fw_new_desc;
    }

    public TextField getTxt_fw_new_port() {
        return txt_fw_new_port;
    }

    public TextField getTxt_fw_new_ip() {
        return txt_fw_new_ip;
    }

    public ComboBox<IpFilter.Protocol> getChoice_fw_protocol() {
        return choice_fw_protocol;
    }
    
    public Label getLbl_fw_allow_monitoring() {
        return lbl_fw_allow_monitoring;
    }
    
    private boolean validateSitesFields() {
        boolean result = true;
        if (!choice_fw_search_pattern.getSelectionModel().isEmpty()) {
            choice_fw_search_pattern.getStyleClass().remove("error");
        } else {
            choice_fw_search_pattern.getStyleClass().add("error");
            result = false;
        }
        if (txt_fw_search_criteria.getText().length() > 0) {
            txt_fw_search_criteria.getStyleClass().remove("error");
        } else {
            txt_fw_search_criteria.getStyleClass().add("error");
            result = false;
        }
        return result;
    }

    private boolean validateServerFields() {
        boolean result = true;
        if (!choice_fw_protocol.getSelectionModel().isEmpty()) {
            choice_fw_protocol.getStyleClass().remove("error");
        } else {
            choice_fw_protocol.getStyleClass().add("error");
            result = false;
        }
        try {
            WelcomeUtil.checkTarget(txt_fw_new_ip.getText(), -1);
            txt_fw_new_ip.getStyleClass().remove("error");
        } catch(ValidationException ex) {
            txt_fw_new_ip.setPromptText(MessageFormat.format(rb.getString(ex.getMessage()), ex.getMessageDetails()));
            txt_fw_new_ip.getStyleClass().add("error");
            result = false;
        }
        try {
            WelcomeUtil.checkPortRange(txt_fw_new_port.getText(), -1);
            txt_fw_new_port.getStyleClass().remove("error");
        } catch(ValidationException ex) {
            txt_fw_new_port.setPromptText(MessageFormat.format(rb.getString(ex.getMessage()), ex.getMessageDetails()));
            txt_fw_new_port.getStyleClass().add("error");
            result = false;
        }
        return result;
    }
    
    public Button getBtnFwHelp() {
        return btn_fw_help;
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
