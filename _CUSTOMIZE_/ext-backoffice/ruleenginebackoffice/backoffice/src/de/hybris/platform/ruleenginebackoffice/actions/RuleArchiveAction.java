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
package de.hybris.platform.ruleenginebackoffice.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.util.EngineRulesRepository;
import de.hybris.platform.ruleengine.util.RuleMappings;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.hybris.platform.ruleengine.util.RuleMappings.moduleName;
import static java.util.Collections.singletonMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * @deprecated since 1811
 */
@Deprecated
public class RuleArchiveAction extends AbstractInteractiveAction<AbstractRuleModel, Object>
{
	private static final String TITLE = "title.rulearchiveaction";
	private static final String DIALOG_TEMPLATE = "/archiverule.zul";
	private static final String CONFIRMATION_MESSAGE = "rule.archive.confirmation.message";
	private static final String CONFIRMATION_MESSAGE_WITH_DEPLOYMENTS = "rule.archive.with.deployments.confirmation.message";

	@Resource
	private transient CockpitEventQueue eventQueue;

	@Resource
	private transient EngineRulesRepository engineRulesRepository;

	@Override
	protected String getDialogTemplate(final ActionContext<AbstractRuleModel> context)
	{
		return DIALOG_TEMPLATE;
	}

	@Override
	protected String getDialogTitle(final ActionContext<AbstractRuleModel> context)
	{
		return TITLE;
	}

	@Override
	public boolean canPerform(final ActionContext<AbstractRuleModel> context)
	{
		final AbstractRuleModel data = context.getData();
		return super.canPerform(context)
				&& !(data.getStatus() == RuleStatus.UNPUBLISHED || data.getStatus() == RuleStatus.ARCHIVED);
	}

	@Override
	public String getConfirmationMessage(final ActionContext<AbstractRuleModel> context)
	{
		final Set<String> deployedModulesNames = context.getData().getEngineRules().stream()
				.filter(DroolsRuleModel.class::isInstance)
				.map(DroolsRuleModel.class::cast)
				.filter(r -> getEngineRulesRepository().checkEngineRuleDeployedForModule(r, moduleName(r)))
				.map(RuleMappings::moduleName).distinct().collect(Collectors.toSet());

		if (isEmpty(deployedModulesNames))
		{
			return context.getLabel(CONFIRMATION_MESSAGE);
		}
		else
		{
			return context.getLabel(CONFIRMATION_MESSAGE_WITH_DEPLOYMENTS, new Object[]
			{ deployedModulesNames.stream().collect(Collectors.joining(", ")) });
		}
	}

	@Override
	protected Map<String, Object> getArguments(final ActionContext<AbstractRuleModel> context)
	{
		return singletonMap("confirmationMessage", getConfirmationMessage(context));
	}

	protected CockpitEventQueue getEventQueue()
	{
		return eventQueue;
	}

	public EngineRulesRepository getEngineRulesRepository()
	{
		return engineRulesRepository;
	}
}
