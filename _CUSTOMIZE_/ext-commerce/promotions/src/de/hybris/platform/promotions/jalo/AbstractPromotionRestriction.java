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


import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Language;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloAbstractTypeException;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.jalo.type.TypeManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * AbstractPromotionRestriction. The base class for all restrictions. Promotion restrictions are attached to promotions
 * to control when a promotion may be evaluated. Restrictions have control over the evaluation of the promotion and also
 * can filter the set of available products. Specific restriction behaviours are implemented in subclasses.
 *
 *
 */
public abstract class AbstractPromotionRestriction extends GeneratedAbstractPromotionRestriction
{
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractPromotionRestriction.class.getName());

	/**
	 * Result values from the evaluate methods.
	 *
	 * Result values from the {@link AbstractPromotionRestriction#evaluate} methods.
	 */
	public enum RestrictionResult
	{
		/**
		 * The restriction has denied the evaluation.
		 */
		DENY,

		/**
		 * The restriction has allowed the evaluation.
		 */
		ALLOW,

		/**
		 * The restriction has allowed the evaluation, but has restricted the set of products that can be operated on.
		 */
		ADJUSTED_PRODUCTS
	}

	/**
	 * Evaluate this restriction.
	 *
	 * Evaluate the restrictions against the order, date and products specified. The <c>order</c> specified may be
	 * <i>null</i>. The restriction may remove products from the list of products and return
	 * {@link RestrictionResult#ADJUSTED_PRODUCTS}.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param products
	 *           The collection of {@link Product} instances
	 * @param date
	 *           The date to use in any evaluations
	 * @param order
	 *           The order (optional)
	 * @return the {@link RestrictionResult} indicating the result of the evaluation
	 */
	public abstract RestrictionResult evaluate(SessionContext ctx, Collection<Product> products, Date date, AbstractOrder order); // NOSONAR

	/**
	 * Evaluate this restriction.
	 *
	 * Evaluate the restrictions against the order, date and products specified. The <c>order</c> specified may be
	 * <i>null</i>. The restriction may remove products from the list of products and return
	 * {@link RestrictionResult#ADJUSTED_PRODUCTS}.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param product
	 *           The {@link Product} instance to test against.
	 * @param date
	 *           The date to use in any evaluations
	 * @param order
	 *           The order (optional)
	 * @return the {@link RestrictionResult} indicating the result of the evaluation
	 */
	public final RestrictionResult evaluate(final SessionContext ctx, final Product product, final Date date, // NOSONAR
			final AbstractOrder order)
	{
		final ArrayList<Product> products = new ArrayList<Product>(1); // NOSONAR

		if (product != null)
		{
			products.add(product);
		}

		return evaluate(ctx, products, date, order);
	}

	@Override
	public String toString()
	{
		if (getImplementation() == null)
		{
			return super.toString();
		}
		return this.getClass().getSimpleName() + " '" + getRenderedDescription() + "' (" + getPK().getLongValueAsString() + ")";
	}

	/**
	 * Return the type name for this restriction instance.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return the type name for this instance.
	 */
	@Override
	public String getRestrictionType(final SessionContext ctx)
	{
		return getComposedType().getName(ctx);
	}

	/**
	 * Get restrictionType for all supported languages.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return a map of the type names keyed by language.
	 */
	@Override
	public Map getAllRestrictionType(final SessionContext ctx)
	{
		final Map result = new HashMap();
		final SessionContext localContext = getSession().createSessionContext();

		for (final Language language : getSession().getC2LManager().getAllLanguages()) // NOSONAR
		{
			localContext.setLanguage(language);
			final String itemTypeName = getRestrictionType(localContext);
			if (itemTypeName != null)
			{
				result.put(language, itemTypeName);
			}
		}

		return result;
	}

	@Override
	public String getRenderedDescription(final SessionContext ctx)
	{
		String pattern = null;
		if (ctx.getLanguage() == null)
		{
			super.getDescriptionPattern();
		}
		else
		{
			pattern = super.getDescriptionPattern(ctx);
		}
		if (pattern != null)
		{
			return MessageFormat.format(pattern, getDescriptionPatternArguments(ctx));
		}
		return null;
	}

	protected Object[] getDescriptionPatternArguments(final SessionContext ctx)
	{
		return new Object[]
		{ getRestrictionType(ctx) };
	}

	/**
	 * Build a unique data driven key.
	 *
	 * Build a unique key that is data driven. This will uniquely identify the rules used in this action and will form
	 * the immutable key for stored promotions.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param builder
	 *           A {@link StringBuilder} used to build up the immutable key
	 */
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder) //NOSONAR
	{
		builder.append(this.getClass().getSimpleName()).append('|');
	}

	/**
	 * Deep clone this restriction instance. Promotions must be deep cloned, therefore promotion restrictions must also
	 * support deep cloning.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return a clone of this instance
	 */
	protected AbstractPromotionRestriction deepClone(final SessionContext ctx)
	{
		try
		{
			final Map values = this.getAllAttributes(ctx);

			// Remove 'standard' values that cannot be set
			values.remove(Item.PK);
			values.remove(Item.MODIFIED_TIME);
			values.remove(Item.CREATION_TIME);
			values.remove("savedvalues");
			values.remove("customLinkQualifier");
			values.remove("synchronizedCopies");
			values.remove("synchronizationSources");
			values.remove("alldocuments");
			values.remove(Item.TYPE);
			values.remove(Item.OWNER);

			// Remove old promotion
			values.remove(AbstractPromotionRestriction.PROMOTION);

			// Clone subclass specific values
			deepCloneAttributes(ctx, values);

			final ComposedType type = TypeManager.getInstance().getComposedType(this.getClass()); // NOSONAR
			try //NOSONAR
			{
				return (AbstractPromotionRestriction) type.newInstance(ctx, values);
			}
			catch (final JaloGenericCreationException | JaloAbstractTypeException ex)
			{
				log.warn("deepClone [" + this + "] failed to create instance of " + this.getClass().getSimpleName(), ex);
			}
		}
		catch (final JaloSecurityException ex)
		{
			log.warn("deepClone [" + this + "] failed to getAllAttributes", ex);
		}
		return null;
	}

	/**
	 * Called to deep clone attributes of this instance
	 *
	 * The values map contains all the attributes defined on this instance. The map will be used to initialize a new
	 * instance of the Action that is a clone of this instance. This method can remove, replace or add to the Map of
	 * attributes.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param values
	 *           The map to write into
	 */
	protected void deepCloneAttributes(final SessionContext ctx, final Map values) //NOSONAR
	{
		// Remove RestrictionType as it is read-only and won't change
		values.remove(AbstractPromotionRestriction.RESTRICTIONTYPE);

		// Remove RenderedDescription as it is read-only and is regenerated on request
		values.remove(AbstractPromotionRestriction.RENDEREDDESCRIPTION);
	}

}
