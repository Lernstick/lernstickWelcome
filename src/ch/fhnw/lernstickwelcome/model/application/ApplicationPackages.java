/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;

/**
 *
 * @author sschw
 */
public abstract class ApplicationPackages {
    private String[] packageNames;
    
    public ApplicationPackages(String[] packageNames) {
        this.packageNames = packageNames;
    }
    
    public String[] getPackageNames() {
        return packageNames;
    }
    
    public int getNumberOfPackages() {
        return packageNames.length;
    }
    
    public abstract String getInstallCommand(ProxyTask proxy);
}
