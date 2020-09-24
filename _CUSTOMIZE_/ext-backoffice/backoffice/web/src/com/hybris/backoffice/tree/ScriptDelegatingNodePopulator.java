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
package com.hybris.backoffice.tree;

import de.hybris.platform.scripting.engine.ScriptExecutable;
import de.hybris.platform.scripting.engine.ScriptExecutionResult;
import de.hybris.platform.scripting.engine.ScriptingLanguagesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.tree.node.DynamicNode;
import com.hybris.cockpitng.tree.node.DynamicNodePopulator;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


public class ScriptDelegatingNodePopulator implements DynamicNodePopulator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptDelegatingNodePopulator.class);

	static final String PARAM_SCRIPT_URI = "scriptUri";

	@Override
	public List<NavigationNode> getChildren(final NavigationNode parent)
	{
		List<NavigationNode> children = Collections.emptyList();

		if (parent instanceof DynamicNode)
		{
			final DynamicNode dynamicNode = (DynamicNode) parent;

			try
			{
				final ScriptExecutionResult executionResult = executeScript(dynamicNode.getContext());

				if (executionResult != null && executionResult.isSuccessful() && executionResult.getScriptResult() != null)
				{
					final Object scriptResult = executionResult.getScriptResult();

					children = extractChildren(parent, scriptResult);
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("Getting children for dynamic node failed", e);
			}
		}

		return children;
	}

	protected ScriptExecutionResult executeScript(final CockpitContext context)
	{
		ScriptExecutionResult executionResult = null;

		if (context != null)
		{
			final String scriptUri = (String)context.getParameter(PARAM_SCRIPT_URI);

			final ScriptingLanguagesService scriptingLanguagesService = getScriptingLanguagesService();

			if (scriptingLanguagesService != null && StringUtils.isNotBlank(scriptUri))
			{
				final ScriptExecutable executable = scriptingLanguagesService.getExecutableByURI(scriptUri);

				if (executable != null)
				{
					executionResult = executable.execute();
				}
				else
				{
					LOGGER.warn("Script not found for URI {}", scriptUri);
				}
			}
			else
			{
				LOGGER.warn("Scripting engine not available");
			}
		}


		return executionResult;
	}

	protected ScriptingLanguagesService getScriptingLanguagesService()
	{
		return BackofficeSpringUtil.getBean("scriptingLanguagesService", ScriptingLanguagesService.class);
	}

	protected List<NavigationNode> extractChildren(final NavigationNode parent, final Object scriptResult)
	{
		final List<NavigationNode> children = Collections.checkedList(new ArrayList<>(), NavigationNode.class);

		try
		{
			if (scriptResult instanceof NavigationNode)
			{
				children.add((NavigationNode) scriptResult);
			}
			else if (scriptResult instanceof Collection)
			{
				children.addAll((Collection) scriptResult);
			}
			else if (scriptResult instanceof Object[])
			{
				final List list = Arrays.asList((Object[]) scriptResult);
				children.addAll(list);
			}
			else if (scriptResult instanceof DynamicNodePopulator)
			{
				children.addAll(((DynamicNodePopulator) scriptResult).getChildren(parent));
			}
			else if (scriptResult instanceof Class && DynamicNodePopulator.class.isAssignableFrom((Class) scriptResult))
			{
				final DynamicNodePopulator populator = (DynamicNodePopulator)((Class) scriptResult).newInstance();
				children.addAll(populator.getChildren(parent));
			}
			else
			{
				LOGGER.warn("The script returned type {} but either NavigationNode, NavigationNode[], Collection<NavigationNode>, " +
						"DynamicNodePopulator class implementation or DynamicNodePopulator instance is expected",
						scriptResult.getClass());
			}
		}
		catch(InstantiationException|IllegalAccessException|RuntimeException e)
		{
			LOGGER.warn("Script result parsing failed", e);
		}

		children.stream().forEach( child -> child.setParent(parent) );

		return children;
	}
}
