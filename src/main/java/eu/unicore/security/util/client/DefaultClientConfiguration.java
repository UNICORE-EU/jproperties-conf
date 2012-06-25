/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */

package eu.unicore.security.util.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import eu.emi.security.authn.x509.X509CertChainValidator;
import eu.emi.security.authn.x509.X509Credential;
import eu.unicore.security.canl.DefaultAuthnAndTrustConfiguration;


/**
 * A default implementation of the {@link IAuthenticationConfiguration} interface
 * which can be used to manually configure all aspects of the interface with constructor parameters.
 * 
 * @author golbi
 */
public class DefaultClientConfiguration extends DefaultAuthnAndTrustConfiguration implements IClientConfiguration
{
	private String httpUser;
	private String httpPassword;
	private boolean sslAuthn;
	private boolean httpAuthn;
	private String[] inHandlerClassNames;
	private String[] outHandlerClassNames;
	private ClassLoader classLoader;
	private boolean sslEnabled;
	private boolean doSignMessage;
	private ETDClientSettings etdSettings = new ETDClientSettings();
	private Properties extraSettings = new Properties();
	private Map<String, Object> extraSecurityTokens = new HashMap<String, Object>();
	private ServerHostnameCheckingMode serverHostnameCheckingMode = ServerHostnameCheckingMode.NONE;
	
	/**
	 * Only default settings, i.e. no security.
	 */
	public DefaultClientConfiguration()
	{
	}

	/**
	 * This constructor is the typical for UNICORE: SSL and ssl authN is on, http authn is off. 
	 * @param validator
	 * @param credential
	 */
	public DefaultClientConfiguration(X509CertChainValidator validator, X509Credential credential)
	{
		super(validator, credential);
		this.sslAuthn = true;
		this.sslEnabled = true;
	}

	/**
	 * @return the httpUser
	 */
	@Override
	public String getHttpUser()
	{
		return httpUser;
	}

	/**
	 * @param httpUser the httpUser to set
	 */
	public void setHttpUser(String httpUser)
	{
		this.httpUser = httpUser;
	}

	/**
	 * @return the httpPassword
	 */
	@Override
	public String getHttpPassword()
	{
		return httpPassword;
	}

	/**
	 * @param httpPassword the httpPassword to set
	 */
	public void setHttpPassword(String httpPassword)
	{
		this.httpPassword = httpPassword;
	}

	/**
	 * @return the sslAuthn
	 */
	@Override
	public boolean doSSLAuthn()
	{
		return sslAuthn;
	}

	/**
	 * @param sslAuthn the sslAuthn to set
	 */
	public void setSslAuthn(boolean sslAuthn)
	{
		this.sslAuthn = sslAuthn;
	}

	/**
	 * @return the httpAuthn
	 */
	@Override
	public boolean doHttpAuthn()
	{
		return httpAuthn;
	}

	/**
	 * @param httpAuthn the httpAuthn to set
	 */
	public void setHttpAuthn(boolean httpAuthn)
	{
		this.httpAuthn = httpAuthn;
	}

	/**
	 * @return the inHandlerClassNames
	 */
	@Override
	public String[] getInHandlerClassNames()
	{
		return inHandlerClassNames;
	}

	/**
	 * @param inHandlerClassNames the inHandlerClassNames to set
	 */
	public void setInHandlerClassNames(String[] inHandlerClassNames)
	{
		this.inHandlerClassNames = inHandlerClassNames;
	}

	/**
	 * @return the outHandlerClassNames
	 */
	@Override
	public String[] getOutHandlerClassNames()
	{
		return outHandlerClassNames;
	}

	/**
	 * @param outHandlerClassNames the outHandlerClassNames to set
	 */
	public void setOutHandlerClassNames(String[] outHandlerClassNames)
	{
		this.outHandlerClassNames = outHandlerClassNames;
	}

