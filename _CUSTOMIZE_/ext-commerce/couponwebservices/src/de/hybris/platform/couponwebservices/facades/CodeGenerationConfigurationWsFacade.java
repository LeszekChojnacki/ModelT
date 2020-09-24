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
package de.hybris.platform.couponwebservices.facades;

import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SortData;
import de.hybris.platform.couponwebservices.dto.CodeGenerationConfigurationWsDTO;

import java.util.List;


/**
 * Interface declaring basic web-services facade operations on Code Generation Configuration
 *
 */
public interface CodeGenerationConfigurationWsFacade
{
	/**
	 * Given the codeGenerationConfiguration name string, returns the CodeGenerationConfiguration DTO, otherwise throws
	 * exception
	 *
	 * @param codeGenerationConfigurationName
	 *           a string to be used to find a codeGenerationConfiguration
	 * @return - an instance of CodeGenerationConfiguration DTO
	 */
	CodeGenerationConfigurationWsDTO getCodeGenerationConfigurationWsDTO(final String codeGenerationConfigurationName);

	/**
	 * Returns a restricted size list (page) of available Code Generation Configuration
	 *
	 * @param paginationData
	 *           Pagination instrumentation
	 * @param sortData
	 * 			 Sorting instrumentation
	 * @return - an instance of SearchPageData containing a list of Code Generation Configuration, registered in the
	 *         system
	 */
	de.hybris.platform.core.servicelayer.data.SearchPageData<CodeGenerationConfigurationWsDTO> getCodeGenerationConfigurations(
			final PaginationData paginationData,final List<SortData> sortData);
}
