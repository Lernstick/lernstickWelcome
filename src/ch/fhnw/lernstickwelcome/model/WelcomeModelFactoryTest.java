package ch.fhnw.lernstickwelcome.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;

public class WelcomeModelFactoryTest {
	
	@Before
	public void setup() {
	}

	@Test
	public void testGetApplicationTask() throws ParserConfigurationException, SAXException, IOException {
		ApplicationTask t = WelcomeModelFactory.getApplicationTask("Kstars");
		assertTrue(t.getName().equals("Kstars"));
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
