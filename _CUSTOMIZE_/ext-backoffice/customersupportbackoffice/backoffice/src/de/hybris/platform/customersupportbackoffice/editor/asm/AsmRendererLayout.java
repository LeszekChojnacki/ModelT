/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.editor.asm;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.util.Config;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.hybris.cockpitng.core.config.impl.jaxb.hybris.Base;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.editor.commonreferenceeditor.AbstractReferenceEditor;
import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorLayout;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.util.YTestTools;


/**
 * Layout for com.hybris.cockpitng.editor.asmdeeplinkeditor editor.
 */
public class AsmRendererLayout<T, K> extends ReferenceEditorLayout<T>
{
	private static final String HTTP_POSTFIX = ".https";
	private static final String LINK_PREFIX = "website.";
	private final AbstractReferenceEditor<T, K> referenceEditor;

	protected static final String CSS_REFERENCE_EDITOR_REMOVE_BTN = "ye-default-reference-editor-remove-button";
	protected static final String CSS_REFERENCE_EDITOR_SELECTED_ITEM_LABEL = "ye-default-reference-editor-selected-item-label";
	protected static final String CSS_REFERENCE_EDITOR_SELECTED_ITEM_CONTAINER = "ye-default-reference-editor-selected-item-container";

	protected static final String YTESTID_REMOVE_BUTTON = "reference-editor-remove-button";
	protected static final String YTESTID_REFERENCE_ENTRY = "reference-editor-reference";

	protected static final String ASM_DEEPLINK_PARAM = "assistedservicestorefront.deeplink.link";

	protected static final String CART = "customersupport_backoffice_asm_cart_prefix";
	protected static final String ORDER = "customersupport_backoffice_asm_order_prefix";

	private final UrlResolver<ProductModel> productModelUrlResolver;
	protected boolean isSimpleSelectEnabled;
	protected boolean showAsmPrefix;
	protected String asmForwardURL;
	protected BaseSiteService baseSiteService;


	public AsmRendererLayout(final AbstractReferenceEditor<T, K> referenceEditorInterface, final Base configuration,
			final UrlResolver<ProductModel> productModelUrlResolver, final BaseSiteService baseSiteService)
	{
		super(referenceEditorInterface, configuration);
		this.productModelUrlResolver = productModelUrlResolver;
		this.referenceEditor = referenceEditorInterface;
		this.baseSiteService = baseSiteService;
	}

