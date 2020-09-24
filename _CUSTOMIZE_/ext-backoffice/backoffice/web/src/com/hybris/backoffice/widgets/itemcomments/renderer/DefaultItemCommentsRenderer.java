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
package com.hybris.backoffice.widgets.itemcomments.renderer;

import de.hybris.platform.comments.model.CommentModel;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.backoffice.widgets.itemcomments.ItemCommentsController;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;


public class DefaultItemCommentsRenderer extends AbstractWidgetComponentRenderer<Div, Object, CommentModel>
{

	protected static final String SCLASS_COMMENTSLIST_ONE_COMMENT = "yw-commentslist-one-comment";
	protected static final String SCLASS_COMMENTSLIST_COMMENT_HEADER = "yw-commentslist-comment-header";
	protected static final String SCLASS_COMMENTSLIST_COMMENT_AUTHOR_LABEL = "yw-commentslist-comment-author-label";
	protected static final String SCLASS_COMMENTSLIST_COMMENT_DATE_LABEL = "yw-commentslist-comment-date-label";
	protected static final String SCLASS_COMMENTSLIST_COMMENT_CONTENT = "yw-commentslist-comment-content";

	private PermissionFacade permissionFacade;
	private LabelService labelService;
	private WidgetInstanceManager widgetInstanceManager;

	@Override
	public void render(final Div commentsContainer, final Object configuration, final CommentModel comment,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		renderOneComment(commentsContainer, comment, widgetInstanceManager);
		fireComponentRendered(commentsContainer, configuration, comment);
	}

	protected void renderOneComment(final Div commentsListContainer, final CommentModel comment,
			final WidgetInstanceManager widgetInstanceManager)
	{
		this.widgetInstanceManager = widgetInstanceManager;

		final Div newCommentContainer = new Div();
		final HtmlBasedComponent commentHeader = createCommentHeader(comment);
		final HtmlBasedComponent commentContent = createCommentContent(comment);
		newCommentContainer.setSclass(SCLASS_COMMENTSLIST_ONE_COMMENT);
		newCommentContainer.appendChild(commentHeader);
		newCommentContainer.appendChild(commentContent);
		commentsListContainer.appendChild(newCommentContainer);
	}

	protected HtmlBasedComponent createCommentHeader(final CommentModel comment)
	{
		final Label authorLabel = new Label(readAuthor(comment));
		authorLabel.setSclass(SCLASS_COMMENTSLIST_COMMENT_AUTHOR_LABEL);


		final Label dateLabel = new Label(readCreationTime(comment));
		dateLabel.setSclass(SCLASS_COMMENTSLIST_COMMENT_DATE_LABEL);

		final Div headerContainer = new Div();
		headerContainer.setSclass(SCLASS_COMMENTSLIST_COMMENT_HEADER);
		headerContainer.appendChild(authorLabel);
		headerContainer.appendChild(dateLabel);

		return headerContainer;
	}

	protected HtmlBasedComponent createCommentContent(final CommentModel comment)
	{
		final Label textLabel = new Label(readText(comment));
		textLabel.setSclass(SCLASS_COMMENTSLIST_COMMENT_CONTENT);
		textLabel.setMultiline(true);
		return textLabel;
	}

	protected String readAuthor(final CommentModel commentModel)
	{
		if (canReadAuthor(commentModel))
		{
			return labelService.getObjectLabel(commentModel.getAuthor());
		}
		return labelService.getAccessDeniedLabel(commentModel);
	}

	protected String readCreationTime(final CommentModel commentModel)
	{
		if (canReadCreationTime(commentModel))
		{
			final SimpleDateFormat formatter = widgetInstanceManager.getModel().getValue(ItemCommentsController.MODEL_DATE_FORMATTER,
					SimpleDateFormat.class);
			return formatter.format(commentModel.getCreationtime());

		}
		return labelService.getAccessDeniedLabel(commentModel);
	}

	protected String readText(final CommentModel commentModel)
	{
		if (canReadText(commentModel))
		{
			return commentModel.getText();
		}
		return labelService.getAccessDeniedLabel(commentModel);
	}

	protected boolean canReadAuthor(final CommentModel commentModel)
	{
		return permissionFacade.canReadInstanceProperty(commentModel, CommentModel.AUTHOR)
				&& permissionFacade.canReadInstance(commentModel.getAuthor());
	}

	protected boolean canReadText(final CommentModel commentModel)
	{
		return permissionFacade.canReadInstanceProperty(commentModel, CommentModel.TEXT);
	}

	protected boolean canReadCreationTime(final CommentModel commentModel)
	{
		return permissionFacade.canReadInstanceProperty(commentModel, CommentModel.CREATIONTIME);
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
