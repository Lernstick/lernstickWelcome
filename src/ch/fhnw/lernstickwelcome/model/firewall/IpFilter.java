/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.firewall;

import javafx.beans.property.ObjectProperty;
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
    private StringProperty portRange = new SimpleStringProperty();
    private ObjectProperty<Protocol> protocol = new SimpleObjectProperty<>();
    private StringProperty description = new SimpleStringProperty();
    
    public IpFilter() {
        protocol.set(Protocol.TCP);
    }
    
    public IpFilter(Protocol protocol, String ipAddress, String portRange, String description) {
        this.protocol.set(protocol);
        this.ipAddress.set(ipAddress);
        this.portRange.set(portRange);
        this.description.set(description);
    }
    
    public StringProperty ipAddressProperty() {
        return ipAddress;
    }
    
    public StringProperty portProperty() {
        return portRange;
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
    
    public String getPortRange() {
        return portRange.get();
    }
    
    public Protocol getProtocol() {
        return protocol.get();
    }
    
    public String getDescription() {
        return description.get();
    }
    
}
