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
package com.hybris.backoffice.widgets.selectivesync.detailsview;

import de.hybris.platform.catalog.model.SyncAttributeDescriptorConfigModel;

import com.hybris.backoffice.widgets.selectivesync.renderer.SelectiveSyncRenderer;


/** Listener for changes in details view of {@link SelectiveSyncRenderer}. */
public interface DetailsViewAttributeValueChangeListener
{
	/**
	 * Fired when some attribute has changed in syncAttributeDescriptorConfigModel.
	 *
	 * @param syncAttributeDescriptorConfigModel
	 *           attribute descriptor model
	 * @param attribute
	 *           name of changed attribute
	 * @param value
	 *           new value of the changed attribute
	 */
	void attributeChanged(final SyncAttributeDescriptorConfigModel syncAttributeDescriptorConfigModel, final String attribute,
			final Object value);
}
