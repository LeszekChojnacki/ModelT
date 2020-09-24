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

import static java.util.Collections.singletonList;

import de.hybris.platform.ruleengineservices.jobs.RuleEngineCronJobLauncher;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;


/**
 * Composer to invoke action for the rule archiving process
 * 
 * @deprecated since 1811
 */
@Deprecated
public class RuleArchiveComposer extends AbstractRuleCompilePublishComposer
{
	private static final Logger LOG = LoggerFactory.getLogger(RuleArchiveComposer.class);

	@Resource
	private transient RuleEngineCronJobLauncher ruleEngineCronJobLauncher;
	@Resource
	private transient CockpitEventQueue eventQueue;
	@Resource
	private transient ObjectFacade objectFacade;

	private transient InteractiveAction interactiveAction;
	private Window window;
	private transient ActionContext<SourceRuleModel> context;
	private Button okBtn;

	/**
	 * called upon on click event of the corresponding OK button.
	 */
	@ViewEvent(componentID = "yesBtn", eventName = Events.ON_CLICK)
	public void perform()
	{
		try
		{
			doArchiveRule(getContext().getData());
		}
		catch (final Exception exception)
		{
			LOG.error("Exception during rule archiving process.", exception);
			onException(exception);
		}

		getWindow().detach();
	}

	@ViewEvent(componentID = "cancelBtn", eventName = Events.ON_CLICK)
	public void closeDialog()
	{
		getWindow().detach();
	}

	protected void doArchiveRule(final SourceRuleModel ruleModel)
	{
		final RuleEngineCronJobModel ruleEngineCronJob = getRuleEngineCronJobLauncher().triggerArchiveRule(ruleModel);
		onJobTriggered(ruleEngineCronJob);
		getEventQueue()
				.publishEvent(new DefaultCockpitEvent("updateProcessForCronJob", ruleEngineCronJob.getJob().getCode(), null));
	}

	@Override
	protected void onSuccess(final String moduleName, final String previousModuleVersion, final String moduleVersion)
	{
		notifyObjectsUpdated(getContext().getData());
	}

	@Override
	protected void onRuleUpdate(final String ruleCode)
	{
		if (getContext().getData().getCode().equals(ruleCode))
		{
			notifyObjectsUpdated(getContext().getData());
		}
	}

	protected void notifyObjectsUpdated(final SourceRuleModel updatedRule)
	{
		final SourceRuleModel rule = reload(updatedRule);

		if (getEventQueue() != null)
		{
			final DefaultCockpitEvent event = new DefaultCockpitEvent(ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, rule, null);

			getEventQueue().publishEvent(event);
		}

		getInteractiveAction().sendOutputDataToSocket("deselectItems", singletonList(rule));
		getInteractiveAction().sendOutputDataToSocket("selectItem", rule);
	}

	protected SourceRuleModel reload(final SourceRuleModel rule)
	{
		try
		{
			return getObjectFacade().reload(rule);
		}
		catch (final ObjectNotFoundException e)
		{
			LOG.error("Attempt to reload current object failed", e);
			//it shouldn't happen in real life scenario, but in case it does let it fail as
			//it's hard to recover from this scenario
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * returns reference to the action instance.
	 *
	 * @return the action instance
	 */
	protected InteractiveAction getInteractiveAction()
	{
		return interactiveAction;
	}

	protected RuleEngineCronJobLauncher getRuleEngineCronJobLauncher()
	{
		return ruleEngineCronJobLauncher;
	}

	protected CockpitEventQueue getEventQueue()
	{
		return eventQueue;
	}

	protected ActionContext<SourceRuleModel> getContext()
	{
		return context;
	}

	protected Window getWindow()
	{
		return window;
	}

	protected Button getOkBtn()
	{
		return okBtn;
	}

	protected ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}
}
