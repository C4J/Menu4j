package com.commander4j.renderer;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.commander4j.sys.Common;
import com.commander4j.tree.JMenuOption;

public class JMenuTreeRenderer extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1;

	public JMenuTreeRenderer()
	{

	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());

		String type = nodeInfo.getType();

		switch (type)
		{
		case "root":
			setFont(Common.config.getFontPreference("branch"));
			setIcon(Common.icon_menuStructure);
			break;
		case "branch":
			setFont(Common.config.getFontPreference("branch"));
			if (expanded)
			{
				setIcon(Common.icon_branchOpen);
			}
			else
			{

				setIcon(Common.icon_branchClose);
			}

			break;
		case "leaf":
			setFont(Common.config.getFontPreference("leaf"));
			if (nodeInfo.getIcon().equals(""))
			{
				setIcon(Common.icon_function);
			}
			else
			{
				setIcon(new ImageIcon(Common.iconPath + nodeInfo.getIcon()));
			}
			break;
		default:
			setIcon(Common.icon_info);
			break;
		}

		if (nodeInfo.getHint().equals(""))
		{
			setToolTipText(null);
		}
		else
		{
			setToolTipText(nodeInfo.getHint());
		}

		return this;
	}

	protected String getMenuItemType(Object value)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());
		String type = nodeInfo.getType();
		return type;
	}

	protected String getMenuHint(Object value)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());
		String hint = nodeInfo.getHint();
		return hint;

	}

	protected String getMenuIconFilename(Object value)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());
		String icon = nodeInfo.getIcon();
		return icon;
	}
}