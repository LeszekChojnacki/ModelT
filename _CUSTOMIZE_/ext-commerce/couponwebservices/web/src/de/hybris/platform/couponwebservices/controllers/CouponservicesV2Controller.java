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
package de.hybris.platform.couponwebservices.controllers;

import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.core.servicelayer.data.SortData;
import de.hybris.platform.couponwebservices.dto.CodeGenerationConfigurationWsDTO;
import de.hybris.platform.couponwebservices.dto.MultiCodeCouponWsDTO;
import de.hybris.platform.couponwebservices.dto.SingleCodeCouponWsDTO;
import de.hybris.platform.couponwebservices.dto.ws.CodeGenerationConfigurationsSearchPageWsDTO;
import de.hybris.platform.couponwebservices.dto.ws.MultiCodeCouponsSearchPageWsDTO;
import de.hybris.platform.couponwebservices.dto.ws.SingleCodeCouponsSearchPageWsDTO;
import de.hybris.platform.couponwebservices.facades.CodeGenerationConfigurationWsFacade;
import de.hybris.platform.couponwebservices.facades.CouponWsFacades;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;


/**
 * Coupon services controller with 2nd API version
 */
@Controller
@RequestMapping(value = "/couponservices/v2")
@Api(tags = "CouponServices")
public class CouponservicesV2Controller
{
	public static final String DEFAULT_FIELD_SET = "DEFAULT";
	public static final String DEFAULT_CURRENT_PAGE = "0";
	public static final String DEFAULT_PAGE_SIZE = "100";
	public static final String DEFAULT_SORT = "name:asc";

	@Resource(name = "singleCodeCouponWsFacades")
	private CouponWsFacades<SingleCodeCouponWsDTO> singleCodeCouponWsFacades;

	@Resource(name = "multiCodeCouponWsFacades")
	private CouponWsFacades<MultiCodeCouponWsDTO> multiCodeCouponWsFacades;

	@Resource(name = "codeGenerationConfigurationWsFacade")
	private CodeGenerationConfigurationWsFacade codeGenerationConfigurationWsFacade;

	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	@Resource(name = "webPaginationUtils")
	private WebPaginationUtils webPaginationUtils;

	/**
	 * Request to get all single-code coupons registered in the system
	 *
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 * @param currentPage
	 *           number of the current page
	 * @param pageSize
	 *           number of items in a page
	 * @param sort
	 *           sorting the results fields and sorting strategy
	 * @return the list of single-code coupons
	 */
	@ApiOperation(value = "Returns list of single-code coupons", notes = "This endpoint retrieves all of the single-code coupons that are registered in the system", produces = "application/text")
	@RequestMapping(value = "/singlecodecoupon/list", method = RequestMethod.GET)
	@ResponseBody
	public SingleCodeCouponsSearchPageWsDTO getSingleCodeCoupons(
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "Current page number", defaultValue = DEFAULT_CURRENT_PAGE) @RequestParam(required = false, defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@ApiParam(value = "Number of items on a page", defaultValue = DEFAULT_PAGE_SIZE) @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@ApiParam(value = "Type of sorting to be applied to the retrieved set", defaultValue = DEFAULT_SORT, allowableValues = "asc, desc") @RequestParam(required = false, defaultValue = DEFAULT_SORT) final String sort)
	{
		final List<SortData> sortData = getWebPaginationUtils().buildSortData(sort);
		final PaginationData paginationData = getWebPaginationUtils().buildPaginationData(currentPage,pageSize);
		final de.hybris.platform.core.servicelayer.data.SearchPageData<SingleCodeCouponWsDTO> couponsSearchPage = getSingleCodeCouponWsFacades()
				.getCoupons(paginationData, sortData);
		return getDataMapper().map(couponsSearchPage, SingleCodeCouponsSearchPageWsDTO.class, fields);
	}

