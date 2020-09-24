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
package com.hybris.backoffice.cockpitng.editorarea.dynamicforms;

import de.hybris.platform.scripting.engine.ScriptExecutable;
import de.hybris.platform.scripting.engine.ScriptExecutionResult;
import de.hybris.platform.scripting.engine.ScriptingLanguagesService;
import de.hybris.platform.scripting.engine.content.ScriptContent;
import de.hybris.platform.scripting.engine.content.impl.SimpleScriptContent;

import java.util.HashMap;
import java.util.Map;

import com.hybris.cockpitng.core.config.impl.jaxb.dynamicforms.ScriptingLanguage;
import com.hybris.cockpitng.core.config.impl.jaxb.dynamicforms.ScriptingType;
import com.hybris.cockpitng.widgets.common.dynamicforms.impl.DefaultExpressionEvaluator;


public class BackofficeExpressionEvaluator extends DefaultExpressionEvaluator
{
	public static final String TARGET_CONTEXT_NAME = "context";
	private ScriptingLanguagesService scriptingLanguagesService;

	@Override
	public <T> T evaluateExpression(final ScriptingLanguage scriptingLanguage, final ScriptingType scriptingType,
			final String expression, final Object target)
	{
		if (scriptingLanguage.value().equalsIgnoreCase(ScriptingLanguage.SP_EL.value()))
		{
			return super.evaluateExpression(scriptingLanguage, scriptingType, expression, target);
		}
		else
		{
			final Map<String, Object> targetContext = new HashMap<>();
			targetContext.put(TARGET_CONTEXT_NAME, target);
			final ScriptExecutable scriptExecutable = getScriptExecutable(scriptingLanguage, scriptingType, expression);
			final ScriptExecutionResult result = scriptExecutable.execute(targetContext);
			final Object value = result.isSuccessful() ? result.getScriptResult() : null;
			return (T) value;
		}
	}

	private ScriptExecutable getScriptExecutable(final ScriptingLanguage scriptingLanguage, final ScriptingType scriptingType,
			final String expression)
	{
		if (scriptingType.equals(ScriptingType.INLINE))
		{
			return getInlineScriptExecutable(scriptingLanguage, expression);
		}
		else if (scriptingType.equals(ScriptingType.URI))
		{
			return getUriScriptExecutable(expression);
		}

		throw new UnsupportedOperationException("BackofficeExpressionEvaluator supports only inline and URI scripts");
	}

	private ScriptExecutable getInlineScriptExecutable(final ScriptingLanguage scriptingLanguage, final String expression)
	{

		final ScriptContent scriptContent = new SimpleScriptContent(scriptingLanguage.value(), expression);
		return scriptingLanguagesService.getExecutableByContent(scriptContent);
	}

	private ScriptExecutable getUriScriptExecutable(final String uri)
	{

		return scriptingLanguagesService.getExecutableByURI(uri);
	}

	public void setScriptingLanguagesService(final ScriptingLanguagesService scriptingLanguagesService)
	{
		this.scriptingLanguagesService = scriptingLanguagesService;
	}
}
