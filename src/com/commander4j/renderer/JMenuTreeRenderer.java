package com.commander4j.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.commander4j.sys.Common;
import com.commander4j.tree.JMenuOption;
import com.commander4j.util.Utility;

public class JMenuTreeRenderer extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1;
	private Utility utils = new Utility();
	private Color rootForeground;
	private Color branchForeground;
	private Color leafForeground;

	public JMenuTreeRenderer()
	{
		setOpaque(true);
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
			rootForeground = utils.fromHex(Common.config.getColorBranchForeground());
			setForeground(rootForeground);
			setFont(Common.config.getFontPreference("branch"));
			setIcon(Common.icon_menuStructure);

			break;
		case "branch":
			branchForeground = utils.fromHex(Common.config.getColorBranchForeground());
			setForeground(branchForeground);
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
			leafForeground = utils.fromHex(Common.config.getColorLeafForeground());
			setForeground(leafForeground);

			setFont(Common.config.getFontPreference("leaf"));
			if (nodeInfo.getIcon().equals(""))
			{
				setIcon(Common.icon_function);
			}
			else
			{
				try
				{
					BufferedImage img = ImageIO.read(new File(Common.iconPath + nodeInfo.getIcon()));
					setIcon(new ImageIcon(img.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
				}
				catch (IOException e)
				{

					setIcon(new ImageIcon(Common.iconPath + nodeInfo.getIcon()));
				}
			}
			break;
		default:
			setIcon(Common.icon_info);
			break;
		}

		if (selected)
		{
			setForeground(Color.white);
			setOpaque(false);
		}
		else
		{
			setOpaque(true);
			setBackground(tree.getBackground());
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