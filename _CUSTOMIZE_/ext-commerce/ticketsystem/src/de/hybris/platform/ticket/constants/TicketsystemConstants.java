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
package de.hybris.platform.ticket.constants;

/**
 * Global class for all Ticketsystem constants. You can add global constants for your extension into this class.
 */
public final class TicketsystemConstants extends GeneratedTicketsystemConstants
{
	public static final String SUPPORT_TICKET_UPDATED = "Support Ticket Message Updated";
	public static final String SUPPORT_TICKET_ASSIGNED = "Support Ticket Assigned";
	public static final String SUPPORT_TICKET_STAGNATION_KEY = "ticket.stagnation.cronjob.email.message";
	public static final String SUPPORT_TICKET_STAGNATION_DEFAULT_CLOSING_MESSAGE = "The ticket was closed because it was inactive for some time. Please reopen the ticket if it is still valid.";
	// implement here constants used by this extension

	// Recent Sessions Cron Job start date key
	public static final String DEFAULT_RECENT_SESSIONS_SURVIVAL_DURATION_KEY = "ticketsystem.recent.sessions.survival.duration";

	//default is 6 hours behind the current time (6 hours old events or will be deleted)
	public static final int DEFAULT_RECENT_SESSIONS_SURVIVAL_DURATION = 24;
}
