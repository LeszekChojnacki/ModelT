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
package com.hybris.backoffice.excel.validators;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for catalog version. The validator checks whether import parameters has "catalog" and
 * "version" key. Based on that, the validator checks whether catalog and version are not empty and exist.
 */
public class ExcelCatalogVersionValidator implements ExcelValidator
{

	protected static final String CATALOGS_KEY = "Catalogs";
	protected static final String VERSIONS_KEY = "Versions";
	protected static final String CATALOG_VERSIONS_KEY = "CatalogVersions";
	protected static final String CATALOG_VERSIONS_FORMAT_KEY = "%s:%s";

	protected static final String VALIDATION_CATALOG_EMPTY = "excel.import.validation.catalog.empty";
	protected static final String VALIDATION_CATALOG_VERSION_EMPTY = "excel.import.validation.catalogversion.empty";
	protected static final String VALIDATION_CATALOG_DOESNT_EXIST = "excel.import.validation.catalog.doesntexists";
	protected static final String VALIDATION_CATALOG_VERSION_DOESNT_EXIST = "excel.import.validation.catalogversion.doesntexists";
	protected static final String VALIDATION_CATALOG_VERSION_DOESNT_MATCH = "excel.import.validation.catalogversion.doesntmatch";

	private CatalogVersionService catalogVersionService;
	private UserService userService;
	private TypeService typeService;

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> ctx)
	{
		if (!ctx.containsKey(CATALOGS_KEY) || !ctx.containsKey(VERSIONS_KEY) || !ctx.containsKey(CATALOG_VERSIONS_KEY))
		{
			populateContext(ctx);
		}

		final List<ValidationMessage> validationMessages = new ArrayList<>();

		for (final Map<String, String> parameters : importParameters.getMultiValueParameters())
		{
			validateCatalog(ctx, validationMessages, parameters);

			validateCatalogVersion(ctx, validationMessages, parameters);

			if (catalogExists(ctx, parameters) && catalogVersionExists(ctx, parameters) && !catalogVersionMatch(ctx, parameters))
			{
				validationMessages.add(new ValidationMessage(VALIDATION_CATALOG_VERSION_DOESNT_MATCH,
						parameters.get(CatalogVersionModel.VERSION), parameters.get(CatalogVersionModel.CATALOG)));
			}
		}
		return new ExcelValidationResult(validationMessages);
	}

	protected void validateCatalogVersion(final Map<String, Object> ctx, final List<ValidationMessage> validationMessages,
			final Map<String, String> parameters)
	{
		if (parameters.get(CatalogVersionModel.VERSION) == null)
		{
			validationMessages.add(new ValidationMessage(VALIDATION_CATALOG_VERSION_EMPTY));
		}
		else if (!catalogVersionExists(ctx, parameters))
		{
			validationMessages
					.add(new ValidationMessage(VALIDATION_CATALOG_VERSION_DOESNT_EXIST, parameters.get(CatalogVersionModel.VERSION)));
		}
	}

	protected void validateCatalog(final Map<String, Object> ctx, final List<ValidationMessage> validationMessages,
			final Map<String, String> parameters)
	{
		if (parameters.get(CatalogVersionModel.CATALOG) == null)
		{
			validationMessages.add(new ValidationMessage(VALIDATION_CATALOG_EMPTY));
		}
		else if (!catalogExists(ctx, parameters))
		{
			validationMessages
					.add(new ValidationMessage(VALIDATION_CATALOG_DOESNT_EXIST, parameters.get(CatalogVersionModel.CATALOG)));
		}
	}

	protected boolean catalogExists(final Map<String, Object> ctx, final Map<String, String> parameters)
	{
		return parameters.get(CatalogVersionModel.CATALOG) != null
				&& ((Set) ctx.get(CATALOGS_KEY)).contains(parameters.get(CatalogVersionModel.CATALOG));
	}

	protected boolean catalogVersionExists(final Map<String, Object> ctx, final Map<String, String> parameters)
	{
		return parameters.get(CatalogVersionModel.VERSION) != null
				&& ((Set) ctx.get(VERSIONS_KEY)).contains(parameters.get(CatalogVersionModel.VERSION));
	}

	protected boolean catalogVersionMatch(final Map<String, Object> ctx, final Map<String, String> parameters)
	{
		return ((Set) ctx.get(CATALOG_VERSIONS_KEY)).contains(String.format(CATALOG_VERSIONS_FORMAT_KEY,
				parameters.get(CatalogVersionModel.CATALOG), parameters.get(CatalogVersionModel.VERSION)));
	}

	protected void populateContext(final Map<String, Object> ctx)
	{
		final UserModel currentUser = getUserService().getCurrentUser();

		final Collection<CatalogVersionModel> allWritableCatalogVersions = getUserService().isAdmin(currentUser) ? getCatalogVersionService().getAllCatalogVersions() : getCatalogVersionService()
				.getAllWritableCatalogVersions(currentUser);

		final Set<String> catalogs = allWritableCatalogVersions.stream().map(catalogVersion -> catalogVersion.getCatalog().getId())
				.collect(Collectors.toSet());
		ctx.put(CATALOGS_KEY, catalogs);

		final Set<String> versions = allWritableCatalogVersions.stream().map(CatalogVersionModel::getVersion)
				.collect(Collectors.toSet());

		ctx.put(VERSIONS_KEY, versions);

		final Set<String> catalogVersions = allWritableCatalogVersions.stream().map(catalogVersionModel -> String
				.format(CATALOG_VERSIONS_FORMAT_KEY, catalogVersionModel.getCatalog().getId(), catalogVersionModel.getVersion()))
				.collect(Collectors.toSet());
		ctx.put(CATALOG_VERSIONS_KEY, catalogVersions);
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		final Map<String, String> singleValueParameters = importParameters.getSingleValueParameters();
		return importParameters.isCellValueNotBlank() && singleValueParameters.containsKey(CatalogVersionModel.CATALOG)
				&& singleValueParameters.containsKey(CatalogVersionModel.VERSION);
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
