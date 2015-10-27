package eu.unicore.util.jetty;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.eclipse.jetty.servlets.GzipFilter;

import eu.unicore.util.configuration.ConfigurationException;

/**
 * Wrapper around Jetty's {@link GzipFilter} that allows to be configured
 * using {@link HttpServerProperties}
 * @see HttpServerProperties
 * @author schuller
 */
public class ConfigurableGzipFilter extends GzipFilter
{	
	private HttpServerProperties properties;
	
	public ConfigurableGzipFilter(HttpServerProperties properties)
	{
		this.properties = properties;
	}
	
	public void init(FilterConfig filterConfig) throws ServletException
	{
		_minGzipSize=65535;
		super.init(new MyFilterConfig(filterConfig));
	}

	private class MyFilterConfig implements FilterConfig{
		private FilterConfig config;
		
		public MyFilterConfig(FilterConfig config){
			this.config=config;
		}
		public String getFilterName() {
			return config.getFilterName();
		}
		public String getInitParameter(String name) {
			String val=config.getInitParameter(name);
			if(val==null)
				try
				{
					val=properties.getValue(HttpServerProperties.GZIP_PREFIX + name);
				} catch (ConfigurationException e)
				{
					throw new RuntimeException(
						"BUG: got no value (even default) for: " + name, e);
				}
			return val;
		}
		public Enumeration<String> getInitParameterNames() {
			return config.getInitParameterNames();
		}
		public ServletContext getServletContext() {
			return config.getServletContext();
		}
	}
}