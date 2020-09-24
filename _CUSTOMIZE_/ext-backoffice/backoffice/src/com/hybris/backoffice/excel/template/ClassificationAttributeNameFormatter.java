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
package com.hybris.backoffice.excel.template;

import de.hybris.platform.catalog.model.classification.ClassificationAttributeModel;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.appender.ExcelMarkAppender;
import com.hybris.backoffice.excel.template.populator.extractor.ClassificationFullNameExtractor;


/**
 * Formats {@link ClassificationAttributeModel} to readable format that includes its ClassificationSystem,
 * ClassificationVersion, ClassificationClass, name and locale.
 */
public class ClassificationAttributeNameFormatter implements AttributeNameFormatter<ExcelClassificationAttribute>
{
	private ClassificationFullNameExtractor classificationFullNameExtractor;
	private List<ExcelMarkAppender<ExcelClassificationAttribute>> appenders = Collections.emptyList();

	/**
	 * Formats given {@link ClassificationAttributeModel} to a human readable format
	 *
	 * @param context
	 *           excel context which contains {@link ExcelAttribute}
	 * @return human readable information about the {@link ExcelAttribute}
	 */
	@Override
	public String format(final @Nonnull ExcelAttributeContext<ExcelClassificationAttribute> context)
	{
		final ExcelClassificationAttribute excelClassificationAttribute = context
				.getExcelAttribute(ExcelClassificationAttribute.class);
		String formattedName = classificationFullNameExtractor.extract(excelClassificationAttribute);
		for (final ExcelMarkAppender<ExcelClassificationAttribute> appender : appenders)
		{
			formattedName = appender.apply(formattedName, excelClassificationAttribute);
		}
		return formattedName;
	}

	@Required
	public void setClassificationFullNameExtractor(final ClassificationFullNameExtractor classificationFullNameExtractor)
	{
		this.classificationFullNameExtractor = classificationFullNameExtractor;
	}

	// optional
	public void setAppenders(final List<ExcelMarkAppender<ExcelClassificationAttribute>> appenders)
	{
		if (appenders != null)
		{
			OrderComparator.sort(appenders);
		}
		this.appenders = ListUtils.emptyIfNull(appenders);
	}
}
