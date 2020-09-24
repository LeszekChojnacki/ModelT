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
package com.hybris.backoffice.excel.translators;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Abstract class which implements {@link ExcelValueTranslator} and simplifies importData method. This class should be
 * used when during importing process a translator will be returned just single value and creation of additional entries
 * (for example creating part-of entries: product - price row, ect.) is not required.
 * 
 * @param <T>
 *           class which is supported by given translator
 */
public abstract class AbstractExcelValueTranslator<T> extends AbstractValidationAwareTranslator<T>
{

	protected int order = 0;

	/**
	 * Imports data based on provided importParameters for given attributeDescriptor. The method returns {@link Impex}
	 * thanks to that it is possible to creating additional entries ( or example creating part-of entries: product -
	 * price row, ect.)
	 * 
	 * @param attributeDescriptor
	 *           describes attribute which should be imported
	 * @param importParameters
	 *           contains information about language for localized field, type code, parsed parameters inserted into
	 *           excel's cell.
	 * @return {@link Impex} object which is representation of impex script.
	 */
	@Override
	public Impex importData(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final ImpexValue singleValue = importValue(attributeDescriptor, importParameters);

		final Impex impex = new Impex();
		final ImpexForType impexForType = impex.findUpdates(importParameters.getTypeCode());
		if (singleValue != null)
		{
			impexForType.putValue(0, singleValue.getHeaderValue(), singleValue.getValue());
		}

		return impex;
	}

	/**
	 * Imports single value instead of whole Impex object. Impex value consists of header and value.
	 *
	 * @param attributeDescriptor
	 *           {@link AttributeDescriptorModel} describes attribute which should be imported
	 * @param importParameters
	 *           - contains information about language for localized field, type code, parsed parameters inserted into
	 *           excel's cell.
	 * @return {@link ImpexValue} value which should be imported
	 */
	public abstract ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor,
			final ImportParameters importParameters);


	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(final int order)
	{
		this.order = order;
	}
}
