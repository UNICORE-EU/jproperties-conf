/*
 * Copyright (c) 2011-2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package eu.unicore.util.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

/**
 * Extension of the PropertiesHelper, adding file backing (with reloading support).
 * If in your case the properties source is backed by a file only optionally, then consider using
 * {@link WrappingFilePropertiesHelper}.
 * The class implements Runnable, so it can be directly used by some scheduler to perform 
 * checks for the updated configuration.  
 * @author K. Benedyczak
 */
public class FilePropertiesHelper extends PropertiesHelper implements Runnable
{
	protected File file;
	protected long lastAccess;
	

	public FilePropertiesHelper(String prefix, String file,
			Map<String, PropertyMD> meta, Logger log)
			throws ConfigurationException, IOException
	{
		this(prefix, new File(file), meta, log);
	}

	public FilePropertiesHelper(String prefix, File file,
			Map<String, PropertyMD> meta, Logger log)
			throws ConfigurationException, IOException
	{
		super(prefix, load(file), meta, log);
		this.file = file;
		lastAccess = file.lastModified();
	}

	public void reload() throws IOException, ConfigurationException
	{
		setProperties(load(file));
	}
	
	public File getFile()
	{
		return file;
	}

	private boolean hasChanged()
	{
		return hasFileChanged(lastAccess, file);
	}
	
	public synchronized boolean reloadIfChanged() throws IOException, ConfigurationException
	{
		if (hasChanged())
		{
			reload();
			lastAccess = file.lastModified();
			return true;
		}
		return false;
	}


	@Override
	public void run()
	{
		try
		{
			reloadIfChanged();
		} catch (ConfigurationException e)
		{
			log.warn("The reloaded configuration is invalid: " + e.getMessage(), e);
		} catch (IOException e)
		{
			log.warn("Can't re-read the configuration file " + file + 
					": " + e.getMessage(), e);
		}
	}
	
	public static boolean hasFileChanged(long lastAccess, File file)
	{
		long fileMod = file.lastModified();
		return (lastAccess==0 || lastAccess<fileMod);
	}
	
	public static Properties load(String file) throws IOException 
	{
		return load(new File(file));
	}
	
	public static Properties load(File file) throws IOException 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));
		Properties properties = new Properties();
		try
		{
			properties.load(reader);
		} catch (Exception e)
		{
			throw new Error("Can not load properties file " + file + ": " + e.getMessage(), e);
		} finally 
		{ 
			reader.close();
		}
		return properties;
	}
}





