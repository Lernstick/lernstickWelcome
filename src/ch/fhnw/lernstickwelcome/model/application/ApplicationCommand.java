/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.proxy.ProxyCategoryTask;

/**
 * Strategy for installing applications
 * 
 * @author sschw
 */
public interface ApplicationCommand {
    
    public String[] getInstallCommands(ProxyCategoryTask proxy, String[] packages);
}
