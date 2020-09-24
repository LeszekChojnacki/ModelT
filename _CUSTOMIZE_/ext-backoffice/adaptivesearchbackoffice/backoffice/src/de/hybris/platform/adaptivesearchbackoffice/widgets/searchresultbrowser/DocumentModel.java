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
package de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser;

import de.hybris.platform.adaptivesearch.data.AsDocumentData;
import de.hybris.platform.core.PK;

import java.io.Serializable;


/**
 * View model for the document data
 */
public class DocumentModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int index;
	private Float score;
	private PK pk;
	private AsDocumentData document;
	private boolean promoted;
	private String promotedItemUid;
	private boolean highlight;
	private boolean showOnTop;
	private boolean fromSearchProfile;
	private boolean fromSearchConfiguration;
	private boolean override;
	private boolean overrideFromSearchProfile;
	private String styleClass;

	public int getIndex()
	{
		return index;
	}

	public void setIndex(final int index)
	{
		this.index = index;
	}

	public Float getScore()
	{
		return score;
	}

	public void setScore(final Float score)
	{
		this.score = score;
	}

	public PK getPk()
	{
		return pk;
	}

	public void setPk(final PK pk)
	{
		this.pk = pk;
	}

	public AsDocumentData getDocument()
	{
		return document;
	}

	public void setDocument(final AsDocumentData document)
	{
		this.document = document;
	}

	public String getPromotedItemUid()
	{
		return promotedItemUid;
	}

	public void setPromotedItemUid(final String promotedItemUid)
	{
		this.promotedItemUid = promotedItemUid;
	}

	public boolean isPromoted()
	{
		return promoted;
	}

	public void setPromoted(final boolean promoted)
	{
		this.promoted = promoted;
	}

	public boolean isFromSearchProfile()
	{
		return fromSearchProfile;
	}

	public void setFromSearchProfile(final boolean fromSearchProfile)
	{
		this.fromSearchProfile = fromSearchProfile;
	}

	public boolean isFromSearchConfiguration()
	{
		return fromSearchConfiguration;
	}

	public void setFromSearchConfiguration(final boolean fromSearchConfiguration)
	{
		this.fromSearchConfiguration = fromSearchConfiguration;
	}

	public boolean isOverride()
	{
		return override;
	}

	public void setOverride(final boolean override)
	{
		this.override = override;
	}

	public boolean isOverrideFromSearchProfile()
	{
		return overrideFromSearchProfile;
	}

	public void setOverrideFromSearchProfile(final boolean overrideFromSearchProfile)
	{
		this.overrideFromSearchProfile = overrideFromSearchProfile;
	}

	public String getStyleClass()
	{
		return styleClass;
	}

	public void setStyleClass(final String styleClass)
	{
		this.styleClass = styleClass;
	}

	public boolean isShowOnTop()
	{
		return showOnTop;
	}

	public void setShowOnTop(final boolean showOnTop)
	{
		this.showOnTop = showOnTop;
	}

	public boolean isHighlight()
	{
		return highlight;
	}

	public void setHighlight(final boolean highlight)
	{
		this.highlight = highlight;
	}
}
