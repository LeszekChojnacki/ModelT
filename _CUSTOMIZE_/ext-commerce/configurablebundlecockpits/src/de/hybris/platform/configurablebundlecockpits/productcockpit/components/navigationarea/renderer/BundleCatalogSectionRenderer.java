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

package de.hybris.platform.configurablebundlecockpits.productcockpit.components.navigationarea.renderer;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cockpit.components.navigationarea.renderer.AbstractNavigationAreaSectionRenderer;
import de.hybris.platform.cockpit.components.notifier.Notification;
import de.hybris.platform.cockpit.components.sectionpanel.Section;
import de.hybris.platform.cockpit.components.sectionpanel.SectionPanel;
import de.hybris.platform.cockpit.components.sync.dialog.ManySourceManyTargetVersionSyncDialog;
import de.hybris.platform.cockpit.services.sync.SynchronizationService;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.BaseUICockpitPerspective;
import de.hybris.platform.cockpit.util.UITools;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.productcockpit.components.navigationarea.CatalogNavigationAreaModel;
import de.hybris.platform.productcockpit.components.navigationarea.renderer.CatalogSectionRenderer;
import de.hybris.platform.productcockpit.services.catalog.CatalogService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;


/**
 * Renderer for catalog section in bundle perspective
 * 
 */
public class BundleCatalogSectionRenderer extends AbstractNavigationAreaSectionRenderer
{
	private static final String CATALOG_SECTION_CONTAINER_CLASS = "catalog_section_container";
	private CatalogService productCockpitCatalogService;
	private SynchronizationService synchronizationService;
	private CommonI18NService commonI18NService;

	@Override
	public CatalogNavigationAreaModel getSectionPanelModel()
	{
		return (CatalogNavigationAreaModel) super.getSectionPanelModel();
	}

	protected Menupopup createContextMenu(final Component parent, final Listbox listbox)
	{
		final Menupopup popupMenu = new Menupopup();
		final Menuitem menuItem = new Menuitem(Labels.getLabel("sync.contextmenu.version"));
		UITools.addBusyListener(menuItem, Events.ON_CLICK, new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception //NOPMD: ZK Specific
			{
				final CatalogVersionModel selectCatalogVersion = getSectionPanelModel().getNavigationArea()
						.getSelectedCatalogVersion();

				if (selectCatalogVersion != null)
				{
					final BaseUICockpitPerspective perspective = (BaseUICockpitPerspective) getNavigationArea().getPerspective();
					final List<CatalogVersionModel> selectedCatalogVersions = Collections.singletonList(selectCatalogVersion);
					final Map<String, String>[] rules = getSynchronizationService().getAllSynchronizationRules(
							Collections.singletonList(selectCatalogVersion));

					if (rules[0].size() != 1)
					{
						final ManySourceManyTargetVersionSyncDialog dialog = new ManySourceManyTargetVersionSyncDialog(rules,
								selectedCatalogVersions)
						{
							@Override
							public void updateBackground(final List<String> chosenRules)
							{
								perspective.getBrowserArea().update();
								sendNotification(perspective, chosenRules);
							}
						};
						dialog.addEventListener(Events.ON_OPEN, new EventListener()
						{
							@Override
							public void onEvent(final Event dialogEvent) throws Exception //NOPMD: ZK Specific
							{
								if (dialogEvent instanceof OpenEvent)
								{
									if (!((OpenEvent) dialogEvent).isOpen())
									{
										dialog.detach();
									}
								}
							}
						});
						parent.appendChild(dialog);
						dialog.doHighlighted();

					}
					else
					// there is only one rule - perform synchronization without dialog
					{
						getSynchronizationService().performCatalogVersionSynchronization(selectedCatalogVersions, null, null, null);

						final List<String> versions = new ArrayList<String>();

						for (final Object item : listbox.getSelectedItems())
						{
							versions.add((String) ((Listitem) item).getAttribute("name"));
						}
						perspective.getBrowserArea().update();
						sendNotification(perspective, versions);

					}

				}
			}
		}, null, "busy.sync");
		menuItem.setParent(popupMenu);

