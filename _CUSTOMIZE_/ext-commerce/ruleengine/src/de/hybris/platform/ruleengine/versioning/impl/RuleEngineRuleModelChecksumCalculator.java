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
package de.hybris.platform.ruleengine.versioning.impl;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.collections.MapUtils.isNotEmpty;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.RuleModelChecksumCalculator;

import java.util.Map;


/**
 * The default implementation of RuleModelChecksumCalculator interface
 */
public class RuleEngineRuleModelChecksumCalculator implements RuleModelChecksumCalculator
{
	@Override
	public String calculateChecksumOf(final AbstractRuleEngineRuleModel rule)
	{
		requireNonNull(rule, "Rule model object is expected to be not null here");

		final StringBuilder checksumPayload = new StringBuilder();
		final String ruleContent = rule.getRuleContent();
		if (nonNull(ruleContent))
		{
			checksumPayload.append(ruleContent);
			if (rule instanceof DroolsRuleModel)
			{
				final DroolsRuleModel droolsRule = (DroolsRuleModel) rule;
				final Map<String, String> ruleGlobals = droolsRule.getGlobals();
				if (isNotEmpty(ruleGlobals))
				{
					checksumPayload.append(";Globals_");
					ruleGlobals.entrySet().stream()
							.forEach(e -> checksumPayload.append(e.getKey()).append(":").append(e.getValue()).append(";"));
				}
			}
			return calculateContentChecksum(checksumPayload.toString());
		}
		else
		{
			return null;
		}
	}

	protected String calculateContentChecksum(final String checksumPayload)
	{
		return md5Hex(requireNonNull(checksumPayload));
	}
}
