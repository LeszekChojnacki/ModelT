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
package de.hybris.platform.solrfacetsearch.attribute;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrAbstractKeywordRedirectModel;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrCategoryRedirectModel;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrProductRedirectModel;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrURIRedirectModel;


public class DefaultRedirectHmcLabelProvider implements DynamicAttributeHandler<String, SolrAbstractKeywordRedirectModel>
{
	@Override
	public String get(final SolrAbstractKeywordRedirectModel model)
	{
		if (model instanceof SolrURIRedirectModel)
		{
			return ((SolrURIRedirectModel) model).getUrl();
		}
		else if (model instanceof SolrProductRedirectModel)
		{
			final ProductModel redirectItem = ((SolrProductRedirectModel) model).getRedirectItem();
			return redirectItem.getName();
		}
		else if (model instanceof SolrCategoryRedirectModel)
		{
			final CategoryModel redirectItem = ((SolrCategoryRedirectModel) model).getRedirectItem();
			return redirectItem.getName();
		}
		return model.toString();
	}

	@Override
	public void set(final SolrAbstractKeywordRedirectModel model, final String value)
	{
		throw new UnsupportedOperationException("The attribute is readonly");
	}

}
