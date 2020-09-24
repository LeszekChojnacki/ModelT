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
/*

 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 *
 */
package de.hybris.platform.solrserver;

import java.util.Map;


/**
 * Represents a Solr instance.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public interface SolrInstance
{
	/**
	 * Returns the name of the instance.
	 *
	 * @return the name of the instance
	 */
	String getName();

	/**
	 * Returns the configuration of the instance.
	 *
	 * @return the configuration of the instance
	 */
	Map<String, String> getConfiguration();

	/**
	 * Returns <code>true</code> if the instance should be started on hybris startup, <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the instance should be started on hybris startup, <code>false</code> otherwise
	 */
	boolean isAutostart();

	/**
	 * Returns the hostname of the instance.
	 *
	 * @return the hostname of the instance
	 */
	String getHostName();

	/**
	 * Returns the port of the instance.
	 *
	 * @return the port of the instance
	 */
	int getPort();

	/**
	 * Returns the mode of the instance.
	 *
	 * @return the mode of the instance
	 */
	SolrServerMode getMode();

	/**
	 * Returns the Zookeeper instance host at which Solr is pointing.
	 *
	 * @return the Zookeeper instance host at which Solr is pointing
	 */
	String getZkHost();

	/**
	 * Returns <code>true</code> if the configuration should be uploaded (updated) into Zookeeper instance,
	 * <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the configuration should be uploaded (updated) into Zookeeper instance,
	 *         <code>false</code> otherwise.
	 */
	boolean isZkUpdateConfig();

	/**
	 * Returns cluster-wide properties stored in Zookeeper.
	 *
	 * @return the cluster-wide properties
	 */
	Map<String, String> getZkProperties();

	/**
	 * Returns the configuration directory of the instance.
	 *
	 * @return the configuration directory of the instance
	 */
	String getConfigDir();

	/**
	 * Returns the data directory of the instance.
	 *
	 * @return the data directory of the instance
	 */
	String getDataDir();

	/**
	 * Returns the log directory of the instance.
	 *
	 * @return the log directory of the instance
	 */
	String getLogDir();

	/**
	 * Returns the memory of the instance.
	 *
	 * @return the memory of the instance
	 */
	String getMemory();

	/**
	 * Returns the additional java options of the instance.
	 *
	 * @return the additional java options of the instance
	 */
	String getJavaOptions();

	/**
	 * Returns the authentication type.
	 *
	 * @return the authentication type
	 */
	String getAuthType();

	/**
	 * Returns the user.
	 *
	 * @return the user
	 */
	String getUser();

	/**
	 * Returns the password.
	 *
	 * @return the password
	 */
	String getPassword();

	/**
	 * Returns whether SSL is enabled or not.
	 *
	 * @return <code>true</code> if SSL is enabled, <code>false</code> otherwise
	 */
	boolean isSSLEnabled();

	/**
	 * Returns the SSL keyStore type.
	 *
	 * @return the keyStore type
	 */
	String getSSLKeyStoreType();

	/**
	 * Returns the SSL keyStore.
	 *
	 * @return the keyStore
	 */
	String getSSLKeyStore();

	/**
	 * Returns the SSL keyStore password.
	 *
	 * @return the keyStore password
	 */
	String getSSLKeyStorePassword();

	/**
	 * Returns the SSL trustStore type.
	 *
	 * @return the trustStore type
	 */
	String getSSLTrustStoreType();

	/**
	 * Returns the SSL trustStore.
	 *
	 * @return the trustStore
	 */
	String getSSLTrustStore();

	/**
	 * Returns the SSL trustStore password.
	 *
	 * @return the trustStore password
	 */
	String getSSLTrustStorePassword();

	/**
	 * Returns whether SSL client authentication is required or not.
	 *
	 * @return <code>true</code> if client authentication should be required, <code>false</code> otherwise
	 */
	boolean isSSLNeedClientAuth();

	/**
	 * Returns whether SSL client authentication should be requested or not.
	 *
	 * @return <code>true</code> if client authentication should be requested, <code>false</code> otherwise
	 */
	boolean isSSLWantClientAuth();
}
