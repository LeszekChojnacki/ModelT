package com.hybris.backoffice.bulkedit.renderer;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.interceptor.impl.InterceptorExecutionPolicy;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableMap;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;


/**
 * Service which allows to create {@link ItemModel} without checking create permissions to such model. BulkEdit creates
 * an instance of a model which is used as a template for modifying other models, so this service allows user to use
 * BulkEdit without create permission.
 */
public class BulkEditTemplateModelCreator
{

	private Set<String> disabledInterceptorsBeanNames = new HashSet<>();
	private ModelService modelService;
	private PermissionFacade permissionFacade;
	private SessionService sessionService;

	public Optional<ItemModel> create(final String typeCode)
	{
		if (permissionFacade.canReadType(typeCode) && permissionFacade.canChangeType(typeCode))
		{
			final Map<String, Object> sessionParams = ImmutableMap.of(InterceptorExecutionPolicy.DISABLED_INTERCEPTOR_BEANS,
					Collections.unmodifiableSet(disabledInterceptorsBeanNames));
			final ItemModel item = sessionService.executeInLocalViewWithParams(sessionParams, new SessionExecutionBody()
			{
				@Override
				public ItemModel execute()
				{
					return modelService.create(typeCode);
				}
			});
			return Optional.ofNullable(item);
		}
		return Optional.empty();
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	@Required
	public void setDisabledInterceptorsBeanNames(final Set<String> disabledInterceptorsBeanNames)
	{
		this.disabledInterceptorsBeanNames = disabledInterceptorsBeanNames;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
