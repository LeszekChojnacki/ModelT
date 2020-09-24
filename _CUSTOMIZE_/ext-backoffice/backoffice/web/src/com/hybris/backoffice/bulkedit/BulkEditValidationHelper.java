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
package com.hybris.backoffice.bulkedit;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hybris.cockpitng.validation.LocalizedQualifier;
import com.hybris.cockpitng.validation.ValidationHandler;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


/**
 * Validation helper for bulk edit.
 */
public interface BulkEditValidationHelper
{
	/**
	 * Tells which attributes from bulk edit's template object {@link BulkEditForm#getTemplateObject()} are validatable.
	 * 
	 * @param bulkEditForm
	 *           bulk edit form.
	 * @return set of validatable qualifiers.
	 * @deprecated since 1811, use {@link #getValidatablePropertiesWithLocales(BulkEditForm)} instead.
	 */
	@Deprecated
	Set<String> getValidatableProperties(BulkEditForm bulkEditForm);

	/**
	 * Tells which attributes from bulk edit's template object {@link BulkEditForm#getTemplateObject()} are validatable. The
	 * returned collection is of {@link LocalizedQualifier} meaning it will contain {@link java.util.Locale}s for each
	 * qualifier.
	 *
	 * @param bulkEditForm
	 *           bulk edit form.
	 * @return collection of validatable {@link LocalizedQualifier}s.
	 */
	Collection<LocalizedQualifier> getValidatablePropertiesWithLocales(BulkEditForm bulkEditForm);

	/**
	 * Creates proxy handler which validates only fields which have been edited in bulk edit form.
	 * 
	 * @param bulkEditForm
	 *           the form.
	 * @return validation handler porxy.
	 */
	ValidationHandler createProxyValidationHandler(BulkEditForm bulkEditForm);

	/**
	 * Validates modified items from given form {@link BulkEditForm#getItemsToEdit()}
	 * 
	 * @param bulkEditForm
	 *           edit form.
	 * @param severityHigherThan
	 *           returns validation info for violations higher than given severity
	 *           {@link ValidationSeverity#isHigherThan(ValidationSeverity)}
	 * @return map of validation info per object.
	 */
	Map<Object, List<ValidationInfo>> validateModifiedItems(BulkEditForm bulkEditForm, ValidationSeverity severityHigherThan);
}
