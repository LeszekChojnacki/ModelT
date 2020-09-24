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
import de.hybris.platform.category.jalo.CategoryManager;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.SearchResult;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.link.Link;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.order.price.Discount;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.AttributeDescriptor;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.TypeManager;
import de.hybris.platform.promotions.constants.PromotionsConstants;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.promotions.result.PromotionException;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.promotions.util.Helper;
import de.hybris.platform.tx.Transaction;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.JspContext;
import de.hybris.platform.util.jeeapi.YNoSuchEntityException;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.util.migration.DeploymentMigrationUtil;
import de.hybris.platform.voucher.jalo.Voucher;
import de.hybris.platform.voucher.jalo.util.VoucherValue;

import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;


/**
 * The manager for the Promotions extension.
 * Manages applying promotions to carts and orders.
 * <ul>
 * <li>Use the {@link #updatePromotions} methods to evaluate the promotions that can be applied to a cart or order.</li>
 * <li>Use the {@link #getPromotionResults} methods to retrieve the promotions calculated for an order.</li>
 * <li>Use the {@link #getProductPromotions} methods to retrieve the promotions that a {@link Product} can be part of.
 * </li>
 * <li>The promotions extension stores additional database items for the cart and order items. When a cart is removed
 * from the system it is necessary to call the {@link #cleanupCart} method to removed these items.</li>
 * </ul>
 *
 * @see #getProductPromotions
 * @see #updatePromotions
 * @see #getPromotionResults
 */
