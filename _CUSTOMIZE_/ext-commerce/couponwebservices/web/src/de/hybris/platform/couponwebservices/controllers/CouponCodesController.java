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

import de.hybris.platform.couponwebservices.CouponCodeGenerationWsException;
import de.hybris.platform.couponwebservices.CouponRequestWsError;
import de.hybris.platform.couponwebservices.facades.CouponCodeGenerationWsFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static de.hybris.platform.couponwebservices.constants.CouponwebservicesConstants.DEFAULT_ENC;
import static org.springframework.http.MediaType.parseMediaType;


/**
 * Coupon codes REST API controller
 */
@Controller
@RequestMapping(value = "/couponcodes")
@Api(tags = "CouponCodes")
public class CouponCodesController
{

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	@Resource(name = "couponCodeGenerationWsFacade")
	private CouponCodeGenerationWsFacade couponCodeGenerationWsFacade;

	private static final Logger LOG = LoggerFactory.getLogger(CouponCodesController.class);

	/**
	 * Request to get generated coupon codes for a multi-code coupon with given couponId and mediaCode corresponding to
	 * the code batch
	 *
	 * @param couponId
	 *           the couponId of the requested coupon
	 * @param mediaCode
	 *           the identifier of the requested code batch
	 *
	 * @return Stream of Media data with the requested coupon codes
	 */
	@ApiOperation(value = "Retrieves the generated coupon codes for a multi-code coupon given its media code.", notes = "The media code is the code attribute of the Media ", produces = "application/text")
	@RequestMapping(value = "/{couponId}/{mediaCode}", method = RequestMethod.GET, produces = "application/text")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getGeneratedCouponCodes(
			@ApiParam(value = "the id of the multicode coupon", required = true) @PathVariable("couponId") final String couponId,
			@ApiParam(value = "the code of the generated codes media", required = true) @PathVariable("mediaCode") final String mediaCode)
	{
		final HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		try
		{
			final byte[] dataFromMedia = getCouponCodeGenerationWsFacade().getCouponCodes(couponId,
					URLDecoder.decode(mediaCode, DEFAULT_ENC));
			if (ArrayUtils.isEmpty(dataFromMedia))
			{
				throw new CouponCodeGenerationWsException(
						"No data was found for generated coupon codes: octet byte array is null or empty");
			}
			return ResponseEntity.ok().headers(headers).contentLength(dataFromMedia.length)
					.contentType(parseMediaType(APPLICATION_OCTET_STREAM))
					.body(new InputStreamResource(new ByteArrayInputStream(dataFromMedia)));
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error(e.getMessage(), e);
			throw new CouponRequestWsError(
					"UnsupportedEncodingException was caught trying to decode the url-encoded string [" + mediaCode + "]");
		}
	}

	protected CouponCodeGenerationWsFacade getCouponCodeGenerationWsFacade()
	{
		return couponCodeGenerationWsFacade;
	}
}