	/**
	 * Request to get all multi-code coupons registered in the system
	 *
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 * @param currentPage
	 *           number of the current page
	 * @param pageSize
	 *           number of items in a page
	 * @param sort
	 *           sorting the results ascending or descending
	 * @return the list of multi-code coupons
	 */
	@ApiOperation(value = "Returns list of multi-code coupons", notes = "This endpoint retrieves all multi-code coupons that are registered in the system", produces = "application/text")
	@RequestMapping(value = "/multicodecoupon/list", method = RequestMethod.GET)
	@ResponseBody
	public MultiCodeCouponsSearchPageWsDTO getMultiCodeCoupons(
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "Current page number", defaultValue = DEFAULT_CURRENT_PAGE) @RequestParam(required = false, defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@ApiParam(value = "Number of items on a page", defaultValue = DEFAULT_PAGE_SIZE) @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@ApiParam(value = "Type of sorting to be applied to the retrieved set", defaultValue = DEFAULT_SORT, allowableValues = "asc, desc") @RequestParam(required = false, defaultValue = DEFAULT_SORT) final String sort)
	{
		final List<SortData> sortData = getWebPaginationUtils().buildSortData(sort);
		final PaginationData paginationData = getWebPaginationUtils().buildPaginationData(currentPage,pageSize);
		final SearchPageData<MultiCodeCouponsSearchPageWsDTO> couponsSearchPage = getMultiCodeCouponWsFacades()
				.getCoupons(paginationData, sortData);
		return getDataMapper().map(couponsSearchPage, MultiCodeCouponsSearchPageWsDTO.class, fields);

	}

	/**
	 * Request to get all code generation configuration registered in the system
	 *
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 * @param currentPage
	 *           number of the current page
	 * @param pageSize
	 *           number of items in a page
	 * @param sort
	 *           sorting the results ascending or descending
	 * @return the list of code generation configuration
	 */
	@ApiOperation(value = "Gets list of code generation configurations", notes = "This endpoint retrieves all the code generation configurations that are registered in the system", produces = "application/text")
	@RequestMapping(value = "/codegenerationconfiguration/list", method = RequestMethod.GET)
	@ResponseBody
	public CodeGenerationConfigurationsSearchPageWsDTO getCodeGenerationConfigurations(
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "Current page number", defaultValue = DEFAULT_CURRENT_PAGE) @RequestParam(required = false, defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@ApiParam(value = "Number of items on a page", defaultValue = DEFAULT_PAGE_SIZE) @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@ApiParam(value = "Type of sorting to be applied to the retrieved set", defaultValue = DEFAULT_SORT, allowableValues = "asc, desc") @RequestParam(required = false, defaultValue = DEFAULT_SORT) final String sort)
	{
		final List<SortData> sortData = getWebPaginationUtils().buildSortData(sort);
		final PaginationData paginationData = getWebPaginationUtils().buildPaginationData(currentPage,pageSize);
		final SearchPageData<CodeGenerationConfigurationWsDTO> couponsSearchPage = getCodeGenerationConfigurationWsFacade()
				.getCodeGenerationConfigurations(paginationData,sortData);
		return dataMapper.map(couponsSearchPage, CodeGenerationConfigurationsSearchPageWsDTO.class, fields);
	}


	protected DataMapper getDataMapper()
	{
		return dataMapper;
	}

	protected CouponWsFacades<SingleCodeCouponWsDTO> getSingleCodeCouponWsFacades()
	{
		return singleCodeCouponWsFacades;
	}

	protected CouponWsFacades<MultiCodeCouponWsDTO> getMultiCodeCouponWsFacades()
	{
		return multiCodeCouponWsFacades;
	}

	protected WebPaginationUtils getWebPaginationUtils()
	{
		return webPaginationUtils;
	}

	protected CodeGenerationConfigurationWsFacade getCodeGenerationConfigurationWsFacade()
	{
		return codeGenerationConfigurationWsFacade;
	}
}
