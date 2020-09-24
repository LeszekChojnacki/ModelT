/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.internal.utils;

//[y]added-start
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
//[y]added-end
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//[y]added-start
import java.util.LinkedHashMap;
//[y]added-end

/**
 * Priority
 * <ul>
 *  <li>System properties</li>
 *  <li>META-INF/ of provided classLoader</li>
 * </ul>
 * <br/>
 * To improve performance in frequent session creation cases, chained properties can be cached by it's conf file name
 * and requesting classloader. To take advantage of the case it must be enabled via system property:<br/>
 * <code>org.kie.property.cache.enabled</code> that needs to be set to <code>true</code>
 * Cache entries are by default limited to 10 to reduce memory consumption but can be fine tuned by system property:<br/>
 * <code>org.kie.property.cache.size</code> that needs to be set to valid integer value
 */
public class ChainedProperties
		implements
		Externalizable, Cloneable {

	protected static transient Logger logger = LoggerFactory.getLogger(ChainedProperties.class);

//[y]added-start
	private static final int MAX_CACHE_ENTRIES;
	private static final boolean CACHE_ENABLED;

	static
	{
		final Properties localProperties = loadDroolsLocalProperties();
		CACHE_ENABLED = Boolean.parseBoolean(localProperties.getProperty("org.kie.property.cache.enabled", "false"));
		MAX_CACHE_ENTRIES = Integer.parseInt(localProperties.getProperty("org.kie.property.cache.size", "10"));
	}

	protected static Map<CacheKey, List<Properties>> propertiesCache =
			Collections.synchronizedMap(
					new LinkedHashMap()
					{
						private static final long serialVersionUID = -4728876927433598466L;

						@Override
						protected boolean removeEldestEntry(
								final Map.Entry eldest)
						{
							return size() > MAX_CACHE_ENTRIES;
						}
					});
//[y]added-end

	private List<Properties> props = new ArrayList<Properties>();
	private List<Properties> defaultProps = new ArrayList<Properties>();

	public ChainedProperties() { }

	public static ChainedProperties getChainedProperties( ClassLoader classLoader ) {
		return getChainedProperties( "properties.conf", classLoader );
	}

	public static ChainedProperties getChainedProperties( String confFileName, ClassLoader classLoader ) {
		return new ChainedProperties( confFileName, classLoader );
	}

//[y]added-start
	private static Properties loadDroolsLocalProperties()
	{
		final Properties props = new Properties();
		final URL url = Resources.getResource("META-INF/drools-local.properties");
		final ByteSource byteSource = Resources.asByteSource(url);
		try (InputStream inputStream = byteSource.openBufferedStream()) {
			props.load(inputStream);
		} catch (final IOException e) {
			logger.error("openBufferedStream failed!", e);
		}
		return props;
	}
//[y]added-end

	public ChainedProperties clone() {
		ChainedProperties clone = new ChainedProperties();
		clone.props.addAll( this.props );
		clone.defaultProps.addAll( this.defaultProps );
		return clone;
	}

	private ChainedProperties(String confFileName, ClassLoader classLoader) {
		addProperties( System.getProperties() );

		loadProperties( "META-INF/kie." + confFileName, classLoader, this.props );
		loadProperties( "META-INF/kie.default." + confFileName, classLoader, this.defaultProps);

		// this happens only in OSGi: for some reason doing
		// ClassLoader.getResources() doesn't work but doing
		// Class.getResourse() does
		if (this.defaultProps.isEmpty()) {
			try {
				Class<?> c = Class.forName( "org.drools.core.WorkingMemory", false, classLoader);
				URL confURL = c.getResource("/META-INF/kie.default." + confFileName);
				loadProperties(confURL, this.defaultProps);
			} catch (ClassNotFoundException e) { }
		}
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		props = (List<Properties>) in.readObject();
		defaultProps = (List<Properties>) in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject( props );
		out.writeObject( defaultProps );
	}

	/**
	 * Specifically added properties take priority, so they go to the front of the list.
	 */
	public void addProperties(Properties properties) {
		this.props.add( 0, properties );
	}

	public String getProperty(String key,
			String defaultValue) {
		String value = null;
		for ( Properties props : this.props ) {
			value = props.getProperty( key );
			if ( value != null ) {
				break;
			}
		}
		if ( value == null ) {
			for ( Properties props : this.defaultProps ) {
				value = props.getProperty( key );
				if ( value != null ) {
					break;
				}
			}
		}
		return (value != null) ? value : defaultValue;
	}

	public void mapStartsWith(Map<String, String> map,
			String startsWith,
			boolean includeSubProperties) {
		for ( Properties props : this.props ) {
			mapStartsWith( map,
					props,
					startsWith,
					includeSubProperties );
		}

		for ( Properties props : this.defaultProps ) {
			mapStartsWith( map,
					props,
					startsWith,
					includeSubProperties );
		}
	}

	private void mapStartsWith(Map<String, String> map,
			Properties properties,
			String startsWith,
			boolean includeSubProperties) {
		Enumeration< ? > enumeration = properties.propertyNames();
		while ( enumeration.hasMoreElements() ) {
			String key = (String) enumeration.nextElement();
			if ( key.startsWith( startsWith ) ) {
				if ( !includeSubProperties && key.substring( startsWith.length() + 1 ).indexOf( '.' ) > 0 ) {
					// +1 to the length, as we do allow the direct property, just not ones below it
					// This key has sub properties beyond the given startsWith, so skip
					continue;
				}
				if ( !map.containsKey( key ) ) {
					map.put( key,
							properties.getProperty( key ) );
				}

			}
		}
	}

	private void loadProperties(String fileName,
			ClassLoader classLoader,
			List<Properties> chain) {

//[y]added-start
		if (!CACHE_ENABLED) {
			try {
				chain.addAll(read(fileName,classLoader));
			}
			catch (final IOException e)
			{
				/* ingoring */
			}
			return;
		}
		final CacheKey ck = new CacheKey(fileName, classLoader);
		List<Properties> cached = propertiesCache.get(ck);
		if (cached == null) {
			try {
				cached = read(fileName, classLoader);
				propertiesCache.put(ck, cached);
			}
			catch (final IOException e)
			{
				/* ingoring */
			}
		}

		if (cached != null)
		 {
			chain.addAll(cached);
//[y]added-end
		}

//[y]removed		try {
//[y]removed			chain.addAll(read(fileName,classLoader));
//[y]removed		} catch (IOException e){}
	}

	private List<Properties> read(String fileName, ClassLoader classLoader)
			throws IOException {
		List<Properties> properties = new ArrayList<>();
		Enumeration<URL> resources;
		if (classLoader != null) {
			resources = classLoader.getResources(fileName);
		} else {
			resources = Collections.enumeration(Collections.singletonList(new File(fileName).toURI().toURL()));
		}
		while (resources.hasMoreElements()) {
			Properties p = new Properties();
			URL nextElement = resources.nextElement();
			try (InputStream is = nextElement.openStream()) {
				p.load(is);
				properties.add(p);
			}
		}
		return properties;
	}

	private void loadProperties(URL confURL, List<Properties> chain) {
		if ( confURL == null ) {
			return;
		}
		try ( InputStream is = confURL.openStream() ) {
			Properties properties = new Properties();
			properties.load( is );
			chain.add( properties );
		} catch ( IOException e ) {
			//throw new IllegalArgumentException( "Invalid URL to properties file '" + confURL.toExternalForm() + "'" );
		}
	}

//[y]added-start
	/*
	 * optional cache handling to improve performance to avoid duplicated loads of properties
	 */
	private static class CacheKey {
		private final String confFileName;
		private final String classLoader;

		CacheKey(final String confFileName, final ClassLoader classLoader) {
			this.confFileName = confFileName;
			this.classLoader = classLoader.getClass().getTypeName();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((classLoader == null) ? 0 : classLoader.hashCode());
			result = prime * result
					+ ((confFileName == null) ? 0 : confFileName.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final CacheKey other = (CacheKey) obj;
			if (classLoader == null) {
				if (other.classLoader != null) {
					return false;
				}
			} else if (!classLoader.equals(other.classLoader)) {
				return false;
			}
			if (confFileName == null) {
				if (other.confFileName != null) {
					return false;
				}
			} else if (!confFileName.equals(other.confFileName)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "CacheKey [confFileName=" + confFileName + ", classLoader="
					+ classLoader + "]";
		}

	}
//[y]added-end

}
