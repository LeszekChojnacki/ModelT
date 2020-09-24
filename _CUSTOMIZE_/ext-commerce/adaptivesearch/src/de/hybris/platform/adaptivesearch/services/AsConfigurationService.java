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
package de.hybris.platform.adaptivesearch.services;

import de.hybris.platform.adaptivesearch.data.AsRankChange;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;

import java.util.List;
import java.util.Optional;


/**
 * Service that provides basic functionality for configurations.
 *
 * @since 6.7
 */
public interface AsConfigurationService
{
	/**
	 * Returns the configuration for a specific type, catalog version and uid.
	 *
	 * @param type
	 *           - the type
	 * @param catalogVersion
	 *           - the catalog version
	 * @param uid
	 *           - the unique identifier
	 *
	 * @return the configuration
	 */
	<T extends AbstractAsConfigurationModel> Optional<T> getConfigurationForUid(Class<T> type,
			final CatalogVersionModel catalogVersion, String uid);

	/**
	 * Requests the persistence to update the given configuration.
	 *
	 * @param configuration
	 *           - the configuration
	 */
	void refreshConfiguration(final AbstractAsConfigurationModel configuration);

	/**
	 * Creates a new configuration. The new configuration remains not persisted.
	 *
	 * @param type
	 *           - the type
	 *
	 * @return the configuration
	 */
	<T extends AbstractAsConfigurationModel> T createConfiguration(Class<T> type);

	/**
	 * Clones the given configuration.
	 *
	 * @param configuration
	 *           - the configuration to be cloned
	 * @return - the cloned version
	 */
	<T extends AbstractAsConfigurationModel> T cloneConfiguration(final T configuration);

	/**
	 * Saves the given configuration.
	 *
	 * @param configuration
	 *           - the configuration
	 */
	void saveConfiguration(final AbstractAsConfigurationModel configuration);

	/**
	 * Removes the given configuration.
	 *
	 * @param configuration
	 *           - the configuration
	 */
	void removeConfiguration(final AbstractAsConfigurationModel configuration);

	/**
	 * Checks whether the given configuration is valid.
	 *
	 * @param configuration
	 *           - the configuration
	 *
	 * @return <code>true</code> if configuration is valid, <code>false</code> otherwise
	 */
	boolean isValid(final AbstractAsConfigurationModel configuration);

	/**
	 * Moves a configuration, all compatible attributes are copied. The given parent configuration will be updated and
	 * unsaved changes might be lost.
	 *
	 * @param parentConfiguration
	 *           - the parent configuration
	 * @param sourceAttribute
	 *           - the source attribute
	 * @param targetAttribute
	 *           - the target attribute
	 * @param uid
	 *           - the the unique identifier
	 *
	 * @return <code>true</code> if the configuration was moved, <code>false</code> otherwise
	 *
	 * @throws AttributeNotSupportedException
	 *            if sourceAttribute or targetAttribute do not exist on the given parentConfiguration or are not of type
	 *            {@link AbstractAsConfigurationModel}
	 */
	boolean moveConfiguration(final AbstractAsConfigurationModel parentConfiguration, final String sourceAttribute,
			String targetAttribute, String uid);

	/**
	 * Ranks multiple configurations before another one. If the given rankBeforeUid is blank the configuration is moved
	 * to the last position. The given parent configuration will be updated and unsaved changes might be lost.
	 *
	 * @param parentConfiguration
	 *           - the parent configuration
	 * @param attribute
	 *           - the attribute
	 * @param rankBeforeUid
	 *           - the rank before unique identifier
	 * @param uids
	 *           - the the unique identifiers
	 *
	 * @return the rank changes
	 *
	 * @throws AttributeNotSupportedException
	 *            if attribute does not exist on the given parentConfiguration or is not of type
	 *            {@link AbstractAsConfigurationModel}
	 */
	List<AsRankChange> rankBeforeConfiguration(final AbstractAsConfigurationModel parentConfiguration, String attribute,
			String rankBeforeUid, String... uids);

	/**
	 * Ranks multiple configurations after another one. If the given rankAfterUid is blank the configuration is moved to
	 * the first position. The given parent configuration will be updated and unsaved changes might be lost.
	 *
	 * @param parentConfiguration
	 *           - the parent configuration
	 * @param attribute
	 *           - the attribute
	 * @param rankAfterUid
	 *           - the rank before unique identifier
	 * @param uids
	 *           - the the unique identifiers
	 *
	 * @return the rank changes
	 *
	 * @throws AttributeNotSupportedException
	 *            if attribute does not exist on the given parentConfiguration or is not of type
	 *            {@link AbstractAsConfigurationModel}
	 */
	List<AsRankChange> rankAfterConfiguration(final AbstractAsConfigurationModel parentConfiguration, String attribute,
			String rankAfterUid, String... uids);

	/**
	 * Reranks a configuration. The given parent configuration will be updated and unsaved changes might be lost.
	 *
	 * @param parentConfiguration
	 *           - the parent configuration
	 * @param attribute
	 *           - the attribute
	 * @param uid
	 *           - the the unique identifier
	 * @param change
	 *           - the change
	 *
	 * @return the rank change
	 *
	 * @throws AttributeNotSupportedException
	 *            if attribute does not exist on the given parentConfiguration or is not of type
	 *            {@link AbstractAsConfigurationModel}
	 */
	AsRankChange rerankConfiguration(final AbstractAsConfigurationModel parentConfiguration, String attribute, String uid,
			int change);

}
