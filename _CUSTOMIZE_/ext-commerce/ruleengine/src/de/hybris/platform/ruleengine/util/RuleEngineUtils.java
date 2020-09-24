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
package de.hybris.platform.ruleengine.util;

import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.DROOLS_BASE_PATH;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.MEDIA_CODE_POSTFIX;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.MEDIA_DRL_FILE_EXTENSION;
import static java.util.Objects.isNull;

import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Static methods for analysis and simple manipulation of the rule content
 */
public class RuleEngineUtils
{

	private static final String RULE_SPLITTING_PATTERN = "(query\\s+.*)(rule\\s+.*)";
	private static Pattern ruleSplittingRegexp;

	static
	{
		ruleSplittingRegexp = Pattern.compile(RULE_SPLITTING_PATTERN, Pattern.DOTALL);
	}

	private RuleEngineUtils()
	{
	}

	public static String getCleanedContent(final String seedRuleContent, final String ruleUuid)
	{
		if (isNull(seedRuleContent))
		{
			return null;
		}
		final Matcher matcher = ruleSplittingRegexp.matcher(seedRuleContent);
		final StringBuilder cleanedContentSB = new StringBuilder();
		if (matcher.find())
		{
			cleanedContentSB.append(matcher.group(1).trim()).append(matcher.group(2).trim());
		}
		else
		{
			cleanedContentSB.append(seedRuleContent.trim());
		}
		final String uuidConcat = ruleUuid.replace("-", "");
		return cleanedContentSB.toString().replace(ruleUuid, "RULE_UUID").replace(uuidConcat, "RULEUUID");
	}

	public static String getNormalizedRulePath(final String rulePath)
	{
		if (isNull(rulePath))
		{
			return null;
		}
		return rulePath.replace(File.separatorChar, '/');
	}

	public static String getRulePath(final DroolsRuleModel rule)
	{
		String rulePackagePath = "";
		if (rule.getRulePackage() != null)
		{
			rulePackagePath = rule.getRulePackage().replace('.', File.separatorChar);
		}
		return getNormalizedRulePath(DROOLS_BASE_PATH + rulePackagePath + rule.getCode() + MEDIA_CODE_POSTFIX
				+ MEDIA_DRL_FILE_EXTENSION);
	}

	public static String stripDroolsMainResources(final String normalizedPath)
	{
		final String normalizedDroolsBasePath = getNormalizedRulePath(DROOLS_BASE_PATH);
		if (normalizedPath.startsWith(normalizedDroolsBasePath))
		{
			return normalizedPath.substring(normalizedDroolsBasePath.length()); // NOSONAR
		}
		return normalizedPath;
	}

	public static String getDeployedRulesModuleVersion(final DroolsKIEModuleModel rulesModule)
	{
		checkArgument(Objects.nonNull(rulesModule), "Rules module shouldn't be null here");

		return rulesModule.getMvnVersion() + "." + rulesModule.getVersion();
	}

	public static boolean isDroolsKieModuleDeployed(final DroolsKIEModuleModel rulesModule)
	{
		return getDeployedRulesModuleVersion(rulesModule).equals(rulesModule.getDeployedMvnVersion());
	}
}
