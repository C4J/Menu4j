package com.commander4j.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import com.commander4j.tree.JMenuOption;

public class JTreeNode4j extends DefaultMutableTreeNode
{

	private static final long serialVersionUID = 1L;
	

    public String getToolTip() 
    {
        return ((JMenuOption) this.getUserObject()).getHint();
    }

}
