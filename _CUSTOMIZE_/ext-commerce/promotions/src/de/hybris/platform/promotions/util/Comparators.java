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
package de.hybris.platform.promotions.util;

import de.hybris.platform.catalog.jalo.Catalog;
import de.hybris.platform.catalog.jalo.CatalogManager;
import de.hybris.platform.catalog.jalo.CatalogVersion;
import de.hybris.platform.category.jalo.Category;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.jalo.PromotionPriceRow;
import de.hybris.platform.promotions.result.PromotionOrderEntry;

import java.util.Comparator;


/**
 * Helper class full of comparators.
 *
 *
 * @version v1
 */
public class Comparators //NOSONAR
{
	public static final Comparator<PromotionPriceRow> promotionPriceRowComparator = new PromotionPriceRowComparator();
	public static final Comparator<Currency> currencyComparator = new CurrencyComparator();
	public static final Comparator<Double> doubleComparator = new DoubleComparator();
	public static final Comparator<String> stringComparator = new StringComparator();
	public static final Comparator<Product> productComparator = new ProductComparator(); //NOSONAR
	public static final Comparator<CatalogVersion> catalogVersionComparator = new CatalogVersionComparator();
	public static final Comparator<Catalog> catalogComparator = new CatalogComparator();
	public static final Comparator<Category> categoryComparator = new CategoryComparator();
	public static final Comparator<PromotionOrderEntry> promotionOrderEntryByPriceComparator = new PromotionOrderEntryByPriceComparator();

	protected static class PromotionPriceRowComparator implements Comparator<PromotionPriceRow>
	{
		@Override
		public int compare(final PromotionPriceRow a, final PromotionPriceRow b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			final SessionContext ctx = JaloSession.getCurrentSession().createSessionContext();

			final int currencyResult = currencyComparator.compare(a.getCurrency(ctx), b.getCurrency(ctx));
			if (currencyResult != 0)
			{
				return currencyResult;
			}

			return doubleComparator.compare(a.getPrice(ctx), b.getPrice(ctx));
		}
	}

	protected static class CurrencyComparator implements Comparator<Currency>
	{
		@Override
		public int compare(final Currency a, final Currency b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			final SessionContext ctx = JaloSession.getCurrentSession().createSessionContext();

			return stringComparator.compare(a.getIsoCode(ctx), b.getIsoCode(ctx));  //NOSONAR
		}
	}

	protected static class DoubleComparator implements Comparator<Double>
	{
		@Override
		public int compare(final Double a, final Double b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			return Double.compare(a.doubleValue(), b.doubleValue());
		}
	}

	protected static class StringComparator implements Comparator<String>
	{
		@Override
		public int compare(final String a, final String b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			return a.compareTo(b);
		}
	}

	protected static class ProductComparator implements Comparator<Product>  //NOSONAR
	{
		@Override
		public int compare(final Product a, final Product b)  //NOSONAR
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			final SessionContext ctx = JaloSession.getCurrentSession().createSessionContext();

			final int codeResult = stringComparator.compare(a.getCode(ctx), b.getCode(ctx));
			if (codeResult != 0)
			{
				return codeResult;
			}

			final CatalogManager mgr = CatalogManager.getInstance();
			return catalogVersionComparator.compare(mgr.getCatalogVersion(ctx, a), mgr.getCatalogVersion(ctx, b));
		}
	}

	protected static class CatalogVersionComparator implements Comparator<CatalogVersion>
	{
		@Override
		public int compare(final CatalogVersion a, final CatalogVersion b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			final SessionContext ctx = JaloSession.getCurrentSession().createSessionContext();

			final int catalogResult = catalogComparator.compare(a.getCatalog(ctx), b.getCatalog(ctx));
			if (catalogResult != 0)
			{
				return catalogResult;
			}

			return stringComparator.compare(a.getVersion(ctx), b.getVersion(ctx));
		}
	}

	protected static class CatalogComparator implements Comparator<Catalog>
	{
		@Override
		public int compare(final Catalog a, final Catalog b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			final SessionContext ctx = JaloSession.getCurrentSession().createSessionContext();

			return stringComparator.compare(a.getId(ctx), b.getId(ctx));
		}
	}

	protected static class CategoryComparator implements Comparator<Category>
	{
		@Override
		public int compare(final Category a, final Category b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			final SessionContext ctx = JaloSession.getCurrentSession().createSessionContext();

			final int codeResult = stringComparator.compare(a.getCode(ctx), b.getCode(ctx));
			if (codeResult != 0)
			{
				return codeResult;
			}

			final CatalogManager mgr = CatalogManager.getInstance();
			return catalogVersionComparator.compare(mgr.getCatalogVersion(ctx, a), mgr.getCatalogVersion(ctx, b));
		}
	}

	protected static class PromotionOrderEntryByPriceComparator implements Comparator<PromotionOrderEntry>
	{
		@Override
		public int compare(final PromotionOrderEntry a, final PromotionOrderEntry b)
		{
			if (a == null && b == null)
			{
				return 0;
			}
			if (a == null)
			{
				return -1;
			}
			if (b == null)
			{
				return 1;
			}

			final SessionContext ctx = JaloSession.getCurrentSession().createSessionContext();

			return a.getBasePrice(ctx).compareTo(b.getBasePrice(ctx));
		}
	}
}
