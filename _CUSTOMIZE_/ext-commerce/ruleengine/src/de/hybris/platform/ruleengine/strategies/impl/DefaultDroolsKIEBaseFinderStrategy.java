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
package de.hybris.platform.ruleengine.strategies.impl;

import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.strategies.DroolsKIEBaseFinderStrategy;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;


public class DefaultDroolsKIEBaseFinderStrategy implements DroolsKIEBaseFinderStrategy
{

	@Override
	public DroolsKIEBaseModel getKIEBaseForKIEModule(final DroolsKIEModuleModel kieModule)
	{
		DroolsKIEBaseModel kieBase = kieModule.getDefaultKIEBase();
		if (kieBase == null)
		{
			final Collection<DroolsKIEBaseModel> droolsKIEBases = kieModule.getKieBases();
			if (CollectionUtils.isEmpty(droolsKIEBases))
			{
				return null;
			}
			kieBase = droolsKIEBases.iterator().next();
		}
		return kieBase;
	}
}
