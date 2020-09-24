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
package de.hybris.platform.adaptivesearchfacades.facades.impl;

import de.hybris.adaptivesearchfacades.data.AsSearchProfileData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileService;
import de.hybris.platform.adaptivesearchfacades.facades.AsSearchProfileFacade;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation for {@link AsSearchProfileFacade}
 */
public class DefaultAsSearchProfileFacade implements AsSearchProfileFacade
{
	public static final String COMMA_SEPARATOR = ",";
	public static final String CATALOG_VERSIONS_FILTER = "catalogVersions";
	public static final String INDEX_TYPES_FILTER = "indexTypes";

	private AsSearchProfileService asSearchProfileService;
	private CatalogVersionService catalogVersionService;
	private UserService userService;

	private Converter<AbstractAsSearchProfileModel, AsSearchProfileData> asSearchProfileDataConverter;

	@Override
	public List<AsSearchProfileData> getSearchProfiles(final String query, final Map<String, String> filters)
	{
		final Map<String, Object> paramsMap = buildParameters(filters);

		if (currentUserReadableCatalogVersionParamsFromFilterIsEmpty(filters, paramsMap)){
			return Collections.emptyList();
		}

		final List<AbstractAsSearchProfileModel> searchProfiles = asSearchProfileService.getSearchProfiles(query, paramsMap);

		return asSearchProfileDataConverter.convertAll(searchProfiles);
	}

	@Override
	public SearchPageData<AsSearchProfileData> getSearchProfiles(final String query, final Map<String, String> filters, final SearchPageData<?> pagination)
	{
		final Map<String, Object> parameters = buildParameters(filters);

		if (currentUserReadableCatalogVersionParamsFromFilterIsEmpty(filters, parameters)){
			return createEmptyAsSearchPageData(pagination);
		}

		final SearchPageData<AbstractAsSearchProfileModel> searchProfiles = asSearchProfileService.getSearchProfiles(query, parameters, pagination);

		return createAsSearchPageData(searchProfiles);
	}

	protected boolean currentUserReadableCatalogVersionParamsFromFilterIsEmpty(final Map<String, String> filters, final Map<String, Object> paramsMap){
		return StringUtils.isNotBlank(filters.get(CATALOG_VERSIONS_FILTER))
				&& CollectionUtils.isEmpty((Collection) paramsMap.get(AbstractAsSearchProfileModel.CATALOGVERSION));
	}

	protected SearchPageData<AsSearchProfileData> createAsSearchPageData(final SearchPageData<AbstractAsSearchProfileModel> input){
		final List<AsSearchProfileData> asSearchProfileData = asSearchProfileDataConverter.convertAll(input.getResults());

		final SearchPageData<AsSearchProfileData> result = new SearchPageData<>();

		result.setPagination(input.getPagination());
		result.setSorts(input.getSorts());
		result.setResults(asSearchProfileData);

		return result;
	}

	protected SearchPageData<AsSearchProfileData> createEmptyAsSearchPageData(final SearchPageData<?> pagination){
		final SearchPageData<AsSearchProfileData> result = new SearchPageData<>();
		result.setResults(Collections.emptyList());
		result.setPagination(pagination.getPagination());

		return result;
	}

	protected Map<String, Object> buildParameters(final Map<String, String> filters){

		final Map<String, Object> parameters = new HashMap();

		final String catalogVersionsFilter = filters.get(CATALOG_VERSIONS_FILTER);
		if (StringUtils.isNotBlank(catalogVersionsFilter))
		{
			parameters.put(AbstractAsSearchProfileModel.CATALOGVERSION, getCurrentUserReadableCatalogVersionsFromFilter(catalogVersionsFilter));
		}

		final String indexTypesFilter = filters.get(INDEX_TYPES_FILTER);
		if (StringUtils.isNotBlank(indexTypesFilter))
		{
			parameters.put(AbstractAsSearchProfileModel.INDEXTYPE, getIndexTypesFromFilter(indexTypesFilter));
		}

		return parameters;
	}

	protected Collection<CatalogVersionModel> getCurrentUserReadableCatalogVersions(){
		final UserModel userModel = userService.getCurrentUser();
		return catalogVersionService.getAllReadableCatalogVersions(userModel);
	}

	protected List<CatalogVersionModel> getCurrentUserReadableCatalogVersionsFromFilter(final String catalogVersions){
		final List<String> filterCatalogVersions = Arrays.asList(catalogVersions.split(COMMA_SEPARATOR));

		return getCurrentUserReadableCatalogVersions()
				.stream()
				.filter( cv -> {
					final String cvStr = cv.getCatalog().getId() + ":" + cv.getVersion();
					return 	filterCatalogVersions.contains(cvStr);
				}).collect(Collectors.toList());
	}


	protected List<String> getIndexTypesFromFilter(final String indexTypes){
		return Arrays.asList(indexTypes.split(COMMA_SEPARATOR));
	}

	public AsSearchProfileService getAsSearchProfileService() {
		return asSearchProfileService;
	}

	@Required
	public void setAsSearchProfileService(final AsSearchProfileService asSearchProfileService)
	{
		this.asSearchProfileService = asSearchProfileService;
	}

	public CatalogVersionService getCatalogVersionService() {
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
		this.catalogVersionService = catalogVersionService;
	}

	public UserService getUserService() {
		return userService;
	}

	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public Converter<AbstractAsSearchProfileModel, AsSearchProfileData> getAsSearchProfileDataConverter() {
		return asSearchProfileDataConverter;
	}
	@Required
	public void setAsSearchProfileDataConverter(
			final Converter<AbstractAsSearchProfileModel, AsSearchProfileData> asSearchProfileDataConverter)
	{
		this.asSearchProfileDataConverter = asSearchProfileDataConverter;
	}
}
