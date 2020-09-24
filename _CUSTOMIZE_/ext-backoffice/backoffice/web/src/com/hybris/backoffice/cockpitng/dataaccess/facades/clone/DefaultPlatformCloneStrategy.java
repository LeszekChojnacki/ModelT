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
package com.hybris.backoffice.cockpitng.dataaccess.facades.clone;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.facades.clone.CloneStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;


/**
 * Default implementation of {@link CloneStrategy}. Can handle all objects that parent is {@link ItemModel} and are not
 * new. </br>
 * !!!! Be aware of using this class. Some unexpected result may appear. If you really want this see how relation
 * 'partOf' == true behave. For example product variant, tax rows, price rows and few more.
 */
public class DefaultPlatformCloneStrategy implements CloneStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPlatformCloneStrategy.class);

	private ModelService modelService;
	private TypeService typeService;
	private TypeFacade typeFacade;
	private ObjectFacade objectFacade;

	@Override
	public <T> boolean canHandle(final T objectToClone)
	{
		try
		{
			return isItemModel(objectToClone) && !isNew(objectToClone) && !isSingleton(objectToClone);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.error("Can't fine object type.", e);
		}
		return false;
	}

	@Override
	public <T> T clone(final T objectToClone)
	{
		if (!canHandle(objectToClone))
		{
			throw new IllegalStateException("You can't clone with strategy for which canHandle() return false");
		}

		final T clonedObject = getModelService().clone(objectToClone);
		clearUniqueAttributes(clonedObject);
		return clonedObject;
	}

	private boolean isItemModel(final Object objectToClone)
	{
		return objectToClone instanceof ItemModel;
	}

	private boolean isNew(final Object objectToClone)
	{
		return getObjectFacade().isNew(objectToClone);
	}

	private boolean isSingleton(final Object objectToClone) throws TypeNotFoundException
	{
		final String typeName = getTypeFacade().getType(objectToClone);
		final DataType typeData = getTypeFacade().load(typeName);
		return typeData.isSingleton();
	}

	private <T> void clearUniqueAttributes(final T clonedObject)
	{
		final Set<String> uniqueAttributes = getTypeService().getUniqueAttributes(getTypeFacade().getType(clonedObject));
		uniqueAttributes.forEach(attribute -> getModelService().setAttributeValue(clonedObject, attribute, (Object) null));
	}

	/**
	 * @return Integer.MAX_VALUE / 2
	 */
	@Override
	public int getOrder()
	{
		return Integer.MAX_VALUE / 2;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
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

}
