/*********************************************************************************
 * Copyright (c) 2008 Forschungszentrum Juelich GmbH 
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

package eu.unicore.util.configuration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Helper for dealing with groups of properties
 * 
 * @author schuller
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class PropertyGroupHelper {

	private final Map properties;

	private final String[] acceptedPatterns;

	private final boolean isRegexp;

	private final Pattern[] patterns;

	/**
	 * filter the given properties using the supplied patterns
	 */
	public PropertyGroupHelper(Map properties, String... patterns) {
		this(properties,false,patterns);
	}

	/**
	 * filter the given properties using the supplied patterns
	 * 
	 * @param properties - the properties
	 * @param isRegexp - whether the patterns denote Java regular expressions
	 * @param patterns - the accepted patterns
	 */
	public PropertyGroupHelper(Map properties, boolean isRegexp, String... patterns) {
		this.properties = properties;
		this.acceptedPatterns = patterns;
		this.isRegexp=isRegexp;
		this.patterns=isRegexp?createPatterns():null;
	}
	
	/**
	 * @return an iterator over the valid keys
	 */
	public Iterator<String> keys()
	{
		final Iterator<String> backing = properties.keySet().iterator();
		return new Iterator<String>()
		{
			private String next = null;

			boolean matchesFilter(String key)
			{
				return isRegexp ? matchesRegexpFilter(key) : matchesPalinFilter(key);
			}

			public boolean hasNext()
			{
				return getNextMatching() != null;
			}

			// this is idempotent
			private String getNextMatching()
			{
				if (next != null)
					return next;
				String res;
				do
				{
					if (!backing.hasNext())
					{
						next = null;
						return null;
					}
					
					res = backing.next();
					if (res == null)
					{
						next = null;
						return null;
					}
				} while (!matchesFilter(res));
				
				next = res;
				return res;
			}

			public String next()
			{
				String res = getNextMatching();
				next = null;
				return res;
			}

			public void remove()
			{
				backing.remove();
			}
		};
	}
	
	private Pattern[] createPatterns(){
		Pattern[] ps=new Pattern[acceptedPatterns.length];
		for(int i=0; i<acceptedPatterns.length; i++){
			ps[i]=Pattern.compile(acceptedPatterns[i]);
		}
		return ps;
	}

	private boolean matchesRegexpFilter(String key){
		for(Pattern p: patterns){
			if(p.matcher(key).matches())return true;
		}
		return false;
	}
	
	private boolean matchesPalinFilter(String key){
		for (String p : acceptedPatterns) {
			if (key.startsWith(p))
				return true;
		}
		return false;
	}
	
	/**
	 * gets the properties whose keys match the accepted patterns
	 */
	public Map<String,String>getFilteredMap(){
		return getFilteredMap(null);
	}

	/**
	 * gets the properties whose keys match the accepted patterns AND
	 * whose keys contain the supplied string
	 */
	public Map<String,String> getFilteredMap(String containedString){
		Map<String, String> props = new HashMap<>();
		Iterator<String>keys=keys();
		while(keys.hasNext()){
			String key=keys.next();
			if(containedString==null){
				props.put(key, String.valueOf(properties.get(key)));
			}
			else if(key.contains(containedString)){
				props.put(key, String.valueOf(properties.get(key)));	
			}
		}
		return props;
	}

	public static Map<String,String> asMap(Properties p) {
		Map<String, String> res = new HashMap<>();
		for(Object k: p.keySet()) {
			res.put(String.valueOf(k), String.valueOf(p.get(k)));
		}
		return res;
	}
}
