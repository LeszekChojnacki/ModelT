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
package de.hybris.platform.solrserver.impl;

import de.hybris.platform.solrserver.SolrInstance;
import de.hybris.platform.solrserver.SolrServerController;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of {@link SolrServerController} for windows systems.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public class WindowsSolrServerController extends AbstractSolrServerController
{
	public static final String SHELL_EXECUTABLE = "cmd.exe";

	@Override
	protected void configureSolrCommandInvocation(final SolrInstance solrInstance, final ProcessBuilder processBuilder,
			final String command)
	{
		final String solrServerPath = getSolrServerPath();

		final List<String> commandParams = new ArrayList<>();
		commandParams.add(SHELL_EXECUTABLE);
		commandParams.add("/C");
		commandParams.add(Paths.get(solrServerPath, "bin", "solr.cmd").toString());

		addCommand(commandParams, command);

		processBuilder.command().addAll(commandParams);

		processBuilder.environment().put("SOLR_SERVER_DIR", Paths.get(solrServerPath, "server").toString());
		processBuilder.environment().put("SOLR_HOME", solrInstance.getConfigDir());
		processBuilder.environment().put("SOLR_DATA_HOME", solrInstance.getDataDir());
		processBuilder.environment().put("SOLR_LOGS_DIR", solrInstance.getLogDir());
		processBuilder.environment().put("LOG4J_CONFIG",
				"file:" + Paths.get(solrInstance.getConfigDir(), "log4j.properties").toString());
	}

	@Override
	protected void configureZKCommandInvocation(final SolrInstance solrInstance, final ProcessBuilder processBuilder,
			final String command)
	{
		final String solrServerPath = getSolrServerPath();

		final List<String> commandParams = new ArrayList<>();
		commandParams.add(SHELL_EXECUTABLE);
		commandParams.add("/C");
		commandParams.add(Paths.get(solrServerPath, "server", "scripts", "cloud-scripts", "zkcli.bat").toString());
		commandParams.add("-cmd");

		addCommand(commandParams, command);

		processBuilder.command().addAll(commandParams);
	}
}