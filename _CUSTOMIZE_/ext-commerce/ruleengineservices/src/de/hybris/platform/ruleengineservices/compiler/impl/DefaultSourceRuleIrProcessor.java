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
package de.hybris.platform.ruleengineservices.compiler.impl;

import static de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator.IN;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrProcessor;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rao.CampaignRAO;


/**
 * Default RuleIrProcessor for SourceRule types
 */
public class DefaultSourceRuleIrProcessor implements RuleIrProcessor
{
	private static final String CAMPAIGN_RAO_CODE_ATTRIBUTE = "code";

	@Override
	public void process(final RuleCompilerContext context, final RuleIr ruleIr)
	{
		final AbstractRuleModel rule = context.getRule();

		if (rule instanceof SourceRuleModel)
		{
			final SourceRuleModel sourceRule = (SourceRuleModel) rule;

			// add all of the source rule's associated campaign codes as a condition
			if (isNotEmpty(sourceRule.getCampaigns()))
			{
				final String campaignRaoVariable = context.generateVariable(CampaignRAO.class);
				final RuleIrAttributeCondition irAttributeCondition = new RuleIrAttributeCondition();
				irAttributeCondition.setAttribute(CAMPAIGN_RAO_CODE_ATTRIBUTE);
				irAttributeCondition.setOperator(IN);
				irAttributeCondition
						.setValue(sourceRule.getCampaigns().stream().map(campaign -> campaign.getCode()).collect(toList()));
				irAttributeCondition.setVariable(campaignRaoVariable);
				ruleIr.getConditions().add(irAttributeCondition);
			}
		}
	}
}
