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
package com.hybris.backoffice.widgets.scriptexecutor;

import de.hybris.platform.scripting.engine.ScriptExecutable;
import de.hybris.platform.scripting.engine.ScriptExecutionResult;
import de.hybris.platform.scripting.engine.ScriptingLanguagesService;
import de.hybris.platform.scripting.engine.content.impl.SimpleScriptContent;
import de.hybris.platform.scripting.engine.exception.ScriptingException;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Widget controller of a Script Executor widget. With this widget it's possible to run a script content using platforms
 * {#ScriptingLanguagesService}.
 */
public class ScriptExecutorController extends DefaultWidgetController
{

	public static final String INPUT_SOCKET_DATA = "data";
	public static final String SETTING_SCRIPT_URI = "scriptUri";
	public static final String SETTING_SCRIPT_CONTENT = "scriptContent";
	public static final String SETTING_SCRIPT_CONTENT_LANG = "scriptContentLang";
	public static final String SOCKET_OUTPUT_SUCCESS = "success";
	public static final String SOCKET_OUTPUT_ERROR = "error";

	private static final Logger LOG = LoggerFactory.getLogger(ScriptExecutorController.class);

	/**
	 * Method runs the script based on it's widget settings.
	 *
	 * @param data
	 *           the data which is accessible in the script under the "data" variable name.
	 */
	@SocketEvent(socketId = INPUT_SOCKET_DATA)
	public void input(final Object data)
	{
		try
		{
			final String scriptURI = getWidgetSettings().getString(SETTING_SCRIPT_URI);
			final String scriptContent = getWidgetSettings().getString(SETTING_SCRIPT_CONTENT);
			final String scriptContentLang = getWidgetSettings().getString(SETTING_SCRIPT_CONTENT_LANG);

			if (StringUtils.isNotBlank(scriptURI))
			{
				executeScriptByURI(data, scriptURI);
			}
			else if (StringUtils.isNotBlank(scriptContent) && StringUtils.isNotBlank(scriptContentLang))
			{
				executeScriptWithInlineContent(data, scriptContent, scriptContentLang);
			}
			else
			{
				LOG.warn("Script not set. Passing unmodified input data to success output.");
				sendOutput(SOCKET_OUTPUT_SUCCESS, data);
			}
		}
		catch (final ScriptingException exception)
		{
			LOG.warn("Script invocation failed. Passing unmodified input data to error output.", exception);
			sendOutput(SOCKET_OUTPUT_ERROR, data);
		}
	}

	protected void executeScriptWithInlineContent(final Object data, final String scriptContent, final String scriptContentLang)
	{
		final ScriptExecutable executable = getScriptingLanguagesService()
				.getExecutableByContent(new SimpleScriptContent(scriptContentLang, scriptContent));
		final ScriptExecutionResult result = executeScript(executable, data);
		if (result.isSuccessful())
		{
			sendOutput(SOCKET_OUTPUT_SUCCESS, result.getScriptResult());
		}
		else
		{
			if (LOG.isWarnEnabled())
			{
				LOG.warn(String.format("Finished script evaluation of %S with errors", scriptContentLang));
			}
			sendOutput(SOCKET_OUTPUT_ERROR, result.getScriptResult());
		}
	}

	protected void executeScriptByURI(final Object data, final String scriptId)
	{
		final ScriptExecutable executable = getScriptingLanguagesService().getExecutableByURI(scriptId.trim());
		final ScriptExecutionResult result = executeScript(executable, data);
		if (result.isSuccessful())
		{
			sendOutput(SOCKET_OUTPUT_SUCCESS, result.getScriptResult());
		}
		else
		{
			LOG.warn("Finished script evaluation with errors: {}", scriptId);
			sendOutput(SOCKET_OUTPUT_ERROR, result.getScriptResult());
		}
	}

	protected ScriptExecutionResult executeScript(final ScriptExecutable executable, final Object data)
	{
		final HashMap<String, Object> context = new HashMap<>();
		context.put("data", data);
		return executable.execute(context);
	}

	protected ScriptingLanguagesService getScriptingLanguagesService()
	{
		return BackofficeSpringUtil.getBean("scriptingLanguagesService", ScriptingLanguagesService.class);
	}

}
