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

package de.hybris.platform.adaptivesearchbackoffice.editors.facets;

import de.hybris.platform.adaptivesearch.data.AsFacetValueData;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetValueConfigurationModel;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Objects;


public class FacetValueModel
{
	private boolean sticky;
	private AsFacetValueData data;
	private AbstractAsFacetValueConfigurationModel model;

	public boolean isSticky()
	{
		return sticky;
	}

	public void setSticky(final boolean sticky)
	{
		this.sticky = sticky;
	}

	public AsFacetValueData getData()
	{
		return data;
	}

	public void setData(final AsFacetValueData data)
	{
		this.data = data;
	}

	public AbstractAsFacetValueConfigurationModel getModel()
	{
		return model;
	}

	public void setModel(final AbstractAsFacetValueConfigurationModel model)
	{
		this.model = model;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final FacetValueModel that = (FacetValueModel) obj;
		return new EqualsBuilder()
				.append(this.data.getValue(), that.data.getValue())
				.append(this.sticky, that.sticky)
				.isEquals();
 	}

	@Override
	public int hashCode()
	{

		return Objects.hash(this.sticky, this.data.getValue());
	}
}