	@Override
	protected ListitemRenderer<T> createSelectedItemsListItemRenderer()
	{
		return new ListitemRenderer<T>() //NOSONAR
		{
			@Override
			public void render(final Listitem item, final T data, final int index) //NOSONAR
			{
				final StringBuilder stringRepresentationOfObject = new StringBuilder();
				if (showAsmPrefix && data instanceof CartModel)
				{
					stringRepresentationOfObject.append(Labels.getLabel(CART));
				}
				if (showAsmPrefix && data instanceof OrderModel)
				{
					stringRepresentationOfObject.append(Labels.getLabel(ORDER));
				}
				stringRepresentationOfObject.append(" ").append(referenceEditor.getStringRepresentationOfObject(data));

				final Label label = new Label(stringRepresentationOfObject.toString());
				label.setSclass(CSS_REFERENCE_EDITOR_SELECTED_ITEM_LABEL);
				YTestTools.modifyYTestId(label, YTESTID_REFERENCE_ENTRY);
				label.setMultiline(true);

				final Div removeImage = new Div();
				removeImage.setSclass(CSS_REFERENCE_EDITOR_REMOVE_BTN);
				YTestTools.modifyYTestId(removeImage, YTESTID_REMOVE_BUTTON);
				removeImage.setVisible(referenceEditor.isEditable());
				removeImage.addEventListener(Events.ON_CLICK, event -> referenceEditor.removeSelectedObject(data));

				final Div layout = new Div();
				layout.setSclass(CSS_REFERENCE_EDITOR_SELECTED_ITEM_CONTAINER);
				final Listcell cell = new Listcell();

				UITools.modifySClass(label, "ye-editor-disabled", true);
				layout.appendChild(label);

				if (StringUtils.isNotBlank(Config.getParameter(ASM_DEEPLINK_PARAM)))
				{
					layout.setSclass(CSS_REFERENCE_EDITOR_SELECTED_ITEM_CONTAINER + " ye-default-asm-deep-link-container");
					appendAsmLinkButton(layout, data);
				}

				if (!referenceEditor.isDisableRemoveReference())
				{
					layout.appendChild(removeImage);
					UITools.modifySClass(layout, "ye-remove-enabled", true);
				}

				if (!isSimpleSelectEnabled && !referenceEditor.isDisableDisplayingDetails())
				{
					cell.addEventListener(Events.ON_DOUBLE_CLICK, event -> referenceEditor.triggerReferenceSelected(
							new TypeAwareSelectionContext(data, getSelectedElementsListModel().getInnerList())));
				}

				if (isSimpleSelectEnabled && !referenceEditor.isDisableDisplayingDetails())
				{
					cell.addEventListener(Events.ON_DOUBLE_CLICK,
							event -> referenceEditor.sendOutput(CustomersupportbackofficeConstants.SIMPLE_SELECT_OUT_SOCKET_ID, data));
				}

				cell.appendChild(layout);
				cell.setParent(item);
			}

			protected void appendAsmLinkButton(final Div layout, final Object data)
			{
				final Div asmImageWrapper = new Div();
				asmImageWrapper.setSclass("ye-default-asm-deep-link");
				asmImageWrapper.addEventListener(Events.ON_CLICK, event -> {
					final Object parentObject = referenceEditor.getParentObject();
					final String deepLink;
					if (parentObject instanceof QuoteModel)
					{
						deepLink = getAsmLink(parentObject);
					}
					else
					{
						deepLink = getAsmLink(data);
					}
					Executions.getCurrent().sendRedirect(deepLink, "_blank");
				});
				layout.appendChild(asmImageWrapper);
			}

			protected String getAsmLink(final Object data)
			{
				if (data instanceof AbstractOrderModel)
				{
					final AbstractOrderModel order = (AbstractOrderModel) data;
					final String typeCode = getTypeCode(data);
					if (order.getSite() != null && typeCode != null)
					{
						final StringBuilder asmDeepLink = new StringBuilder(
								Config.getParameter(LINK_PREFIX + order.getSite().getUid() + HTTP_POSTFIX));
						if (null != getAsmForwardURL())
						{
							//redirect to specific page
							asmDeepLink.append(Config.getParameter(ASM_DEEPLINK_PARAM)).append("?customerId=")
									.append(order.getUser().getUid()).append("&fwd=").append(getAsmForwardURL());
						}
						else
						{
							asmDeepLink.append(Config.getParameter(ASM_DEEPLINK_PARAM)).append("?customerId=")
									.append(order.getUser().getUid()).append("&").append(typeCode).append("Id=").append(order.getGuid());
						}
						if (data instanceof QuoteModel)
						{
							asmDeepLink.append(((QuoteModel) data).getCode());
						}
						return asmDeepLink.toString();
					}
				}
				else if (data instanceof CustomerReviewModel)
				{
					final CustomerReviewModel reviewModel = (CustomerReviewModel) data;

					final Stream<BaseSiteModel> filtered = baseSiteService.getAllBaseSites().stream()
							.filter(site -> !baseSiteService.getProductCatalogs(site).stream()
									.filter(cm -> cm.getId().equals(reviewModel.getProduct().getCatalogVersion().getCatalog().getId()))
									.collect(Collectors.toList()).isEmpty());

					final Optional<BaseSiteModel> first = filtered.findFirst();
					if (!first.isPresent())
					{
						return null;
					}
					final BaseSiteModel siteModel = first.get();
					final StringBuilder asmDeepLink = new StringBuilder(
							Config.getParameter(LINK_PREFIX + siteModel.getUid() + HTTP_POSTFIX));
					asmDeepLink.append(productModelUrlResolver.resolve(reviewModel.getProduct()));
					return asmDeepLink.toString();
				}
				else if (data instanceof ProductModel)
				{
					final ProductModel productModel = (ProductModel) data;

					final Stream<BaseSiteModel> filtered = baseSiteService.getAllBaseSites().stream()
							.filter(site -> !baseSiteService.getProductCatalogs(site).stream()
									.filter(cm -> cm.getId().equals(productModel.getCatalogVersion().getCatalog().getId()))
									.collect(Collectors.toList()).isEmpty());

					final Optional<BaseSiteModel> first = filtered.findFirst();
					if (!first.isPresent())
					{
						return null;
					}
					final BaseSiteModel siteModel = first.get();

					final StringBuilder asmDeepLink = new StringBuilder(
							Config.getParameter(LINK_PREFIX + siteModel.getUid() + HTTP_POSTFIX));
					asmDeepLink.append(productModelUrlResolver.resolve(productModel));
					return asmDeepLink.toString();
				}
				return null;
			}

			protected String getTypeCode(final Object data)
			{
				if (data instanceof CartModel)
				{
					return "cart";
				}
				else if (data instanceof OrderModel)
				{
					return "order";
				}
				else if (data instanceof QuoteModel)
				{
					return QuoteModel._TYPECODE;
				}
				return null;
			}
		};
	}

	public boolean isSimpleSelectEnabled()
	{
		return isSimpleSelectEnabled;
	}

	protected void setSimpleSelectEnabled(final boolean isSimpleSelectEnabled)
	{
		this.isSimpleSelectEnabled = isSimpleSelectEnabled;
	}

	public boolean isShowAsmPrefix()
	{
		return showAsmPrefix;
	}

	protected void setShowAsmPrefix(final boolean showAsmPrefix)
	{
		this.showAsmPrefix = showAsmPrefix;
	}

	public String getAsmForwardURL()
	{
		return asmForwardURL;
	}

	protected void setAsmForwardURL(final String asmForwardURL)
	{
		this.asmForwardURL = asmForwardURL;
	}
}
