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
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;


/**
 * RuleConfigurationRrdTemplatePopulator is a populator for creating a copy of a {@link RuleConfigurationRRD} from a
 * {@code RuleConfigurationRRD}.
 */
public class RuleConfigurationRrdTemplatePopulator implements Populator<RuleConfigurationRRD, RuleConfigurationRRD>
{

	@Override
	public void populate(final RuleConfigurationRRD source, final RuleConfigurationRRD target) 
	{
		target.setRuleCode(source.getRuleCode());
		target.setCurrentRuns(source.getCurrentRuns());
		target.setMaxAllowedRuns(source.getMaxAllowedRuns());
		target.setRuleGroupCode(source.getRuleGroupCode());
	}
}