@SuppressWarnings({ "squid:ClassCyclomaticComplexity", "squid:S00104" })
public class PromotionsManager extends GeneratedPromotionsManager                    //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(PromotionsManager.class.getName());

	private boolean migrationMode = false;

	private static final String DEPLOYMENT_CHECK_PROPERTY = "deployment.check";

	@Override
	public void notifyInitializationStart(final Map<String, String> params, final JspContext ctx)
	{
		if ("update".equals(params.get("initmethod")))
		{
			final boolean deploymentCheck = Config.getBoolean(DEPLOYMENT_CHECK_PROPERTY, true);
			ComposedType type = null;
			try
			{
				type = TypeManager.getInstance().getComposedType("PromotionOrderEntryConsumed"); // NOSONAR
			}
			catch (final JaloSystemException e) //NOSONAR
			{
				// OK, then not
				type = null;
			}

			if (deploymentCheck && type != null
					&& "de.hybris.jakarta.entity.PromotionOrderEntryConsumed".equalsIgnoreCase(type.getJNDIName()))
			{
				LOG.info("Activating migration mode.");
				migrationMode = true;
				Config.setParameter(DEPLOYMENT_CHECK_PROPERTY, Boolean.FALSE.toString());
				System.setProperty(DEPLOYMENT_CHECK_PROPERTY, Boolean.FALSE.toString());

				// rename automatically
				DeploymentMigrationUtil.migrateDeployments("promotions");

				// if not possible because of missing ydeployment entries try manually
				DeploymentMigrationUtil.migrateDeploymentManually(5014, "PromotionOrderEntryConsumed");
				DeploymentMigrationUtil.migrateDeploymentManually(5016, "PromotionQuantityAndPricesRow");

				LOG.info("The following update will be performed with deployment.check set to false");
			}
		}
	}

	@Override
	public void notifyInitializationEnd(final Map<String, String> params, final JspContext ctx)
	{
		if (migrationMode)
		{
			migrationMode = false;
			LOG.info("Deactivating migration mode.");
			Config.setParameter(DEPLOYMENT_CHECK_PROPERTY, Boolean.TRUE.toString());
			System.setProperty(DEPLOYMENT_CHECK_PROPERTY, Boolean.FALSE.toString());
		}
	}

	/**
	 * Get the instance of this manager.
	 *
	 * @return instance of this manager
	 */
	public static PromotionsManager getInstance()
	{
		final JaloSession js = JaloSession.getCurrentSession();
		return (PromotionsManager) js.getExtensionManager().getExtension(PromotionsConstants.EXTENSIONNAME);
	}

	/**
	 * Implement this method to create initial objects. This method will be called by system creator.
	 *
	 * @param params
	 * 		the parameters provided by user for creation of objects for the extension
	 * @param jspc
	 * 		the jsp context; you can use it to write progress information to the jsp page during creation
	 * @throws Exception
	 * 		if something goes wrong
	 */
	@Override
	public void createEssentialData(final Map params, final JspContext jspc) throws Exception //NOPMD
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Setting localized default values");
		}

		final SessionContext languageNeutralSessionContext = getLanguageNeutralSessionContext();
		initialiseDefaultLocalisedValues(languageNeutralSessionContext, getComposedType(AbstractPromotion.class).getAllSubTypes());
		initialiseDefaultLocalisedValues(languageNeutralSessionContext,
				getComposedType(AbstractPromotionRestriction.class).getAllSubTypes());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating default promotions group");
		}

		final PromotionGroup defaultPromotionGroup = getDefaultPromotionGroup(getSession().getSessionContext());
		if (defaultPromotionGroup == null)
		{
			createPromotionGroup(getSession().getSessionContext(), PromotionsConstants.DEFAULT_PROMOTION_GROUP_IDENTIFIER);
		}
	}

	/**
	 * Create a language neutral session context
	 */
	protected SessionContext getLanguageNeutralSessionContext()
	{
		final SessionContext ctx = getSession().createSessionContext();
		ctx.setLanguage(null);
		return ctx;
	}

	/**
	 * Set the default values on the set of types specified.
	 *
	 * @param languageNeutralSessionContext
	 * 		- instance of {@link SessionContext}
	 * @param types
	 * 		- set of composed type instances
	 */
	protected void initialiseDefaultLocalisedValues(final SessionContext languageNeutralSessionContext,
			final Set<ComposedType> types)
	{
		for (final ComposedType subType : types)
		{
			initialiseDefaultLocalisedValues(languageNeutralSessionContext, subType);
		}
	}

	/**
	 * Set default values for localized attributes on the type specified.
	 * Looks up the localized value from the items.xml localized resources.
	 *
	 * @param languageNeutralSessionContext
	 * 		- instance of {@link SessionContext}
	 * @param type
	 * 		The type to localize attributes on
	 */
	protected void initialiseDefaultLocalisedValues(final SessionContext languageNeutralSessionContext, final ComposedType type)
	{
		for (final AttributeDescriptor attributeDescriptor : type.getAttributeDescriptorsIncludingPrivate())
		{
			if (attributeDescriptor.isLocalized() && attributeDescriptor.getAttributeType().isInstance(""))
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Setting default value on " + type.getCode() + "." + attributeDescriptor.getQualifier());
				}

				// Found localizable string attribute
				final String resourceKey = "type." + type.getCode().toLowerCase() + "."
						+ attributeDescriptor.getQualifier().toLowerCase() + ".defaultvalue";
				attributeDescriptor.setDefaultValue(languageNeutralSessionContext, Localization.getLocalizedMap(resourceKey));
			}
		}
	}

	/**
	 * Lookup a composed type for a class
	 */
	protected ComposedType getComposedType(final Class aClass)
	{
		try
		{
			final ComposedType type = getSession().getTypeManager().getComposedType(aClass); // NOSONAR
			if (type == null)
			{
				throw new JaloSystemException("Got type null for " + aClass, 0);
			}
			return type;
		}
		catch (final JaloItemNotFoundException e)
		{
			throw new JaloSystemException(e, "Required type missing", 0);
		}
	}


	// ----------------------------------------------------------------------------
	// Get Product Promotions Methods
	// ----------------------------------------------------------------------------

	/**
	 * Get the ordered list of {@link ProductPromotion} instances that are related to the {@link Product} specified.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param product
	 * 		The product that the promotions are related to
	 * @return The list of {@link ProductPromotion} related to the {@link Product} specified
	 */
	public final List<ProductPromotion> getProductPromotions(final Collection<PromotionGroup> promotionGroups,
			final Product product) // NOSONAR
	{
		return getProductPromotions(this.getSession().getSessionContext(), promotionGroups, product, true,
				Helper.getDateNowRoundedToMinute());
	}

	/**
	 * Get the ordered list of {@link ProductPromotion} instances that are related to the {@link Product} specified.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param product
	 * 		The product that the promotions are related to
	 * @param evaluateRestrictions
	 * 		Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 * 		restrictions.
	 * @param date
	 * 		The date to check for promotions, typically the current date
	 * @return The list of {@link ProductPromotion} related to the {@link Product} specified
	 */
	public final List<ProductPromotion> getProductPromotions(final Collection<PromotionGroup> promotionGroups,
			final Product product, final boolean evaluateRestrictions, final Date date) // NOSONAR
	{
		return getProductPromotions(this.getSession().getSessionContext(), promotionGroups, product, evaluateRestrictions, date);
	}

	/**
	 * Get the ordered list of {@link ProductPromotion} instances that are related to the {@link Product} specified.
	 *
	 * @param ctx
	 * 		The session context
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param product
	 * 		The product that the promotions are related to
	 * @param evaluateRestrictions
	 * 		Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 * 		restrictions.
	 * @param date
	 * 		The date to check for promotions, typically the current date
	 * @return The list of {@link ProductPromotion} related to the {@link Product} specified
	 */
	public List<ProductPromotion> getProductPromotions(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final Product product, final boolean evaluateRestrictions, Date date) // NOSONAR
	{
		try
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("getProductPromotions for [" + product + "] promotionGroups=[" + Helper.join(promotionGroups)
						+ "] evaluateRestrictions=[" + evaluateRestrictions + "] date=[" + date + "]"); //NOSONAR
			}

			if (promotionGroups != null && product != null && !promotionGroups.isEmpty())
			{
				if (date == null)
				{
					// Default date to now
					date = Helper.getDateNowRoundedToMinute(); //NOSONAR
				}

				final Map<String, Object> args = new HashMap<>();
				args.put("promotionGroups", promotionGroups); //NOSONAR
				args.put("product", product);
				args.put("now", date);
				args.put("true", Boolean.TRUE);

				final String query = buildQueryForDistinctProductPromotionQuery(product, ctx, args);

				final List<ProductPromotion> allPromotions = getSession().getFlexibleSearch()
						.search(ctx, query, args, Collections.singletonList(ProductPromotion.class), true, false, 0, -1).getResult();

				List<ProductPromotion> availablePromotions;
				if (evaluateRestrictions)
				{
					availablePromotions = filterPromotionsByRestrictions(ctx, allPromotions, product, date);
				}
				else
				{
					availablePromotions = new ArrayList<>(allPromotions);
				}

				// Dump out list of available promotions
				if (LOG.isDebugEnabled())
				{
					for (final ProductPromotion promotion : availablePromotions) //NOSONAR
					{
						LOG.debug("getProductPromotions for [" + product + "] available promotion [" + promotion + "]");
					}
				}

				return availablePromotions;
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to getProductPromotions", ex);
		}
		return Collections.emptyList();
	}

	protected String buildQueryForDistinctProductPromotionQuery(final Product product, final SessionContext ctx, // NOSONAR
			final Map<String, Object> args)
	{
		// Build query to find all distinct list of all promotions that are related to
		// the source product either via the ProductPromotionRelation or via the CategoryPromotionRelation.
		// Filter the results so that only Promotions with Start and End dates valid for time 'now'
		// order by Priority with the highest value first
		final StringBuilder promQuery = new StringBuilder("SELECT DISTINCT pprom.pk, pprom.prio FROM (");
		promQuery.append(" {{ SELECT {p." + ProductPromotion.PK + "} as pk, "); //NOSONAR
		promQuery.append(" {p." + ProductPromotion.PRIORITY + "} as prio FROM"); // NOSONAR
		promQuery.append(" {" + TypeManager.getInstance().getComposedType(ProductPromotion.class).getCode() + " AS p"); //NOSONAR
		promQuery.append(" JOIN " + PromotionsConstants.Relations.PRODUCTPROMOTIONRELATION + " AS p2p "); //NOSONAR
		promQuery.append(" ON {p." + ProductPromotion.PK + "} = {p2p." + Link.TARGET + "} "); //NOSONAR
		promQuery.append(" AND {p2p." + Link.SOURCE + "} = ?product } ");
		promQuery.append(" WHERE {p." + AbstractPromotion.PROMOTIONGROUP + "} IN (?promotionGroups) AND"); //NOSONAR
		promQuery.append(" {p." + AbstractPromotion.ENABLED + "} =?true AND "); //NOSONAR
		promQuery.append(" {p." + AbstractPromotion.STARTDATE + "} <= ?now AND "); //NOSONAR
		promQuery.append(" ?now <= {p." + AbstractPromotion.ENDDATE + "} }}"); //NOSONAR

		final Collection<Category> productCategories = CategoryManager.getInstance().getCategoriesByProduct(product, ctx);
		if (!productCategories.isEmpty())
		{
			promQuery.append(" UNION ");

			promQuery.append(" {{ SELECT {p." + ProductPromotion.PK + "} as pk, ");
			promQuery.append(" {p." + ProductPromotion.PRIORITY + "} as prio FROM");
			promQuery.append(" {" + TypeManager.getInstance().getComposedType(ProductPromotion.class).getCode() + " AS p"); //NOSONAR
			promQuery.append(" JOIN " + PromotionsConstants.Relations.CATEGORYPROMOTIONRELATION + " AS c2p ");
			promQuery.append(" ON {p." + ProductPromotion.PK + "} = {c2p." + Link.TARGET + "} ");
			promQuery.append(" AND {c2p." + Link.SOURCE + "} IN (?productCategories) } ");
			promQuery.append(" WHERE {p." + AbstractPromotion.PROMOTIONGROUP + "} IN (?promotionGroups) AND");
			promQuery.append(" {p." + AbstractPromotion.ENABLED + "} =?true AND ");
			promQuery.append(" {p." + AbstractPromotion.STARTDATE + "} <= ?now AND ");
			promQuery.append(" ?now <= {p." + AbstractPromotion.ENDDATE + "} }}");

			// finds all the categories that the product is in, and all of their super categories
			final Set<Category> productSuperCategories = new HashSet<>();
			for (final Category cat : productCategories)
			{
				productSuperCategories.add(cat);
				productSuperCategories.addAll(cat.getAllSupercategories(ctx)); //NOSONAR
			}
			args.put("productCategories", productSuperCategories);
		}

		promQuery.append(" )pprom ORDER BY pprom.prio DESC");

		return promQuery.toString();
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups)
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, true, null,
				Helper.getDateNowRoundedToMinute());
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param date
	 * 		The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups, final Date date)
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, true, null, date);
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param product
	 * 		The product to pass to restrictions
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups,
			final Product product) // NOSONAR
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, true, product,
				Helper.getDateNowRoundedToMinute());
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param product
	 * 		The product to pass to restrictions
	 * @param date
	 * 		The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups, final Product product, // NOSONAR
			final Date date)
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, true, product, date);
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param evaluateRestrictions
	 * 		Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 * 		restrictions.
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups,
			final boolean evaluateRestrictions)
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, evaluateRestrictions, null,
				Helper.getDateNowRoundedToMinute());
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param evaluateRestrictions
	 * 		Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 * 		restrictions.
	 * @param date
	 * 		The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups,
			final boolean evaluateRestrictions, final Date date)
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, evaluateRestrictions, null, date);
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param evaluateRestrictions
	 * 		Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 * 		restrictions.
	 * @param product
	 * 		The product to pass to restrictions if evaluateRestrictions is true
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups,
			final boolean evaluateRestrictions, final Product product) //NOSONAR
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, evaluateRestrictions, product,
				Helper.getDateNowRoundedToMinute());
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param evaluateRestrictions
	 * 		Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 * 		restrictions.
	 * @param product
	 * 		The product to pass to restrictions if evaluateRestrictions is true
	 * @param date
	 * 		The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final Collection<PromotionGroup> promotionGroups,
			final boolean evaluateRestrictions, final Product product, final Date date) //NOSONAR
	{
		return getOrderPromotions(this.getSession().getSessionContext(), promotionGroups, evaluateRestrictions, product, date);
	}

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param ctx
	 * 		The session context
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param evaluateRestrictions
	 * 		Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 * 		restrictions.
	 * @param product
	 * 		The product to pass to restrictions if evaluateRestrictions is true
	 * @param date
	 * 		The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	public List<OrderPromotion> getOrderPromotions(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final boolean evaluateRestrictions, final Product product, Date date) //NOSONAR
	{
		try
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("getOrderPromotions promotionGroups=[" + Helper.join(promotionGroups) + "] evaluateRestrictions=["
						+ evaluateRestrictions + "] product=[" + product + "] date=[" + date + "]");
			}

			if (promotionGroups != null && !promotionGroups.isEmpty())
			{
				if (date == null)
				{
					// Default date to now
					date = Helper.getDateNowRoundedToMinute(); //NOSONAR
				}

				// Build query to find all order promotions filter the results so that only
				// Promotions with Start and End dates valid for time 'now'
				// order by Priority with the highest value first
				final String query = "SELECT DISTINCT {promo:" + Item.PK + "}, {promo:" + AbstractPromotion.PRIORITY + "} " + "FROM {"
						+ TypeManager.getInstance().getComposedType(OrderPromotion.class).getCode() + " as promo } " + "WHERE " + "( "//NOSONAR
						+ "  {promo:" + AbstractPromotion.PROMOTIONGROUP + "} IN (?promotionGroups) " + ") " + "AND " + "( "
						+ "  {promo:" + AbstractPromotion.ENABLED + "}=1 AND {promo:" + AbstractPromotion.STARTDATE
						+ "} <= ?now AND ?now <= {promo:" + AbstractPromotion.ENDDATE + "} " + ") " + "ORDER BY {promo:"
						+ AbstractPromotion.PRIORITY + "} DESC";

				final HashMap args = new HashMap();
				args.put("promotionGroups", promotionGroups);
				args.put("now", date);

				final List<OrderPromotion> allPromotions = getSession().getFlexibleSearch()
						.search(ctx, query, args, Collections.singletonList(OrderPromotion.class), true, false, 0, -1).getResult();

				List<OrderPromotion> availablePromotions = null;
				if (evaluateRestrictions)
				{
					availablePromotions = filterPromotionsByRestrictions(ctx, allPromotions, product, date);
				}
				else
				{
					availablePromotions = new ArrayList<>(allPromotions);
				}

				// Dump out list of available promotions
				if (LOG.isDebugEnabled())
				{
					for (final OrderPromotion promotion : availablePromotions) //NOSONAR
					{
						LOG.debug("getOrderPromotions available promotion [" + promotion + "]");
					}
				}

				return availablePromotions;
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to getOrderPromotions", ex);
		}
		return new ArrayList<>(0);
	}

	/**
	 * Filter a list of promotions by their restrictions
	 *
	 * @param allPromotions
	 * 		The promotions list to filter
	 * @param product
	 * 		Optional product to pass when evaluating restrictions
	 * @param date
	 * 		The restriction date
	 * @return The filtered list of promotions
	 */
	public <T extends AbstractPromotion> List<T> filterPromotionsByRestrictions(final SessionContext ctx,
			final List<T> allPromotions, final Product product, final Date date) //NOSONAR
	{
		final ArrayList<T> availablePromotions = new ArrayList<>(allPromotions.size());

		for (final T promotion : allPromotions)
		{
			boolean satifiedRestrictions = true;

			final Collection<AbstractPromotionRestriction> restrictions = promotion.getRestrictions();
			if (restrictions != null)
			{
				for (final AbstractPromotionRestriction restriction : restrictions)
				{
					// Check restriction
					final AbstractPromotionRestriction.RestrictionResult result = restriction.evaluate(ctx, product, date, null);
					if (result == AbstractPromotionRestriction.RestrictionResult.DENY //NOSONAR
							|| result == AbstractPromotionRestriction.RestrictionResult.ADJUSTED_PRODUCTS)
					{
						satifiedRestrictions = false;
						break;
					}
				}
			}

			if (satifiedRestrictions)
			{
				availablePromotions.add(promotion);
			}
		}

		return availablePromotions;
	}

	// ----------------------------------------------------------------------------
	// Update Promotions Methods
	// ----------------------------------------------------------------------------

	/**
	 * Update the promotions on the specified {@link AbstractOrder} object.
	 * <p/>
	 * This method will automatically apply all possible product promotional updates to the cart, but not apply order
	 * level promotions. Any previously applied order level promotions will remain applied. Promotions are evaluated at
	 * the current system time.
	 * <p/>
	 * The promotion results are stored in the database and the same {@link PromotionOrderResults} can be obtained later
	 * by calling {@link #getPromotionResults}.
	 *
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param order
	 * 		The order object to update with the results of the promotions
	 * @return The promotion results
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder)
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 * @see #getPromotionResults(de.hybris.platform.jalo.order.AbstractOrder)
	 * @see #getPromotionResults(de.hybris.platform.jalo.SessionContext, de.hybris.platform.jalo.order.AbstractOrder)
	 * @see #getPromotionResults(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 */
	public final PromotionOrderResults updatePromotions(final Collection<PromotionGroup> promotionGroups,
			final AbstractOrder order)
	{
		return updatePromotions(this.getSession().getSessionContext(), promotionGroups, order);
	}

	/**
	 * Update the promotions on the specified {@link AbstractOrder} object.
	 * <p/>
	 * This method will automatically apply all possible product promotional updates to the cart, but not apply order
	 * level promotions. Any previously applied order level promotions will remain applied. Promotions are evaluated at
	 * the current system time.
	 * <p/>
	 * The promotion results are stored in the database and the same {@link PromotionOrderResults} can be obtained later
	 * by calling {@link #getPromotionResults}.
	 *
	 * @param ctx
	 * 		The hybris session context
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param order
	 * 		The order object to update with the results of the promotions
	 * @return The promotion results
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 * @see #getPromotionResults(de.hybris.platform.jalo.order.AbstractOrder)
	 * @see #getPromotionResults(de.hybris.platform.jalo.SessionContext, de.hybris.platform.jalo.order.AbstractOrder)
	 * @see #getPromotionResults(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 */
	public PromotionOrderResults updatePromotions(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final AbstractOrder order)
	{
		return updatePromotions(ctx, promotionGroups, order, true, AutoApplyMode.APPLY_ALL, AutoApplyMode.KEEP_APPLIED,
				Helper.getDateNowRoundedToMinute());
	}

	/**
	 * Update the promotions on the specified {@link AbstractOrder} object.
	 * <p/>
	 * The resulting promotions can be retrieved later by calling {@link #getPromotionResults}. The order must be
	 * calculated before calling this method. {@link #updatePromotions} must be called after calling
	 * {@link AbstractOrder#recalculate()} on the {@link AbstractOrder}. Where the {@link AutoApplyMode} is set to
	 * {@link AutoApplyMode#KEEP_APPLIED} the state of any previously applied {@link PromotionResult} is recorded and if
	 * it is still in the fired state ({@link PromotionResult#isApplied()}) after reevaluating the promotions it will be
	 * automatically reapplied.
	 * <p/>
	 * The promotion results are stored in the database and the same {@link PromotionOrderResults} can be obtained later
	 * by calling {@link #getPromotionResults}.
	 *
	 * @param ctx
	 * 		The hybris session context
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param order
	 * 		The AbstractOrder object to update the promotions for
	 * @param evaluateRestrictions
	 * 		If <i>true</i> any promotion restrictions will be observed, if <i>false</i> all promotion restrictions
	 * 		are ignored
	 * @param productPromotionMode
	 * 		The auto apply mode. This determines whether this method applies any product promotional changes to line
	 * 		items or discounts to the overall amount
	 * @param orderPromotionMode
	 * 		The auto apply mode. This determines whether this method applies any order promotional changes to line
	 * 		items or discounts to the overall amount
	 * @param date
	 * 		The effective date for the promotions to check. Use this to to see the effects of promotions in the past
	 * 		or future.
	 * @return The promotion results
	 * @see #getPromotionResults(de.hybris.platform.jalo.order.AbstractOrder)
	 * @see #getPromotionResults(de.hybris.platform.jalo.SessionContext, de.hybris.platform.jalo.order.AbstractOrder)
	 * @see #getPromotionResults(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 */
	public PromotionOrderResults updatePromotions(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final AbstractOrder order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{
		synchronized (order.getSyncObject())
		{
			return updatePromotionsNotThreadSafe(ctx, promotionGroups, order, evaluateRestrictions, productPromotionMode,
					orderPromotionMode, date);
		}
	}


	protected PromotionOrderResults updatePromotionsNotThreadSafe(final SessionContext ctx, //NOSONAR
			final Collection<PromotionGroup> promotionGroups, final AbstractOrder order, final boolean evaluateRestrictions,
			final AutoApplyMode productPromotionMode, final AutoApplyMode orderPromotionMode, Date date)
	{
		try
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("updatePromotions for [" + order + "] promotionGroups=[" + Helper.join(promotionGroups)
						+ "] evaluateRestrictions=[" + evaluateRestrictions + "] productPromotionMode=[" + productPromotionMode
						+ "] orderPromotionMode=[" + orderPromotionMode + "] date=[" + date + "]");
			}

			if (promotionGroups != null && order != null)
			{
				if (date == null)
				{
					// Default value is Now
					date = Helper.getDateNowRoundedToMinute(); //NOSONAR
				}

				if (!order.isCalculated(ctx).booleanValue())
				{
					if (LOG.isDebugEnabled()) //NOSONAR
					{
						LOG.debug("updatePromotions order [" + order + "] not calculated, calculating");
					}
					order.calculate(date); //NOSONAR
				}

				// Record list of promotions to keep applied if AutoApplyMode.KEEP_APPLIED is specifed
				final List<String> promotionResultsToKeepApplied = new ArrayList<>();
				double oldTotalAppliedDiscount = 0;

				// Find the current total value of promotions active
				final List<PromotionResult> currResults = this.getPromotionResultsInternal(ctx, order);
				if (currResults != null && !currResults.isEmpty())
				{
					for (final PromotionResult pr : currResults) //NOSONAR
					{
						if (pr.getFired(ctx))
						{
							final boolean prApplied = pr.isApplied(ctx);
							if (prApplied)
							{
								// We want to capture the total applied discount even for promotion results that are now invalid.
								oldTotalAppliedDiscount += pr.getTotalDiscount(ctx);
							}

							if (pr.isValid(ctx) && ((productPromotionMode == AutoApplyMode.KEEP_APPLIED //NOSONAR
									&& pr.getPromotion(ctx) instanceof ProductPromotion)
									|| (orderPromotionMode == AutoApplyMode.KEEP_APPLIED
									&& pr.getPromotion(ctx) instanceof OrderPromotion))
									&& prApplied)
							{
								final String prKey = pr.getDataUnigueKey(ctx);
								if (prKey != null && prKey.length() > 0)
								{
									if (LOG.isDebugEnabled())
									{
										LOG.debug("updatePromotions found applied PromotionResult [" + pr + "] key [" + prKey
												+ "] that should be reapplied");
									}
									promotionResultsToKeepApplied.add(prKey);
								}
							}
						}
					}
				}


				// Delete any results stored from a previous run
				deleteStoredPromotionResults(ctx, order, true);

				// Find all runnable promotions in the system

				// Get the list of base products in the cart
				final Collection<Product> products = getBaseProductsForOrder(ctx, order); //NOSONAR
				final List<PromotionResult> results = new LinkedList<>();
				double newTotalAppliedDiscount = 0.0D;

				// Find the promotions that can be evaluated
				// will find all OrderPromotions and any ProductPromotions that are related to the products specified
				final List<AbstractPromotion> activePromotions = findOrderAndProductPromotionsSortByPriority(ctx, getSession(),
						promotionGroups, products, date);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("updatePromotions found [" + activePromotions.size() + "] promotions to run");
				}


				newTotalAppliedDiscount = updateForActivePromotions(ctx, order, evaluateRestrictions, productPromotionMode,
						orderPromotionMode, date, promotionResultsToKeepApplied, results, newTotalAppliedDiscount, activePromotions);

				// Log all the PromotionResults that could not be reapplied.
				if (LOG.isDebugEnabled())
				{
					for (final String prKey : promotionResultsToKeepApplied) //NOSONAR
					{
						LOG.debug("updatePomrotions PromotionResult not reapplied because it did not fire [" + prKey + "]");
					}
				}


				final double appliedDiscountChange = newTotalAppliedDiscount - oldTotalAppliedDiscount;

				if (LOG.isDebugEnabled())
				{
					LOG.debug("updatePromotions for [" + order + "] returned [" + results.size()
							+ "] PromotionResults appliedDiscountChange=[" + appliedDiscountChange + "]");
				}

				return new PromotionOrderResults(ctx, order, Collections.unmodifiableList(results), appliedDiscountChange);
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to updatePromotions", ex);
		}
		return null;
	}

	/**
	 *
	 */
	private double updateForActivePromotions(final SessionContext ctx, final AbstractOrder order, //NOSONAR
			final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode, final AutoApplyMode orderPromotionMode,
			final Date date, final List<String> promotionResultsToKeepApplied, final List<PromotionResult> results,
			double newTotalAppliedDiscount, final List<AbstractPromotion> activePromotions) throws JaloPriceFactoryException
	{
		if (!activePromotions.isEmpty())
		{
			// Remove existing vouchers to prevent them from interfering with the order threshold calculations
			final List<Voucher> vouchers = fixupVouchersRemoveVouchers(ctx, order);

			final PromotionEvaluationContext promoContext = new PromotionEvaluationContext(order, evaluateRestrictions, date);

			for (final AbstractPromotion promotion : activePromotions)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("updatePromotions evaluating promotion [" + promotion + "]");
				}
				final List<PromotionResult> promoResults = evaluatePromotion(ctx, promoContext, promotion);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("updatePromotions promotion [" + promotion + "] returned [" + promoResults.size() + "] results");
				}

				// Work out if we need to apply this promotion

				final boolean autoApply = autoApplyApplies(productPromotionMode, orderPromotionMode, promotion);
				final boolean keepApplied = keepApplied(productPromotionMode, orderPromotionMode, promotion, autoApply);

				boolean needsCalculateTotals = false;

				if (autoApply || keepApplied)
				{
					// Apply the promotion results if required
					for (final PromotionResult pr : promoResults) //NOSONAR
					{
						if (pr.getFired(ctx))
						{
							if (autoApply)
							{
								if (LOG.isDebugEnabled())
								{
									LOG.debug("updatePromotions auto applying result [" + pr + "] from promotion [" + promotion    //NOSONAR
											+ "]");
								}
								needsCalculateTotals |= pr.apply(ctx);

								// Add this promotion to the new total
								newTotalAppliedDiscount += pr.getTotalDiscount(ctx); //NOSONAR
							}
							else if (keepApplied)
							{
								final String prKey = pr.getDataUnigueKey(ctx);
								if (prKey == null || prKey.length() == 0)
								{
									LOG.error("updatePromotions promotion result [" + pr + "] from promotion [" + promotion
											+ "] returned NULL or Empty DataUnigueKey");
								}
								else
								{
									// See if the promotion result is in the list of promotions to keep applied
									if (promotionResultsToKeepApplied.remove(prKey))
									{

										if (LOG.isDebugEnabled())
										{
											LOG.debug("updatePromotions keeping applied the result [" + pr + "] from promotion [" + promotion
													+ "]");
										}
										needsCalculateTotals |= pr.apply(ctx);

										// Add this promotion to the new total
										newTotalAppliedDiscount += pr.getTotalDiscount(ctx); //NOSONAR
									}
								}
							}
						}
					}
				}

				if (needsCalculateTotals)
				{
					order.calculateTotals(true); //NOSONAR
				}

				results.addAll(promoResults);
			}

			// Fixup vouchers, if required
			fixupVouchersReapplyVouchers(ctx, order, vouchers);
		}
		return newTotalAppliedDiscount;
	}

	/**
	 *
	 */
	protected boolean keepApplied(final AutoApplyMode productPromotionMode, final AutoApplyMode orderPromotionMode,
			final AbstractPromotion promotion, final boolean autoApply)
	{
		boolean keepApplied = false;
		if (!autoApply && (productPromotionMode == AutoApplyMode.KEEP_APPLIED && orderPromotionMode == AutoApplyMode.KEEP_APPLIED) //NOSONAR
				|| (productPromotionMode == AutoApplyMode.KEEP_APPLIED && promotion instanceof ProductPromotion)
				|| (orderPromotionMode == AutoApplyMode.KEEP_APPLIED && promotion instanceof OrderPromotion))
		{
			keepApplied = true;
		}
		return keepApplied;
	}

	/**
	 *
	 */
	protected boolean autoApplyApplies(final AutoApplyMode productPromotionMode, final AutoApplyMode orderPromotionMode,
			final AbstractPromotion promotion)
	{
		boolean autoApply = false;
		if ((productPromotionMode == AutoApplyMode.APPLY_ALL && orderPromotionMode == AutoApplyMode.APPLY_ALL) //NOSONAR
				|| (productPromotionMode == AutoApplyMode.APPLY_ALL && promotion instanceof ProductPromotion)
				|| (orderPromotionMode == AutoApplyMode.APPLY_ALL && promotion instanceof OrderPromotion))
		{
			autoApply = true;
		}
		return autoApply;
	}

	//see https://jira.hybris.com/browse/PRO-89
	protected List<PromotionResult> evaluatePromotion(final SessionContext ctx, final PromotionEvaluationContext promoContext,
			final AbstractPromotion promotion)
	{
		final List<PromotionResult> results = promotion.evaluate(ctx, promoContext);
		if (Transaction.current().isRunning())
		{
			Transaction.current().flushDelayedStore();
		}
		return results;
	}

	/**
	 * Method to fixup the vouchers applied to the order.
	 * <p/>
	 * This method is called before promotions have been calculates. It identifies any voucher discounts that are applied
	 * to the order, and removes them.
	 *
	 * @param ctx
	 * 		the hybris session context
	 * @param order
	 * 		the order to fixup
	 */
	protected static List<Voucher> fixupVouchersRemoveVouchers(final SessionContext ctx, final AbstractOrder order) //NOSONAR
	{
		// This method is essentially a hack to try to work around an interaction problem
		// between the promotions extension and the voucher extension. Basically the voucher
		// extension ignores global discounts when calculating its relative discounts.
		// We try to fix this by recalculating the voucher's global discount values
		// based on the total rather than the subtotal.

		try
		{
			// Check to see if the config parameter is set to enable fixups
			if (Boolean.parseBoolean(Config.getParameter("promotions.voucher.fixupVouchers")))
			{
				// Get the list of discounts and check to see if any of them are Vouchers
				final Collection<Discount> discounts = order.getDiscounts();
				if (discounts != null && !discounts.isEmpty())
				{
					final List<Voucher> appliedVouchers = new ArrayList<>();

					for (final Discount discount : discounts) //NOSONAR
					{
						if (discount instanceof Voucher)
						{
							final Voucher voucher = (Voucher) discount;

							// Ask the voucher to try to run, this will evaluate the voucher conditions and will tell us if the voucher is enabled
							// and also what the 'code' for the voucher is
							final DiscountValue testDiscountValue = voucher.getDiscountValue(order);
							if (testDiscountValue != null)
							{
								// Found discount to remove
								final DiscountValue oldDiscountValue = Helper.findGlobalDiscountValue(ctx, order,
										testDiscountValue.getCode());
								if (oldDiscountValue != null)
								{
									if (LOG.isDebugEnabled())
									{
										LOG.debug("Removing GlobalDiscountValue created by Voucher [" + voucher.getName(ctx) + "]");
									}

									// Remove the global discount value added by the voucher
									order.removeGlobalDiscountValue(ctx, oldDiscountValue); //NOSONAR
								}

								// Store the voucher and the test discount, we need to remove all the vouchers before we can add them back again
								appliedVouchers.add(voucher);
							}
						}
					}

					return appliedVouchers;
				}
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to fixupVouchersRemoveVouchers", ex);
		}

		return null; //NOSONAR
	}

	/**
	 * Method to fixup the vouchers applied to the order.
	 * <p/>
	 * This method is called after promotions have been applied. It takes a list of voucher that were removed from the
	 * order. This method reapplies the voucher's discount to the order. The voucher calculates its discount based on the
	 * subtotal and does not take into account other discounts. This method then recalculates the discount that should be
	 * applied for each voucher and adds a new global discount for this.
	 *
	 * @param ctx
	 * 		the hybris session context
	 * @param order
	 * 		the order to fixup
	 * @param vouchers
	 * 		the vouchers to reapply
	 */
	@SuppressWarnings("squid:S134")
	protected static void fixupVouchersReapplyVouchers(final SessionContext ctx, final AbstractOrder order,
			final List<Voucher> vouchers)
	{
		// This method is essentially a hack to try to work around an interaction problem
		// between the promotions extension and the voucher extension. Basically the voucher
		// extension ignores global discounts when calculating its relative discounts.
		// We try to fix this by recalculating the voucher's global discount values
		// based on the total rather than the subtotal.

		try
		{
			if (vouchers != null && !vouchers.isEmpty())
			{
				// If we have removed global discount values, we must recalculate the totals
				order.calculateTotals(true); //NOSONAR

				final double orderSubtotal = order.getSubtotal(ctx).doubleValue();

				// create global discounts for all vouchers
				for (final Voucher voucher : vouchers)
				{
					if (voucher.isAbsolute().booleanValue())
					{
						// add back in the absolute discount
						order.addGlobalDiscountValue(ctx, voucher.getDiscountValue(order)); //NOSONAR
					}
					else
					{
						// calculate the relative discount again
						//PRO-70
						final VoucherValue voucherValue = voucher.getVoucherValue(order);
						final double voucherDiscount = voucherValue.getValue();
						final DiscountValue voucherDiscountValue = new DiscountValue(voucher.getCode(), voucherDiscount, true,
								voucherDiscount, order.getCurrency(ctx).getIsoCode(ctx)); //NOSONAR
						order.addGlobalDiscountValue(ctx, voucherDiscountValue); //NOSONAR
						if (LOG.isDebugEnabled())
						{
							LOG.debug("Reapplying Voucher [" + voucher.getName(ctx) + "], Relative Value: [" + voucher.getValue()
									+ "%], Order Total: [" + orderSubtotal + "], New Adjustment Discount [" + voucherDiscountValue + "]");
						}
					}
				}

				// After adding these global discounts we must calculate the totals again
				order.calculateTotals(true); //NOSONAR
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to fixupVouchersReapplyVouchers", ex);
		}
	}

	/**
	 * Which changes should the promotions manager apply to an order automatically.
	 * <p/>
	 * Used in updatePromotions. The mode is specified separately for product and order promotions.
	 */
	public enum AutoApplyMode
	{
		/**
		 * Do not apply any promotions.
		 */
		APPLY_NONE,

		/**
		 * Reapply promotions that are currently applied if they can still be applied.
		 */
		KEEP_APPLIED,

		/**
		 * Apply all promotions that can be applied.
		 */
		APPLY_ALL
	}

	/**
	 * Find all promotions that can be evaluated on the list of product specified.
	 * <p/>
	 * This includes all OrderPromotions and any ProductPromotions that are related to the any of the products passed
	 *
	 * @param ctx
	 * 		The hybris context
	 * @param jaloSession
	 * 		The jalo session
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param products
	 * 		The list of products to find associated promotions for
	 * @param date
	 * 		The date to test against promotions
	 * @return The list of promotions
	 */
	public static List<AbstractPromotion> findOrderAndProductPromotionsSortByPriority(final SessionContext ctx,
			final JaloSession jaloSession, final Collection<PromotionGroup> promotionGroups, final Collection<Product> products, //NOSONAR
			final Date date)
	{
		// No promotion groups, no matching promotions
		if (promotionGroups == null || promotionGroups.isEmpty())
		{
			return Collections.emptyList();
		}

		final StringBuilder promQuery = new StringBuilder("SELECT DISTINCT pprom.pk, pprom.prio FROM (");
		final HashMap args = new HashMap();

		if (products != null && !products.isEmpty())
		{
			// Build query to find all distinct list of all promotions that are related to
			// the source product either via the ProductPromotionRelation or via the CategoryPromotionRelation.
			// Filter the results so that only Promotions with Start and End dates valid for time 'now'
			// order by Priority with the highest value first

			promQuery.append(" {{ SELECT {p.").append(ProductPromotion.PK).append("} as pk, ");
			promQuery.append(" {p.").append(ProductPromotion.PRIORITY).append("} as prio FROM");
			promQuery.append(" {").append(TypeManager.getInstance().getComposedType(ProductPromotion.class).getCode()) //NOSONAR
					.append(" AS p");
			promQuery.append(" JOIN ").append(PromotionsConstants.Relations.PRODUCTPROMOTIONRELATION).append(" AS p2p ");
			promQuery.append(" ON {p.").append(ProductPromotion.PK).append("} = {p2p.").append(Link.TARGET).append("} ");
			promQuery.append(" AND {p2p.").append(Link.SOURCE).append("} in (?products) } ");
			promQuery.append(" WHERE {p.").append(AbstractPromotion.PROMOTIONGROUP).append("} IN (?promotionGroups) AND");
			promQuery.append(" {p.").append(AbstractPromotion.ENABLED).append("} =?true AND ");
			promQuery.append(" {p.").append(AbstractPromotion.STARTDATE).append("} <= ?now AND ");
			promQuery.append(" ?now <= {p.").append(AbstractPromotion.ENDDATE).append("} }}");

			args.put("products", products);

			final Set<Category> productCategories = new HashSet<>();
			for (final Product product : products) //NOSONAR
			{
				for (final Category cat : CategoryManager.getInstance().getCategoriesByProduct(product, ctx))
				{
					productCategories.add(cat);
					productCategories.addAll(cat.getAllSupercategories(ctx)); //NOSONAR
				}
			}

			if (!productCategories.isEmpty())
			{
				promQuery.append(" UNION ");

				promQuery.append(" {{ SELECT {p.").append(ProductPromotion.PK).append("} as pk, ");
				promQuery.append(" {p.").append(ProductPromotion.PRIORITY).append("} as prio FROM");
				promQuery.append(" {").append(TypeManager.getInstance().getComposedType(ProductPromotion.class).getCode()) //NOSONAR
						.append(" AS p");
				promQuery.append(" JOIN ").append(PromotionsConstants.Relations.CATEGORYPROMOTIONRELATION).append(" AS c2p ");
				promQuery.append(" ON {p.").append(ProductPromotion.PK).append("} = {c2p.").append(Link.TARGET).append("} ");
				promQuery.append(" AND {c2p.").append(Link.SOURCE).append("} IN (?productCategories) } ");
				promQuery.append(" WHERE {p.").append(AbstractPromotion.PROMOTIONGROUP).append("} IN (?promotionGroups) AND");
				promQuery.append(" {p.").append(AbstractPromotion.ENABLED).append("} =?true AND ");
				promQuery.append(" {p.").append(AbstractPromotion.STARTDATE).append("} <= ?now AND ");
				promQuery.append(" ?now <= {p.").append(AbstractPromotion.ENDDATE).append("} }}");

				// Find all the categories that the product is in, and all of their super categories
				args.put("productCategories", productCategories);
			}
			promQuery.append(" UNION ALL ");
		}

		// Add query for OrderPromotions
		promQuery.append("{{ SELECT {p3:").append(OrderPromotion.PK).append("}, {p3.").append(OrderPromotion.PRIORITY)
				.append("} as prio ");
		promQuery.append(" FROM {").append(TypeManager.getInstance().getComposedType(OrderPromotion.class).getCode()) //NOSONAR
				.append(" as p3} ");
		promQuery.append(" WHERE {p3.").append(AbstractPromotion.PROMOTIONGROUP).append("} IN (?promotionGroups) AND");
		promQuery.append(" {p3.").append(AbstractPromotion.ENABLED).append("} =?true AND ");
		promQuery.append(" {p3.").append(AbstractPromotion.STARTDATE).append("} <= ?now AND ");
		promQuery.append(" ?now <= {p3.").append(AbstractPromotion.ENDDATE).append("}").append("        }} ");

		// Close AND and add OrderBy
		promQuery.append(" )pprom ORDER BY pprom.prio DESC");

		args.put("now", date);
		args.put("true", Boolean.TRUE);
		args.put("promotionGroups", promotionGroups);

		return jaloSession.getFlexibleSearch().search(ctx, promQuery.toString(), args, AbstractPromotion.class).getResult();
	}

	/**
	 * Get the list of products and base products in the order. If the product is a variant then the base product is
	 * selected.
	 *
	 * @param ctx
	 * 		the hybris context
	 * @param order
	 * 		the order
	 * @return the list of products from the order
	 */
	public static Collection<Product> getBaseProductsForOrder(final SessionContext ctx, final AbstractOrder order) //NOSONAR
	{
		final SortedSet<Product> products = new TreeSet<Product>(); //NOSONAR

		for (final AbstractOrderEntry aoe : (Iterable<AbstractOrderEntry>) order.getAllEntries()) //NOSONAR
		{
			// Get the real product if it is a variant
			final Product product = aoe.getProduct(ctx); //NOSONAR
			if (product != null)
			{
				products.add(product);
				final List<Product> baseProducts = Helper.getBaseProducts(ctx, product); //NOSONAR
				if (baseProducts != null && !baseProducts.isEmpty())
				{
					products.addAll(baseProducts);
				}

			}
		}

		return products;
	}

	// ----------------------------------------------------------------------------
	// Get Promotion Order Results Methods
	// ----------------------------------------------------------------------------

	/**
	 * Get the promotion results for the specified order.
	 * <p/>
	 * These are the promotion results stored in the database for the specified order as generated by the last call to
	 * {@link #updatePromotions} for the same order.
	 * <p/>
	 * If any of the promotion results are invalid then they will be ignored.
	 *
	 * @param order
	 * 		The order to get the promotion results for
	 * @return The promotion results
	 * @see #updatePromotions(Collection, AbstractOrder)
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder)
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 */
	public final PromotionOrderResults getPromotionResults(final AbstractOrder order)
	{
		return getPromotionResults(getSession().getSessionContext(), order);
	}

	/**
	 * Get the promotion results for the specified order.
	 * <p/>
	 * These are the promotion results stored in the database for the specified order as generated by the last call to
	 * {@link #updatePromotions} for the same order.
	 * <p/>
	 * If any of the promotion results are invalid then they will be ignored. To force the list of promotions to be
	 * recalculated either call {@link #updatePromotions} or call the version of
	 * {@link #getPromotionResults(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)}
	 * .
	 *
	 * @param ctx
	 * 		The session context
	 * @param order
	 * 		The order to get the promotion results for
	 * @return The promotion results
	 * @see #updatePromotions(Collection, AbstractOrder)
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder)
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 */
	public PromotionOrderResults getPromotionResults(final SessionContext ctx, final AbstractOrder order)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPromotionResults for [" + order + "]"); //NOSONAR
		}
		final List<PromotionResult> promotionResults = getPromotionResultsInternal(ctx, order);

		final List<PromotionResult> validPromotionResults = new ArrayList<>(promotionResults.size());

		// Check to see if any of the promotions have become invalid since they were last requested
		// If so then ignore them rather than recalculate as we don't have enough params to do an update.
		for (final PromotionResult pr : promotionResults)
		{
			if (pr.isValid(ctx))
			{
				validPromotionResults.add(pr);
			}
		}

		// Not recalculating so the difference is zero
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPromotionResults for [" + order + "] found [" + validPromotionResults.size() + "] promotion results");
		}
		return new PromotionOrderResults(ctx, order, validPromotionResults, 0.0D);
	}

	/**
	 * Get the promotion results for the specified order.
	 * <p/>
	 * These are the promotion results stored in the database for the specified order as generated by the last call to
	 * {@link #updatePromotions} for the same order.
	 * <p/>
	 * If any of the promotion results are invalid then this method will recalculate the promotions by calling
	 * {@link #updatePromotions}.
	 *
	 * @param ctx
	 * 		The hybris session context
	 * @param promotionGroups
	 * 		The promotion groups to evaluate
	 * @param order
	 * 		The AbstractOrder object to get the promotions for
	 * @param evaluateRestrictions
	 * 		If <i>true</i> any promotion restrictions will be observed, if <i>false</i> all promotion restrictions
	 * 		are ignored
	 * @param productPromotionMode
	 * 		The auto apply mode. This determines whether this method applies any product promotional changes to line
	 * 		items or discounts to the overall amount
	 * @param orderPromotionMode
	 * 		The auto apply mode. This determines whether this method applies any order promotional changes to line
	 * 		items or discounts to the overall amount
	 * @param date
	 * 		The effective date for the promotions to check. Use this to to see the effects of promotions in the past
	 * 		or future.
	 * @return The promotion results
	 * @see #updatePromotions(Collection, AbstractOrder)
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder)
	 * @see #updatePromotions(SessionContext, Collection, AbstractOrder, boolean, AutoApplyMode, AutoApplyMode, Date)
	 */
	public PromotionOrderResults getPromotionResults(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final AbstractOrder order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPromotionResults for [" + order + "]");
		}
		final List<PromotionResult> promotionResults = getPromotionResultsInternal(ctx, order);

		// Check to see if any of the promotions have become invalid since they were last requested
		// If so then recalculate the promotions as they have changed
		boolean needsUpdate = false;
		for (final PromotionResult pr : promotionResults)
		{
			if (!pr.isValid(ctx))
			{
				needsUpdate = true;
				break;
			}
		}

		if (needsUpdate)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("getPromotionResults for [" + order + "] some of the promotions are invalid, rebuilding promotions");
			}
			return this.updatePromotions(ctx, promotionGroups, order, evaluateRestrictions, productPromotionMode, orderPromotionMode,
					date);
		}
		else
		{
			// Not recalculating so the difference is zero
			if (LOG.isDebugEnabled())
			{
				LOG.debug("getPromotionResults for [" + order + "] found [" + promotionResults.size() + "] promotion results");
			}
			return new PromotionOrderResults(ctx, order, promotionResults, 0.0D);
		}
	}

	/**
	 * Get the list of promotion results for the specified order.
	 *
	 * @param ctx
	 * 		The session context
	 * @param order
	 * 		The order to get the promotion results for
	 * @return The promotion results
	 */
	protected List<PromotionResult> getPromotionResultsInternal(final SessionContext ctx, final AbstractOrder order)
	{
		try
		{
			if (order != null)
			{
				// Find PromotionResults associated with the order
				final String query = "SELECT {pr:" + Item.PK + "},{promo:" + AbstractPromotion.PRIORITY + "} " + "FROM   {"
						+ TypeManager.getInstance().getComposedType(PromotionResult.class).getCode() + " as pr LEFT JOIN " //NOSONAR
						+ TypeManager.getInstance().getComposedType(AbstractPromotion.class).getCode() + " AS promo ON {pr:" //NOSONAR
						+ PromotionResult.PROMOTION + "}={promo:" + Item.PK + "} } " + "WHERE  {pr:" + PromotionResult.ORDER
						+ "} = ?order " + "ORDER BY {promo:" + AbstractPromotion.PRIORITY + "} DESC";

				final Flat3Map args = new Flat3Map();
				args.put("order", order);

				final List<PromotionResult> promotionResults = getSession().getFlexibleSearch() //NOSONAR
						.search(ctx, query, args, PromotionResult.class).getResult();

				return promotionResults;
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to getPromotionResultsInternal", ex);
		}
		return new ArrayList<>(0);
	}

	/**
	 * Delete the the stored promotion results for an AbstractOrder.
	 *
	 * @param ctx
	 * 		The session context
	 * @param order
	 * 		The order to delete the results for
	 * @param undoActions
	 * 		Undo the actions before deleting the promotion results
	 */
	protected void deleteStoredPromotionResults(final SessionContext ctx, final AbstractOrder order, final boolean undoActions)
	{

		final AbstractOrder orderMtx = order;
		boolean calculateTotals = false;
		synchronized (orderMtx)
		{
			final List<PromotionResult> results = getPromotionResultsInternal(ctx, order);

			for (final PromotionResult result : results)
			{
				try
				{
					if (undoActions)
					{
						calculateTotals |= result.undo(ctx);
					}
					result.remove(ctx);
				}
				catch (final ConsistencyCheckException ccEx)
				{
					LOG.error("deleteStoredPromotionResult failed to undo and remove result [" + result + "]", ccEx);
				}
				catch (final YNoSuchEntityException noEntity)
				{
					LOG.error("deleteStoredPromotionResult failed to undo and remove result", noEntity);
				}
			}
		}

		if (calculateTotals)
		{
			try
			{
				order.calculateTotals(true); //NOSONAR
			}
			catch (final JaloPriceFactoryException ex)
			{
				LOG.error("deleteStoredPromotionResult failed to calculateTotals on order [" + order + "]", ex);
			}
		}
	}

	// ----------------------------------------------------------------------------
	// Cleanup Cart Methods
	// ----------------------------------------------------------------------------

	/**
	 * Delete the the stored promotion results for a Cart.
	 *
	 * @param cart
	 * 		The {@link Cart} to delete the results for
	 */
	public final void cleanupCart(final Cart cart)
	{
		cleanupCart(getSession().getSessionContext(), cart);
	}

	/**
	 * Delete the the stored promotion results for a Cart.
	 *
	 * @param ctx
	 * 		The session context
	 * @param cart
	 * 		The {@link Cart} to delete the results for
	 */
	public void cleanupCart(final SessionContext ctx, final Cart cart)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("cleanupCart for [" + cart + "]");
		}
		if (ctx != null && cart != null)
		{
			deleteStoredPromotionResults(ctx, cart, false);
		}
	}

	/**
	 * Delete any promotion results that are orphaned.
	 * This method should not be used in normal operation, the {@link #cleanupCart} method should be used instead. This
	 * method can be used in an admin context if required.
	 */
	public final void cleanupOrphanedResults()
	{
		cleanupOrphanedResults(getSession().getSessionContext());
	}

	/**
	 * Delete any promotion results that are orphaned.
	 * This method should not be used in normal operation, the {@link #cleanupCart} method should be used instead. This
	 * method can be used in an admin context if required.
	 *
	 * @param ctx
	 * 		The session context
	 */
	public void cleanupOrphanedResults(final SessionContext ctx)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("cleanupOrphanedResults");
		}
		try
		{
			// Get all promotion results that are no longer associated with an order
			final String query = "SELECT {pr:" + Item.PK + "} " + "FROM   {"
					+ TypeManager.getInstance().getComposedType(PromotionResult.class).getCode() + " as pr LEFT JOIN " //NOSONAR
					+ TypeManager.getInstance().getComposedType(AbstractOrder.class).getCode() + " AS order ON {pr:" //NOSONAR
					+ PromotionResult.ORDER + "}={order:" + Item.PK + "} } " + "WHERE  {order:" + Item.PK + "} IS NULL";

			final List<PromotionResult> promotionResults = getSession().getFlexibleSearch()
					.search(ctx, query, Collections.emptyMap(), PromotionResult.class).getResult();
			if (promotionResults != null)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("cleanupOrphanedResults found [" + promotionResults.size() + "] results to remove");
				}

				for (final PromotionResult result : promotionResults)
				{
					try //NOSONAR
					{
						result.remove(ctx);
					}
					catch (final ConsistencyCheckException ccEx)
					{
						LOG.error("In cleanupOrphanedResults failed to remove promotion result [" + result + "]", ccEx);
					}
				}
			}

		}
		catch (final Exception ex)
		{
			LOG.error("Failed to cleanupOrphanedResults", ex);
		}
	}

	// ----------------------------------------------------------------------------
	// Transfer Promotions To Order Methods
	// ----------------------------------------------------------------------------

	/**
	 * Transfer the promotions applied to a cart to a new order. This is used when an order is created from a cart.
	 *
	 * @param source
	 * 		The cart that has promotions
	 * @param target
	 * 		The order that promotions should be applied to
	 * @param onlyTransferAppliedPromotions
	 * 		Flag to indicate that only applied promotions should be transfered. If false all promotion results will
	 * 		be transfered
	 */
	public final void transferPromotionsToOrder(final AbstractOrder source, final Order target,
			final boolean onlyTransferAppliedPromotions)
	{
		transferPromotionsToOrder(getSession().getSessionContext(), source, target, onlyTransferAppliedPromotions);
	}

	/**
	 * Transfer the promotions applied to a cart to a new order. This is used when an order is created from a cart.
	 *
	 * @param ctx
	 * 		The session context
	 * @param source
	 * 		The cart that has promotions
	 * @param target
	 * 		The order that promotions should be applied to
	 * @param onlyTransferAppliedPromotions
	 * 		Flag to indicate that only applied promotions should be transfered. If false all promotion results will
	 * 		be transfered
	 */
	public void transferPromotionsToOrder(final SessionContext ctx, final AbstractOrder source, final Order target,
			final boolean onlyTransferAppliedPromotions)
	{
		try
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("transferPromotionsToOrder from [" + source + "] to [" + target + "] onlyTransferAppliedPromotions=["
						+ onlyTransferAppliedPromotions + "]");

				LOG.debug("Dump Source Order\r\n" + Helper.dumpOrder(ctx, source));
				LOG.debug("Dump Target Order\r\n" + Helper.dumpOrder(ctx, target));
			}

			// Get all the promotion results that we need to transfer
			final List<PromotionResult> promotionResults = getPromotionResultsInternal(ctx, source);
			if (promotionResults != null && !promotionResults.isEmpty())
			{
				for (final PromotionResult result : promotionResults)
				{
					// Filter to applied promotions only, if required
					if (!onlyTransferAppliedPromotions || result.isApplied(ctx)) //NOSONAR
					{
						result.transferToOrder(ctx, target);
					}
				}
			}

			if (LOG.isDebugEnabled())
			{
				LOG.debug("transferPromotionsToOrder completed");
				LOG.debug("Dump Target Order after transfer\r\n" + Helper.dumpOrder(ctx, target));
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to transferPromotionsToOrder", ex);
		}
	}

	// ----------------------------------------------------------------------------
	// Evaluate Restrictions Methods
	// ----------------------------------------------------------------------------

	/**
	 * Evaluate all of the restrictions for a given promotion and return a result object containing the allowed items.
	 *
	 * @param ctx
	 * 		The current context
	 * @param products
	 * 		The products to be considered for restriction
	 * @param order
	 * 		The current order
	 * @param promo
	 * 		The promotion to evaluate
	 * @param date
	 * 		The effective date to evaluate the restrictions on
	 * @return A RestrictionSetResult object
	 */
	public final RestrictionSetResult evaluateRestrictions(final SessionContext ctx, final List<Product> products, //NOSONAR
			final AbstractOrder order, final AbstractPromotion promo, final Date date)
	{
		return evaluateRestrictions(ctx, products, order, promo.getRestrictions(), date);
	}

	/**
	 * Evaluate the specified restrictions and return a result object containing the allowed items.
	 *
	 * @param ctx
	 * 		The current context
	 * @param products
	 * 		The products to be considered for restriction
	 * @param order
	 * 		The current order
	 * @param restrictions
	 * 		The set of restrictions to evaluate
	 * @param date
	 * 		The effective date to evaluate the restrictions on
	 * @return A RestrictionSetResult object
	 */
	public RestrictionSetResult evaluateRestrictions(final SessionContext ctx, final List<Product> products, //NOSONAR
			final AbstractOrder order, final Collection<AbstractPromotionRestriction> restrictions, final Date date)
	{
		final List<Product> allowedProducts = new ArrayList<Product>(products); //NOSONAR

		if (restrictions != null && !restrictions.isEmpty())
		{
			for (final AbstractPromotionRestriction apr : restrictions)
			{
				final AbstractPromotionRestriction.RestrictionResult res = apr.evaluate(ctx, allowedProducts, date, order);
				if (res == AbstractPromotionRestriction.RestrictionResult.DENY
						|| (res == AbstractPromotionRestriction.RestrictionResult.ADJUSTED_PRODUCTS
						&& apr.getPromotion() instanceof OrderPromotion && allowedProducts.isEmpty()))
				{
					// if deny or (PRO-59) if all products in the cart are contained in the product restriction, the promotion is invalid
					// Return deny immediately
					return new RestrictionSetResult();
				}
			}
		}

		// Return allow with products list
		return new RestrictionSetResult(allowedProducts);
	}

	/**
	 * Class representing the result of evaluating a set of restrictions.
	 */
	public static final class RestrictionSetResult
	{
		private final boolean allowedToContinue;
		private final List<Product> allowedProducts; //NOSONAR

		/**
		 * Create a deny restriction set
		 */
		public RestrictionSetResult()
		{
			this.allowedToContinue = false;
			this.allowedProducts = new ArrayList<Product>(0);//NOSONAR
		}

		/**
		 * Create an allow restriction set with specified products.
		 *
		 * @param allowedProducts
		 * 		the list of allowed products
		 */
		public RestrictionSetResult(final List<Product> allowedProducts) //NOSONAR
		{
			this.allowedToContinue = true;
			this.allowedProducts = Collections.unmodifiableList(allowedProducts);
		}

		/**
		 * Check if the restrictions allow the promotion evaluation to continue.
		 *
		 * @return <i>true</i> if the promotion is allowed to evaluate
		 */
		public boolean isAllowedToContinue()
		{
			return allowedToContinue;
		}

		/**
		 * Get the filtered product list.
		 *
		 * @return The filtered product list
		 */
		public List<Product> getAllowedProducts() //NOSONAR
		{
			return allowedProducts;
		}

	}

	/**
	 * Lookup the default promotion group created by the promotions extension.
	 *
	 * @return the default promotion group
	 */
	public PromotionGroup getDefaultPromotionGroup()
	{
		return getDefaultPromotionGroup(getSession().getSessionContext());
	}

	/**
	 * Lookup the default promotion group created by the promotions extension.
	 *
	 * @param ctx
	 * 		The session context
	 * @return the default promotion group
	 */
	public PromotionGroup getDefaultPromotionGroup(final SessionContext ctx) //NOSONAR
	{
		final HashMap<String, Object> params = new HashMap<>();
		params.put("identifier", PromotionsConstants.DEFAULT_PROMOTION_GROUP_IDENTIFIER);

		final SearchResult res = getSession().getFlexibleSearch().search(
				"SELECT {" + Item.PK + "} FROM {" + PromotionsConstants.TC.PROMOTIONGROUP + "} WHERE {" + PromotionGroup.IDENTIFIER
						+ "} = ?identifier",
				params, Collections.singletonList(PromotionGroup.class), true, // fail on unknown fields
				true, // don't need total
				0, -1 // range
		);

		final List<PromotionGroup> results = res.getResult();
		if (results != null && !results.isEmpty())
		{
			return results.get(0);
		}

		return null;
	}

	/**
	 * Lookup a promotion group with the given identifier.
	 *
	 * @param identifier
	 * 		identifier of the promotion group to look for
	 * @return the promotion group or null if no group with the given identifier is found *
	 * @throws IllegalArgumentException
	 * 		if identifier is null
	 */
	public PromotionGroup getPromotionGroup(final String identifier)
	{
		if (identifier == null)
		{
			throw new IllegalArgumentException("identifier cannot be null");
		}
		final HashMap<String, String> params = new HashMap<>();
		params.put("identifier", identifier);
		final String query = "SELECT {" + Item.PK + "} FROM {" + PromotionsConstants.TC.PROMOTIONGROUP + "} WHERE {"
				+ PromotionGroup.IDENTIFIER + "} = ?identifier";
		final SearchResult res = getSession().getFlexibleSearch().search(query, params,
				Collections.singletonList(PromotionGroup.class), true, true, 0, -1);
		final List<PromotionGroup> results = res.getResult();
		if (results != null && !results.isEmpty())
		{
			return results.get(0);
		}
		return null;
	}

	// ----------------------------------------------------------------------------
	// Create Methods
	// ----------------------------------------------------------------------------

	/**
	 * Create a new {@link PromotionOrderEntryConsumed} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param code
	 * 		The object's code
	 * @param orderEntry
	 * 		The {@link AbstractOrderEntry} that is the base order entry for the object
	 * @param quantity
	 * 		The quantity mapped through from the base order entry
	 * @return the new {@link PromotionOrderEntryConsumed}
	 */
	public PromotionOrderEntryConsumed createPromotionOrderEntryConsumed(final SessionContext ctx, final String code,
			final AbstractOrderEntry orderEntry, final long quantity)
	{
		// get the current unit price from the orderEntry
		final double unitPrice = orderEntry.getBasePrice(ctx).doubleValue();

		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(PromotionOrderEntryConsumed.CODE, code);
		parameters.put(PromotionOrderEntryConsumed.ORDERENTRY, orderEntry);
		parameters.put(PromotionOrderEntryConsumed.QUANTITY, Long.valueOf(quantity));

		// Default value for adjusted unit price is the real unit price
		parameters.put(PromotionOrderEntryConsumed.ADJUSTEDUNITPRICE, Double.valueOf(unitPrice));
		return super.createPromotionOrderEntryConsumed(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionOrderEntryConsumed} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param code
	 * 		The object's code
	 * @param orderEntry
	 * 		The {@link AbstractOrderEntry} that is the base order entry for the object
	 * @param quantity
	 * 		The quantity mapped through from the base order entry
	 * @param adjustedUnitPrice
	 * 		The adjusted unit price
	 * @return the new {@link PromotionOrderEntryConsumed}
	 */
	public PromotionOrderEntryConsumed createPromotionOrderEntryConsumed(final SessionContext ctx, final String code,
			final AbstractOrderEntry orderEntry, final long quantity, final double adjustedUnitPrice)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(PromotionOrderEntryConsumed.CODE, code);
		parameters.put(PromotionOrderEntryConsumed.ORDERENTRY, orderEntry);
		parameters.put(PromotionOrderEntryConsumed.QUANTITY, Long.valueOf(quantity));
		parameters.put(PromotionOrderEntryConsumed.ADJUSTEDUNITPRICE, Double.valueOf(adjustedUnitPrice));
		return super.createPromotionOrderEntryConsumed(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionResult} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param promotion
	 * 		The {@link AbstractPromotion} that created the {@link PromotionResult}
	 * @param order
	 * 		The {@link AbstractOrder} that the {@link PromotionResult} is related to
	 * @param certainty
	 * 		The certainty of firing in the range 0 to 1. 1.0 is fired, less than 1.0 is could fire.
	 * @return the new {@link PromotionResult}
	 */
	public PromotionResult createPromotionResult(final SessionContext ctx, final AbstractPromotion promotion,
			final AbstractOrder order, final float certainty)
	{
		if (promotion == null || order == null || certainty < 0.0F || certainty > 1.0F)
		{
			throw new PromotionException("Invalid attempt to create a promotion result");
		}

		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(PromotionResult.PROMOTION, promotion);
		parameters.put(PromotionResult.ORDER, order);
		parameters.put(PromotionResult.CERTAINTY, Float.valueOf(certainty));
		return super.createPromotionResult(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionOrderAdjustTotalAction} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param totalAdjustment
	 * 		The amount to adjust the order total (positive increases order total, negative decreases)
	 * @return the new {@link PromotionOrderAdjustTotalAction}
	 */
	public PromotionOrderAdjustTotalAction createPromotionOrderAdjustTotalAction(final SessionContext ctx,
			final double totalAdjustment)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
		parameters.put(PromotionOrderAdjustTotalAction.AMOUNT, Double.valueOf(totalAdjustment));
		return super.createPromotionOrderAdjustTotalAction(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionOrderAddFreeGiftAction} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param product
	 * 		The {@link Product} to give away
	 * @param result
	 * 		The {@link PromotionResult} that owns this action.
	 * @return the new {@link PromotionOrderAddFreeGiftAction}
	 */
	public PromotionOrderAddFreeGiftAction createPromotionOrderAddFreeGiftAction(final SessionContext ctx, final Product product, //NOSONAR
			final PromotionResult result)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
		parameters.put(PromotionOrderAddFreeGiftAction.FREEPRODUCT, product);
		parameters.put(AbstractPromotionAction.PROMOTIONRESULT, result);
		return super.createPromotionOrderAddFreeGiftAction(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionOrderChangeDeliveryModeAction} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param deliveryMode
	 * 		The {@link DeliveryMode} to set on the order
	 * @return the new {@link PromotionOrderChangeDeliveryModeAction}
	 */
	public PromotionOrderChangeDeliveryModeAction createPromotionOrderChangeDeliveryModeAction(final SessionContext ctx,
			final DeliveryMode deliveryMode)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
		parameters.put(PromotionOrderChangeDeliveryModeAction.DELIVERYMODE, deliveryMode);
		return super.createPromotionOrderChangeDeliveryModeAction(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionPriceRow} instance.
	 *
	 * @param currency
	 * 		The {@link de.hybris.platform.jalo.c2l.Currency} that the price is specified in
	 * @param price
	 * 		The price value
	 * @return the new {@link PromotionPriceRow}
	 */
	public PromotionPriceRow createPromotionPriceRow(final de.hybris.platform.jalo.c2l.Currency currency, final double price)
	{
		return createPromotionPriceRow(getSession().getSessionContext(), currency, price);
	}

	/**
	 * Create a new {@link PromotionPriceRow} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param currency
	 * 		The {@link de.hybris.platform.jalo.c2l.Currency} that the price is specifed in
	 * @param price
	 * 		The price value
	 * @return the new {@link PromotionPriceRow}
	 */
	public PromotionPriceRow createPromotionPriceRow(final SessionContext ctx, final de.hybris.platform.jalo.c2l.Currency currency,
			final double price)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(PromotionPriceRow.CURRENCY, currency);
		parameters.put(PromotionPriceRow.PRICE, Double.valueOf(price));
		return super.createPromotionPriceRow(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionQuantityAndPricesRow} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param quantity
	 * 		The quantity
	 * @param prices
	 * 		The prices for the quantity
	 * @return the new {@link PromotionQuantityAndPricesRow}
	 */
	public PromotionQuantityAndPricesRow createPromotionQuantityAndPricesRow(final SessionContext ctx, final long quantity,
			final Collection<PromotionPriceRow> prices)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(PromotionQuantityAndPricesRow.QUANTITY, Long.valueOf(quantity));
		parameters.put(PromotionQuantityAndPricesRow.PRICES, prices);
		return super.createPromotionQuantityAndPricesRow(ctx, parameters);
	}

	/**
	 * @deprecated Since 4.4 use {@link #createPromotionOrderEntryAdjustAction(SessionContext, AbstractOrderEntry, long, double)},
	 *             see PRO-75
	 *
	 *             Create a new {@link PromotionOrderEntryAdjustAction} instance.
	 *
	 * @param ctx
	 *           The session context
	 * @param product
	 *           The {@link Product} to adjust
	 * @param quantity
	 *           The quantity to adjust
	 * @param adjustment
	 *           The adjustment to make
	 * @return the new {@link PromotionOrderEntryAdjustAction}
	 */
	@Deprecated
	public PromotionOrderEntryAdjustAction createPromotionOrderEntryAdjustAction(final SessionContext ctx, final Product product, //NOSONAR
			final long quantity, final double adjustment)
	{
		final Map parameters = new HashMap();
		parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
		parameters.put(PromotionOrderEntryAdjustAction.AMOUNT, Double.valueOf(adjustment));
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYPRODUCT, product);
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYQUANTITY, Long.valueOf(quantity));
		return super.createPromotionOrderEntryAdjustAction(ctx, parameters);
	}
	
	/**
	 * Create a new {@link PromotionOrderEntryAdjustAction} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param entry
	 * 		The {@link AbstractOrderEntry} to adjust
	 * @param quantity
	 * 		The quantity to adjust
	 * @param adjustment
	 * 		The adjustment to make
	 * @return the new {@link PromotionOrderEntryAdjustAction}
	 * @since 4.4
	 */
	public PromotionOrderEntryAdjustAction createPromotionOrderEntryAdjustAction(final SessionContext ctx,
			final AbstractOrderEntry entry, final long quantity, final double adjustment)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
		parameters.put(PromotionOrderEntryAdjustAction.AMOUNT, Double.valueOf(adjustment));
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYPRODUCT, entry.getProduct(ctx));
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYNUMBER, entry.getEntryNumber());
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYQUANTITY, Long.valueOf(quantity));
		return super.createPromotionOrderEntryAdjustAction(ctx, parameters);
	}

	/**
	 * This does not store the order entry but records its information so that a similar order entry with the same
	 * product and quantity can be located in this and other orders.
	 *
	 * @param ctx
	 * 		The session context
	 * @param entry
	 * 		The {@link AbstractOrderEntry} to adjust
	 * @param adjustment
	 * 		The adjustment to make
	 * @return the new {@link PromotionOrderEntryAdjustAction}
	 */
	public PromotionOrderEntryAdjustAction createPromotionOrderEntryAdjustAction(final SessionContext ctx,
			final AbstractOrderEntry entry, final double adjustment)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
		parameters.put(PromotionOrderEntryAdjustAction.AMOUNT, Double.valueOf(adjustment));
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYPRODUCT, entry.getProduct(ctx));
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYNUMBER, entry.getEntryNumber());
		parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYQUANTITY, entry.getQuantity(ctx));
		return super.createPromotionOrderEntryAdjustAction(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionGroup} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @param identifier
	 * 		The identifier for the promotion group
	 * @return the new {@link PromotionGroup}
	 */
	public PromotionGroup createPromotionGroup(final SessionContext ctx, final String identifier)
	{
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(PromotionGroup.IDENTIFIER, identifier);
		return super.createPromotionGroup(ctx, parameters);
	}

	/**
	 * Create a new {@link PromotionNullAction} instance.
	 *
	 * @param ctx
	 * 		The session context
	 * @return the new {@link PromotionNullAction}
	 */
	public PromotionNullAction createPromotionNullAction(final SessionContext ctx)
	{
		final Map<String, Object> parameters = Maps.newHashMap();
		parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
		return super.createPromotionNullAction(ctx, parameters);
	}

	/**
	 * Create a new unique string to identify an action instance.
	 *
	 * @return a unique identifier
	 */
	protected static String makeActionGUID()
	{
		return "Action[" + (new VMID()).toString() + "]";
	}
	
}
