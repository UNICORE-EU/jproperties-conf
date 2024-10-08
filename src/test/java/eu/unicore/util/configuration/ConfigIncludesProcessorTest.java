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

public class ConfigIncludesProcessorTest
{
	private static final Logger log = LogManager.getLogger(ConfigIncludesProcessorTest.class);
	
	@Test
	public void shouldIncludeRecursivelyIncludedProperties() throws IOException
	{
		Properties ret = FilePropertiesHelper.load(
				"src/test/resources/props/base.properties");
		ret = ConfigIncludesProcessor.preprocess(ret, log); 
		
		assertEquals(ret.getProperty("regular.property"), "value1");
		assertEquals(ret.getProperty("regular.property2"), "value2");
		assertEquals(ret.getProperty("regular.property3"), "value3");
		assertTrue(ret.size() == 3);
	}

	@Test
	public void shouldIncludeFromFileGivenWithVariable() throws IOException
	{
		Properties ret = FilePropertiesHelper.load(
				"src/test/resources/props/baseWithVars.properties");
		ret = ConfigIncludesProcessor.preprocess(ret, log); 
		assertEquals(ret.getProperty("regular.property"), "value1");
		assertEquals(ret.getProperty("regular.property2"), "value2");
		assertEquals(ret.getProperty("regular.property3"), "Dynamic");
		assertTrue(ret.size() == 3);
	}
	
	@Test
	public void shouldFailOnDuplicateKeyInIncludedProperties() throws IOException
	{
		try {
			ConfigIncludesProcessor.preprocess(FilePropertiesHelper.load(
					"src/test/resources/props/baseWithDuplicate.properties"), log);
			ConfigIncludesProcessor.preprocess(FilePropertiesHelper.load(
				"src/test/resources/props/baseWithDuplicate.properties"), log);
			fail("Should throw an exception");
		} catch (ConfigurationException e)
		{
			assertTrue(e.getMessage().contains("Duplicate"));
			assertTrue(e.getMessage().contains("regular.property"));
		}
	}
}
