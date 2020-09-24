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
package de.hybris.platform.fraud.symptom.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.fraud.constants.FrauddetectionConstants;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection;

import java.util.Collections;
import java.util.Set;


/**
 *
 */
public class BlackListSymptom extends AbstractOrderFraudSymptomDetection
{

	private Set<String> bannedEmails;
	private Set<String> bannedUserIDs;

	/**
	 * @return the bannedEmails
	 */
	public Set<String> getBannedEmails()
	{
		return bannedEmails == null ? Collections.emptySet() : bannedEmails;
	}

	/**
	 * @param bannedEmails
	 *           the bannedEmails to set
	 */
	public void setBannedEmails(final Set<String> bannedEmails)
	{
		this.bannedEmails = bannedEmails;
	}

	/**
	 * @return the bannedUserIDs
	 */
	public Set<String> getBannedUserIDs()
	{
		return bannedUserIDs == null ? Collections.emptySet() : bannedUserIDs;
	}

	/**
	 * @param bannedUserIDs
	 *           the bannedUserIDs to set
	 */
	public void setBannedUserIDs(final Set<String> bannedUserIDs)
	{
		this.bannedUserIDs = bannedUserIDs;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.fraud.strategy.AbstractOrderFraudDetectionStrategy#recognizeSymptom(de.hybris.platform.fraud
	 * .impl.FraudServiceResponse, de.hybris.platform.core.model.order.AbstractOrderModel)
	 */
	@Override
	public FraudServiceResponse recognizeSymptom(final FraudServiceResponse fraudResponse, final AbstractOrderModel order)
	{
		boolean foundEmail = false;
		boolean foundUid = false;
		if (getBannedUserIDs().contains(order.getUser().getUid()))
		{
			fraudResponse.addSymptom(new FraudSymptom(getSymptomName() + ":" + FrauddetectionConstants.USERID, getIncrement()));
			foundUid = true;
		}

		for (final AddressModel address : order.getUser().getAddresses())
		{
			if (isUsersCurrentAddress(address) && address.getEmail() != null && getBannedEmails().contains(address.getEmail()))
			{
				fraudResponse.addSymptom(new FraudSymptom(getSymptomName() + ":" + FrauddetectionConstants.EMAIL, getIncrement()));
				foundEmail = true;
			}
		}
		if (!foundEmail && !foundUid)
		{
			fraudResponse.addSymptom(new FraudSymptom(getSymptomName(), 0));
		}
		return fraudResponse;
	}

	protected boolean isUsersCurrentAddress(final AddressModel address)
	{
		return Boolean.FALSE.equals(address.getDuplicate());
	}

}
