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


import de.hybris.platform.category.jalo.Category;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloAbstractTypeException;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.jalo.type.TypeManager;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.promotions.util.Comparators;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.Flat3Map;


/**
 * AbstractPromotion. The base class for all promotions.
 */
public abstract class AbstractPromotion extends GeneratedAbstractPromotion // NOSONAR
{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AbstractPromotion.class.getName());

	/**
	 * Create the item.
	 */
	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		if (!allAttributes.containsKey(AbstractPromotion.ENABLED))
		{
			allAttributes.put(AbstractPromotion.ENABLED, Boolean.FALSE);
		}
		if (!allAttributes.containsKey(AbstractPromotion.PROMOTIONGROUP))
		{
			// Use the default promotion group if non specified
			allAttributes.put(AbstractPromotion.PROMOTIONGROUP, PromotionsManager.getInstance().getDefaultPromotionGroup(ctx));
		}

		// then create the item
		return super.createItem(ctx, type, allAttributes);
	}

	/**
	 * Remove the item. When the promotion is removed any associated restriction items are also removed.
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// Remove all of our owned restrictions
		setRestrictions(ctx, Collections.emptyList());

		// then remove this item
		super.remove(ctx);
	}

	/**
	 * Get a simple description of promotion object.
	 *
	 * @return string with type name, code and PK.
	 */
	@Override
	public String toString()
	{
		if (getImplementation() == null)
		{
			return super.toString();
		}
		return this.getClass().getSimpleName() + " '" + getCode() + "' (" + getPK().getLongValueAsString() + ")";
	}

	/**
	 * Return the type name for this promotion instance.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return the type name for this instance.
	 */
	@Override
	public String getPromotionType(final SessionContext ctx)
	{
		return getComposedType().getName(ctx);
	}

	/**
	 * Get promotionType for all supported languages.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return a map of the type names keyed by language.
	 */
	@Override
	public Map getAllPromotionType(final SessionContext ctx)
	{
		return getComposedType().getAllNames(ctx);
	}

	/**
	 * Override the default behaviour to prevent PromotionGroup from being set to null.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param promotionGroup
	 *           The promotion group to set
	 */
	@Override
	public void setPromotionGroup(final SessionContext ctx, final PromotionGroup promotionGroup)
	{
		if (promotionGroup == null)
		{
			throw new JaloInvalidParameterException("Cannot set promotionGroup to NULL", 999);
		}
		super.setPromotionGroup(ctx, promotionGroup);
	}

	/**
	 * Evaluate whether a promotion can fire or not.
	 *
	 * @param ctx
	 *           The context to run the operation in
	 * @param promoContext
	 *           The evaluation context
	 * @return the list of promotion results that this promotions creates
	 */
	public abstract List<PromotionResult> evaluate(SessionContext ctx, PromotionEvaluationContext promoContext);

	/**
	 * Get a localized user presentable description for the result of this promotion.
	 *
	 * @param ctx
	 *           The context
	 * @param promotionResult
	 *           The promotion result to be described
	 * @param locale
	 *           The locale to use to generate the message
	 * @return a string description
	 */
	public abstract String getResultDescription(SessionContext ctx, PromotionResult promotionResult, Locale locale);

	/**
	 * Lookup the price for an order. Lookup the price for the currency specified on the order.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param prices
	 *           The collection of {@link PromotionPriceRow} which is the set of prices
	 * @param order
	 *           The order to lookup the price for
	 * @param fieldLabel
	 *           A string label to use when generating error messages.
	 * @return The price for the order's currency or null if not specified
	 */
	protected final Double getPriceForOrder(final SessionContext ctx, final Collection<PromotionPriceRow> prices,
			final AbstractOrder order, final String fieldLabel)
	{
		if (order == null)
		{
			return null;
		}

		final Currency currency = order.getCurrency(ctx);
		if (currency == null)
		{
			LOG.warn("Order [" + order + "] has null currency");
			return null;
		}
		if (prices != null)
		{
			for (final PromotionPriceRow ppr : prices)
			{
				if (currency.equals(ppr.getCurrency(ctx)))
				{
					return ppr.getPrice(ctx);
				}
			}
		}
		LOG.warn("Missing currency row [" + currency.getName(ctx) + "] for [" + fieldLabel + "] on promotion [" + this + "]");

		return null;
	}

	/**
	 * Format a message pattern using the MessageFormat
	 *
	 * @param pattern
	 *           the message pattern
	 * @param arguments
	 *           the arguments to pass into the pattern
	 * @param locale
	 *           the rendering locale
	 * @return a formatted string
	 */
	protected static final String formatMessage(final String pattern, final Object[] arguments, final Locale locale)
	{
		if (pattern == null)
		{
			return "Error, message not found";
		}

		final MessageFormat mf = new MessageFormat(pattern, locale);
		return mf.format(arguments);
	}

	/**
	 * Find or create an immutable clone of this promotion.
	 *
	 * @param ctx
	 *           the hybris context
	 * @return the immutable version of this promotion
	 */
	protected final AbstractPromotion findOrCreateImmutableClone(final SessionContext ctx)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("findImmutableClone [" + this + "]"); // NOSONAR
		}

		if (getImmutableKeyHash(ctx) != null)
		{
			// The current instance is already immutable!
			return this;
		}
		else
		{
			// Create the immutable key (unique based on promotion rules data)
			final String immutableKey = getDataUniqueKey(ctx);
			final String immutableKeyHash = buildMD5Hash(immutableKey);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("findImmutableClone [" + this + "] immutableKey=[" + immutableKey + "] immutableKeyHash=["
						+ immutableKeyHash + "]");
			}

			// Look for Immutable Instance
			AbstractPromotion immutableInstance = findImmutablePromotionByUniqueKey(getSession(), ctx, immutableKeyHash,
					immutableKey);
			if (immutableInstance == null)
			{
				// Create new immutable instance
				if (LOG.isDebugEnabled())
				{
					LOG.debug("findImmutableClone [" + this + "] creating new clone of promotion");
				}

				immutableInstance = createImmutableDeepClone(ctx, immutableKeyHash, immutableKey);
			}

			return immutableInstance;
		}
	}

	protected static final String buildMD5Hash(final String message)
	{
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(message);
	}

	protected final AbstractPromotion createImmutableDeepClone(final SessionContext ctx, final String immutableKeyHash,
			final String immutableKey)
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

			// Add the immutable key and hash
			values.put(AbstractPromotion.IMMUTABLEKEY, immutableKey);
			values.put(AbstractPromotion.IMMUTABLEKEYHASH, immutableKeyHash);

			// Force the clone not to be enabled
			values.put(AbstractPromotion.ENABLED, Boolean.FALSE);

			// Delegate cloning specific values to subtypes
			deepCloneAttributes(ctx, values);

			final ComposedType type = TypeManager.getInstance().getComposedType(this.getClass()); // NOSONAR
			AbstractPromotion dupAbstractPromotion = null;
			try //NOSONAR
			{
				dupAbstractPromotion = (AbstractPromotion) type.newInstance(ctx, values);
			}
			catch (final JaloGenericCreationException | JaloAbstractTypeException ex)
			{
				LOG.warn("createDeepClone failed to create instance of AbstractPromotion", ex);
			}

			return dupAbstractPromotion;
		}
		catch (final JaloSecurityException ex)
		{
			LOG.warn("createDeepClone failed to get attributes from [" + this + "]", ex);
		}
		return null;
	}

	protected static final AbstractPromotion findImmutablePromotionByUniqueKey(final JaloSession jaloSession,
			final SessionContext ctx, final String immutableKeyHash, final String immutableKey)
	{
		// Find promotions with specified immutable key hash
		final String query = "SELECT {" + Item.PK + "} " + "FROM   {"
				+ TypeManager.getInstance().getComposedType(AbstractPromotion.class).getCode() + "} " + "WHERE  {" // NOSONAR
				+ AbstractPromotion.IMMUTABLEKEYHASH + "} = ?immutableKeyHash";

		final HashMap args = new HashMap();
		args.put("immutableKeyHash", immutableKeyHash);

		final List<AbstractPromotion> matchingPromotions = jaloSession.getFlexibleSearch()
				.search(ctx, query, args, AbstractPromotion.class).getResult();

		if (LOG.isDebugEnabled())
		{
			LOG.debug("findImmutablePromotionByUniqueKey found [" + matchingPromotions.size()
					+ "] promotions with immutable key hash=[" + immutableKeyHash + "]");
		}

		if (!matchingPromotions.isEmpty())
		{
			// Look for first match with correct immutable key
			for (final AbstractPromotion promo : matchingPromotions)
			{
				if (immutableKey.equals(promo.getImmutableKey(ctx)))
				{
					// found match for immutable key
					return promo;
				}
			}
		}

		// Did not find matching immutable key
		return null;
	}

	/**
	 * Build a unique data driven key. Build a unique key that is data driven. This will uniquely identify the rules used
	 * in this promotion and will form the immutable key for stored promotions.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return A {@link StringBuilder} used to build up the immutable key
	 */
	protected final String getDataUniqueKey(final SessionContext ctx)
	{
		final StringBuilder builder = new StringBuilder();
		buildDataUniqueKey(ctx, builder);
		return builder.toString();
	}

	/**
	 * Build a unique data driven key. Build a unique key that is data driven. This will uniquely identify the rules used
	 * in this promotion and will form the immutable key for stored promotions. This method may be overridden in a
	 * subclass to customize the identifier.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param builder
	 *           A {@link StringBuilder} used to build up the immutable key
	 */
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		builder.append(this.getClass().getSimpleName()).append('|').append(getPromotionGroup(ctx).getIdentifier(ctx)).append('|')
				.append(getCode(ctx)).append('|').append(getPriority(ctx)).append('|').append(ctx.getLanguage().getIsocode())
				.append('|');

		final Date startDate = getStartDate(ctx);
		if (startDate == null)
		{
			builder.append("x|");
		}
		else
		{
			builder.append(startDate.getTime()).append('|');
		}

		final Date endDate = getEndDate(ctx);
		if (endDate == null)
		{
			builder.append("x|");
		}
		else
		{
			builder.append(endDate.getTime()).append('|');
		}

		final Collection<AbstractPromotionRestriction> restrictions = getRestrictions(ctx);
		if (restrictions != null && !restrictions.isEmpty())
		{
			for (final AbstractPromotionRestriction restriction : restrictions)
			{
				restriction.buildDataUniqueKey(ctx, builder);
			}
		}
		builder.append('|');
	}

	/**
	 * Called to deep clone attributes of this instance. The values map contains all the attributes defined on this
	 * instance. The map will be used to initialize a new instance of the Action that is a clone of this instance. This
	 * method can remove, replace or add to the Map of attributes.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param values
	 *           The map to write into
	 */
	protected void deepCloneAttributes(final SessionContext ctx, final Map values)
	{
		// Remove PromotionType as it is read-only and won't change
		values.remove(AbstractPromotion.PROMOTIONTYPE);

		// Keep all existing attributes apart from Restrictions
		values.remove(AbstractPromotion.RESTRICTIONS);

		// Clone restrictions
		final Collection<AbstractPromotionRestriction> dupRestrictions = new ArrayList<>();
		final Collection<AbstractPromotionRestriction> restrictions = getRestrictions(ctx);
		if (restrictions != null && !restrictions.isEmpty())
		{
			for (final AbstractPromotionRestriction restriction : restrictions)
			{
				dupRestrictions.add(restriction.deepClone(ctx));
			}
		}

		values.put(AbstractPromotion.RESTRICTIONS, dupRestrictions);
	}

	protected static final Collection<PromotionPriceRow> deepClonePriceRows(final SessionContext ctx,
			final Collection<PromotionPriceRow> priceRows)
	{
		final Collection<PromotionPriceRow> dupPriceRows = new ArrayList<>();

		if (priceRows != null && !priceRows.isEmpty())
		{
			for (final PromotionPriceRow row : priceRows)
			{
				dupPriceRows.add(PromotionsManager.getInstance().createPromotionPriceRow(ctx, row.getCurrency(ctx),
						row.getPrice(ctx).doubleValue()));
			}
		}

		return dupPriceRows;
	}

	protected static final void buildDataUniqueKeyForPriceRows(final SessionContext ctx, final StringBuilder builder,
			final Collection<PromotionPriceRow> priceRows)
	{
		if (priceRows != null && !priceRows.isEmpty())
		{
			// Sort the price rows into a stable and reproducible list
			final List<PromotionPriceRow> sortedPriceRows = new ArrayList<>(priceRows);
			Collections.sort(sortedPriceRows, Comparators.promotionPriceRowComparator);

			for (final PromotionPriceRow row : sortedPriceRows)
			{
				builder.append(row.getCurrency(ctx).getIsoCode(ctx)).append('=').append(row.getPrice(ctx)).append(','); // NOSONAR
			}
		}
		builder.append('|');
	}

	protected static final void buildDataUniqueKeyForProducts(final SessionContext ctx, final StringBuilder builder,
			final Collection<Product> products) // NOSONAR
	{
		final StringBuilder productBuilder = new StringBuilder();

		if (products != null && !products.isEmpty())
		{
			// Sort the products into a stable and reproducible list
			final List<Product> sortedProducts = new ArrayList<Product>(products); // NOSONAR
			Collections.sort(sortedProducts, Comparators.productComparator);

			for (final Product p : sortedProducts) // NOSONAR
			{
				productBuilder.append(p.getCode(ctx)).append(',');
			}
		}

		// use the hash of the product id's in the immutableKey otherwise we can run into length issues
		builder.append(buildMD5Hash(productBuilder.toString()));

		builder.append('|');
	}

	protected static final void buildDataUniqueKeyForCategories(final SessionContext ctx, final StringBuilder builder,
			final Collection<Category> categories)
	{
		final StringBuilder categoryBuilder = new StringBuilder();

		if (categories != null && !categories.isEmpty())
		{
			// Sort the categories into a stable and reproducible list
			final List<Category> sortedProducts = new ArrayList<>(categories);
			Collections.sort(sortedProducts, Comparators.categoryComparator);

			for (final Category category : sortedProducts)
			{
				categoryBuilder.append(category.getCode(ctx)).append(',');
			}
		}

		// use the hash of the category id's in the immutableKey otherwise we can run into length issues
		builder.append(buildMD5Hash(categoryBuilder.toString()));

		builder.append('|');
	}

	/**
	 * Get the collection of {@link AbstractPromotionRestriction} instances.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return A collection of {@link AbstractPromotionRestriction} instances attached to this promotion.
	 */
	@Override
	public final Collection getRestrictions(final SessionContext ctx)
	{
		final String query = "SELECT {" + Item.PK + "} " + "FROM   {"
				+ TypeManager.getInstance().getComposedType(AbstractPromotionRestriction.class).getCode() + "} " + "WHERE  {" // NOSONAR
				+ AbstractPromotionRestriction.PROMOTION + "} = ?promotion";

		final Flat3Map args = new Flat3Map();
		args.put("promotion", this);

		final Collection results = getSession().getFlexibleSearch().search(ctx, query, args, AbstractPromotionRestriction.class)
				.getResult();
		return Collections.unmodifiableCollection(results);
	}

	/**
	 * Set the collection of {@link AbstractPromotionRestriction} instance. The {@link AbstractPromotionRestriction}
	 * instances associated with this promotion are owned (and part of) this promotion. They cannot belong to another
	 * instance, therefore when setting the collection any {@link AbstractPromotionRestriction} instances previously
	 * associated with this promotion, that are no longer associated are deleted from the database.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param restrictions
	 *           the collection of restrictions
	 */
	@Override
	public final void setRestrictions(final SessionContext ctx, final Collection restrictions) //NOSONAR
	{
		// Copy the inbound collection into a modifiable list of the correct types
		final ArrayList<AbstractPromotionRestriction> newRestrictions = new ArrayList<>();
		if (restrictions != null && !restrictions.isEmpty())
		{
			for (final Object obj : restrictions)
			{
				if (obj instanceof AbstractPromotionRestriction)
				{
					newRestrictions.add((AbstractPromotionRestriction) obj);
				}
			}
		}

		// Loop through existing restrictions, remove any that are not in the newRestrictions list
		final Collection<AbstractPromotionRestriction> oldRestrictions = getRestrictions(ctx);
		if (oldRestrictions != null && !oldRestrictions.isEmpty())
		{
			for (final AbstractPromotionRestriction oldRestriction : oldRestrictions)
			{
				// This is a bit hard to read but basically if the oldRestriction is in the
				// newRestrictions list then we are keeping it, we don't need to add or remove it
				// remove here returns true if it was in the list
				final boolean keepItem = newRestrictions.remove(oldRestriction);
				if (!keepItem)
				{
					// we are not keeping this restriction, delete from DB
					try // NOSONAR
					{
						oldRestriction.remove(ctx);
					}
					catch (final ConsistencyCheckException ex)
					{
						LOG.error("setRestrictions failed to remove [" + oldRestriction + "] from database", ex);
					}
				}
			}
		}

		// Now loop through what is left in newRestrictions and add them
		if (!newRestrictions.isEmpty())
		{
			for (final AbstractPromotionRestriction newRestriction : newRestrictions)
			{
				newRestriction.setPromotion(ctx, this);
			}
		}
	}

	/**
	 * Helper method to delete promotion price rows from the database.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param prices
	 *           The prices to delete
	 * @throws ConsistencyCheckException
	 */
	public static void deletePromotionPriceRows(final SessionContext ctx, final Collection<PromotionPriceRow> prices)
			throws ConsistencyCheckException
	{
		if (prices != null && !prices.isEmpty())
		{
			for (final PromotionPriceRow row : prices)
			{
				row.remove(ctx);
			}
		}
	}

	/**
	 * Generate a string identifier that can be used to establish if 2 PromotionResults are the same. The identifier
	 * should be based on the data for the PromotionResult, e.g. the promotion that created it, the number and type of
	 * products consumed, the actions created.
	 * <p/>
	 * This method is final, but calls the {@link #buildPromotionResultDataUnigueKey} method to allow subclasses to
	 * affect the way the identifier is built.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param promotionResult
	 *           The promotion result
	 * @return a string that identifies this promotion result
	 */
	protected final String getPromotionResultDataUnigueKey(final SessionContext ctx, final PromotionResult promotionResult)
	{
		final StringBuilder builder = new StringBuilder(255);

		builder.append(getClass().getSimpleName()).append('|');
		builder.append(getCode(ctx)).append('|');

		buildPromotionResultDataUnigueKey(ctx, promotionResult, builder);

		return builder.toString();
	}

	/**
	 * Build a unique identifier for a PromotionResult. This method is called from
	 * {@link #getPromotionResultDataUnigueKey}. This method may be overridden by a subclass to customize how the
	 * identifier is built.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param promotionResult
	 *           The promotion result
	 * @param builder
	 *           A StringBuilder used to build up the identifier
	 */
	protected void buildPromotionResultDataUnigueKey(final SessionContext ctx, final PromotionResult promotionResult,
			final StringBuilder builder)
	{
		builder.append(promotionResult.getCertainty(ctx)).append('|');
		builder.append(promotionResult.getCustom(ctx)).append('|');

		final Collection<PromotionOrderEntryConsumed> entries = promotionResult.getConsumedEntries(ctx);
		if (entries != null && !entries.isEmpty())
		{
			for (final PromotionOrderEntryConsumed entry : entries)
			{
				builder.append(entry.getOrderEntry(ctx).getProduct(ctx).getCode(ctx)).append(',');
				builder.append(entry.getQuantity(ctx)).append('|');
			}
		}

		final Collection<AbstractPromotionAction> actions = promotionResult.getActions(ctx);
		if (actions != null && !actions.isEmpty())
		{
			for (final AbstractPromotionAction action : actions)
			{
				builder.append(action.getClass().getSimpleName()).append('|');
			}
		}
	}

}
