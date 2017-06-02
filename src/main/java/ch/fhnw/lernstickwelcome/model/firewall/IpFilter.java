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
package ch.fhnw.lernstickwelcome.model.firewall;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Describes a host which should be whitelisted.
 * @author sschw
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
