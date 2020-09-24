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

import static de.hybris.platform.couponwebservices.constants.CouponwebservicesConstants.DEFAULT_ENC;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.couponwebservices.CouponCodeGenerationWsException;
import de.hybris.platform.couponwebservices.CouponRequestWsError;
import de.hybris.platform.couponwebservices.dto.CodeGenerationConfigurationWsDTO;
import de.hybris.platform.couponwebservices.dto.CouponGeneratedCodeWsDTO;
import de.hybris.platform.couponwebservices.dto.CouponRedemptionWsDTO;
import de.hybris.platform.couponwebservices.dto.CouponStatusWsDTO;
import de.hybris.platform.couponwebservices.dto.CouponValidationResponseWsDTO;
import de.hybris.platform.couponwebservices.dto.MultiCodeCouponWsDTO;
import de.hybris.platform.couponwebservices.dto.SingleCodeCouponWsDTO;
import de.hybris.platform.couponwebservices.facades.CodeGenerationConfigurationWsFacade;
import de.hybris.platform.couponwebservices.facades.CouponCodeGenerationWsFacade;
import de.hybris.platform.couponwebservices.facades.CouponRedemptionWsFacade;
import de.hybris.platform.couponwebservices.facades.CouponWsFacades;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Couponservices Controller
 */
@Controller
@RequestMapping(value = "/couponservices")
@Api(tags = "CouponServices")
public class CouponservicesController
{

	public static final String DEFAULT_FIELD_SET = "DEFAULT";
	public static final String DEFAULT_CURRENT_PAGE = "0";
	public static final String DEFAULT_PAGE_SIZE = "100";
	public static final String DEFAULT_SORT = "asc";
	public static final String URL_V2 = "/v2";

	@Resource(name = "singleCodeCouponWsFacades")
	private CouponWsFacades<SingleCodeCouponWsDTO> singleCodeCouponWsFacades;

	@Resource(name = "multiCodeCouponWsFacades")
	private CouponWsFacades<MultiCodeCouponWsDTO> multiCodeCouponWsFacades;

	@Resource(name = "couponCodeGenerationWsFacade")
	private CouponCodeGenerationWsFacade couponCodeGenerationWsFacade;

	@Resource(name = "couponRedemptionWsFacade")
	private CouponRedemptionWsFacade couponRedemptionWsFacade;

	@Resource(name = "codeGenerationConfigurationWsFacade")
	private CodeGenerationConfigurationWsFacade codeGenerationConfigurationWsFacade;

	@Resource(name = "singleCodeCouponWsDTOValidator")
	private Validator singleCodeCouponWsDTOValidator;

	@Resource(name = "multiCodeCouponWsDTOValidator")
	private Validator multiCodeCouponWsDTOValidator;

	@Resource(name = "couponStatusWsDTOValidator")
	private Validator couponStatusWsDTOValidator;

	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	private static final Logger LOG = LoggerFactory.getLogger(CouponservicesController.class);

