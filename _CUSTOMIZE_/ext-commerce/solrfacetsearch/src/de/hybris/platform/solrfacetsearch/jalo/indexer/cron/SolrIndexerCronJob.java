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
package de.hybris.platform.solrfacetsearch.jalo.indexer.cron;

import de.hybris.platform.jalo.JaloSession;


public class SolrIndexerCronJob extends GeneratedSolrIndexerCronJob
{
	public static final String DISABLE_RESTRICTIONS = "disableRestrictions";
	public static final String DISABLE_RESTRICTIONS_GROUP_INHERITANCE = "disableRestrictionGroupInheritance";

	@Override
	protected JaloSession createSessionForCronJob(final JaloSession jaloSession)
	{
		final JaloSession parentSession = super.createSessionForCronJob(jaloSession);
		jaloSession.getSessionContext().setAttribute(DISABLE_RESTRICTIONS, Boolean.FALSE);
		jaloSession.getSessionContext().setAttribute(DISABLE_RESTRICTIONS_GROUP_INHERITANCE, Boolean.FALSE);
		return parentSession;
	}
}
