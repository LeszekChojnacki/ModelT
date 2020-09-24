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

/**
 * Describes the fraud checking result for a single symptom.
 */
public class FraudSymptom
{
	private final String explanation;
	private final double score;
	private final String symptom;


	public FraudSymptom(final String symptom, final double score)
	{
		this(null, score, symptom);

	}

	public FraudSymptom(final String explanation, final double score, final String symptom)
	{
		this.explanation = explanation;
		this.score = score;
		this.symptom = symptom;
	}

	/**
	 * @return the explanation
	 */
	public String getExplanation()
	{
		return explanation;
	}


	/**
	 * @return the symptom
	 */
	public String getSymptom()
	{
		return symptom;
	}


	/**
	 * @return the score
	 */
	public double getScore()
	{
		return score;
	}

}
