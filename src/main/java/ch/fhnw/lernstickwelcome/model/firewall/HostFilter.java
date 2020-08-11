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
package ch.fhnw.lernstickwelcome.model.firewall;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Describes a host which should be whitelisted.
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class HostFilter {

    public enum Protocol {
        TCP,
        UDP
    }

    private final ObjectProperty<Protocol> protocolProperty;
    private final StringProperty hostProperty;
    private final StringProperty portRangeProperty;
    private final StringProperty descriptionProperty;

    public HostFilter() {
        this(Protocol.TCP, null, null, null);
    }

    public HostFilter(Protocol protocol, String host,
            String portRange, String description) {

        this.protocolProperty = new SimpleObjectProperty<>();
        this.hostProperty = new SimpleStringProperty();
        this.portRangeProperty = new SimpleStringProperty();
        this.descriptionProperty = new SimpleStringProperty();

        protocolProperty.set(protocol);
        hostProperty.set(host);
        portRangeProperty.set(portRange);
        descriptionProperty.set(description);
    }

    public ObjectProperty<Protocol> protocolProperty() {
        return protocolProperty;
    }

    public StringProperty hostProperty() {
        return hostProperty;
    }

    public StringProperty portRangeProperty() {
        return portRangeProperty;
    }

    public StringProperty descriptionProperty() {
        return descriptionProperty;
    }

    public Protocol getProtocol() {
        return protocolProperty.get();
    }

    public String getHost() {
        return hostProperty.get();
    }

    public String getPortRange() {
        return portRangeProperty.get();
    }

    public String getDescription() {
        return descriptionProperty.get();
    }
}
