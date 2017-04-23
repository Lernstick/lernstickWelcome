/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class AptGetPackages extends ApplicationPackages {
    private final static Logger LOGGER = Logger.getLogger(AptGetPackages.class.getName());
    
    public AptGetPackages(String[] packageNames) {
        super(packageNames);
    }

    @Override
    public String getInstallCommand(ProxyTask proxy) {
        for (String packageName : getPackageNames()) {
            LOGGER.log(Level.INFO, "installing package \"{0}\"", packageName);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("#!/bin/sh\n"
                + "export DEBIAN_FRONTEND=noninteractive\n");
        builder.append("apt-get ");
        builder.append(proxy.getAptGetProxy());
        builder.append("-y --force-yes install ");
        for (String packageName : getPackageNames()) {
            builder.append(packageName);
            builder.append(' ');
        }
        return builder.toString();
    }
}
