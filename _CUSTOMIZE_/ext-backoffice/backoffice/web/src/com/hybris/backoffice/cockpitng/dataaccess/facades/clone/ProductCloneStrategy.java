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

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.internal.model.ModelCloningContext;
import de.hybris.platform.servicelayer.model.ModelService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.facades.clone.CloneStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;


/**
 * Implementation {@link CloneStrategy} for {@link ProductModel}
 */
public class ProductCloneStrategy implements CloneStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(ProductCloneStrategy.class);

	private ModelService modelService;
	private TypeFacade typeFacade;
	private ObjectFacade objectFacade;

	@Override
	public <T> boolean canHandle(final T objectToClone)
	{
		try
		{
			return isProductModel(objectToClone) && !isNew(objectToClone) && !isSingleton(objectToClone);
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

		final ModelCloningContext context = new ProductModelCloningContext(typeFacade);

		final T clonedProduct = getModelService().clone(objectToClone, context);
		getModelService().setAttributeValue(clonedProduct, ProductModel.CODE, null);
		return clonedProduct;
	}

	private static class ProductModelCloningContext implements ModelCloningContext
	{
		private TypeFacade typeFacade;

		public ProductModelCloningContext(final TypeFacade typeFacade)
		{
			this.typeFacade = typeFacade;
		}

		@Override
		public boolean skipAttribute(final Object original, final String qualifier)
		{
			final DataAttribute attr = typeFacade.getAttribute(original, qualifier);
			if (attr != null && attr.isPartOf())
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Skipping {}.{}", original, qualifier);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean treatAsPartOf(final Object original, final String qualifier)
		{
			return false;
		}

		@Override
		public boolean usePresetValue(final Object original, final String qualifier)
		{
			return false;
		}

		@Override
		public Object getPresetValue(final Object original, final String qualifier)
		{
			return null;
		}
	}

	private static boolean isProductModel(final Object objectToClone)
	{
		return objectToClone instanceof ProductModel;
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

	/**
	 * @return Integer.MAX_VALUE / 4
	 */
	@Override
	public int getOrder()
	{
		return Integer.MAX_VALUE / 4;
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
