/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.firewall;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author user
 */
public class IpFilter {
    public enum Protocol {
        TCP,
        UDP
    }

    private StringProperty ipAddress = new SimpleStringProperty();
    private IntegerProperty port = new SimpleIntegerProperty();
    private ObjectProperty<Protocol> protocol = new SimpleObjectProperty<Protocol>();
    private StringProperty description = new SimpleStringProperty();
    
    public StringProperty ipAddressProperty() {
        return ipAddress;
    }
    
    public IntegerProperty portProperty() {
        return port;
    }
    
    public ObjectProperty<Protocol> protocolProperty() {
        return protocol;
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getIpAddress() {
        return ipAddress.get();
    }
    
    public Integer getPort() {
        return port.get();
    }
    
    public Protocol getProtocol() {
        return protocol.get();
    }
    
    public String getDescription() {
        return description.get();
    }
    
}
