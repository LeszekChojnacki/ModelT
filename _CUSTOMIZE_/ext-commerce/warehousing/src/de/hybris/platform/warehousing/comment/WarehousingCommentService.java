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
package de.hybris.platform.warehousing.comment;

import de.hybris.platform.comments.model.CommentModel;
import de.hybris.platform.comments.model.CommentTypeModel;
import de.hybris.platform.comments.model.ComponentModel;
import de.hybris.platform.comments.model.DomainModel;
import de.hybris.platform.comments.services.CommentService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentContext;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentEventType;


/**
 * Service to simplify working with the {@link CommentService}.
 *
 * @param <T>
 * 		The type of item that this comment service is used for.
 */
public interface WarehousingCommentService<T extends ItemModel> extends CommentService

{
	/**
	 * Create and save the comment subject, text, code and creation time in the associated item.
	 *
	 * @param context
	 * 		the comment context to process; cannot be <tt>null</tt>
	 * @param code
	 * 		the comment code
	 * @return the created {@link CommentModel}
	 */
	CommentModel createAndSaveComment(WarehousingCommentContext context, final String code);

	/**
	 * Get the component for the requested {@link WarehousingCommentEventType}
	 *
	 * @param eventType
	 * 		- the event type specifying the component code and name
	 * @return the requested component model
	 */
	ComponentModel getOrCreateComponent(final WarehousingCommentEventType eventType);

	/**
	 * Get the comment type for the requested {@link WarehousingCommentEventType}
	 *
	 * @param eventType
	 * 		- the event type specifying the comment type code and name
	 * @return the requested comment type model
	 */
	CommentTypeModel getOrCreateCommentType(final WarehousingCommentEventType eventType);

	/**
	 * Get the domain with code "warehousing"
	 *
	 * @return the domain model
	 */
	DomainModel getOrCreateDomainForCodeWarehousing();
}
