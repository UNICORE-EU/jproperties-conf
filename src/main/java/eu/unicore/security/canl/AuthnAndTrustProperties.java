/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */
package eu.unicore.security.canl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;

import eu.unicore.util.Log;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.FilePropertiesHelper;



/**
 * Class wrapping all security related settings for a UNICORE client or server:
 * truststore configuration and credentials, configured from a single {@link Properties} source. 
 * 
 * @author K. Benedyczak
 */
public class AuthnAndTrustProperties extends DefaultAuthnAndTrustConfiguration
{
	private static final Logger log = Log.getLogger(Log.SECURITY, AuthnAndTrustProperties.class);
	
	private TruststoreProperties truststoreProperties;
	private CredentialProperties credentialProperties;
	
	public AuthnAndTrustProperties(String file) throws IOException, ConfigurationException
	{
		this(new File(file));
	}

	public AuthnAndTrustProperties(File file) throws IOException, ConfigurationException
	{
		this(FilePropertiesHelper.load(file));
	}
	
	public AuthnAndTrustProperties(String file, String trustPrefix, String credPrefix) throws IOException, ConfigurationException
	{
		this(new File(file), trustPrefix, credPrefix);
	}

	public AuthnAndTrustProperties(File file, String trustPrefix, String credPrefix) throws IOException, ConfigurationException
	{
		this(FilePropertiesHelper.load(file), trustPrefix, credPrefix, false, false);
	}
	
	public AuthnAndTrustProperties(Properties p) throws ConfigurationException
	{
		this(p, TruststoreProperties.DEFAULT_PREFIX, CredentialProperties.DEFAULT_PREFIX, false, false);
	}

	public AuthnAndTrustProperties(Properties p, String trustPrefix, String credPrefix) throws ConfigurationException
	{
		this(p, trustPrefix, credPrefix, false, false);
	}

	/**
	 * only for cloning
	 */
	protected AuthnAndTrustProperties(TruststoreProperties trustProps, CredentialProperties credProps, 
			DefaultAuthnAndTrustConfiguration configured)
	{
		this.credentialProperties = credProps;
		this.truststoreProperties = trustProps;
		setValidator(configured.getValidator());
		setCredential(configured.getCredential());
	}
	
	public AuthnAndTrustProperties(Properties p, String trustPrefix, String credPrefix, 
			boolean trustOptional, boolean credOptional) throws ConfigurationException
	{
		try
		{
			truststoreProperties = new TruststoreProperties(p, Collections.singleton(new LoggingStoreUpdateListener()),
					trustPrefix);
			setValidator(truststoreProperties.getValidator());
		} catch (ConfigurationException e)
		{
			if (!trustOptional)
				throw e;
			else
				log.info("Trust store settings (optional) were not loaded as: " + e.getMessage());
		}
		
		try
		{
			credentialProperties = new CredentialProperties(p, credPrefix); 
			setCredential(credentialProperties.getCredential());
		} catch (ConfigurationException e)
		{
			if (!credOptional)
				throw e;
			else
				log.info("Credential (optional) was not loaded as: " + e.getMessage());
		} 
	}

	/**
	 * @return the truststoreProperties
	 */
	public TruststoreProperties getTruststoreProperties()
	{
		return truststoreProperties;
	}

	/**
	 * @return the credentialProperties
	 */
	public CredentialProperties getCredentialProperties()
	{
		return credentialProperties;
	}
	
	public AuthnAndTrustProperties clone()
	{
		DefaultAuthnAndTrustConfiguration clonedRaw = super.clone();
		return new AuthnAndTrustProperties(truststoreProperties.clone(), credentialProperties.clone(), clonedRaw);
		
	}
}