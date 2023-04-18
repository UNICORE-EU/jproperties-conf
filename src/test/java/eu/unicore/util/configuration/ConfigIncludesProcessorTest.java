/*
 * Copyright (c) 2017 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package eu.unicore.util.configuration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class ConfigIncludesProcessorTest
{
	private static final Logger log = LogManager.getLogger(ConfigIncludesProcessorTest.class);
	
	@Test
	public void shouldIncludeRecursivelyIncludedProperties() throws IOException
	{
		Properties ret = FilePropertiesHelper.load(
				"src/test/resources/props/base.properties");
		ret = ConfigIncludesProcessor.preprocess(ret, log); 
		
		assertThat(ret.getProperty("regular.property"), CoreMatchers.is("value1"));
		assertThat(ret.getProperty("regular.property2"), CoreMatchers.is("value2"));
		assertThat(ret.getProperty("regular.property3"), CoreMatchers.is("value3"));
		assertThat(ret.size(), CoreMatchers.is(3));
	}

	@Test
	public void shouldIncludeFromFileGivenWithVariable() throws IOException
	{
		Properties ret = FilePropertiesHelper.load(
				"src/test/resources/props/baseWithVars.properties");
		ret = ConfigIncludesProcessor.preprocess(ret, log); 
		
		assertThat(ret.getProperty("regular.property"), CoreMatchers.is("value1"));
		assertThat(ret.getProperty("regular.property2"), CoreMatchers.is("value2"));
		assertThat(ret.getProperty("regular.property3"), CoreMatchers.is("Dynamic"));
		assertThat(ret.size(), CoreMatchers.is(3));
	}
	
	@Test
	public void shouldFailOnDuplicateKeyInIncludedProperties() throws IOException
	{
		try
		{
			ConfigIncludesProcessor.preprocess(FilePropertiesHelper.load(
				"src/test/resources/props/baseWithDuplicate.properties"), log);
			fail("Should throw an exception");
		} catch (ConfigurationException e)
		{
			assertThat(e.getMessage(), containsString("Duplicate"));
			assertThat(e.getMessage(), containsString("regular.property"));
		}

	}
}
