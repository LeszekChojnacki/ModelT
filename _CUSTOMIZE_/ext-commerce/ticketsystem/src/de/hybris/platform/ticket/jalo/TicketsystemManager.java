/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ticket.jalo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.impex.jalo.ImpExManager;
import de.hybris.platform.ticket.constants.TicketsystemConstants;
import de.hybris.platform.util.CSVConstants;

import java.io.InputStream;

import org.apache.log4j.Logger;



/**
 * This is the extension manager of the Ticketsystem extension.
 */
public class TicketsystemManager extends GeneratedTicketsystemManager
{
	/** Edit the local|project.properties to change logging behavior (properties 'log4j.*'). */
	private static final Logger LOG = Logger.getLogger(TicketsystemManager.class.getName());

	/*
	 * Some important tips for development:
	 *
	 * Do NEVER use the default constructor of manager's or items. => If you want to do something whenever the manger is
	 * created use the init() or destroy() methods described below
	 *
	 * Do NEVER use STATIC fields in your manager or items! => If you want to cache anything in a "static" way, use an
	 * instance variable in your manager, the manager is created only once in the lifetime of a "deployment" or tenant.
	 */


	/**
	 * Never call the constructor of any manager directly, call getInstance() You can place your business logic here -
	 * like registering a jalo session listener. Each manager is created once for each tenant.
	 */
	public TicketsystemManager() // NOPMD
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("constructor of TicketsystemManager called.");
		}
	}

	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static TicketsystemManager getInstance()
	{
		return (TicketsystemManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(TicketsystemConstants.EXTENSIONNAME);
	}

	@SuppressWarnings("unused")
	protected void importCSVFromResources(final String csv)
	{
		LOG.info("Importing [" + csv + "]");
		importCSVFromResources(csv, CSVConstants.HYBRIS_ENCODING, CSVConstants.HYBRIS_FIELD_SEPARATOR,
				CSVConstants.HYBRIS_QUOTE_CHARACTER, true);
		LOG.info("DONE importing [" + csv + "]");
	}

	protected void importCSVFromResources(final String csv, final String encoding, final char fieldseparator,
			final char quotecharacter, final boolean codeExecution)
	{
		LOG.info("importing resource " + csv);
		final InputStream input = TicketsystemManager.class.getResourceAsStream(csv);
		if (input == null)
		{
			LOG.warn("Import resource '" + csv + "' not found!");
			return;
		}
		ImpExManager.getInstance().importData(input, encoding, fieldseparator, quotecharacter, codeExecution);
	}

}
