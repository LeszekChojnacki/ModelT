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
package de.hybris.platform.ruleengine.versioning.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Long.parseLong;
import static java.util.Optional.ofNullable;

import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersionResolver;

import java.util.Optional;


/**
 * Default implementation of {@link de.hybris.platform.ruleengine.versioning.ModuleVersionResolver}) for
 * {@link de.hybris.platform.ruleengine.model.DroolsKIEModuleModel}) type
 */
public class DroolsKieModuleVersionResolver implements ModuleVersionResolver<DroolsKIEModuleModel>
{
	@Override
	public Optional<Long> getDeployedModuleVersion(final DroolsKIEModuleModel rulesModule)
	{
		checkNotNull(rulesModule, "The instance of DroolsKIEModuleModel must not be null here");

		return ofNullable(rulesModule.getDeployedMvnVersion()).map(v -> extractModuleVersion(rulesModule.getName(), v));
	}

	@Override
	public Long extractModuleVersion(final String moduleName, final String deployedMvnVersion)
	{
		Long deployedModuleVersion = null;
		try
		{
			final int idx = deployedMvnVersion.lastIndexOf('.');
			if (idx != -1 && deployedMvnVersion.length() > (idx + 1))
			{
				deployedModuleVersion = parseLong(deployedMvnVersion.substring(idx + 1).trim());
			}
		}
		catch (final RuntimeException e)
		{
			throw new IllegalArgumentException("Error during the deployed version of module [" + moduleName + "] occurred: ", e);
		}
		return deployedModuleVersion;
	}

}
