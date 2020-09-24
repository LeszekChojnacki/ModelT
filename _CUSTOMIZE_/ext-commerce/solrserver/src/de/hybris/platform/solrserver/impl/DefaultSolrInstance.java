/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.solrserver.impl;

import de.hybris.platform.solrserver.SolrInstance;
import de.hybris.platform.solrserver.SolrServerMode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;


/**
 * Default implementation of {@link SolrInstance}.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public class DefaultSolrInstance implements SolrInstance
{
	public static final String AUTOSTART_PROPERTY = "autostart";
	public static final String AUTOSTART_DEFAULT_VALUE = Boolean.FALSE.toString();

	public static final String HOST_NAME_PROPERTY = "hostname";
	public static final String HOST_NAME_DEFAULT_VALUE = "localhost";

	public static final String PORT_PROPERTY = "port";
	public static final String PORT_DEFAULT_VALUE = "8983";

	public static final String MODE_PROPERTY = "mode";
	public static final String MODE_DEFAULT_VALUE = "standalone";

	public static final String ZK_HOST_PROPERTY = "zk.host";
	public static final String ZK_HOST_DEFAULT_VALUE = "";

	public static final String ZK_UPDATE_CONFIG_PROPERTY = "zk.upconfig";
	public static final String ZK_UPDATE_CONFIG_DEFAULT_VALUE = Boolean.TRUE.toString();

	public static final String ZK_PROPERTIES_PREFIX = "zk.prop.";

	public static final String CONFIG_DIR_PROPERTY = "config.dir";
	public static final String CONFIG_DIR_DEFAULT_VALUE = "";

	public static final String DATA_DIR_PROPERTY = "data.dir";
	public static final String DATA_DIR_DEFAULT_VALUE = "";

	public static final String LOG_DIR_PROPERTY = "log.dir";
	public static final String LOG_DIR_DEFAULT_VALUE = "";

	public static final String MEMORY_PROPERTY = "memory";
	public static final String MEMORY_DEFAULT_VALUE = "512m";

	public static final String JAVA_OPTIONS_PROPERTY = "javaoptions";
	public static final String JAVA_OPTIONS_DEFAULT_VALUE = "";

	public static final String AUTH_TYPE_PROPERTY = "authtype";
	public static final String AUTH_TYPE_DEFAULT_VALUE = "basic";

	public static final String USER_PROPERTY = "user";
	public static final String USER_DEFAULT_VALUE = "";

	public static final String PASSWORD_PROPERTY = "password";
	public static final String PASSWORD_DEFAULT_VALUE = "";

	public static final String SSL_ENABLED = "ssl.enabled";
	public static final String SSL_ENABLED_DEFAULT_VALUE = Boolean.TRUE.toString();

	public static final String SSL_KEY_STORE_TYPE = "ssl.keyStoreType";
	public static final String SSL_KEY_STORE_TYPE_DEFAULT_VALUE = "JKS";

	public static final String SSL_KEY_STORE = "ssl.keyStore";
	public static final String SSL_KEY_STORE_DEFAULT_VALUE = null;

	public static final String SSL_KEY_STORE_PASSWORD = "ssl.keyStorePassword";
	public static final String SSL_KEY_STORE_PASSWORD_DEFAULT_VALUE = "";

	public static final String SSL_TRUST_STORE_TYPE = "ssl.trustStoreType";
	public static final String SSL_TRUST_STORE_TYPE_DEFAULT_VALUE = "JKS";

	public static final String SSL_TRUST_STORE = "ssl.trustStore";
	public static final String SSL_TRUST_STORE_DEFAULT_VALUE = null;

	public static final String SSL_TRUST_STORE_PASSWORD = "ssl.trustStorePassword";
	public static final String SSL_TRUST_STORE_PASSWORD_DEFAULT_VALUE = "";

	public static final String SSL_NEED_CLIENT_AUTH = "ssl.needClientAuth";
	public static final String SSL_NEED_CLIENT_AUTH_DEFAULT_VALUE = Boolean.FALSE.toString();

	public static final String SSL_WANT_CLIENT_AUTH = "ssl.wantClientAuth";
	public static final String SSL_WANT_CLIENT_AUTH_DEFAULT_VALUE = Boolean.FALSE.toString();

	private final String name;
	private final Map<String, String> configuration;

	/**
	 * Creates an empty Solr instance (with default configuration).
	 *
	 * @param name
	 *           - the name
	 */
	public DefaultSolrInstance(final String name)
	{
		this.name = name;
		this.configuration = initializeConfiguration();
	}

	/**
	 * Creates an empty Solr instance.
	 *
	 * @param name
	 *           - the name
	 * @param configuration
	 *           - the configuration
	 */
	public DefaultSolrInstance(final String name, final Map<String, String> configuration)
	{
		this.name = name;
		this.configuration = initializeConfiguration();

		if (configuration != null && !configuration.isEmpty())
		{
			this.configuration.putAll(configuration);
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Map<String, String> getConfiguration()
	{
		return configuration;
	}

	@Override
	public boolean isAutostart()
	{
		return Boolean.parseBoolean(configuration.get(AUTOSTART_PROPERTY));
	}

	@Override
	public String getHostName()
	{
		return configuration.get(HOST_NAME_PROPERTY);
	}

	@Override
	public int getPort()
	{
		return Integer.parseInt(configuration.get(PORT_PROPERTY));
	}

	@Override
	public SolrServerMode getMode()
	{
		return SolrServerMode.valueOf(configuration.get(MODE_PROPERTY).toUpperCase(Locale.ROOT));
	}

	@Override
	public String getZkHost()
	{
		return configuration.get(ZK_HOST_PROPERTY);
	}

	@Override
	public boolean isZkUpdateConfig()
	{
		return Boolean.parseBoolean(configuration.get(ZK_UPDATE_CONFIG_PROPERTY));
	}

	@Override
	public Map<String, String> getZkProperties()
	{
		return configuration.entrySet().stream().filter(entry -> StringUtils.startsWith(entry.getKey(), ZK_PROPERTIES_PREFIX))
				.collect(Collectors.toMap(entry -> StringUtils.removeStart(entry.getKey(), ZK_PROPERTIES_PREFIX), Entry::getValue));
	}

	@Override
	public String getConfigDir()
	{
		return configuration.get(CONFIG_DIR_PROPERTY);
	}

	@Override
	public String getDataDir()
	{
		return configuration.get(DATA_DIR_PROPERTY);
	}

	@Override
	public String getLogDir()
	{
		return configuration.get(LOG_DIR_PROPERTY);
	}

	@Override
	public String getMemory()
	{
		return configuration.get(MEMORY_PROPERTY);
	}

	@Override
	public String getJavaOptions()
	{
		return configuration.get(JAVA_OPTIONS_PROPERTY);
	}

	@Override
	public String getAuthType()
	{
		return configuration.get(AUTH_TYPE_PROPERTY);
	}

	@Override
	public String getUser()
	{
		return configuration.get(USER_PROPERTY);
	}

	@Override
	public String getPassword()
	{
		return configuration.get(PASSWORD_PROPERTY);
	}

	@Override
	public boolean isSSLEnabled()
	{
		return Boolean.parseBoolean(configuration.get(SSL_ENABLED));
	}

	@Override
	public String getSSLKeyStoreType()
	{
		return configuration.get(SSL_KEY_STORE_TYPE);
	}

	@Override
	public String getSSLKeyStore()
	{
		return configuration.get(SSL_KEY_STORE);
	}

	@Override
	public String getSSLKeyStorePassword()
	{
		return configuration.get(SSL_KEY_STORE_PASSWORD);
	}

	@Override
	public String getSSLTrustStoreType()
	{
		return configuration.get(SSL_TRUST_STORE_TYPE);
	}

	@Override
	public String getSSLTrustStore()
	{
		return configuration.get(SSL_TRUST_STORE);
	}

	@Override
	public String getSSLTrustStorePassword()
	{
		return configuration.get(SSL_TRUST_STORE_PASSWORD);
	}

	@Override
	public boolean isSSLNeedClientAuth()
	{
		return Boolean.parseBoolean(configuration.get(SSL_NEED_CLIENT_AUTH));
	}

	@Override
	public boolean isSSLWantClientAuth()
	{
		return Boolean.parseBoolean(configuration.get(SSL_WANT_CLIENT_AUTH));
	}

	protected final Map<String, String> initializeConfiguration()
	{
		final Map<String, String> defaultConfig = new HashMap<>();

		defaultConfig.put(AUTOSTART_PROPERTY, AUTOSTART_DEFAULT_VALUE);
		defaultConfig.put(HOST_NAME_PROPERTY, HOST_NAME_DEFAULT_VALUE);
		defaultConfig.put(PORT_PROPERTY, PORT_DEFAULT_VALUE);
		defaultConfig.put(MODE_PROPERTY, MODE_DEFAULT_VALUE);
		defaultConfig.put(ZK_HOST_PROPERTY, ZK_HOST_DEFAULT_VALUE);
		defaultConfig.put(ZK_UPDATE_CONFIG_PROPERTY, ZK_UPDATE_CONFIG_DEFAULT_VALUE);
		defaultConfig.put(CONFIG_DIR_PROPERTY, CONFIG_DIR_DEFAULT_VALUE);
		defaultConfig.put(DATA_DIR_PROPERTY, DATA_DIR_DEFAULT_VALUE);
		defaultConfig.put(LOG_DIR_PROPERTY, LOG_DIR_DEFAULT_VALUE);
		defaultConfig.put(MEMORY_PROPERTY, MEMORY_DEFAULT_VALUE);
		defaultConfig.put(JAVA_OPTIONS_PROPERTY, JAVA_OPTIONS_DEFAULT_VALUE);

		defaultConfig.put(AUTH_TYPE_PROPERTY, AUTH_TYPE_DEFAULT_VALUE);
		defaultConfig.put(USER_PROPERTY, USER_DEFAULT_VALUE);
		defaultConfig.put(PASSWORD_PROPERTY, PASSWORD_DEFAULT_VALUE);

		defaultConfig.put(SSL_ENABLED, SSL_ENABLED_DEFAULT_VALUE);
		defaultConfig.put(SSL_KEY_STORE_TYPE, SSL_KEY_STORE_TYPE_DEFAULT_VALUE);
		defaultConfig.put(SSL_KEY_STORE, SSL_KEY_STORE_DEFAULT_VALUE);
		defaultConfig.put(SSL_KEY_STORE_PASSWORD, SSL_TRUST_STORE_PASSWORD_DEFAULT_VALUE);
		defaultConfig.put(SSL_TRUST_STORE_TYPE, SSL_TRUST_STORE_TYPE_DEFAULT_VALUE);
		defaultConfig.put(SSL_TRUST_STORE, SSL_TRUST_STORE_DEFAULT_VALUE);
		defaultConfig.put(SSL_TRUST_STORE_PASSWORD, SSL_TRUST_STORE_PASSWORD_DEFAULT_VALUE);
		defaultConfig.put(SSL_NEED_CLIENT_AUTH, SSL_NEED_CLIENT_AUTH_DEFAULT_VALUE);
		defaultConfig.put(SSL_WANT_CLIENT_AUTH, SSL_WANT_CLIENT_AUTH_DEFAULT_VALUE);

		return defaultConfig;
	}

	@Override
	public String toString()
	{
		return "[name: " + getName() + ", hostname: " + getHostName() + ", port: " + getPort() + "]";
	}
}
