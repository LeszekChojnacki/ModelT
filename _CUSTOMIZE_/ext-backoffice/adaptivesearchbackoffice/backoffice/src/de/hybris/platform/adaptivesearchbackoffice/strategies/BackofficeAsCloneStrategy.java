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
package de.hybris.platform.adaptivesearchbackoffice.strategies;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsCloneStrategy;
import de.hybris.platform.core.model.ItemModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.facades.clone.CloneStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectCloningException;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;


/**
 * Default Clone Strategy for Adaptive Search Types
 */
public class BackofficeAsCloneStrategy implements CloneStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(BackofficeAsCloneStrategy.class);

	private TypeFacade typeFacade;
	private ObjectFacade objectFacade;
	private AsCloneStrategy asCloneStrategy;

	@Override
	public int getOrder()
	{
		return Integer.MAX_VALUE / 4;
	}

	@Override
	public <T> boolean canHandle(final T objectToClone)
	{
		try
		{
			return isSupportedModel(objectToClone) && !isNew(objectToClone) && !isSingleton(objectToClone);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.error("Can't find object type.", e);
		}
		return false;

	}

	@Override
	public <T> T clone(final T objectToClone) throws ObjectCloningException
	{
		if (!canHandle(objectToClone))
		{
			throw new IllegalStateException("You can't clone with strategy for which canHandle() return false");
		}

		final T cloned = (T) asCloneStrategy.clone((ItemModel) objectToClone);
		if (cloned instanceof AbstractAsSearchProfileModel)
		{
			final AbstractAsSearchProfileModel asCloned = (AbstractAsSearchProfileModel) cloned;
			asCloned.setCode(StringUtils.EMPTY);
			asCloned.setActivationSet(null);
		}

		return cloned;

	}

	protected boolean isSupportedModel(final Object objectToClone)
	{
		return objectToClone instanceof AbstractAsSearchProfileModel || objectToClone instanceof AbstractAsConfigurationModel;
	}

	protected boolean isNew(final Object objectToClone)
	{
		return getObjectFacade().isNew(objectToClone);
	}

	protected boolean isSingleton(final Object objectToClone) throws TypeNotFoundException
	{
		final String typeName = getTypeFacade().getType(objectToClone);
		final DataType typeData = getTypeFacade().load(typeName);
		return typeData.isSingleton();
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	public AsCloneStrategy getAsCloneStrategy()
	{
		return asCloneStrategy;
	}

	@Required
	public void setAsCloneStrategy(final AsCloneStrategy asCloneStrategy)
	{
		this.asCloneStrategy = asCloneStrategy;
	}

}
