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
package de.hybris.platform.fraud.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Contains the result obtained by performing any fraud detection upon a single service.
 */
public class FraudServiceResponse
{
	private final String providerName;

	private final String description;

	// YTODO: what are these for
	private final String externalDescription;

	private List<FraudSymptom> symptoms;

	public FraudServiceResponse(final String providerName)
	{
		this(null, providerName);
	}

	public FraudServiceResponse(final String description, final String providerName)
	{
		this(description, providerName, null, null);
	}

	public FraudServiceResponse(final String description, final String providerName, final String externalDescription,
			final Collection<FraudSymptom> symptoms)
	{
		this.description = description;
		this.providerName = providerName;
		this.externalDescription = externalDescription;
		this.symptoms = symptoms == null ? null : new ArrayList<FraudSymptom>(symptoms);
	}

	/**
	 * Sums up all symptom scores.
	 */
	public double getScore()
	{
		double score = 0;
		for (final FraudSymptom symptom : getSymptoms())
		{
			score += symptom.getScore();
		}
		return score;
	}

	/**
	 * Sums up all symptom scores of a specific symptom .
	 */
	public double getScore(final String symptomName)
	{
		double score = 0;
		for (final FraudSymptom symptom : getSymptoms())
		{
			if (symptomName.equalsIgnoreCase(symptomName))
			{
				score += symptom.getScore();
			}
		}
		return score;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return the providerName
	 */
	public String getProviderName()
	{
		return providerName;
	}

	/**
	 * @return the externalDescription
	 */
	public String getExternalDescription()
	{
		return externalDescription;
	}

	public void addSymptom(final FraudSymptom symptom)
	{
		if (null == this.symptoms)
		{
			this.symptoms = new ArrayList<FraudSymptom>();
		}
		this.symptoms.add(symptom);
	}

	/**
	 * @return the symptoms
	 */
	public List<FraudSymptom> getSymptoms()
	{
		return symptoms == null ? Collections.emptyList() : symptoms;
	}

}
