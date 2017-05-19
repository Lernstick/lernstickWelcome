/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;

/**
 * Subclasses of this class defines a strategy how packages should be installed.
 * 
 * @author sschw
 */
public abstract class ApplicationPackages {
    private String[] packageNames;
    
    public ApplicationPackages(String[] packageNames) {
        this.packageNames = packageNames;
    }
    
    /**
     * The packages which are installed by this command.
     * @return 
     */
    public String[] getPackageNames() {
        return packageNames;
    }
    
    /**
     * The number of packages which are installed by this command.
     * @return 
     */
    public int getNumberOfPackages() {
        return packageNames.length;
    }
    
    /**
     * Creates the installation command for the installation strategy.
     * @param proxy The proxy task which provides the proxy string for the 
     * command.
     * @return 
     */
    public abstract String getInstallCommand(ProxyTask proxy);
}
