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
package com.hybris.backoffice.solrsearch.daos;

import java.util.Collection;

import com.hybris.backoffice.solrsearch.enums.SolrItemModificationType;
import com.hybris.backoffice.solrsearch.model.SolrModifiedItemModel;


/**
 * DAO for {@link SolrModifiedItemModel}
 *
 * @deprecated since 1808 {@link SolrModifiedItemModel} is no longer used in solr index update strategy
 */
@Deprecated
public interface SolrModifiedItemDAO
{
	Collection<SolrModifiedItemModel> findByModificationType(SolrItemModificationType modificationType);
}
