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
package com.hybris.backoffice.widgets.actions.enumeration;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.servicelayer.interceptor.impl.InterceptorExecutionPolicy;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.tx.Transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.spring.SpringUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeOperationResult;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectAccessException;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.type.ObjectValueService;


/**
 * EnumerationAction is a generic class which allows to update a list of selected items with chosen enumeration value.
 * Enumeration action accepts 2 parameters through configuration:
 * <li>
 * <ul>
 * {@value com.hybris.backoffice.widgets.actions.enumeration.EnumerationAction#PARAMETER_QUALIFIER} - is a required
 * parameter. The type of the items is inferred from the input data and next, the type of the given qualifier is
 * retrieved. For example, configuration for Product type can be provided as follows: {@code
 *    			<y:parameter>
 *    			    <y:name>qualifier</y:name>
 *    			    <y:value>approvalStatus</y:value>
 *    			</y:parameter>
 *    		}
 * </ul>
 * <ul>
 * {@value com.hybris.backoffice.widgets.actions.enumeration.EnumerationAction#PARAMETER_VALIDATOR_ID} - is an optional
 * parameter. You can provide id of validator's bean which allows to check whether the given data can be updated with
 * chosen enumeration value. For example: {@code
 * 				<y:parameter>
 * 				    <y:name>validatorId</y:name>
 * 					<y:value>someBeanId</y:value>
 * 				</y:parameter>
 * }
 * </ul>
 * </li>
 */
public class EnumerationAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Collection<Object>, Object>
{

	protected static final String DEFAULT_ENUMERATION_VALIDATOR_BEAN_ID = "defaultEnumerationValidator";

	protected static final String ENUMERATION_KEY = "enumeration";

	protected static final String PARAMETER_QUALIFIER = "qualifier";
	protected static final String PARAMETER_VALIDATOR_ID = "validatorId";

	@Resource
	private ObjectValueService objectValueService;
	@Resource
	private ObjectFacade objectFacade;
	@Resource
	private NotificationService notificationService;
	@Resource
	private ModelService modelService;
	@Resource
	private SessionService sessionService;
	@Resource(name = "enumerationActionDisabledInterceptorsBeanNames")
	private Set<String> disabledInterceptorsBeanNames;

	@Override
	public ActionResult<Object> perform(final ActionContext<Collection<Object>> context)
	{
		final String qualifier = (String) context.getParameter(PARAMETER_QUALIFIER);
		final ObjectFacadeOperationResult<Object> reloadResult = objectFacade.reload(context.getData());
		final Collection<Object> dataToUpdate = reloadResult.getSuccessfulObjects();
		final HybrisEnumValue hybrisEnumValue = (HybrisEnumValue) context.getParameter(ENUMERATION_KEY);
		return save(context, qualifier, dataToUpdate, hybrisEnumValue);
	}

	protected ActionResult<Object> save(final ActionContext<Collection<Object>> context, final String expression,
			final Collection<Object> list, final HybrisEnumValue hybrisEnumValue)
	{
		for (final Object element : list)
		{
			try
			{
				objectValueService.setValue(expression, element, hybrisEnumValue);
			}
			catch (final Exception e)
			{
				notifyUpdateFailed(context, ImmutableMap.of(element, e));
				revertChanges(list);
				return new ActionResult<>(ActionResult.ERROR);
			}
		}

		final ObjectFacadeOperationResult<Object> result = saveChangesWithoutInterceptors(list);
		if (result.hasError() || !result.getFailedObjects().isEmpty())
		{
			final Map<Object, ObjectAccessException> notificationReferences = result.getFailedObjects().stream()
					.collect(Collectors.toMap(Function.identity(), result::getErrorForObject, ObjectUtils::defaultIfNull));
			revertChanges(list);
			notifyUpdateFailed(context, notificationReferences);
			return new ActionResult<>(ActionResult.ERROR);
		}

		notificationService.notifyUser(getNotificationSource(context), NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE,
				NotificationEvent.Level.SUCCESS, list);
		return new ActionResult<>(ActionResult.SUCCESS);
	}

	protected ObjectFacadeOperationResult<Object> saveChangesWithoutInterceptors(final Collection<Object> itemsToSave)
	{
		final Map<String, Object> params = ImmutableMap.of(InterceptorExecutionPolicy.DISABLED_INTERCEPTOR_BEANS,
				Collections.unmodifiableSet(getDisabledInterceptorsBeanNames()));
		return sessionService.executeInLocalViewWithParams(params, new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				return getObjectFacade().save(itemsToSave, null);
			}
		});
	}

	protected void revertChanges(final Collection<Object> list)
	{
		list.forEach(modelService::refresh);
	}

	protected void notifyUpdateFailed(final ActionContext<Collection<Object>> context, final Map<Object, ? extends Exception> map)
	{
		notificationService.notifyUser(getNotificationSource(context), NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE,
				NotificationEvent.Level.FAILURE, map);
	}

	/**
	 * @deprecated since 1808, not used anymore. {@link #saveChangesWithoutInterceptors} uses save method which became
	 *             transactional in 1808
	 */
	@Deprecated
	protected void beginTransaction()
	{
		Transaction.current().begin();
	}

	/**
	 * @deprecated since 1808, not used anymore. {@link #saveChangesWithoutInterceptors} uses save method which became
	 *             transactional in 1808
	 */
	@Deprecated
	protected void rollbackTransaction()
	{
		Transaction.current().rollback();
	}

	/**
	 * @deprecated since 1808, not used anymore. {@link #saveChangesWithoutInterceptors} uses save method which became
	 *             transactional in 1808
	 */
	@Deprecated
	protected void commitTransaction()
	{
		Transaction.current().commit();
	}

	@Override
	public boolean canPerform(final ActionContext<Collection<Object>> ctx)
	{
		final Object object = ctx.getParameter(EnumerationAction.PARAMETER_QUALIFIER);
		Preconditions.checkArgument(object instanceof String, "'%s' is a mandatory action's parameter!", PARAMETER_QUALIFIER);

		return CollectionUtils.isNotEmpty(ctx.getData())
				&& getEnumerationValidator(ctx).map(enumerationValidator -> enumerationValidator.validate(ctx)).orElse(true);
	}

	protected String getNotificationSource(final ActionContext<Collection<Object>> context)
	{
		return notificationService.getWidgetNotificationSource(context);
	}

	protected Optional<EnumerationValidator> getEnumerationValidator(final ActionContext<Collection<Object>> context)
	{
		final Object parameter = context.getParameter(EnumerationAction.PARAMETER_VALIDATOR_ID);
		if (!(parameter instanceof String))
		{
			return getBean(DEFAULT_ENUMERATION_VALIDATOR_BEAN_ID);
		}

		final String validatorId = (String) context.getParameter(EnumerationAction.PARAMETER_VALIDATOR_ID);

		return StringUtils.isBlank(validatorId) ? Optional.empty() : getBean(validatorId);
	}

	protected Optional<EnumerationValidator> getBean(final String beanId)
	{
		return Optional.ofNullable((EnumerationValidator) SpringUtil.getBean(beanId, EnumerationValidator.class));
	}

	public ObjectValueService getObjectValueService()
	{
		return objectValueService;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	public Set<String> getDisabledInterceptorsBeanNames()
	{
		if (disabledInterceptorsBeanNames == null)
		{
			return new HashSet<>();
		}
		return disabledInterceptorsBeanNames;
	}
}
