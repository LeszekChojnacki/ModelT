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
package de.hybris.platform.couponwebservices.populators;

import static org.springframework.util.Assert.notNull;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponwebservices.dto.CodeGenerationConfigurationWsDTO;


/**
 * Populator for CodeGenerationConfigurationWsDTO data model
 *
 */
public class CodeGenerationConfigurationWsPopulator
		implements Populator<CodeGenerationConfigurationModel, CodeGenerationConfigurationWsDTO>
{
	@Override
	public void populate(final CodeGenerationConfigurationModel source, final CodeGenerationConfigurationWsDTO target)
	{
		notNull(source, "Parameter source cannot be null.");
		notNull(target, "Parameter target cannot be null.");

		target.setName(source.getName());
		target.setCodeSeparator(source.getCodeSeparator());
		target.setCouponPartCount(source.getCouponPartCount());
		target.setCouponPartLength(source.getCouponPartLength());
	}
}
