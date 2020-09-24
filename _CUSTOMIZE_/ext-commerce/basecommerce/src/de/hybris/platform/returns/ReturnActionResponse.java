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
package de.hybris.platform.returns;

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.List;



/**
 * Represents Return Action responses. Instances of this class can represent:
 * <ul>
 * <li>Responses for approving/cancelling/receiving whole return (all return entries of a ReturnRequest are
 * approved/cancelled/received)</li>
 * <li>Responses for approving/cancelling/receiving only some of the return entries of an ReturnRequest</li> A return
 * entry may be approved/cancelled/received completely (whole return entry is approved/cancelled/received) or partially
 * (i.e. only return entry quantity is reduced).
 *
 * </ul>
 */
public class ReturnActionResponse extends ReturnActionRequest
{

	public enum ResponseStatus
	{
		denied, full, partial, error // NOSONAR
	}

	private ResponseStatus responseStatus;

	public ReturnActionResponse(final ReturnRequestModel returnRequest, final List<ReturnActionEntry> returnActionEntries)
	{
		super(returnRequest, returnActionEntries);
		this.responseStatus = ResponseStatus.partial;
	}

	public ReturnActionResponse(final ReturnRequestModel returnRequest, final List<ReturnActionEntry> returnActionEntries,
			final ResponseStatus status, final String statusMessage)
	{
		super(returnRequest, returnActionEntries, statusMessage);
		this.responseStatus = status;
	}

	public ReturnActionResponse(final ReturnRequestModel returnRequest)
	{
		super(returnRequest);
		this.responseStatus = ResponseStatus.full;
	}

	public ReturnActionResponse(final ReturnRequestModel returnRequest, final ResponseStatus status, final String statusMessage)
	{
		super(returnRequest, CancelReason.NA, statusMessage);
		this.responseStatus = ResponseStatus.full;
		this.responseStatus = status;
	}

	public ResponseStatus getResponseStatus()
	{
		return responseStatus;
	}
}
