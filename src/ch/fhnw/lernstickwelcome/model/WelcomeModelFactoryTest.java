package ch.fhnw.lernstickwelcome.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;

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
		t = WelcomeModelFactory.getApplicationTask("AdobeReader");
		assertTrue(t.getName().equals("AdobeReader"));
		assertTrue(t.getNoPackages() == 2);
		String[] expectedPackages2 = {"lernstick-adobereader-enu", "AdbeRdr9.5.5-1_i386linux_enu.deb"};
		assertArrayEquals(t.getPackages().getPackageNames(), expectedPackages2);
		// checking the fetchurl
		assertTrue(t.getPackages().getInstallCommand(pt).contains("ftp://ftp.adobe.com/pub/adobe/reader/unix/9.x/9.5.5/enu/"));
	}
	
	@Test
	public void testGetApplicationTasks() throws ParserConfigurationException, SAXException, IOException {
		List<ApplicationTask> ts = WelcomeModelFactory.getApplicationTasks("recommended");
		ts.forEach(t -> System.out.println(t.getName()));
		assertTrue(ts.stream().anyMatch(t -> t.getName().equals("AdobeReader")));
		assertTrue(ts.stream().anyMatch(t -> t.getName().equals("Kstars")));
		assertTrue(ts.stream().anyMatch(t -> t.getName().equals("Omnitux")));
	}

}
