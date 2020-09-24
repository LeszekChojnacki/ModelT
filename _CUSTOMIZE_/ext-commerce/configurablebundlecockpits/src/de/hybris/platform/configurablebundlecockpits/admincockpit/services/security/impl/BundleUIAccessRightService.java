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

package de.hybris.platform.configurablebundlecockpits.admincockpit.services.security.impl;

import de.hybris.platform.cockpit.model.meta.ObjectType;
import de.hybris.platform.cockpit.model.meta.PropertyDescriptor;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.security.impl.DefaultUIAccessRightService;
import de.hybris.platform.configurablebundlecockpits.admincockpit.services.security.data.UIAccessRightDependency;
import de.hybris.platform.core.model.ItemModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Extends the {@link DefaultUIAccessRightService} to add/evaluate a generic handling for the display (writable or not)
 * of an attribute that is dependent on the another attribute (null or not null) of the same type, e.g. the attribute
 * 'description' of an item is only writable if the attribute 'name' is not null. The list of dependencies is injected
 * via Spring.
 */
// NOSONAR
public class BundleUIAccessRightService extends DefaultUIAccessRightService
{
	private static final Logger LOG = Logger.getLogger(BundleUIAccessRightService.class);

	private List<UIAccessRightDependency> dependencies;
	private Map<String, UIAccessRightDependency> dependencyMap;

	/**
	 * Overrides the super method to evaluate the injected list of dependencies before the standard method is called.
	 */
	@Override
	public boolean isWritable(final ObjectType type, final TypedObject item, final PropertyDescriptor propDescr,
			final boolean creationMode)
	{
		if (dependencyMap == null)
		{
			fillDependencyMap();
		}

		if (MapUtils.isNotEmpty(dependencyMap) && propDescr != null && propDescr.getQualifier() != null)
		{
			if (dependencyMap.containsKey(propDescr.getQualifier().toLowerCase()))
			{
				final boolean isWritable = isAttributeWritable(item, dependencyMap.get(propDescr.getQualifier().toLowerCase()));
				if (!isWritable)
				{
					return false;
				}
			}
		}

		return super.isWritable(type, item, propDescr, creationMode);
	}

	protected boolean isAttributeWritable(final TypedObject item, final UIAccessRightDependency dependency)
	{
		if (!(item.getObject() instanceof ItemModel))
		{
			return true;
		}

		final ItemModel itemModel = (ItemModel) item.getObject();
		final Object attributeValue = getModelService().getAttributeValue(itemModel, dependency.getDependentOnAttributeName());

		if (Boolean.FALSE.equals(dependency.getIsNull()) && attributeValue == null)
		{
			return false;
		}
		if (Boolean.TRUE.equals(dependency.getIsNull()) && attributeValue != null)
		{
			return false;
		}

		return true;
	}

	/**
	 * Creates a map based on the injected list of dependencies. It is checked if the attributes that are provided by the
	 * list do exist. In case an attribute is not defined in the system it is ignored.
	 */
	protected void fillDependencyMap()
	{
		if (CollectionUtils.isEmpty(getDependencies()))
		{
			dependencyMap = Collections.emptyMap();
			return;
		}

		if (MapUtils.isEmpty(dependencyMap))
		{
			if (dependencyMap == null)
			{
				dependencyMap = new HashMap<String, UIAccessRightDependency>();
			}

			for (final UIAccessRightDependency dependency : getDependencies())
			{
				final String qualifier = dependency.getTypeCode() + "." + dependency.getAttributeName();
				final PropertyDescriptor propDescKey = getTypeService().getPropertyDescriptor(qualifier);
				if (propDescKey == null)
				{
					LOG.error("Cannot find attribute '" + qualifier + "'; check configuration of dependencies in spring config");
					continue;
				}
				int pos = propDescKey.getQualifier().indexOf(".");
				dependency.setAttributeName(propDescKey.getQualifier().substring(pos + 1));

				final String dependentOnQualifier = dependency.getTypeCode() + "." + dependency.getDependentOnAttributeName();
				final PropertyDescriptor propDescDependentOnKey = getTypeService().getPropertyDescriptor(dependentOnQualifier);
				if (propDescDependentOnKey == null)
				{
					LOG.error("Cannot find attribute '" + dependentOnQualifier
							+ "'; check configuration of dependencies in spring config");
					continue;
				}
				pos = propDescDependentOnKey.getQualifier().indexOf(".");
				dependency.setDependentOnAttributeName(propDescDependentOnKey.getQualifier().substring(pos + 1));

				if (dependency.getIsNull() == null)
				{
					dependency.setIsNull(Boolean.FALSE);
				}

				dependencyMap.put(qualifier.toLowerCase(), dependency);

				final ObjectType type = getTypeService().getObjectTypeFromPropertyQualifier(qualifier);
				final Set<ObjectType> subTypes = getTypeService().getAllSubtypes(type);

				for (final ObjectType subType : subTypes)
				{
					final String subQualifier = subType.getCode() + "." + dependency.getAttributeName();
					if (dependencyMap.containsKey(subQualifier.toLowerCase()))
					{
						continue;
					}
					final UIAccessRightDependency subTypeDependency = new UIAccessRightDependency();
					subTypeDependency.setTypeCode(subType.getCode());
					subTypeDependency.setAttributeName(dependency.getAttributeName());
					subTypeDependency.setDependentOnAttributeName(dependency.getDependentOnAttributeName());
					subTypeDependency.setIsNull(dependency.getIsNull());
					dependencyMap.put(subQualifier.toLowerCase(), subTypeDependency);
				}
			}
		}
	}

	protected List<UIAccessRightDependency> getDependencies()
	{
		return dependencies;
	}

	@Required
	public void setDependencies(final List<UIAccessRightDependency> dependencies)
	{
		this.dependencies = dependencies;
	}


}
