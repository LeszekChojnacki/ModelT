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
import de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection;

import java.util.Collections;
import java.util.Set;


/**
 *
 */
public class WhiteListSymptom extends AbstractOrderFraudSymptomDetection
{
	private Set<String> favoredEmails;
	private Set<String> favoredUserIDs;

	/**
	 * @return the favoredEmails
	 */
	public Set<String> getFavoredEmails()
	{
		return favoredEmails == null ? Collections.emptySet() : favoredEmails;
	}

	/**
	 * @param favoredEmails
	 *           the favoredEmails to set
	 */
	public void setFavoredEmails(final Set<String> favoredEmails)
	{
		this.favoredEmails = favoredEmails;
	}

	/**
	 * @return the favoredUserIDs
	 */
	public Set<String> getFavoredUserIDs()
	{
		return favoredUserIDs == null ? Collections.emptySet() : favoredUserIDs;
	}

	/**
	 * @param favoredUserIDs
	 *           the favoredUserIDs to set
	 */
	public void setFavoredUserIDs(final Set<String> favoredUserIDs)
	{
		this.favoredUserIDs = favoredUserIDs;
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

		if (getFavoredUserIDs().contains(order.getUser().getUid()))
		{
			fraudResponse.addSymptom(createSymptom(getSymptomName() + ":" + FrauddetectionConstants.USERID, true));
			foundUid = true;
		}

		for (final AddressModel address : order.getUser().getAddresses())
		{
			if (address.getEmail() != null && getFavoredEmails().contains(address.getEmail()))
			{
				fraudResponse.addSymptom(createSymptom(getSymptomName() + ":" + FrauddetectionConstants.EMAIL, true));
				foundEmail = true;
			}
		}

		if (!foundEmail && !foundUid)
		{
			fraudResponse.addSymptom(createSymptom(false));
		}

		return fraudResponse;
	}
}
