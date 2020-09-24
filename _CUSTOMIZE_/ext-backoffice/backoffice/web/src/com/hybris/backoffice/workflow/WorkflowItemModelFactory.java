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
package com.hybris.backoffice.workflow;

import de.hybris.platform.core.model.link.LinkModel;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowActionTemplateModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionTemplateModel;

import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.labels.LabelService;


/**
 * A factory for {@link WorkflowItem}
 */
public class WorkflowItemModelFactory
{

	static final String PROPERTY_AND_CONNECTION = "andConnection";
	static final String PROPERTY_AND_CONNECTION_TEMPLATE = "andConnectionTemplate";

	private CockpitLocaleService localeService;
	private LabelService labelService;

	/**
	 * Checks whether the given {@link LinkModel} is an "and" connection
	 *
	 * @param link
	 *           to be checked
	 * @return true if the link is an "and" connection
	 */
	public static boolean isAndConnection(final @Nullable LinkModel link)
	{
		return Optional.ofNullable(link) //
				.<Boolean> map(model -> model.getProperty(PROPERTY_AND_CONNECTION)) //
				.orElse(false);
	}

	/**
	 * Checks whether the given {@link LinkModel} is an "and" connection template
	 *
	 * @param link
	 *           to be checked
	 * @return true if the link is an "and" connection template
	 */
	public static boolean isAndConnectionTemplate(final @Nullable LinkModel link)
	{
		return Optional.ofNullable(link) //
				.<Boolean> map(model -> model.getProperty(PROPERTY_AND_CONNECTION_TEMPLATE)) //
				.orElse(false);
	}

	/**
	 * Creates new {@link WorkflowItem} from the {@link WorkflowActionTemplateModel}
	 *
	 * @param action
	 *           that should be used to create new {@link WorkflowItem}
	 * @return WorkflowItem built from the action
	 */
	public WorkflowItem create(final WorkflowActionTemplateModel action)
	{
		return new WorkflowItemFromWorkflowActionTemplateModel(action, localeService);
	}

	/**
	 * Creates new {@link WorkflowItem} from the {@link WorkflowActionModel}
	 *
	 * @param action
	 *           that should be used to create new {@link WorkflowItem}
	 * @return WorkflowItem built from the action
	 */
	public WorkflowItem create(final WorkflowActionModel action)
	{
		return new WorkflowItemFromWorkflowActionModel(action, localeService, labelService);
	}

	/**
	 * Creates new {@link WorkflowItem} from the {@link WorkflowDecisionTemplateModel}
	 *
	 * @param decision
	 *           that should be used to create new {@link WorkflowItem}
	 * @return WorkflowItem built from the decision
	 */
	public WorkflowItem create(final WorkflowDecisionTemplateModel decision)
	{
		return new WorkflowItemFromWorkflowDecisionTemplateModel(decision, localeService);
	}

	/**
	 * Creates new {@link WorkflowItem} from the {@link WorkflowDecisionModel}
	 *
	 * @param decision
	 *           that should be used to create new {@link WorkflowItem}
	 * @return WorkflowItem built from the decision
	 */
	public WorkflowItem create(final WorkflowDecisionModel decision)
	{
		return new WorkflowItemFromWorkflowDecisionModel(decision, localeService);
	}

	/**
	 * Creates new {@link WorkflowItem} from the {@link LinkModel}
	 *
	 * @param link
	 *           that should be used to create new {@link WorkflowItem}
	 * @return WorkflowItem built from the link
	 */
	public WorkflowItem create(final LinkModel link)
	{
		return new WorkflowItemFromWorkflowLinkModel(link);
	}

	/**
	 * Creates new {@link WorkflowItem} that contains neighbors from source and target
	 *
	 * @param target
	 *           {@link WorkflowItem} that should be used to create new {@link WorkflowItem}
	 * @param source
	 *           {@link WorkflowItem} from which the neighbors should be added to the target
	 * @return a copy of target that additionally contains source's neighbors
	 */
	public WorkflowItem mergeNeighbors(final WorkflowItem target, final WorkflowItem source)
	{
		return new WorkflowItemWithMergedNeighbors(target, source);
	}

	public CockpitLocaleService getLocaleService()
	{
		return localeService;
	}

	@Required
	public void setLocaleService(final CockpitLocaleService localeService)
	{
		this.localeService = localeService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
