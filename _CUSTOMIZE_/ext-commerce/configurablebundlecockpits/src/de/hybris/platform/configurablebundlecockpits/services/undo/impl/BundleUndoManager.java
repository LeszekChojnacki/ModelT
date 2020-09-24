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

package de.hybris.platform.configurablebundlecockpits.services.undo.impl;

import de.hybris.platform.cockpit.model.undo.UndoableOperation;
import de.hybris.platform.cockpit.model.undo.impl.CannotRedoException;
import de.hybris.platform.cockpit.model.undo.impl.CannotUndoException;
import de.hybris.platform.cockpit.model.undo.impl.ItemChangeUndoableOperation;
import de.hybris.platform.cockpit.services.undo.UndoManager;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Bundle specific implementation of the {@link UndoManager}. It prevents that changes of
 * BundleTemplate.bundleSelectionCriteria are added to the cockpit undo/redo history
 */
public class BundleUndoManager implements UndoManager
{

	private TypeService typeService;

	private final int maxSize;
	private final LinkedList<UndoableOperation> undoStack;
	private final LinkedList<UndoableOperation> redoStack;


	public BundleUndoManager(final int size)
	{
		this.maxSize = size;
		this.undoStack = new LinkedList();
		this.redoStack = new LinkedList();
	}

	public int getMaxSize()
	{
		return this.maxSize;
	}

	@Override
	public synchronized void addOperation(final UndoableOperation operation)
	{
		// if attribute bundleTemplate.bundleSelectionCriteria was changed, do not add this operation to the history
		if (operation instanceof ItemChangeUndoableOperation)
		{
			final AttributeDescriptorModel attrDescriptor = getTypeService().getAttributeDescriptor(BundleTemplateModel._TYPECODE,
					BundleTemplateModel.BUNDLESELECTIONCRITERIA);
			final String attrName = attrDescriptor.getName();
			final ItemChangeUndoableOperation undoOp = (ItemChangeUndoableOperation) operation;
			final String undoPresentationName = undoOp.getUndoPresentationName();

			if (attrName != null && attrName.equals(undoPresentationName))
			{
				return;
			}
		}

		if (this.undoStack.size() >= this.maxSize)
		{
			this.undoStack.pollLast();
		}

		this.undoStack.push(operation);
		this.redoStack.clear();
	}

	@Override
	public boolean canUndo()
	{
		final UndoableOperation operation = this.undoStack.peek();
		return operation != null && operation.canUndo();
	}

	@Override
	public boolean canRedo()
	{
		final UndoableOperation operation = this.redoStack.peek();
		return operation != null && operation.canRedo();
	}

	@Override
	public synchronized void undo() throws CannotUndoException
	{
		if (canUndo())
		{
			final UndoableOperation operation = this.undoStack.pop();
			operation.undo();
			this.redoStack.push(operation);
		}
		else
		{
			throw new CannotUndoException("Unable to perform undo.");
		}
	}

	@Override
	public synchronized void redo() throws CannotRedoException
	{
		if (canRedo())
		{
			final UndoableOperation operation = this.redoStack.pop();
			operation.redo();
			this.undoStack.push(operation);
		}
		else
		{
			throw new CannotRedoException("Unable to perform redo.");
		}
	}

	@Override
	public UndoableOperation peekUndoOperation()
	{
		return this.undoStack.peek();
	}

	@Override
	public UndoableOperation peekRedoOperation()
	{
		return this.redoStack.peek();
	}

	@Override
	public List<UndoableOperation> getUndoOperations()
	{
		return Collections.unmodifiableList(this.undoStack);
	}

	@Override
	public List<UndoableOperation> getRedoOperations()
	{
		return Collections.unmodifiableList(this.redoStack);
	}

	@Override
	public String getUndoPresentationName()
	{
		final UndoableOperation operation = this.undoStack.peek();
		return (operation == null) ? null : operation.getUndoPresentationName();
	}

	@Override
	public String getRedoPresentationName()
	{
		final UndoableOperation operation = this.redoStack.peek();
		return (operation == null) ? null : operation.getRedoPresentationName();
	}

	public synchronized void clear()
	{
		this.undoStack.clear();
		this.redoStack.clear();
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
