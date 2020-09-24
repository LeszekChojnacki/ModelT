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
package com.hybris.backoffice.excel.export.wizard;

import de.hybris.platform.core.model.ItemModel;

import java.util.List;
import java.util.Objects;

import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.cockpitng.search.data.pageable.Pageable;


public class ExcelExportWizardForm
{
	private Pageable<ItemModel> pageable;
	private String typeCode;
	/**
	 * @deprecated since 6.7 no longer used please use {@link #attributesForm}
	 */
	@Deprecated
	private List<SelectedAttribute> attributes;
	private AttributeChooserForm attributesForm;
	private AttributeChooserForm classificationAttributesForm;
	private boolean exportTemplate;


	public Pageable<ItemModel> getPageable()
	{
		return pageable;
	}

	public void setPageable(final Pageable<ItemModel> pageable)
	{
		this.pageable = pageable;
	}

	public String getTypeCode()
	{
		return typeCode;
	}

	public void setTypeCode(final String typeCode)
	{
		this.typeCode = typeCode;
	}

	public int getSize()
	{
		return pageable != null ? pageable.getTotalCount() : 0;
	}

	/**
	 * @deprecated since 6.7 no longer used please use {@link #getAttributesForm()}
	 */
	@Deprecated
	public List<SelectedAttribute> getAttributes()
	{
		return attributes;
	}

	/**
	 * @deprecated since 6.7 no longer used please use {@link #getAttributesForm()}
	 */
	@Deprecated
	public void setAttributes(final List<SelectedAttribute> attributes)
	{
		this.attributes = attributes;
	}


	public AttributeChooserForm getAttributesForm()
	{
		if (attributesForm == null)
		{
			attributesForm = new AttributeChooserForm();
		}
		return attributesForm;
	}

	public void setAttributesForm(final AttributeChooserForm attributesForm)
	{
		this.attributesForm = attributesForm;
	}


	public AttributeChooserForm getClassificationAttributesForm()
	{
		if (classificationAttributesForm == null)
		{
			classificationAttributesForm = new AttributeChooserForm();
		}
		return classificationAttributesForm;
	}

	public void setClassificationAttributesForm(final AttributeChooserForm classificationAttributesForm)
	{
		this.classificationAttributesForm = classificationAttributesForm;
	}

	public boolean isExportTemplate()
	{
		return exportTemplate;
	}

	public void setExportTemplate(final boolean exportTemplate)
	{
		this.exportTemplate = exportTemplate;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		final ExcelExportWizardForm that = (ExcelExportWizardForm) o;
		return Objects.equals(pageable, that.pageable) && Objects.equals(typeCode, that.typeCode)
				&& Objects.equals(attributesForm, that.attributesForm)
				&& Objects.equals(classificationAttributesForm, that.classificationAttributesForm);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(pageable, typeCode, attributesForm, classificationAttributesForm);
	}
}
