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
package de.hybris.platform.couponwebservices.facades.impl;

import static java.util.stream.Collectors.toList;

import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SortData;
import de.hybris.platform.couponservices.dao.CodeGenerationConfigurationDao;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponwebservices.CodeGenerationConfigurationNotFoundException;
import de.hybris.platform.couponwebservices.dto.CodeGenerationConfigurationWsDTO;
import de.hybris.platform.couponwebservices.facades.CodeGenerationConfigurationWsFacade;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.search.paginated.dao.PaginatedGenericDao;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.cache.annotation.Cacheable;



/**
 * Default implementation of CodeGenerationConfigurationWsFacade for Code Generation Configuration
 */
public class DefaultCodeGenerationConfigurationWsFacade implements CodeGenerationConfigurationWsFacade
{
	private Converter<CodeGenerationConfigurationModel, CodeGenerationConfigurationWsDTO> codeGenerationConfigurationWsDTOConverter;

	private CodeGenerationConfigurationDao codeGenerationConfigurationDao;

	private PaginatedGenericDao<CodeGenerationConfigurationModel> codeGenerationConfigurationPaginatedGenericDao;

	@Override
	public de.hybris.platform.core.servicelayer.data.SearchPageData<CodeGenerationConfigurationWsDTO> getCodeGenerationConfigurations(
			final PaginationData paginationData, final List<SortData> sortData)
	{
		final de.hybris.platform.core.servicelayer.data.SearchPageData<CodeGenerationConfigurationModel> searchPageData = new de.hybris.platform.core.servicelayer.data.SearchPageData<>();
		searchPageData.setPagination(paginationData);
		searchPageData.setSorts(sortData);
		final de.hybris.platform.core.servicelayer.data.SearchPageData<CodeGenerationConfigurationModel> codeGenerationConfigurationSearchPageData = getCodeGenerationConfigurationPaginatedGenericDao()
				.find(searchPageData);
		if (CollectionUtils.isEmpty(codeGenerationConfigurationSearchPageData.getResults()))
		{
			throw new CodeGenerationConfigurationNotFoundException("No Code Generation Configurations found on the System",
					"No Records");
		}
		return convertSearchResults(codeGenerationConfigurationSearchPageData);
	}

	/**
	 * Converts the result of {@link de.hybris.platform.core.servicelayer.data.SearchPageData}
	 *
	 * @param source
	 *           searchPageData containing original results
	 * @return converted SearchPageData
	 */
	protected de.hybris.platform.core.servicelayer.data.SearchPageData<CodeGenerationConfigurationWsDTO> convertSearchResults(
			final de.hybris.platform.core.servicelayer.data.SearchPageData<CodeGenerationConfigurationModel> source)
	{
		final de.hybris.platform.core.servicelayer.data.SearchPageData<CodeGenerationConfigurationWsDTO> result = new de.hybris.platform.core.servicelayer.data.SearchPageData<>();
		result.setPagination(source.getPagination());
		result.setSorts(source.getSorts());
		result.setResults(
				source.getResults().stream().map(getCodeGenerationConfigurationWsDTOConverter()::convert).collect(toList()));
		return result;
	}

	@Override
	@Cacheable(value = "codeGenerationConfigurationWsCache", key = "T(de.hybris.platform.webservicescommons.cache.CacheKeyGenerator).generateKey(false,false,'getCodeGenerationConfigurationWsDTO',#value)")
	public CodeGenerationConfigurationWsDTO getCodeGenerationConfigurationWsDTO(final String codeGenerationConfigurationName)
	{
		final CodeGenerationConfigurationModel codeGenerationConfigurationModel = getCodeGenerationConfigurationDao()
				.findCodeGenerationConfigurationByName(codeGenerationConfigurationName)
				.orElseThrow(() -> new CodeGenerationConfigurationNotFoundException(
						"No Code Generation Configuration found for name [" + codeGenerationConfigurationName + "]", "invalid",
						"codeGenerationConfiguration"));

		return getCodeGenerationConfigurationWsDTOConverter().convert(codeGenerationConfigurationModel);
	}

	protected Converter<CodeGenerationConfigurationModel, CodeGenerationConfigurationWsDTO> getCodeGenerationConfigurationWsDTOConverter()
	{
		return codeGenerationConfigurationWsDTOConverter;
	}

	@Required
	public void setCodeGenerationConfigurationWsDTOConverter(
			final Converter<CodeGenerationConfigurationModel, CodeGenerationConfigurationWsDTO> codeGenerationConfigurationWsDTOConverter)
	{
		this.codeGenerationConfigurationWsDTOConverter = codeGenerationConfigurationWsDTOConverter;
	}

	protected CodeGenerationConfigurationDao getCodeGenerationConfigurationDao()
	{
		return codeGenerationConfigurationDao;
	}

	@Required
	public void setCodeGenerationConfigurationDao(final CodeGenerationConfigurationDao codeGenerationConfigurationDao)
	{
		this.codeGenerationConfigurationDao = codeGenerationConfigurationDao;
	}

	protected PaginatedGenericDao<CodeGenerationConfigurationModel> getCodeGenerationConfigurationPaginatedGenericDao()
	{
		return codeGenerationConfigurationPaginatedGenericDao;
	}

	@Required
	public void setCodeGenerationConfigurationPaginatedGenericDao(
			final PaginatedGenericDao<CodeGenerationConfigurationModel> codeGenerationConfigurationPaginatedGenericDao)
	{
		this.codeGenerationConfigurationPaginatedGenericDao = codeGenerationConfigurationPaginatedGenericDao;
	}
}
