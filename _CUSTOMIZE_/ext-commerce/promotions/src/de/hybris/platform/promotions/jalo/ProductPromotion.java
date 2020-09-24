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


import de.hybris.platform.category.constants.CategoryConstants;
import de.hybris.platform.category.jalo.Category;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.link.Link;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.TypeManager;
import de.hybris.platform.promotions.constants.PromotionsConstants;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.Flat3Map;


/**
 * Product Promotion.
 */
public abstract class ProductPromotion extends GeneratedProductPromotion //NOSONAR
{
	// The relationships between this ProductPromotion and the products and categories
	// will be removed automatically when this instance is removed, therefore we do not
	// need to override the void remove(SessionContext) method.
	//

	/**
	 * Find all products that this promotion applies to by both direct association and via categories.
	 *
	 * @param ctx
	 * @param promoContext
	 * @return A list of Products
	 */
	protected PromotionsManager.RestrictionSetResult findAllProducts(final SessionContext ctx,
			final PromotionEvaluationContext promoContext)
	{
		// Find all the categories that the promotion is in, and all of their sub categories
		final Set<Category> promotionCategories = new HashSet<>();
		for (final Category cat : getCategories(ctx))
		{
			promotionCategories.add(cat);
			promotionCategories.addAll(cat.getAllSubcategories(ctx)); // NOSONAR
		}
		final List<Category> promotionCategoriesList = new ArrayList<>();
		promotionCategoriesList.addAll(promotionCategories);

		final String queryProdPromo = "	SELECT DISTINCT {product:" + Item.PK + "} " + "	FROM {"
				+ TypeManager.getInstance().getComposedType(Product.class).getCode() + " as product} " + "	WHERE " + "	( " // NOSONAR
				+ "		{product:" + Item.PK + "} IN ( " + "			{{ " + "				SELECT {prod2promo:" + Link.SOURCE
				+ "} FROM {" + PromotionsConstants.Relations.PRODUCTPROMOTIONRELATION + " AS prod2promo} "
				+ "				WHERE {prod2promo:" + Link.TARGET + "} = ?promo " + "			}} " + "		) ";

		String queryCategories = "		OR " + "		{product:" + Item.PK + "} IN ( " + "			{{ " + "				SELECT {cat2prod:"
				+ Link.TARGET + "} FROM { " + CategoryConstants.Relations.CATEGORYPRODUCTRELATION + " AS cat2prod} " // NOSONAR
				+ "				WHERE";

		final String categoriesQueryEnd = "			}} " + "		) ";

		final Flat3Map queryParams = new Flat3Map();
		queryParams.put("promo", this);

		//jira issue [COM-940]
		//pagination of categories due to Oracle IN list limitation
		if (!Config.isOracleUsed())
		{
			queryCategories += " {cat2prod:" + Link.SOURCE + "} IN ( ?promotionCategories ) ";
			queryParams.put("promotionCategories", promotionCategories);
		}

		else
		{
			int pages = 0;
			for (int i = 0; i < promotionCategoriesList.size(); i += 1000)
			{
				queryParams.put("promotionCategories_" + pages,
						promotionCategoriesList.subList(i, Math.min(i + 1000, promotionCategoriesList.size())));
				pages++;
			}
			for (int i = 0; i < pages; i++)
			{
				if (i > 0)
				{
					queryCategories += " OR "; //NOSONAR
				}
				queryCategories += ("{cat2prod:" + Link.SOURCE + "} IN ( ?promotionCategories_" + i + " )"); //NOSONAR
			}
		}

		queryCategories += categoriesQueryEnd;
		final String queryEnd = "	) ";
		String query;
		if (promotionCategories.isEmpty())
		{
			query = queryProdPromo + queryEnd;
		}
		else
		{
			query = queryProdPromo + queryCategories + queryEnd;
		}

		// Get the list of products that are associated with this product promotion
		final List<Product> products = getSession().getFlexibleSearch().search(ctx, query, queryParams, Product.class).getResult(); // NOSONAR

		// Run restrictions if appropriate
		if (promoContext.getObserveRestrictions())
		{
			return PromotionsManager.getInstance().evaluateRestrictions(ctx, products, promoContext.getOrder(), this,
					promoContext.getDate());
		}
		else
		{
			return new PromotionsManager.RestrictionSetResult(products);
		}
	}

