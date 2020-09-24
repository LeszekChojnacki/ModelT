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
package de.hybris.platform.marketplacebackoffice.widgets;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.marketplaceservices.strategies.AutoApproveProductStrategy;
import de.hybris.platform.validation.coverage.CoverageInfo;
import de.hybris.platform.validation.coverage.CoverageInfo.CoveragePropertyInfoMessage;
import de.hybris.platform.validation.coverage.strategies.impl.ValidationBasedCoverageCalculationStrategy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.impl.DefaultValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;
import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaLogicHandler;


/**
 * A handler to check a product coverage info before saving.
 */
public class ProductSaleableEditorAreaLogicHandler extends DefaultEditorAreaLogicHandler
{
	private AutoApproveProductStrategy autoApproveProductStrategy;
	private ValidationBasedCoverageCalculationStrategy validationCoverageCalculationStrategy;

	@Override
	public List<ValidationInfo> performValidation(final WidgetInstanceManager widgetInstanceManager, final Object currentObject,
			final ValidationContext validationContext)
	{

		final List<ValidationInfo> superValidationInfos = super.performValidation(widgetInstanceManager, currentObject,
				validationContext);
		final ProductModel product = (ProductModel) currentObject;
		
		final CoverageInfo coverageInfo = getAutoApproveProductStrategy().autoApproveVariantAndApparelProduct(product);
		if (ArticleApprovalStatus.APPROVED.equals(product.getApprovalStatus()) && coverageInfo != null)
		{
			final List<ValidationInfo> validationInfos = new ArrayList<>();
			final List<CoveragePropertyInfoMessage> messages = coverageInfo.getPropertyInfoMessages();
			messages.forEach(message -> {
				final DefaultValidationInfo validationInfo = new DefaultValidationInfo();
				validationInfo.setValidationMessage(message.getMessage());
				validationInfo.setInvalidPropertyPath(getPropertyQualifier(message.getPropertyQualifier()));
				validationInfo.setConfirmed(false);
				validationInfo.setInvalidValue(product.getProperty(validationInfo.getInvalidPropertyPath()));
				validationInfo.setValidationSeverity(ValidationSeverity.ERROR);
				validationInfos.add(validationInfo);
			});
			superValidationInfos.addAll(validationInfos);
		}

		return superValidationInfos;
	}

	protected String getPropertyQualifier(final String source)
	{
		return source.substring(source.indexOf('.') + 1);
	}

	protected ValidationBasedCoverageCalculationStrategy getValidationCoverageCalculationStrategy()
	{
		return validationCoverageCalculationStrategy;
	}

	@Required
	public void setValidationCoverageCalculationStrategy(
			final ValidationBasedCoverageCalculationStrategy validationCoverageCalculationStrategy)
	{
		this.validationCoverageCalculationStrategy = validationCoverageCalculationStrategy;
	}

	public AutoApproveProductStrategy getAutoApproveProductStrategy() {
		return autoApproveProductStrategy;
	}

	public void setAutoApproveProductStrategy(final AutoApproveProductStrategy autoApproveProductStrategy) {
		this.autoApproveProductStrategy = autoApproveProductStrategy;
	}

}
