/*
 * Copyright (c) 2017 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package eu.unicore.util.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class VariablesProcessorTest
{
	private static final Logger log = LogManager.getLogger(VariablesProcessorTest.class);
	
	@Test
	public void shouldResolveConfigVariable() throws IOException
	{
		Properties props = new Properties();
		props.setProperty("$var.var1", "Dynamic");
		props.setProperty("p", "some${var1}Value");
		Properties processed = VariablesProcessor.process(props, log);
		assertEquals(processed.getProperty("p"), "someDynamicValue");
	}

	@Test
	public void shouldFailOnUnknownVariable() throws IOException
	{
		Properties props = new Properties();
		props.setProperty("p", "some${var2}Value");
		try
		{
			VariablesProcessor.process(props, log);
			fail("Should throw exception");
		} catch (ConfigurationException e)
		{
			assertTrue(e.getMessage().contains("var2"));
		}
	}

	@Test
	public void shouldUseSystemPropertyAsValue() throws IOException
	{
		Properties props = new Properties();
		props.setProperty("p", "some${java.version}Value");
		Properties processed = VariablesProcessor.process(props, log);
		assertEquals(processed.getProperty("p"), "some" + 
				System.getProperty("java.version") + "Value");
	}

	@Test
	public void systemPropertyShouldOverrideConfigVariable() throws IOException
	{
		Properties props = new Properties();
		props.setProperty("$var.java.version", "CONFIG-DEFINED");
		props.setProperty("p", "some${java.version}Value");
		Properties processed = VariablesProcessor.process(props, log);
		assertEquals(processed.getProperty("p"), "some" + 
				System.getProperty("java.version") + "Value");
	}
	
	@Test
	public void shouldUseEnvVariableAsValue() throws IOException
	{
		Properties props = new Properties();
		String firstEnvVar = System.getenv().keySet().iterator().next();
		props.setProperty("p", "some${" + firstEnvVar + "}Value");
		Properties processed = VariablesProcessor.process(props, log);
		assertEquals(processed.getProperty("p"), "some" + 
				System.getenv(firstEnvVar) + "Value");
	}

}
