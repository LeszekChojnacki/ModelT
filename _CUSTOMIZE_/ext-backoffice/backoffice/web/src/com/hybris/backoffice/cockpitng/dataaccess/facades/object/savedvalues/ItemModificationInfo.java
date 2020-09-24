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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object.savedvalues;

import de.hybris.platform.core.model.ItemModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;


/**
 * Helper class which collects information about modification of given object
 */
public class ItemModificationInfo
{
	private final ItemModel model;
	private final Map<String, Object> originalValues = new HashMap<String, Object>();
	private final Map<String, Object> modifiedValues = new HashMap<String, Object>();
	private final Set<String> localizedAttributes = new HashSet<String>();
	private boolean newFlag = false;

	public ItemModificationInfo(final ItemModel model)
	{
		this.model = model;
	}

	public void addEntry(final String attribute, final boolean localized, final Object originalValue, final Object modifiedValue)
	{
		if (!valuesEqual(originalValue, modifiedValue))
		{
			this.originalValues.put(attribute, originalValue);
			this.modifiedValues.put(attribute, modifiedValue);
			if (localized)
			{
				this.localizedAttributes.add(attribute);
			}
		}
	}

	public ItemModel getModel()
	{
		return this.model;
	}

	public Set<String> getModifiedAttributes()
	{
		return Collections.unmodifiableSet(this.originalValues.keySet());
	}

	public Object getOriginalValue(final String attribute)
	{
		return this.originalValues.get(attribute);
	}

	public Object getModifiedValue(final String attribute)
	{
		return this.modifiedValues.get(attribute);
	}

	public boolean isLocalized(final String attribute)
	{
		return this.localizedAttributes.contains(attribute);
	}

	public boolean isNew()
	{
		return this.newFlag;
	}

	public void setNew(final boolean isModelNew)
	{
		this.newFlag = isModelNew;
	}

	protected boolean valuesEqual(final Object value1, final Object value2)
	{
		return ObjectUtils.equals(value1, value2);
	}
}
