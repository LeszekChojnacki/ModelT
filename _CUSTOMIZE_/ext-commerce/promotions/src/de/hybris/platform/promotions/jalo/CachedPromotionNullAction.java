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
package de.hybris.platform.promotions.jalo;

import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.JaloOnlyItem;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.servicelayer.internal.jalo.order.JaloOnlyItemHelper;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


public class CachedPromotionNullAction extends GeneratedCachedPromotionNullAction implements JaloOnlyItem//NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CachedPromotionNullAction.class.getName());

	private JaloOnlyItemHelper data;

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		final Set missing = new HashSet();
		if (!checkMandatoryAttribute(AbstractPromotionAction.MARKEDAPPLIED, allAttributes, missing))
		{
			throw new JaloInvalidParameterException("missing parameters " + missing + " to create a cart ", 0);
		}
		final Class cl = type.getJaloClass();
		try
		{
			final CachedPromotionNullAction newOne = (CachedPromotionNullAction) cl.newInstance();
			newOne.setTenant(type.getTenant());
			newOne.data = new JaloOnlyItemHelper(//
					(de.hybris.platform.core.PK) allAttributes.get(PK), //
					newOne, //
					type, //
					new Date(), //
					null//
			);
			return newOne;
		}
		catch (final ClassCastException | InstantiationException | IllegalAccessException e)
		{
			throw new JaloGenericCreationException(
					"could not instantiate wizard class " + cl + " of type " + type.getCode() + " : " + e, 0);
		}
	}

	@Override
	public Boolean isMarkedApplied(final SessionContext ctx)
	{
		return data.getProperty(ctx, MARKEDAPPLIED);
	}

	@Override
	public void setMarkedApplied(final SessionContext ctx, final Boolean markedApplied)
	{
		data.setProperty(ctx, MARKEDAPPLIED, markedApplied);
	}

	@Override
	public String getGuid(final SessionContext ctx)
	{
		return data.getProperty(ctx, GUID);
	}

	@Override
	public void setGuid(final SessionContext ctx, final String guid)
	{
		data.setProperty(ctx, GUID, guid);
	}

	@Override
	public PromotionResult getPromotionResult(final SessionContext ctx)
	{
		return data.getProperty(ctx, PROMOTIONRESULT);
	}

	@Override
	public void setPromotionResult(final SessionContext ctx, final PromotionResult promotionResult)
	{
		data.setProperty(ctx, PROMOTIONRESULT, promotionResult);
	}

	//----------------------------------------------------------------------------------
	// --- JaloOnlyItem methods
	//----------------------------------------------------------------------------------

	/**
	 * Provides composed as part of {@link JaloOnlyItem} contract. Never call directly
	 */
	@Override
	public final ComposedType provideComposedType()
	{
		return this.data.provideComposedType();
	}

	/**
	 * Provides creation time as part of {@link JaloOnlyItem} contract. Never call directly
	 */
	@Override
	public final Date provideCreationTime()
	{
		return this.data.provideCreationTime();
	}

	/**
	 * Provides modification time as part of {@link JaloOnlyItem} contract. Never call directly
	 */
	@Override
	public final Date provideModificationTime()
	{
		return this.data.provideModificationTime();
	}

	/**
	 * Provides PK part of {@link JaloOnlyItem} contract. Never call directly
	 */
	@Override
	public final de.hybris.platform.core.PK providePK()
	{
		return this.data.providePK();
	}

	/**
	 * Custom removal logic as part of {@link JaloOnlyItem} contract. Never call directly
	 */
	@Override
	public void removeJaloOnly() throws ConsistencyCheckException
	{
		this.data.removeJaloOnly();
	}

	/**
	 * Custom attribute access as part of {@link JaloOnlyItem} contract. Never call directly
	 */
	@Override
	public Object doGetAttribute(final SessionContext ctx, final String attrQualifier) throws JaloSecurityException
	{
		return this.data.doGetAttribute(ctx, attrQualifier);
	}

	/**
	 * Custom attribute access as part of {@link JaloOnlyItem} contract. Never call directly
	 */
	@Override
	public void doSetAttribute(final SessionContext ctx, final String attrQualifier, final Object value)
			throws JaloBusinessException
	{
		this.data.doSetAttribute(ctx, attrQualifier, value);
	}
}
