/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousingbackoffice.dtos;

import de.hybris.platform.warehousing.model.AtpFormulaModel;


/**
 * DTO used to display ATP Values
 */
public class AtpViewDto
{
	private AtpFormulaModel atpFormula;
	private Long atp;
	private boolean isActive;

	public AtpViewDto(final AtpFormulaModel atpFormula, final Long atp, final Boolean isActive)
	{
		this.atpFormula = atpFormula;
		this.atp = atp;
		this.isActive = isActive;
	}

	public AtpFormulaModel getAtpFormula()
	{
		return atpFormula;
	}

	public void setAtpFormula(final AtpFormulaModel atpFormula)
	{
		this.atpFormula = atpFormula;
	}

	public Long getAtp()
	{
		return atp;
	}

	public void setAtp(final Long atp)
	{
		this.atp = atp;
	}

	public Boolean getIsActive()
	{
		return isActive;
	}

	public void setIsActive(final Boolean active)
	{
		this.isActive = active;
	}
}
