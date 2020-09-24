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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.cockpitng.validation.model.ValidationInfo;


/**
 * Pojo which represents bulk edit form.
 */
public class BulkEditForm
{
	private AttributeChooserForm attributesForm;
	private Object templateObject;
	private Set<String> qualifiersToBeCleared = new HashSet<>();
	private Collection<Object> itemsToEdit;
	private Set<String> qualifiersToMerge = new HashSet<>();
	private Map<String, Object> parameters = new HashMap<>();
	private boolean validateAllAttributes;
	private List<ValidationResult> validations;

	/**
	 * Attributes form with attributes to edit.
	 * 
	 * @return attributes form. If not defined new object will be created.
	 */
	public AttributeChooserForm getAttributesForm()
	{
		if (attributesForm == null)
		{
			this.attributesForm = new AttributeChooserForm();
		}
		return attributesForm;
	}

	public void setAttributesForm(final AttributeChooserForm attributesForm)
	{
		this.attributesForm = attributesForm;
	}

	/**
	 * @return template object from which chosen attributes values will be taken.
	 */
	public Object getTemplateObject()
	{
		return templateObject;
	}

	public void setTemplateObject(final Object templateObject)
	{
		this.templateObject = templateObject;
	}

	/**
	 * @return set of qualifiers for which value should be cleared not matter what is in template object.
	 */
	public Set<String> getQualifiersToBeCleared()
	{
		return qualifiersToBeCleared;
	}

	/**
	 * Sets list of qualifiers to be cleared.
	 * 
	 * @param qualifiersToBeCleared
	 *           set of qualifiers to be cleared.
	 */
	public void setQualifiersToBeCleared(final Set<String> qualifiersToBeCleared)
	{
		this.qualifiersToBeCleared = qualifiersToBeCleared;
	}

	/**
	 * Adds qualifier to be cleared list.
	 * 
	 * @param qualifier
	 *           qualifier to be cleared.
	 */
	public void addQualifierToClear(final String qualifier)
	{
		qualifiersToBeCleared.add(qualifier);
	}

	/**
	 * Removes qualifier from to be cleared list.
	 * 
	 * @param qualifier
	 *           qualifier to be removed.
	 */
	public void removeQualifierToClear(final String qualifier)
	{
		qualifiersToBeCleared.remove(qualifier);
	}

	/**
	 * Tells if qualifier is on the {@link #getQualifiersToBeCleared()}
	 *
	 * @param qualifier
	 *           qualifier
	 * @return true if qualifier should be cleared.
	 */
	public boolean isClearAttribute(final String qualifier)
	{
		return qualifiersToBeCleared.contains(qualifier);
	}

	/**
	 * @return set of qualifiers where value should be overridden for given qualifier. It should be used with references
	 *         which are collections or maps. If qualifier is not specified on the list it will merge collection/map with
	 *         existing value.
	 */
	public Set<String> getQualifiersToMerge()
	{
		return qualifiersToMerge;
	}

	/**
	 * Sets list of qualifiers to override.
	 *
	 * @param qualifiersToMerge
	 *           qualifiers to be overridden.
	 */
	public void setQualifiersToMerge(final Set<String> qualifiersToMerge)
	{
		this.qualifiersToMerge = qualifiersToMerge;
	}

	/**
	 * Adds qualifier to {@link #getQualifiersToMerge()} set
	 *
	 * @param qualifier
	 *           qualifier to be added.
	 */
	public void addQualifierToMerge(final String qualifier)
	{
		qualifiersToMerge.add(qualifier);
	}

	/**
	 * Removes qualifier from {@link #getQualifiersToMerge()}
	 *
	 * @param qualifier
	 *           qualifier to be removed.
	 */
	public void removeQualifierToMerge(final String qualifier)
	{
		qualifiersToMerge.remove(qualifier);
	}

	/**
	 * Tells if qualifier value should be overridden.
	 * 
	 * @param qualifier
	 *           qualifier.
	 * @return true if qualifier value should be overridden.
	 */
	public boolean isQualifierToMerge(final String qualifier)
	{
		return qualifiersToMerge.contains(qualifier);
	}

	/**
	 * @return Collection of items to be modified with bulk edit data.
	 */
	public Collection<Object> getItemsToEdit()
	{
		return itemsToEdit != null ? itemsToEdit : Collections.emptyList();
	}

	/**
	 * Sets collection of items to edit
	 *
	 * @param itemsToEdit
	 *           collection of items to edit.
	 */
	public void setItemsToEdit(final Collection<Object> itemsToEdit)
	{
		this.itemsToEdit = itemsToEdit;
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	public void setParameters(final Map<String, Object> parameters)
	{
		this.parameters = parameters;
	}

	public boolean isValidateAllAttributes()
	{
		return validateAllAttributes;
	}

	public void setValidateAllAttributes(final boolean validateAllAttributes)
	{
		this.validateAllAttributes = validateAllAttributes;
	}

	public List<ValidationResult> getValidations()
	{
		return validations;
	}

	public void setValidations(final List<ValidationResult> validations)
	{
		this.validations = validations;
	}
}
