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
package ch.fhnw.lernstickwelcome.model.application.proxy;

import ch.fhnw.lernstickwelcome.model.Processable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 * This class handles the proxy settings which are used for installing the
 * applications.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
 *
 * @author sschw
 */
public class ProxyTask implements Processable<String> {

    private final BooleanProperty proxyActive = new SimpleBooleanProperty();
    private final StringProperty hostname = new SimpleStringProperty();
    private final StringProperty port = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();

    // Init to prevent typos in commands if inactive
    private String wgetProxy;
    private String aptGetProxy;
    private String flatpakProxy;

    /**
     * Settings of previous starts wont be saved.
     */
    public ProxyTask() {
    }

    public String getWgetProxy() {
        return wgetProxy;
    }

    public String getAptGetProxy() {
        return aptGetProxy;
    }

    public String getFlatpakProxy() {
        return flatpakProxy;
    }

    /**
     * Modifies the wgetProxy string to allow a connection over a proxy when
     * calling a wget command.
     */
    private void setupWgetProxy() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" -e http_proxy=http://");
        if (hostname.get() != null) {
            stringBuilder.append(hostname.get());
        }
        if (port.get() != null && !port.get().isEmpty()) {
            stringBuilder.append(':');
            stringBuilder.append(port.get());
        }
        if (username.get() != null && !username.get().isEmpty()) {
            stringBuilder.append(" --proxy-user=");
            stringBuilder.append(username.get());
        }
        if (password.get() != null && !password.get().isEmpty()) {
            stringBuilder.append(" --proxy-password=");
            stringBuilder.append(password.get());
        }
        stringBuilder.append(' ');
        wgetProxy = stringBuilder.toString();
    }

    /**
     * Modifies the aptGetProxy string to allow a connection over a proxy when
     * calling a apt-get command.
     */
    private void setupAptGetProxy() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" -o Acquire::http::proxy=http://");
        if (username.get() != null && !username.get().isEmpty()) {
            stringBuilder.append(username.get());
            if (password != null && !password.get().isEmpty()) {
                stringBuilder.append(':');
                stringBuilder.append(password.get());
            }
            stringBuilder.append('@');
        }
        stringBuilder.append(hostname.get());
        if (port.get() != null && !port.get().isEmpty()) {
            stringBuilder.append(':');
            stringBuilder.append(port.get());
        }
        stringBuilder.append(' ');
        aptGetProxy = stringBuilder.toString();
    }

    /**
     * Modifies the flathub string to allow a connection over a proxy when
     * calling a flatpak command.
     */
    private void setupFlatpakProxy() {

        String proxyUser = "";
        if (username.get() != null && !username.get().isEmpty()) {
            proxyUser = username.get();
            if (password != null && !password.get().isEmpty()) {
                proxyUser += ':' + password.get();
            }
            proxyUser += '@';
        }

        String proxyPort = "";
        if (port.get() != null && !port.get().isEmpty()) {
            proxyPort = ':' + port.get();
        }

        if (hostname.get() != null) {
            String proxyLine = proxyUser + hostname.get() + proxyPort + '/';
            flatpakProxy
                    = "export HTTP_PROXY=http://" + proxyLine + "\n"
                    + "export HTTPS_PROXY=https://" + proxyLine + "\n";
        }
    }

    public BooleanProperty proxyActiveProperty() {
        return proxyActive;
    }

    public StringProperty hostnameProperty() {
        return hostname;
    }

    public StringProperty portProperty() {
        return port;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    /**
     * Task for {@link #newTask() }
     *
     * @see Processable
     */
    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            updateProgress(0, 1);
            if (proxyActive.get()) {
                updateTitle("ProxyTask.title");
                updateMessage("ProxyTask.message");
                setupWgetProxy();
                setupAptGetProxy();
                setupFlatpakProxy();
            } else {
                wgetProxy = " ";
                aptGetProxy = " ";
                flatpakProxy = "";
            }
            updateProgress(1, 1);
            return null;
        }
    }
}