		return popupMenu;
	}

	@Override
	public void render(final SectionPanel panel, final Component parent, final Component captionComponent, final Section section)
	{
		final Div container = new Div();
		container.setSclass(CATALOG_SECTION_CONTAINER_CLASS);
		parent.appendChild(container);
		final List<CatalogVersionModel> versions = getSectionPanelModel().getNavigationArea().getCatalogVersions();
		final Listbox listbox = createList("navigation_catalogues", versions, new CatalogSectionSingleItemRenderer());
		listbox.setFixedLayout(true);

		final Menupopup ctxMenu = createContextMenu(container, listbox);
		parent.appendChild(ctxMenu);
		listbox.setContext(ctxMenu);
		listbox.setParent(container);

		// get the Staged catalog version (usually the one being not active)
		if (getSectionPanelModel().getNavigationArea().getSelectedCatalogVersion() == null)
		{
			selectionChanged(getNonActiveCatalogVersionModel(versions));
		}

		UITools.addBusyListener(listbox, Events.ON_SELECT, new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception //NOPMD: ZK Specific
			{
				selectionChanged((CatalogVersionModel) listbox.getModel().getElementAt(listbox.getSelectedIndex()));
			}

		}, null, null);
	}


	/**
	 * Custom renderer in order to display items of listbox
	 * 
	 * @author Karol Walczak <karol.walczak@hybris.com>
	 */
	protected class CatalogSectionSingleItemRenderer implements ListitemRenderer
	{
		@Override
		public void render(final Listitem item, final Object data) throws Exception //NOPMD: ZK Specific
		{
			String label = null;
			String mnemonicLabel = null;
			String labelLanguage = null;
			String sclass = null;
			String catalogname = "";
			final Listcell listCell = new Listcell();

			if (UISessionUtils.getCurrentSession().isUsingTestIDs())
			{
				final String id = "NavigationArea_Catalog_item_element";
				UITools.applyTestID(listCell, id);
				listCell.getUuid();
			}

			final CatalogVersionModel catalogVersion = (CatalogVersionModel) data;

			if (getSectionPanelModel().getNavigationArea().getSelectedCatalogVersion() != null)
			{
				final boolean selected = getSectionPanelModel().getNavigationArea().getSelectedCatalogVersion().equals(data);
				if (selected)
				{
					listCell.setFocus(selected);
					item.setSelected(selected);
					((Listbox) item.getParent()).setSelectedIndex(item.getIndex());
				}
			}

			final CatalogModel catalog = getSectionPanelModel().getNavigationArea().getProductCockpitCatalogService()
					.getCatalog(catalogVersion);
			final String langIso = UISessionUtils.getCurrentSession().getGlobalDataLanguageIso();
			final LanguageModel languageModel = getCommonI18NService().getLanguage(langIso);
			final Locale locale = getCommonI18NService().getLocaleForLanguage(languageModel);

			if (catalog.getName(locale) != null)
			{
				catalogname = catalog.getName(locale);
			}
			else
			{
				final List<String> result = UITools.searchForLabel(catalog, CatalogModel.class.getMethod("getName", Locale.class),
						catalogVersion.getLanguages());
				if (result.size() == 2)
				{
					catalogname = result.get(0);
					labelLanguage = result.get(1);
				}
			}
			label = (catalogname != null ? catalogname : "<" + catalog.getId() + ">") + " " + catalogVersion.getVersion();
			item.setAttribute("name", label);
			mnemonicLabel = catalogVersion.getMnemonic();
			sclass = catalogVersion.getActive().booleanValue() ? "activeCatalogVersionTreeItem" : "catalogVersionTreeItem";
			listCell.setLabel(label);

			UITools.modifySClass(listCell, sclass, true);

			if (mnemonicLabel != null)
			{
				final Label mnemLabel = new Label(" (" + mnemonicLabel + ")");
				mnemLabel.setSclass("catalog-mnemonic-label");
				listCell.appendChild(mnemLabel);
			}

			if (labelLanguage != null)
			{
				final Label langLabel = new Label(" [" + labelLanguage + "]");
				langLabel.setSclass("catalog-language-label");
				listCell.appendChild(langLabel);
			}

			UITools.addDragHoverClickEventListener(listCell, new EventListener()
			{
				@Override
				public void onEvent(final Event event) throws Exception //NOPMD: ZK Specific
				{
					if (!item.equals(item.getListbox().getSelectedItem()))
					{
						item.getListbox().setSelectedItem(item);
						Events.postEvent(Events.ON_SELECT, item.getListbox(), null);
					}
				}
			}, 500, BaseUICockpitPerspective.DRAG_DROP_ID);

			item.appendChild(listCell);
		}
	}


	/**
	 * @deprecated Since 5.0.1 use {@link CatalogSectionRenderer#selectionChanged(CatalogVersionModel)}
	 */
	@Deprecated
	protected void selectionChanged(final CatalogVersionModel catalogVersion, @SuppressWarnings("unused") final Listbox listbox)
	{
		if (catalogVersion == null)
		{
			return;
		}
		else
		{
			getSectionPanelModel().getNavigationArea().setSelectedCatalogItems(catalogVersion);
		}
	}

	@SuppressWarnings("unchecked")
	protected void selectionChanged(final CatalogVersionModel catalogVersion)
	{
		if (catalogVersion == null)
		{
			return;
		}
		else
		{
			getSectionPanelModel().getNavigationArea().setSelectedCatalogItems(catalogVersion);
		}
	}


	protected void sendNotification(final BaseUICockpitPerspective perspective, final List<String> chosenRules)
	{
		final StringBuilder detailInformation = new StringBuilder();
		for (final String chosenRule : chosenRules)
		{
			detailInformation.append(", " + chosenRule);
		}
		detailInformation.append(" ");
		final Notification notification = new Notification(Labels.getLabel("synchronization.finished.start")
				+ detailInformation.substring(1) + Labels.getLabel("synchronization.finished.end"));
		if (perspective.getNotifier() != null)
		{
			perspective.getNotifier().setNotification(notification);
		}
	}

	protected CatalogVersionModel getNonActiveCatalogVersionModel(final List<CatalogVersionModel> versions)
	{

		CatalogVersionModel defaultModel = null;
		for (final CatalogVersionModel model : versions)
		{
			// return the model which is currently NOT active - this would usually be the Staged catalog
			if (!model.getActive().booleanValue())
			{
				defaultModel = model;
			}
		}

		return defaultModel;

	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setProductCockpitCatalogService(final CatalogService productCockpitCatalogService)
	{
		this.productCockpitCatalogService = productCockpitCatalogService;
	}

	protected CatalogService getProductCockpitCatalogService()
	{
		return productCockpitCatalogService;
	}


	@Required
	public void setSynchronizationService(final SynchronizationService synchronizationService)
	{
		this.synchronizationService = synchronizationService;
	}

	protected SynchronizationService getSynchronizationService()
	{
		return synchronizationService;
	}

}
