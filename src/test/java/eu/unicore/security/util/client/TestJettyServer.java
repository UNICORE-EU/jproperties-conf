/*********************************************************************************
 * Copyright (c) 2006 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/

package eu.unicore.security.util.client;

import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.mortbay.jetty.servlet.Context;

import eu.unicore.security.util.AuthnAndTrustProperties;
import eu.unicore.security.util.ConfigurationException;
import eu.unicore.security.util.CredentialPropertiesConfig;
import eu.unicore.security.util.TruststorePropertiesConfig;
import eu.unicore.security.util.jetty.JettyLogger;
import eu.unicore.security.util.jetty.JettyProperties;
import eu.unicore.security.util.jetty.JettyServerBase;


/**
 * a Jetty server hosting an xfire servlet
 * 
 * @author schuller
 */
public class TestJettyServer extends JettyServerBase {
	public static final String KEYSTORE = "src/test/resources/client/httpserver.jks";
	public static final String KEYSTORE_P = "the!server";
	
	private Context root;

	protected static final HashMap<String, Integer> defaults = new HashMap<String, Integer>();

	
	public TestJettyServer(URL[] listenUrls, AuthnAndTrustProperties secProperties,
			JettyProperties extraSettings) throws ConfigurationException
	{
		super(listenUrls, secProperties, extraSettings, JettyLogger.class);
		initServer();
	}

	
	public static TestJettyServer getInstance(int soLinger) throws Exception {
		int port = 62407;
		String host = "127.0.0.1";
		URL[] urls = new URL[] {new URL("http://" + host + ":" + port),
				new URL("https://" + host + ":" + (port+1))};
		Properties p = new Properties();
		p.setProperty(JettyProperties.DEFAULT_PREFIX+JettyProperties.SO_LINGER_TIME, soLinger+"");
		p.setProperty(JettyProperties.DEFAULT_PREFIX+JettyProperties.FAST_RANDOM, "true");
		p.setProperty(CredentialPropertiesConfig.DEFAULT_PFX +
			CredentialPropertiesConfig.PROP_LOCATION, KEYSTORE);
		p.setProperty(CredentialPropertiesConfig.DEFAULT_PFX +
			CredentialPropertiesConfig.PROP_TYPE, "JKS");
		p.setProperty(CredentialPropertiesConfig.DEFAULT_PFX +
			CredentialPropertiesConfig.PROP_PASSWORD, KEYSTORE_P);
		p.setProperty(TruststorePropertiesConfig.DEFAULT_PFX + 
			TruststorePropertiesConfig.PROP_TYPE, TruststorePropertiesConfig.TYPE_KEYSTORE);
		p.setProperty(TruststorePropertiesConfig.DEFAULT_PFX + 
			TruststorePropertiesConfig.PROP_KS_PATH, KEYSTORE);
		p.setProperty(TruststorePropertiesConfig.DEFAULT_PFX + 
			TruststorePropertiesConfig.PROP_KS_TYPE, "JKS");
		p.setProperty(TruststorePropertiesConfig.DEFAULT_PFX + 
			TruststorePropertiesConfig.PROP_KS_PASSWORD, KEYSTORE_P);
	
		AuthnAndTrustProperties secCfg = new AuthnAndTrustProperties(p);
		JettyProperties extra = new JettyProperties(p);
		return new TestJettyServer(urls, secCfg, extra);
	}

	@Override
	public Context getRootContext() {
		return root;
	}

	@Override
	protected void addServlets() throws ConfigurationException
	{
		root = new Context(getServer(), "/", Context.SESSIONS);		
	}

	public String getUrl() {
		return "http://" + getServer().getConnectors()[0].getHost() + ":" + 
				getServer().getConnectors()[0].getPort();
	}

	public String getSecUrl() {
		return "https://" + getServer().getConnectors()[1].getHost() + ":" + 
				getServer().getConnectors()[1].getPort();
	}

	public void addServlet(String servlet, String path) throws Exception {
		root.addServlet(Class.forName(servlet), path);
	}
}
