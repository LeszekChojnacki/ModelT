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
package de.hybris.platform.ruleengineservices.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengineservices.rrd.RuleGroupExecutionRRD;


/**
 * RuleGroupExecutionRrdTemplatePopulator is a populator for creating a copy of a {@link RuleGroupExecutionRRD} from a
 * {@code RuleGroupExecutionRRD}.
 */
public class RuleGroupExecutionRrdTemplatePopulator implements Populator<RuleGroupExecutionRRD, RuleGroupExecutionRRD>
{

	@Override
	public void populate(final RuleGroupExecutionRRD source, final RuleGroupExecutionRRD target)
	{
		target.setCode(source.getCode());
		target.setExclusive(source.isExclusive());
	}
}
