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
package de.hybris.platform.couponservices.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;

import de.hybris.platform.couponservices.dao.CodeGenerationConfigurationDao;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;


/**
 * Default implementation of data access model interface for CodeGenerationConfiguration
 */
public class DefaultCodeGenerationConfigurationDao extends DefaultGenericDao<CodeGenerationConfigurationModel>
		implements CodeGenerationConfigurationDao
{

	public DefaultCodeGenerationConfigurationDao()
	{
		super(CodeGenerationConfigurationModel._TYPECODE);
	}

	@Override
	public Optional<CodeGenerationConfigurationModel> findCodeGenerationConfigurationByName(final String name)
	{
		validateParameterNotNull(name, "String name cannot be null");
		final Map<String, String> params = singletonMap("name", name);

		final List<CodeGenerationConfigurationModel> configurationModels = find(params);
		return ofNullable(CollectionUtils.isNotEmpty(configurationModels) ? configurationModels.get(0) : null);
	}


}
