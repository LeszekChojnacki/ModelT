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
package de.hybris.platform.ruleengine.init.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.init.ContentMatchRulesFilter;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.util.EngineRulesRepository;
import de.hybris.platform.ruleengine.util.RuleMappings;
import de.hybris.platform.ruleengine.versioning.ModuleVersionResolver;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.ruleengine.util.EngineRulePreconditions.checkRuleHasKieModule;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


/**
 * Rules filter based on analysis of deployed rules content. Filters out all the rules having the rule content already
 * deployed
 */
public class DefaultContentMatchRulesFilter implements ContentMatchRulesFilter
{

	private EngineRuleDao engineRuleDao;

	private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;
	private EngineRulesRepository engineRulesRepository;

	/**
	 * Filter out rules that has been already deployed
	 *
	 * @param ruleUuids
	 * 		a collection of rule UUIDs of the rules to be filtered out
	 * @return a tuple, containing a collection of {@link DroolsRuleModel} to add (LHS), and a collection of
	 * {@link DroolsRuleModel} to delete
	 */
	@Override
	public Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(final Collection<String> ruleUuids)
	{
		return apply(ruleUuids, null);
	}

	@Override
	public Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(final Collection<String> ruleUuids,
			final Long newModuleVersion)
	{
		checkArgument(isNotEmpty(ruleUuids), "The provided rule UUIDs collections shouldn't be NULL or empty");

		final Collection<DroolsRuleModel> rulesByUuids = getEngineRuleDao().getRulesByUuids(ruleUuids);

		final Optional<DroolsKIEModuleModel> kieModule = verifyTheRulesModuleIsSame(getRulesWithKieBase(rulesByUuids));

		if (kieModule.isPresent())
		{
			final DroolsKIEModuleModel module = kieModule.get();
			final Optional<Long> moduleDeployedVersion = getModuleVersionResolver().getDeployedModuleVersion(module);

			if (moduleDeployedVersion.isPresent())
			{

				final Collection<DroolsRuleModel> deployedRuleset = getEngineRulesRepository()
						.getDeployedEngineRulesForModule(module.getName());
				final Collection<DroolsRuleModel> rulesetToDeploy = getRulesetWithMaxVersion(rulesByUuids, newModuleVersion);
				final Set<DroolsRuleModel> rulesetToAdd = Sets.newHashSet(rulesetToDeploy);
				rulesetToAdd.removeAll(deployedRuleset);
				final Set<DroolsRuleModel> rulesetToRemove = Sets.newHashSet(deployedRuleset);
				rulesetToRemove.removeAll(rulesetToDeploy);

				return ImmutablePair.of(rulesetToAdd, rulesetToRemove);

			}
		}
		return ImmutablePair.of(rulesByUuids, newArrayList());
	}

	protected List<DroolsRuleModel> getRulesWithKieBase(final Collection<DroolsRuleModel> rules)
	{
		return rules.stream().filter(r -> nonNull(r.getKieBase())).collect(toList());
	}

	protected Collection<DroolsRuleModel> getRulesetWithMaxVersion(final Collection<DroolsRuleModel> rulesByUuids,
			final Long version)
	{
		Long maxVersion = version;
		if (Objects.isNull(version))
		{
			maxVersion = Long.MAX_VALUE;
		}
		final Map<String, DroolsRuleModel> rulesByCodeMap = Maps.newHashMap();
		final List<DroolsRuleModel> activeDroolRules = rulesByUuids.stream().filter(DroolsRuleModel::getActive).collect(toList());

		for (DroolsRuleModel ruleByUuid : activeDroolRules)
		{
			final String code = ruleByUuid.getCode();
			if (rulesByCodeMap.containsKey(code))
			{
				final DroolsRuleModel ruleForCode = rulesByCodeMap.get(code);
				final Long ruleVersion = ruleByUuid.getVersion();
				if (ruleVersion > ruleForCode.getVersion() && ruleVersion <= maxVersion)
				{
					rulesByCodeMap.replace(code, ruleByUuid);
				}
			}
			else if (ruleByUuid.getVersion() <= maxVersion)
			{
				rulesByCodeMap.put(code, ruleByUuid);
			}
		}
		return rulesByCodeMap.values();
	}

	protected Optional<DroolsKIEModuleModel> verifyTheRulesModuleIsSame(final Collection<DroolsRuleModel> droolRules)
	{
		Optional<DroolsKIEModuleModel> rulesModule = Optional.empty();
		if (isNotEmpty(droolRules))
		{
			final DroolsRuleModel firstDroolsRule = droolRules.iterator().next();
			checkRuleHasKieModule(firstDroolsRule);
			final DroolsKIEModuleModel kieModule = firstDroolsRule.getKieBase().getKieModule();
			final String kieModuleName = kieModule.getName();
			if (isNull(kieModuleName))
			{
				throw new IllegalStateException("The KIE module cannot have the empty name");
			}
			if (droolRules.stream().anyMatch(r -> !RuleMappings.module(r).getName().equals(kieModuleName)))
			{
				throw new IllegalStateException(
						"All the rules in the collection should have the same DroolsKIEModuleModel [" + kieModuleName + "]");
			}
			return Optional.of(kieModule);
		}
		return rulesModule;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}

	protected ModuleVersionResolver<DroolsKIEModuleModel> getModuleVersionResolver()
	{
		return moduleVersionResolver;
	}

	@Required
	public void setModuleVersionResolver(final ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver)
	{
		this.moduleVersionResolver = moduleVersionResolver;
	}

	protected EngineRulesRepository getEngineRulesRepository()
	{
		return engineRulesRepository;
	}

	@Required
	public void setEngineRulesRepository(final EngineRulesRepository engineRulesRepository)
	{
		this.engineRulesRepository = engineRulesRepository;
	}
}
