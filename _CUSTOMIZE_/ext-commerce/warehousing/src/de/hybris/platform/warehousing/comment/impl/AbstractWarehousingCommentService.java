/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.comment.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.hybris.platform.comments.model.CommentModel;
import de.hybris.platform.comments.model.CommentTypeModel;
import de.hybris.platform.comments.model.ComponentModel;
import de.hybris.platform.comments.model.DomainModel;
import de.hybris.platform.comments.services.impl.DefaultCommentService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.warehousing.comment.WarehousingCommentService;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentContext;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentEventType;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Objects;


/**
 * Abstract implementation of the {@link WarehousingCommentService} provides implementations for comment creation and
 * initialization. It only requires that subclasses provide a means to obtain the comments associated with the item
 * model provided.
 *
 * @param <T>
 *           The type of item that this business process service is used for.
 */
public abstract class AbstractWarehousingCommentService<T extends ItemModel> extends DefaultCommentService implements
		WarehousingCommentService<T>
{
	protected static final String DOMAIN_CODE = "warehousing";
	protected static final String DOMAIN_NAME = "Warehousing Domain";

	private transient TimeService timeService;
	private transient UserService userService;

	@Override
	public CommentModel createAndSaveComment(final WarehousingCommentContext context,final String code)
	{
		Preconditions.checkArgument(!Objects.isNull(context.getItem()), "WarehousingCommentContext.item argument cannot be null.");
		Preconditions.checkArgument(!Objects.isNull(context.getText()), "WarehousingCommentContext.text argument cannot be null.");

		final UserModel currentUser = getUserService().getCurrentUser();

		final CommentModel comment = getModelService().create(CommentModel.class);
		comment.setCode(code);
		comment.setSubject(context.getSubject());
		comment.setText(context.getText());
		comment.setCreationtime(getTimeService().getCurrentTime());
		comment.setOwner(context.getItem());
		comment.setAuthor(currentUser);
		comment.setCommentType(getOrCreateCommentType(context.getCommentType()));
		comment.setComponent(getOrCreateComponent(context.getCommentType()));

		final List<CommentModel> comments = Lists.newArrayList();
		final ItemModel item = context.getItem();
		if (!item.getComments().isEmpty())
		{
			comments.addAll(item.getComments());
		}
		comments.add(comment);
		item.setComments(comments);
		getModelService().save(comment);
		getModelService().save(item);

		return comment;
	}

	@Override
	public DomainModel getOrCreateDomainForCodeWarehousing()
	{
		DomainModel domain = getDomainForCode(DOMAIN_CODE);
		if (Objects.isNull(domain))
		{
			domain = getModelService().create(DomainModel.class);
			domain.setCode(DOMAIN_CODE);
			domain.setName(DOMAIN_NAME);
			getModelService().save(domain);
		}
		return domain;
	}

	@Override
	public CommentTypeModel getOrCreateCommentType(final WarehousingCommentEventType eventType)
	{
		CommentTypeModel commentType = getCommentTypeForCode(getOrCreateComponent(eventType), eventType.getCommentTypeCode());
		if (Objects.isNull(commentType))
		{
			commentType = getModelService().create(CommentTypeModel.class);
			commentType.setCode(eventType.getCommentTypeCode());
			commentType.setName(eventType.getCommentTypeName());
			commentType.setDomain(getOrCreateDomainForCodeWarehousing());
			getModelService().save(commentType);
		}
		return commentType;
	}

	@Override
	public ComponentModel getOrCreateComponent(final WarehousingCommentEventType eventType)
	{
		final DomainModel domain = getOrCreateDomainForCodeWarehousing();
		ComponentModel component = getComponentForCode(domain, eventType.getComponentCode());
		if (Objects.isNull(component))
		{
			component = getModelService().create(ComponentModel.class);
			component.setCode(eventType.getComponentCode());
			component.setName(eventType.getComponentName());
			component.setDomain(domain);
			getModelService().save(component);
		}
		return component;
	}

	protected TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

}
