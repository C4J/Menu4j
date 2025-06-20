package com.commander4j.tree;

import org.w3c.dom.*;

import com.commander4j.menu.JMenuTree;
import com.commander4j.sys.Common;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.io.File;
import java.util.LinkedList;

public class JMenuTreeLoader {

	public void loadTree(JMenuTree m)
	{
		try
		{
			DefaultMutableTreeNode newRoot = buildTreeFromXml(Common.treeFolderFile);
			Common.treeModel = new DefaultTreeModel(newRoot);
			Common.treeChanged = false;
			Common.treeModel.addTreeModelListener(new TreeModelListener()
			{
				@Override
				public void treeNodesChanged(TreeModelEvent e)
				{
					Common.treeChanged = true;
				}

				@Override
				public void treeNodesInserted(TreeModelEvent e)
				{
					Common.treeChanged = true;
				}

				@Override
				public void treeNodesRemoved(TreeModelEvent e)
				{
					Common.treeChanged = true;
				}

				@Override
				public void treeStructureChanged(TreeModelEvent e)
				{
					Common.treeChanged = true;
				}
			});

			Common.tree.setModel(Common.treeModel);
			
			Common.treeChanged=false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

		}

		m.setFrameTitle();

		Common.treeState.loadTreeState(Common.tree);
	}
	
    public DefaultMutableTreeNode buildTreeFromXml(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        Element rootElement = doc.getDocumentElement();
        if (!"tree".equals(rootElement.getAttribute("type"))) {
            throw new IllegalArgumentException("Root element is not of expected type 'tree'");
        }
        
        JMenuOption menuroot = new JMenuOption();
        menuroot.setType("root");
        menuroot.setDescription(rootElement.getAttribute("description"));
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(menuroot);
        walkNodes(rootElement, rootNode);
        return rootNode;
    }

    private void walkNodes(Node parent, DefaultMutableTreeNode treeNode) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("node[@type]");
        NodeList nodeList = (NodeList) expr.evaluate(parent, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
            	
                Element elem = (Element) node;
                
            	JMenuOption menunode = new JMenuOption();
            	
            	menunode.setType(elem.getAttribute("type"));
            	menunode.setDescription(elem.getAttribute("description"));
            	menunode.setIcon(elem.getAttribute("icon"));
            	
            	menunode.setCommand(getChildValue(elem, "command"));
            	menunode.setConfirmExecute(Boolean.valueOf(getChildValue(elem,"confirmExecute")));
            	menunode.setShellScriptRequired(getChildValue(elem, "shell_script_required"));
            	menunode.setTerminalWindowRequired(getChildValue(elem, "terminal_window_required"));
            	menunode.setHint(getChildValue(elem, "hint"));
            	menunode.setDirectory(getChildValue(elem, "directory"));
            	menunode.setRedirectInput(getChildValue(elem, "redirectInput"));
            	menunode.setRedirectOutput(getChildValue(elem, "redirectOutput"));
            	menunode.setLinkToMenuTreeEnabled(getChildValue(elem,"link_to_menu_tree_enabled"));
            	menunode.setMenuTreeFilename(getChildValue(elem,"menu_tree_filename"));
            	menunode.setParameters(getChildParams(elem));  // <-- New line here
            	
                DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(menunode);

                treeNode.add(childTreeNode);
 
                if ("branch".equals(menunode.getType())) {
                    walkNodes(node, childTreeNode);
                }
            }
        }
    }
    
    private String getChildValue(Element elem, String key) {
        String result = "";
        NodeList children = elem.getElementsByTagName(key);
        if (children.getLength() > 0) {
            Element childElement = (Element) children.item(0);
            if (childElement != null) {
                result = childElement.getTextContent().trim();
            }
        }
        return result;
    }
    
    private LinkedList<String> getChildParams(Element elem) {
        LinkedList<String> paramsList = new LinkedList<>();
        
        NodeList paramsNodes = elem.getElementsByTagName("params");
        if (paramsNodes.getLength() > 0) {
            Element paramsElement = (Element) paramsNodes.item(0);
            if (paramsElement != null) {
                NodeList paramNodes = paramsElement.getElementsByTagName("param");
                for (int i = 0; i < paramNodes.getLength(); i++) {
                    Element paramElement = (Element) paramNodes.item(i);
                    if (paramElement != null) {
                        String value = paramElement.getTextContent().trim();
                        if (!value.isEmpty()) {  // Optional: ignore empty param
                            paramsList.add(value);
                        }
                    }
                }
            }
        }
        
        return paramsList;  // Always returns non-null (empty list if none found)
    }
}
