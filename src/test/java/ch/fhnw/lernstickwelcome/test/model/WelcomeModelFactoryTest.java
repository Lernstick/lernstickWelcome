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
package ch.fhnw.lernstickwelcome.test.model;

import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;

/**
 * Attention: these testcases highly depend on the content of the applications.xml file, so
 * if content there is changed, testcases may need to be adjusted.
 * @author patric
 */
public class WelcomeModelFactoryTest {
	
	@Before
	public void setup() {
	}

	@Test
	public void testGetApplicationTask() throws ParserConfigurationException, SAXException, IOException {
		ApplicationTask t = WelcomeModelFactory.getApplicationTask("Kstars");
		assertTrue(t.getName().equals("Kstars"));
		assertTrue(t.getNoPackages() == 4);
		String[] expectedPackages = {"lernstick-kstars", "kstars", "kstars-data", "kstars-data-extra-tycho2"};
		assertArrayEquals(t.getPackages().getPackageNames(), expectedPackages);
		ProxyTask pt = WelcomeModelFactory.getProxyTask();
		assertTrue(t.getPackages().getInstallCommand(pt).contains("install lernstick-kstars"));
		
		//testing a wget application
		t = WelcomeModelFactory.getApplicationTask("Adobe Reader");
		assertTrue(t.getName().equals("Adobe Reader"));
		assertTrue(t.getNoPackages() == 2);
		String[] expectedPackages2 = {"AdbeRdr9.5.5-1_i386linux_enu.deb", "lernstick-adobereader-enu"};
		assertArrayEquals(t.getPackages().getPackageNames(), expectedPackages2);
		// checking the fetchurl
		assertTrue(t.getPackages().getInstallCommand(pt).contains("ftp://ftp.adobe.com/pub/adobe/reader/unix/9.x/9.5.5/enu/"));
	}
	
	@Test
	public void testGetApplicationTasks() throws ParserConfigurationException, SAXException, IOException {
		List<ApplicationTask> ts = WelcomeModelFactory.getApplicationTasks("recommended");
		ts.forEach(t -> System.out.println(t.getName()));
		assertTrue(ts.stream().anyMatch(t -> t.getName().equals("Adobe Reader")));
		assertTrue(ts.stream().anyMatch(t -> t.getName().equals("Adobe Flash Player")));
	}

}