	/**
	 * Return the intersection of the set of products bound to this promotion and the current cart contents.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param promoContext
	 *           The promotion evaluation context
	 * @return The set of products that are associated with this promotion and available in the cart or order.
	 */
	protected PromotionsManager.RestrictionSetResult findEligibleProductsInBasket(final SessionContext ctx, //NOSONAR
			final PromotionEvaluationContext promoContext)
	{
		final Collection<Product> products = PromotionsManager.getBaseProductsForOrder(ctx, promoContext.getOrder()); // NOSONAR

		if (!products.isEmpty())
		{
			// Find all the categories that the promotion is in, and all of their sub categories
			final Set<Category> promotionCategories = new HashSet<>();
			for (final Category cat : getCategories(ctx))
			{
				promotionCategories.add(cat);
				promotionCategories.addAll(cat.getAllSubcategories(ctx)); // NOSONAR
			}
			final List<Category> promotionCategoriesList = new ArrayList<>(promotionCategories);

			final Flat3Map params = new Flat3Map();
			params.put("promo", this);
			params.put("product", products);


			// Build query to find all distinct list of all promotions that are related to
			// the source product either via the ProductPromotionRelation or via the CategoryPromotionRelation.
			// Filter the results so that only Promotions with Start and End dates valid for time 'now'
			// order by Priority with the highest value first
			final StringBuilder promQuery = new StringBuilder("SELECT DISTINCT pprom.pk FROM (");
			promQuery.append(" {{ SELECT {p2p:").append(Link.SOURCE).append("} as pk "); // NOSONAR
			promQuery.append(" FROM {").append(PromotionsConstants.Relations.PRODUCTPROMOTIONRELATION).append(" AS p2p } ");
			promQuery.append(" WHERE ?promo = {p2p:").append(Link.TARGET).append("} ");
			promQuery.append(" AND {p2p:").append(Link.SOURCE).append("} in (?product) }} "); // NOSONAR

			//jira issue: [COM-1940]
			if (!Config.isOracleUsed())
			{
				if (!promotionCategoriesList.isEmpty())
				{
					promQuery.append(" UNION ");

					promQuery.append("{{ SELECT {cat2prod:").append(Link.TARGET).append("} as pk ");
					promQuery.append(" FROM { ").append(CategoryConstants.Relations.CATEGORYPRODUCTRELATION).append(" AS cat2prod} ");
					promQuery.append(" WHERE {cat2prod:").append(Link.SOURCE).append("} in (?promotionCategories)  ");
					promQuery.append("   AND {cat2prod:").append(Link.TARGET).append("} in (?product) }} ");

					params.put("promotionCategories", promotionCategories);
				}
				promQuery.append(" ) AS pprom");
			}

			//Oracle limitation of IN lists to 1000    ITEM IN (?,?,?,?,?,?,?,...)
			//pagination needed
			else
			{
				if (!promotionCategoriesList.isEmpty())
				{
					int pages = 0;
					for (int i = 0; i < promotionCategoriesList.size(); i += 1000) //NOSONAR
					{
						//pagination of categories for pages of size 1000
						params.put("promotionCategories_" + pages,
								promotionCategoriesList.subList(i, Math.min(i + 1000, promotionCategoriesList.size())));
						pages++;
					}
					for (int i = 0; i < pages; i++) //NOSONAR
					{
						promQuery.append(" UNION ");

						promQuery.append("{{ SELECT {cat2prod:").append(Link.TARGET).append("} as pk ");
						promQuery.append(" FROM { ").append(CategoryConstants.Relations.CATEGORYPRODUCTRELATION)
								.append(" AS cat2prod} ");
						promQuery.append(" WHERE {cat2prod:").append(Link.SOURCE).append("} in (?promotionCategories_").append(i);
						promQuery.append(")   AND {cat2prod:").append(Link.TARGET).append("} in (?product) }} ");
					}
				}
				promQuery.append(" ) pprom");
			}

			// Find the set of all products which are in the basket and can be considered by this promotion
			final List<Product> cartProducts = getSession().getFlexibleSearch() // NOSONAR
					.search(ctx, promQuery.toString(), params, Product.class).getResult(); // NOSONAR

			// Run restrictions if appropriate
			if (promoContext.getObserveRestrictions())
			{
				return PromotionsManager.getInstance().evaluateRestrictions(ctx, cartProducts, promoContext.getOrder(), this,
						promoContext.getDate());
			}
			else
			{
				return new PromotionsManager.RestrictionSetResult(cartProducts);
			}
		}
		else
		{
			// By default return proceed with an empty set
			return new PromotionsManager.RestrictionSetResult(new ArrayList<>(0));
		}
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);

		buildDataUniqueKeyForProducts(ctx, builder, getProducts(ctx));
		buildDataUniqueKeyForCategories(ctx, builder, getCategories(ctx));
	}

}
