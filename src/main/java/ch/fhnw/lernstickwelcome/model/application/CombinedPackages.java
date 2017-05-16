/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import java.util.Arrays;

/**
 * If a package is installed by multiple aptGet and Wget packages, use this
 * Composer class to combine the commands.
 * @author sschw
 */
public class CombinedPackages extends ApplicationPackages {
    private ApplicationPackages[] packageList;
    
    public CombinedPackages(ApplicationPackages... packages) {
        super(Arrays.stream(packages).flatMap(p -> Arrays.stream(p.getPackageNames())).toArray(i -> new String[i]));
    	this.packageList = packages;
    }

    @Override
    public String getInstallCommand(ProxyTask proxy) {
        StringBuilder sb = new StringBuilder();
        for(ApplicationPackages packages : packageList) {
            sb.append(packages.getInstallCommand(proxy)).append("\n");
        }
        return sb.toString();
    }
    
}