	/**
	 * @return the classLoader
	 */
	@Override
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	/**
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	/**
	 * @return the sslEnabled
	 */
	@Override
	public boolean isSslEnabled()
	{
		return sslEnabled;
	}

	/**
	 * @param sslEnabled the sslEnabled to set
	 */
	public void setSslEnabled(boolean sslEnabled)
	{
		this.sslEnabled = sslEnabled;
	}

	/**
	 * @return the doSignMessages
	 */
	@Override
	public boolean doSignMessage()
	{
		return doSignMessage;
	}

	/**
	 * @param doSignMessages the doSignMessages to set
	 */
	public void setDoSignMessage(boolean doSignMessage)
	{
		this.doSignMessage = doSignMessage;
	}

	/**
	 * @return the etdClientSettings
	 */
	@Override
	public ETDClientSettings getETDSettings()
	{
		return etdSettings;
	}

	/**
	 * @param etdSettings the etdSettings to set
	 */
	public void setEtdSettings(ETDClientSettings etdSettings)
	{
		this.etdSettings = etdSettings;
	}

	/**
	 * @return the extraSettings
	 */
	@Override
	public Properties getExtraSettings()
	{
		return extraSettings;
	}

	/**
	 * @param extraSettings the extraSettings to set
	 */
	public void setExtraSettings(Properties extraSettings)
	{
		this.extraSettings = extraSettings;
	}

	/**
	 * @return the extraSecurityTokens
	 */
	@Override
	public Map<String, Object> getExtraSecurityTokens()
	{
		return extraSecurityTokens;
	}

	/**
	 * @param extraSecurityTokens the extraSecurityTokens to set
	 */
	public void setExtraSecurityTokens(Map<String, Object> extraSecurityTokens)
	{
		this.extraSecurityTokens = extraSecurityTokens;
	}

	/**
	 * @return the serverHostnameCheckingMode
	 */
	@Override
	public ServerHostnameCheckingMode getServerHostnameCheckingMode()
	{
		return serverHostnameCheckingMode;
	}

	/**
	 * @param serverHostnameCheckingMode the serverHostnameCheckingMode to set
	 */
	public void setServerHostnameCheckingMode(ServerHostnameCheckingMode serverHostnameCheckingMode)
	{
		this.serverHostnameCheckingMode = serverHostnameCheckingMode;
	}

	/**
	 * Note - credential and validator objects are not cloned - are copied by reference.
	 * This doesn't affect threading (both are thread safe). Credential is usually immutable.
	 * Changes to validator settings will be visible also in the validator of the cloned object.
	 */
	@Override
	public IClientConfiguration clone()
	{
		DefaultClientConfiguration ret = new DefaultClientConfiguration();
		cloneTo(ret);
		return ret;
	}

	/**
	 * for implementing clone in subclasses
	 * @param ret
	 * @return
	 */
	protected IClientConfiguration cloneTo(DefaultClientConfiguration ret)
	{
		ret.setClassLoader(classLoader);
		ret.setCredential(getCredential());
		ret.setDoSignMessage(doSignMessage);
		ret.setEtdSettings(etdSettings.clone());
		Map<String, Object> extra = new HashMap<String, Object>();
		extra.putAll(extraSecurityTokens);
		ret.setExtraSecurityTokens(extra);
		Properties p = new Properties();
		p.putAll(extraSettings);
		ret.setExtraSettings(p);
		ret.setHttpAuthn(httpAuthn);
		ret.setHttpPassword(httpPassword);
		ret.setHttpUser(httpUser);
		ret.setInHandlerClassNames(inHandlerClassNames);
		ret.setOutHandlerClassNames(outHandlerClassNames);
		ret.setSslAuthn(sslAuthn);
		ret.setSslAuthn(sslAuthn);
		ret.setSslEnabled(sslEnabled);
		ret.setValidator(getValidator());
		ret.setServerHostnameCheckingMode(serverHostnameCheckingMode);
		return ret;
	}
}
