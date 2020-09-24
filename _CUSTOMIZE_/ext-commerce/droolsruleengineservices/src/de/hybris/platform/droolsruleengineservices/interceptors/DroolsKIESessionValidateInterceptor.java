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
package de.hybris.platform.droolsruleengineservices.interceptors;

import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIESessionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Optional;


/**
 * Validate intercepter for DroolsKIESession. Validates that a KIESession has a corresponding KIEBase and KIEModule set.
 */
public class DroolsKIESessionValidateInterceptor implements ValidateInterceptor<DroolsKIESessionModel>
{

	@Override
	public void onValidate(final DroolsKIESessionModel session, final InterceptorContext context) throws InterceptorException
	{
		validateProperties(session);
		final DroolsKIEModuleModel kmodule = session.getKieBase().getKieModule();

		// ensure unique session name and default flag across KIEModule
		if (kmodule.getKieBases() != null)
		{
			validateKieBases(session, kmodule);
		}
		else if (session.getKieBase().getKieSessions() != null)
		{
			// check at least the existing kbase for session uniqueness
			validateKieSessions(session, kmodule);
		}
	}

	protected void validateProperties(final DroolsKIESessionModel session) throws InterceptorException
	{
		if (session.getKieBase() == null)
		{
			throw new InterceptorException("session:" + session.getName() + " must have a DroolsKIEBase set.");
		}
		if (session.getKieBase().getKieModule() == null)
		{
			throw new InterceptorException("DroolsKIEBase: " + session.getKieBase().getName() + " must have DroolsKIEModule set!");
		}
	}

	protected void validateKieBases(final DroolsKIESessionModel session, final DroolsKIEModuleModel kmodule)
			throws InterceptorException
	{
		final String name = session.getName();
		final Optional<DroolsKIESessionModel> existingSession = kmodule.getKieBases().stream()
				.flatMap(b -> b.getKieSessions().stream().filter(s -> name.equals(s.getName()) && !(s.equals(session)))).findAny();
		if (existingSession.isPresent())
		{
			final DroolsKIEBaseModel kieBase = existingSession.get().getKieBase();
			throw new InterceptorException("session with name:" + name + " already defined in DroolsKIEBase " + kieBase.getName()
					+ " of same module:" + kmodule.getName());
		}
	}

	protected void validateKieSessions(final DroolsKIESessionModel session, final DroolsKIEModuleModel kmodule)
			throws InterceptorException
	{
		final String name = session.getName();
		for (final DroolsKIESessionModel s : session.getKieBase().getKieSessions())
		{
			if (name.equals(s.getName()) && !(s.equals(session)))
			{
				throw new InterceptorException("session with name:" + name + " already defined in DroolsKIEBase "
						+ session.getKieBase().getName() + " of same module:" + kmodule.getName());
			}
		}
	}
}
