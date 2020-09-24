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
import de.hybris.platform.jalo.JaloOnlyItem;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.servicelayer.internal.jalo.order.JaloOnlyItemHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;


public class CachedPromotionResult extends GeneratedCachedPromotionResult implements JaloOnlyItem // NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CachedPromotionResult.class.getName());

	private JaloOnlyItemHelper data;

	private final List<AbstractPromotionAction> cachedActions = new ArrayList<>(); // NOSONAR
	private final List<CachedPromotionOrderEntryConsumed> cachedConsumedEntries = new ArrayList<>(); // NOSONAR

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		final Class cl = type.getJaloClass();
		try
		{
			final CachedPromotionResult newOne = (CachedPromotionResult) cl.newInstance();
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
	public Float getCertainty(final SessionContext ctx)
	{
		return data.getProperty(ctx, CERTAINTY);
	}

	@Override
	public void setCertainty(final SessionContext ctx, final Float certainty)
	{
		this.data.setProperty(ctx, CERTAINTY, certainty);
	}

	@Override
	public String getCustom(final SessionContext ctx)
	{
		return data.getProperty(ctx, CUSTOM);
	}

	@Override
	public void setCustom(final SessionContext ctx, final String custom)
	{
		this.data.setProperty(ctx, CUSTOM, custom);
	}

	@Override
	public AbstractOrder getOrder(final SessionContext ctx)
	{
		return data.getProperty(ctx, ORDER);
	}

	@Override
	public void setOrder(final SessionContext ctx, final AbstractOrder order)
	{
		this.data.setProperty(ctx, ORDER, order);
	}

	@Override
	public AbstractPromotion getPromotion(final SessionContext ctx)
	{
		return data.getProperty(ctx, PROMOTION);
	}

	@Override
	public void setPromotion(final SessionContext ctx, final AbstractPromotion promotion)
	{
		this.data.setProperty(ctx, PROMOTION, promotion);
	}

	@Override
	public void addAction(final SessionContext ctx, final AbstractPromotionAction action)
	{
		this.addToCachedActions(ctx, action);
		action.setPromotionResult(this);
	}

	@Override
	public Collection<AbstractPromotionAction> getActions(final SessionContext ctx)
	{
		return this.getCachedActions();
	}

	@Override
	public void setActions(final SessionContext ctx, final Collection actions)
	{
		this.setCachedActions(actions);
		for (final AbstractPromotionAction action : (Collection<AbstractPromotionAction>) actions)
		{
			action.setPromotionResult(this);
		}
	}

	@Override
	public Set<AbstractPromotionAction> getAllPromotionActions(final SessionContext ctx)
	{
		return Collections.unmodifiableSet(new HashSet<AbstractPromotionAction>(this.getCachedActions()));
	}

	@Override
	public void setAllPromotionActions(final SessionContext ctx, final Set<AbstractPromotionAction> actions)
	{
		this.setCachedActions(ctx, actions);
		for (final AbstractPromotionAction action : actions)
		{
			action.setPromotionResult(this);
		}
	}

	@Override
	public void addToAllPromotionActions(final SessionContext ctx, final AbstractPromotionAction action)
	{
		this.addToCachedActions(ctx, action);
		action.setPromotionResult(this);
	}

	@Override
	public void removeFromAllPromotionActions(final SessionContext ctx, final AbstractPromotionAction action)
	{
		this.removeFromCachedActions(ctx, action);
	}

	@Override
	public java.util.Collection<AbstractPromotionAction> getCachedActions(final SessionContext ctx)
	{
		return Collections.unmodifiableCollection(cachedActions);
	}

	@Override
	public void setCachedActions(final SessionContext ctx, final Collection<AbstractPromotionAction> abstractPromotionActions)
	{
		this.cachedActions.clear();
		this.cachedActions.addAll(abstractPromotionActions);
	}

	@SuppressWarnings("unused")
	public void addToCachedActions(final SessionContext ctx, final AbstractPromotionAction action) //NOSONAR
	{
		this.cachedActions.add(action);
	}

	@SuppressWarnings("unused")
	public void removeFromCachedActions(final SessionContext ctx, final AbstractPromotionAction action) //NOSONAR
	{
		this.cachedActions.remove(action);
	}

	@Override
	public java.util.Collection<CachedPromotionOrderEntryConsumed> getCachedConsumedEntries(final SessionContext sessionContext)
	{
		return Collections.unmodifiableCollection(cachedConsumedEntries);
	}

	@Override
	public void setCachedConsumedEntries(final SessionContext ctx,
			final Collection<CachedPromotionOrderEntryConsumed> cachedConsumedEntries)
	{
		this.cachedConsumedEntries.clear();
		this.cachedConsumedEntries.addAll(cachedConsumedEntries);
	}

	@SuppressWarnings("unused")
	public void addToCachedConsumedEntries(final SessionContext ctx, final CachedPromotionOrderEntryConsumed poec) //NOSONAR
	{
		this.cachedConsumedEntries.add(poec);
	}

	@SuppressWarnings("unused")
	public void removeFromCachedConsumedEntries(final SessionContext ctx, final CachedPromotionOrderEntryConsumed poec) //NOSONAR
	{
		this.cachedConsumedEntries.remove(poec);
	}

	@Override
	public void addConsumedEntry(final SessionContext ctx, final PromotionOrderEntryConsumed poec)
	{
		if (!(poec instanceof CachedPromotionOrderEntryConsumed))
		{
			throw new UnsupportedOperationException("Can't store persistent POEC in cache");
		}
		this.addToCachedConsumedEntries(ctx, (CachedPromotionOrderEntryConsumed) poec);
		poec.setPromotionResult(this);
	}

	@Override
	public void removeConsumedEntry(final SessionContext ctx, final PromotionOrderEntryConsumed poec)
	{
		if (!(poec instanceof CachedPromotionOrderEntryConsumed))
		{
			throw new UnsupportedOperationException("Can't remove persistent POEC from cache");
		}
		this.removeFromCachedConsumedEntries(ctx, (CachedPromotionOrderEntryConsumed) poec);
	}

	@Override
	public Collection<PromotionOrderEntryConsumed> getConsumedEntries(final SessionContext ctx)
	{
		return (Collection) this.getCachedConsumedEntries();
	}

	@Override
	public void setConsumedEntries(final SessionContext ctx, final Collection entries)
	{
		this.setCachedConsumedEntries(ctx, entries);
		for (final PromotionOrderEntryConsumed poec : (Collection<PromotionOrderEntryConsumed>) entries)
		{
			poec.setPromotionResult(this);
		}
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
