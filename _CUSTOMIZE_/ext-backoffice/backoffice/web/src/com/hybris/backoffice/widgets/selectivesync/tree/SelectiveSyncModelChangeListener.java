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
package com.hybris.backoffice.widgets.selectivesync.tree;

import de.hybris.platform.catalog.model.SyncAttributeDescriptorConfigModel;

import java.util.Collection;


/** Listener for changes in attributes model. */
public interface SelectiveSyncModelChangeListener
{
	void onValueChanged(final Object source, final Collection<SyncAttributeDescriptorConfigModel> syncAttributeDescriptors);
}
