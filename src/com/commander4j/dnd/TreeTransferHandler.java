package com.commander4j.dnd;

import javax.swing.*;
import javax.swing.tree.*;

import com.commander4j.tree.JMenuOption;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;

public class TreeTransferHandler extends TransferHandler
{

	private static final long serialVersionUID = 1L;
	public static DataFlavor NODE_ARRAY_FLAVOR;
	private DefaultMutableTreeNode[] nodesToRemove;
	DataFlavor[] flavors = new DataFlavor[1];

	public TreeTransferHandler()
	{
		try
		{
			String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + JMenuOption[].class.getName() + "\"";
			NODE_ARRAY_FLAVOR = new DataFlavor(mimeType);
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("ClassNotFound: " + e.getMessage());
		}
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		JTree tree = (JTree) c;
		TreePath[] paths = tree.getSelectionPaths();
		if (paths == null)
			return null;

		List<DefaultMutableTreeNode> nodeList = new ArrayList<>();
		for (TreePath path : paths)
		{
			nodeList.add((DefaultMutableTreeNode) path.getLastPathComponent());
		}

		nodesToRemove = nodeList.toArray(new DefaultMutableTreeNode[0]);
		return new NodesTransferable(nodesToRemove);
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		if (!support.isDrop())
			return false;
		if (!support.isDataFlavorSupported(NODE_ARRAY_FLAVOR))
			return false;

		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		TreePath destPath = dl.getPath();
		int childIndex = dl.getChildIndex();
		if (destPath == null)
			return false;

		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) destPath.getLastPathComponent();

		try
		{
			Transferable t = support.getTransferable();
			DefaultMutableTreeNode[] draggedNodes = (DefaultMutableTreeNode[]) t.getTransferData(NODE_ARRAY_FLAVOR);

			for (DefaultMutableTreeNode draggedNode : draggedNodes)
			{
				JMenuOption draggedOption = (JMenuOption) draggedNode.getUserObject();
				if (draggedOption == null)
					return false;

				DefaultMutableTreeNode dropParentNode;
				JMenuOption dropParentOption;

				if (childIndex == -1)
				{
					// Drop directly ON a node
					dropParentNode = targetNode;
					dropParentOption = (JMenuOption) dropParentNode.getUserObject();

					// ❌ Strictly block dropping a leaf ON a leaf
					if ("leaf".equals(dropParentOption.getType()) && "leaf".equals(draggedOption.getType()))
					{
						return false;
					}

					// ❌ Don't allow dropping a branch onto a leaf
					if ("leaf".equals(dropParentOption.getType()) && "branch".equals(draggedOption.getType()))
					{
						return false;
					}

					// ✅ Allow reordering branches within same parent
					if ("branch".equals(draggedOption.getType()))
					{
						DefaultMutableTreeNode draggedParent = (DefaultMutableTreeNode) draggedNode.getParent();
						if (draggedParent != null && draggedParent == dropParentNode.getParent())
						{
							return true;
						}
					}

				}
				else
				{
					// Drop is BETWEEN children — use targetNode as the parent
					dropParentNode = targetNode;
					dropParentOption = (JMenuOption) dropParentNode.getUserObject();

					// ✅ Only allow dropping into branches or root
					if (!"branch".equals(dropParentOption.getType()) && !"root".equals(dropParentOption.getType()))
					{
						return false;
					}

					// ✅ Allow leaf reordering within same parent
					if ("leaf".equals(draggedOption.getType()))
					{
						DefaultMutableTreeNode draggedParent = (DefaultMutableTreeNode) draggedNode.getParent();
						if (draggedParent != null && draggedParent == dropParentNode)
						{
							return true;
						}
						else
						{
							return false;
						}
					}

					// ✅ Allow branch drop (reorder or move) into branch/root
					if ("branch".equals(draggedOption.getType()))
					{
						return true;
					}
				}

				// Shared constraints
				if (draggedNode.isRoot())
					return false;
				if (dropParentNode.isRoot() && "leaf".equals(draggedOption.getType()))
					return false;
				if (draggedNode.isNodeDescendant(dropParentNode))
					return false;
			}

			return true;

		}
		catch (UnsupportedFlavorException | IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		if (!canImport(support))
			return false;

		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		TreePath destPath = dl.getPath();
		int childIndex = dl.getChildIndex();

		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) destPath.getLastPathComponent();
		JTree tree = (JTree) support.getComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

		try
		{
			Transferable t = support.getTransferable();
			DefaultMutableTreeNode[] nodes = (DefaultMutableTreeNode[]) t.getTransferData(NODE_ARRAY_FLAVOR);

			for (DefaultMutableTreeNode node : nodes)
			{
				DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) node.getParent();
				int oldIndex = oldParent != null ? oldParent.getIndex(node) : -1;

				// Normalize index (drop at end)
				if (childIndex == -1)
				{
					childIndex = parent.getChildCount();
				}

				// Adjust if moving within same parent and dropping "after"
				// current position
				if (oldParent == parent && oldIndex != -1 && childIndex > oldIndex)
				{
					childIndex--;
				}

				// Avoid no-op: don't re-insert at same position
				if (oldParent == parent && oldIndex == childIndex)
				{
					return false;
				}

				model.removeNodeFromParent(node);
				model.insertNodeInto(node, parent, childIndex);
			}

			return true;

		}
		catch (UnsupportedFlavorException | IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		if ((action & MOVE) == MOVE && nodesToRemove != null)
		{
			nodesToRemove = null; // Already removed during import
		}
	}

	@SuppressWarnings("unused")
	private boolean isRootNode(DefaultMutableTreeNode node)
	{
		return node.getParent() == null;
	}

	private static class NodesTransferable implements Transferable
	{
		private final DefaultMutableTreeNode[] nodes;

		NodesTransferable(DefaultMutableTreeNode[] nodes)
		{
			this.nodes = nodes;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
			if (!isDataFlavorSupported(flavor))
				throw new UnsupportedFlavorException(flavor);
			return nodes;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[]
			{ NODE_ARRAY_FLAVOR };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return NODE_ARRAY_FLAVOR.equals(flavor);
		}
	}
}