	/**
	 * Method to create a new single-code coupon object given in POST body parameter <br/>
	 * Example :</br>
	 * URL : http://localhost:9001/couponwebservices/couponservices/v2/singlecodecoupon </br>
	 * Method : POST</br>
	 * Header : Content-Type=application/json</br>
	 * POST body parameter :{ "couponId" : "TEST_COUPON1", "name":"test_coupon", "maxRedemptionsPerCustomer":"2",
	 * "maxTotalRedemptions":"10" }</br>
	 *
	 * @param couponWsDTO
	 *           - Request body parameter (DTO in xml or json format)</br>
	 * @return - instance of SingleCodeCouponWsDTO, that was saved in the system</br>
	 *
	 */
	@ApiOperation(value = "Creates single-code coupon entity", notes = "This endpoint creates a new single-code coupon object with the parameters provided in POST body", produces = "application/text", consumes = "application/json, application/xml")
	@RequestMapping(value =
	{ URL_V2 + "/singlecodecoupon/create" }, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public SingleCodeCouponWsDTO createSingleCodeCouponWsDTO(
			@ApiParam(value = "Object that contains data about to-be-created coupon, such as 'startDate', 'endDate', 'couponId', 'name' etc", required = true, example = "{ \"couponId\" : \"TEST_COUPON1\", \"name\":\"test_coupon\", \"maxRedemptionsPerCustomer\":\"2\", \"maxTotalRedemptions\":\"10\" }") @RequestBody final SingleCodeCouponWsDTO couponWsDTO)
	{
		validate(couponWsDTO, "singleCodeCouponWsDTO", getSingleCodeCouponWsDTOValidator());
		return getSingleCodeCouponWsFacades().createCoupon(couponWsDTO);
	}

	/**
	 * Method to update a single-code coupon status<br/>
	 * Example :</br>
	 * URL : http://localhost:9001/couponwebservices/couponservices/v2/singlecodecoupon/update/status </br>
	 * Method : PUT</br>
	 * PUT body parameters </br>
	 *
	 * @param couponStatusWsDTO
	 *           - request object containing couponId and active attributes
	 */
	@RequestMapping(value =
	{ URL_V2 + "/singlecodecoupon/update/status" }, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	@ApiOperation(value = "Updates status of single-code coupon", notes = "This endpoint updates status of a single-code coupon as provided in POST body", consumes = "application/json, application/xml")
	public void updateSingleCodeStatusCouponWsDTO(
			@ApiParam(value = "Object that contains coupon data whose status needs to be updated, such as 'couponId', 'active'", required = true) @RequestBody final CouponStatusWsDTO couponStatusWsDTO)
	{
		validate(couponStatusWsDTO, "couponStatusWsDTO", getCouponStatusWsDTOValidator());
		getSingleCodeCouponWsFacades().updateCouponStatus(couponStatusWsDTO.getCouponId(), couponStatusWsDTO.getActive());
	}

	/**
	 * Method to update a multi-code coupon status<br/>
	 * Example :</br>
	 * URL : http://localhost:9001/couponwebservices/couponservices/v2/multicodecoupon/update/status </br>
	 * Method : PUT</br>
	 * PUT body parameters </br>
	 *
	 * @param couponStatusWsDTO
	 *           - request object containing couponId and active attributes
	 *
	 */
	@RequestMapping(value =
	{ URL_V2 + "/multicodecoupon/update/status" }, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	@ApiOperation(value = "Updates status of multi-code coupon", notes = "This endpoint updates status of a multi-code coupon as provided in POST body", consumes = "application/json, application/xml")
	public void updateMultiCodeStatusCouponWsDTO(
			@ApiParam(value = "Object that contains coupon data whose status needs to be updated, such as 'couponId', 'active'", required = true) @RequestBody final CouponStatusWsDTO couponStatusWsDTO)
	{
		validate(couponStatusWsDTO, "couponStatusWsDTO", getCouponStatusWsDTOValidator());
		getMultiCodeCouponWsFacades().updateCouponStatus(couponStatusWsDTO.getCouponId(), couponStatusWsDTO.getActive());
	}

	/**
	 * Method to update a single-code coupon object given in PUT body parameter <br/>
	 * Example :</br>
	 * URL : http://localhost:9001/couponwebservices/couponservices/v2/singlecodecoupon/update </br>
	 * Method : PUT</br>
	 * Header : Content-Type=application/json</br>
	 * PUT body parameter :{ "couponId" : "TEST_COUPON1", "name":"test_coupon", "maxRedemptionsPerCustomer":"2",
	 * "maxTotalRedemptions":"10" }</br>
	 *
	 * @param couponWsDTO
	 *           - Request body parameter (DTO in xml or json format)</br>
	 *
	 */
	@RequestMapping(value =
	{ URL_V2 + "/singlecodecoupon/update" }, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	@ApiOperation(value = "Updates a single-code coupon", notes = "This endpoint updates single-code coupon data as provided in PUT body", consumes = "application/json, application/xml")
	public void updateSingleCodeCouponWsDTO(
			@ApiParam(value = "Request body parameter (DTO in xml or json format)", required = true, example = "{ \"couponId\" : \"TEST_COUPON1\", \"name\":\"test_coupon\", \"maxRedemptionsPerCustomer\":\"2\", \"maxTotalRedemptions\":\"10\" }") @RequestBody final SingleCodeCouponWsDTO couponWsDTO)
	{
		validate(couponWsDTO, "singleCodeCouponWsDTO", getSingleCodeCouponWsDTOValidator());
		getSingleCodeCouponWsFacades().updateCoupon(couponWsDTO);
	}

	/**
	 * Method to update a multi-code coupon object given in PUT body parameter <br/>
	 * Example :</br>
	 * URL : http://localhost:9001/couponwebservices/couponservices/v2/multicodecoupon/update </br>
	 * Method : PUT</br>
	 * Header : Content-Type=application/json</br>
	 * PUT body parameter :{ "couponId" : "TEST_COUPON1", "name":"test_coupon" }</br>
	 *
	 * @param couponWsDTO
	 *           - Request body parameter (DTO in xml or json format)</br>
	 *
	 */
	@RequestMapping(value =
	{ URL_V2 + "/multicodecoupon/update" }, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	@ApiOperation(value = "Updates a multi-code coupon", notes = "This endpoint updates multi-code coupon data as provided in PUT body", consumes = "application/json, application/xml")
	public void updateMultiCodeCouponWsDTO(
			@ApiParam(value = "Request body parameter (DTO in xml or json format)", required = true, example = "{ \"couponId\" : \"TEST_COUPON1\", \"name\":\"test_coupon\", \"maxRedemptionsPerCustomer\":\"2\", \"maxTotalRedemptions\":\"10\" }") @RequestBody final MultiCodeCouponWsDTO couponWsDTO)
	{
		validate(couponWsDTO, "multiCodeCouponWsDTO", getMultiCodeCouponWsDTOValidator());
		getMultiCodeCouponWsFacades().updateCoupon(couponWsDTO);
	}

	/**
	 * Method to create a new multi-code coupon object given in POST body parameter <br/>
	 * Example :</br>
	 * URL : http://localhost:9001/couponwebservices/couponservices/v2/multicodecoupon </br>
	 * Method : POST</br>
	 * Header : Content-Type=application/json</br>
	 * POST body parameter :{ "couponId" : "COUPON123", "name":"test_multi_coupon", }</br>
	 *
	 * @param couponWsDTO
	 *           - Request body parameter (DTO in xml or json format)</br>
	 * @return - instance of {@link MultiCodeCouponWsDTO}, that was saved in the system</br>
	 *
	 */
	@RequestMapping(value =
	{ URL_V2 + "/multicodecoupon/create" }, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiOperation(value = "Creates a multi-code coupon", notes = "This endpoint creates a new multi-code coupon given in POST body", produces = "application/text", consumes = "application/json, application/xml")
	public MultiCodeCouponWsDTO createMultiCodeCouponWsDTO(
			@ApiParam(value = "Request body parameter (DTO in xml or json format)", required = true, example = "{ \"couponId\" : \"COUPON123\", \"name\":\"test_multi_coupon\"}") @RequestBody final MultiCodeCouponWsDTO couponWsDTO)
	{
		validate(couponWsDTO, "multiCodeCouponWsDTO", getMultiCodeCouponWsDTOValidator());
		return getMultiCodeCouponWsFacades().createCoupon(couponWsDTO);
	}

	/**
	 * Request to get a single-code coupon with given couponId
	 *
	 * @param couponId
	 *           the couponId of the requested coupon
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 *
	 * @return instance of {@link SingleCodeCouponWsDTO}, corresponding to {@code couponId}
	 */
	@RequestMapping(value =
	{ URL_V2 + "/singlecodecoupon/get/{couponId}" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Gets a single-code coupon", notes = "This endpoint retrieves a single-code coupon for a given couponId", produces = "application/text")
	public SingleCodeCouponWsDTO getSingleCodeCoupon(
			@ApiParam(value = "The couponId of the requested coupon", required = true) @PathVariable final String couponId,
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final SingleCodeCouponWsDTO coupon = getSingleCodeCouponWsFacades().getCouponWsDTO(couponId);
		return getDataMapper().map(coupon, SingleCodeCouponWsDTO.class, fields);
	}

	/**
	 * Request to validate a single-code coupon with given couponId
	 *
	 * @param couponId
	 *           the couponId of the requested coupon
	 * @param customerId
	 *           the user id
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 *
	 * @return instance of {@link SingleCodeCouponWsDTO}, corresponding to {@code couponId}
	 */
	@RequestMapping(value =
	{ URL_V2 + "/singlecodecoupon/validate/{couponId}" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Validates a single-code coupon", notes = "This endpoint validates a single-code coupon with a given couponId", produces = "application/text")
	public CouponValidationResponseWsDTO validateSingleCodeCoupon(
			@ApiParam(value = "The couponId of the coupon to validate", required = true) @PathVariable final String couponId,
			@ApiParam(value = "The customerId") @RequestParam(required = false) final String customerId,
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CouponValidationResponseWsDTO coupon = getSingleCodeCouponWsFacades().validateCoupon(couponId, customerId);
		return getDataMapper().map(coupon, CouponValidationResponseWsDTO.class, fields);
	}

	/**
	 * Request to get a multi-code coupon with given couponId
	 *
	 * @param couponId
	 *           the couponId of the requested coupon
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 *
	 * @return instance of {@link MultiCodeCouponWsDTO}, corresponding to {@code couponId}
	 */
	@RequestMapping(value =
	{ URL_V2 + "/multicodecoupon/get/{couponId}" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Retrieves a multi-code coupon", notes = "This endpoint retrieves a multi-code coupon for a given couponId", produces = "application/text")
	public MultiCodeCouponWsDTO getMultiCodeCoupon(
			@ApiParam(value = "The couponId of the requested coupon", required = true) @PathVariable final String couponId,
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final MultiCodeCouponWsDTO couponDto = getMultiCodeCouponWsFacades().getCouponWsDTO(couponId);
		final Collection<MediaModel> couponCodeBatches = getCouponCodeGenerationWsFacade().getCouponCodeBatches(couponId);
		if (isNotEmpty(couponCodeBatches))
		{
			final List<CouponGeneratedCodeWsDTO> generatedCodes = couponCodeBatches.stream()
					.map(media -> createCouponGeneratedCodeWsDTO(couponId, media)).collect(toList());
			couponDto.setGeneratedCodes(generatedCodes);
		}
		return getDataMapper().map(couponDto, MultiCodeCouponWsDTO.class, fields);
	}

	protected CouponGeneratedCodeWsDTO createCouponGeneratedCodeWsDTO(final String couponId, final MediaModel mediaModel)
	{
		final CouponGeneratedCodeWsDTO couponGeneratedCodeWsDTO = new CouponGeneratedCodeWsDTO();
		final String generatedCodesMediaCode = mediaModel.getCode();
		couponGeneratedCodeWsDTO.setCode(mediaModel.getCode());
		try
		{
			couponGeneratedCodeWsDTO.setLink(linkTo(CouponCodesController.class).slash(couponId)
					.slash(URLEncoder.encode(generatedCodesMediaCode, DEFAULT_ENC)).toUri().toString());
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error(e.getMessage(), e);
			throw new CouponRequestWsError(
					"UnsupportedEncodingException was caught trying to encode the string [" + generatedCodesMediaCode + "]");
		}
		return couponGeneratedCodeWsDTO;
	}

	/**
	 * Request to validate a multi-code coupon with given couponId
	 *
	 * @param couponCode
	 *           the couponCode of the requested coupon
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 *
	 * @return instance of {@link CouponValidationResponseWsDTO}, corresponding to {@code couponCode}
	 */
	@RequestMapping(value =
	{ URL_V2 + "/multicodecoupon/validate/{couponCode}" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Validates a single-code coupon", notes = "This endpoint validates a single-code coupon with a given couponId", produces = "application/text")
	public CouponValidationResponseWsDTO validateMultiCodeCoupon(
			@ApiParam(value = "The couponId of the coupon to validate", required = true) @PathVariable final String couponCode,
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CouponValidationResponseWsDTO coupon = getMultiCodeCouponWsFacades().validateCoupon(couponCode);
		return getDataMapper().map(coupon, CouponValidationResponseWsDTO.class, fields);
	}


	/**
	 * Generate multi-code coupon codes
	 *
	 * @param couponId
	 *           the couponId of related MultiCodeCoupon
	 * @param batchsize
	 *           batch size for generated coupons
	 *
	 * @return a link to a generated coupons resource
	 */
	@RequestMapping(value =
	{ URL_V2 + "/multicodecoupon/generate/{couponId}/{batchsize}" }, method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Generate multi-code coupon codes", notes = "This endpoint generates a batch of the multi-code coupon codes for a provided couponId and batch size", produces = "application/text")
	public HttpEntity<HttpHeaders> generateCouponCodes(
			@ApiParam(value = "The couponId of related multi-code coupon", required = true) @PathVariable("couponId") final String couponId,
			@ApiParam(value = "Batch size for generated coupons", required = true) @PathVariable("batchsize") final int batchsize)
	{
		final MediaModel generatedCodesMedia = getCouponCodeGenerationWsFacade().generateCouponCodes(couponId, batchsize)
				.orElseThrow(() -> new CouponCodeGenerationWsException("No generated coupon codes found in the system"));
		try
		{
			final String generatedCodesMediaCode = URLEncoder.encode(generatedCodesMedia.getCode(), DEFAULT_ENC);
			final HttpHeaders headers = new HttpHeaders();
			headers.setLocation(linkTo(CouponCodesController.class).slash(couponId).slash(generatedCodesMediaCode).toUri());

			return new ResponseEntity(headers, HttpStatus.CREATED);
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error(e.getMessage(), e);
			throw new CouponRequestWsError(
					"UnsupportedEncodingException was caught trying to encode the string [" + generatedCodesMedia.getCode() + "]");
		}
	}


	/**
	 * Request to get a redemption status for a single-code coupon with given couponId
	 *
	 * @param couponId
	 *           the couponId of the requested coupon
	 * @param customerId
	 *           the user id
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 *
	 * @return instance of {@link CouponRedemptionWsDTO}, corresponding to {@code couponId}
	 */
	@RequestMapping(value =
	{ URL_V2 + "/singlecodecouponredemption/get/{couponId}" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Gets redemption status for a single-code coupon", notes = "This endpoint provides redemption status for a single-code coupon with given couponId", produces = "application/text")
	public CouponRedemptionWsDTO getSingleCodeCouponRedemption(
			@ApiParam(value = "The couponId of the requested coupon", required = true) @PathVariable final String couponId,
			@ApiParam(value = "The user id", required = true) @RequestParam(required = false) final String customerId,
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CouponRedemptionWsDTO couponRedemptionDto = getCouponRedemptionWsFacade().getSingleCodeCouponRedemption(couponId,
				customerId);
		return getDataMapper().map(couponRedemptionDto, CouponRedemptionWsDTO.class, fields);
	}

	/**
	 * Request to get a single-code coupon with given couponId
	 *
	 * @param codeGenerationConfigurationName
	 *           the codeGenerationConfigurationName of the requested codeGenerationConfiguration
	 * @param fields
	 *           defaulted to DEFAULT but can be FULL or BASIC
	 *
	 * @return instance of {@link CodeGenerationConfigurationWsDTO}, corresponding to
	 *         {@code codeGenerationConfigurationName}
	 */
	@RequestMapping(value =
	{ URL_V2 + "/codegenerationconfiguration/get/{codeGenerationConfigurationName}" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Gets a code generation configuration", notes = "This endpoint retrieves a single code generation configuration for a given couponId", produces = "application/text")
	public CodeGenerationConfigurationWsDTO getCodeGenerationConfiguration(
			@ApiParam(value = "The codeGenerationConfigurationName of the requested codeGenerationConfiguration", required = true) @PathVariable final String codeGenerationConfigurationName,
			@ApiParam(value = "Fields to retrieve", defaultValue = DEFAULT_FIELD_SET, allowableValues = "DEFAULT, BASIC, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CodeGenerationConfigurationWsDTO codeGenerationConfiguration = getCodeGenerationConfigurationWsFacade()
				.getCodeGenerationConfigurationWsDTO(codeGenerationConfigurationName);
		return dataMapper.map(codeGenerationConfiguration, CodeGenerationConfigurationWsDTO.class, fields);
	}

	/**
	 * Validates the object by using the passed validator
	 *
	 * @param object
	 *           the object ot be validated
	 * @param objectName
	 *           the object name
	 * @param validator
	 *           validator which will validate the object
	 */
	protected void validate(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			throw new WebserviceValidationException(errors);
		}
	}


	protected DataMapper getDataMapper()
	{
		return dataMapper;
	}

	protected CouponWsFacades<SingleCodeCouponWsDTO> getSingleCodeCouponWsFacades()
	{
		return singleCodeCouponWsFacades;
	}

	protected Validator getSingleCodeCouponWsDTOValidator()
	{
		return singleCodeCouponWsDTOValidator;
	}

	protected Validator getCouponStatusWsDTOValidator()
	{
		return couponStatusWsDTOValidator;
	}

	protected Validator getMultiCodeCouponWsDTOValidator()
	{
		return multiCodeCouponWsDTOValidator;
	}

	protected CouponWsFacades<MultiCodeCouponWsDTO> getMultiCodeCouponWsFacades()
	{
		return multiCodeCouponWsFacades;
	}

	protected CouponCodeGenerationWsFacade getCouponCodeGenerationWsFacade()
	{
		return couponCodeGenerationWsFacade;
	}

	protected CouponRedemptionWsFacade getCouponRedemptionWsFacade()
	{
		return couponRedemptionWsFacade;
	}

	protected CodeGenerationConfigurationWsFacade getCodeGenerationConfigurationWsFacade()
	{
		return codeGenerationConfigurationWsFacade;
	}
}
