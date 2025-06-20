package com.commander4j.tree;

import org.w3c.dom.*;

import com.commander4j.menu.JMenuTree;
import com.commander4j.sys.Common;

import javax.swing.JOptionPane;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class JMenuTreeSaver
{

	public void saveTree(JMenuTree m)
	{

		try
		{
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) Common.tree.getModel().getRoot();

			Document doc = JMenuTreeSaver.buildXmlFromTree(root);

			JMenuTreeSaver.saveXmlToFile(doc, Common.treeFolderFile);
			
			Common.configSaver.save();
			
			Common.treeChanged = false;

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(m, "Failed to save XML: " + ex.getMessage());
		}
		m.setFrameTitle();

		Common.treeState.saveTreeState();

	}
	
	public static Document buildXmlFromTree(DefaultMutableTreeNode rootTreeNode) throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();

		// Create the <root type="tree"> element
		Element rootElement = doc.createElement("root");
		rootElement.setAttribute("type", "tree");

		JMenuOption nodeInfo = (JMenuOption) (rootTreeNode.getUserObject());
		rootElement.setAttribute("description", nodeInfo.getDescription());
		doc.appendChild(rootElement);

		// Recursively process children of the tree root
		for (int i = 0; i < rootTreeNode.getChildCount(); i++)
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootTreeNode.getChildAt(i);
			buildNodeElement(doc, rootElement, child);
		}

		return doc;
	}

	private static void buildNodeElement(Document doc, Element parentElement, DefaultMutableTreeNode treeNode)
	{

		JMenuOption nodeInfo = (JMenuOption) (treeNode.getUserObject());

		Element nodeElement = doc.createElement("node");
		nodeElement.setAttribute("type", nodeInfo.getType());
		nodeElement.setAttribute("description", nodeInfo.getDescription());
		nodeElement.setAttribute("icon", nodeInfo.getIcon());

		if (nodeInfo.getType().equals("leaf"))
		{
			Element elementDirectory = doc.createElement("directory");
			elementDirectory.setTextContent(nodeInfo.getDirectory());

			Element elementCommand = doc.createElement("command");
			elementCommand.setTextContent(nodeInfo.getCommand());
			
			Element elementConfirmExecute = doc.createElement("confirmExecute");
			elementConfirmExecute.setTextContent(String.valueOf(nodeInfo.isConfirmExecute()));
			
			Element elementShellScript = doc.createElement("shell_script_required");
			elementShellScript.setTextContent(nodeInfo.getShellScriptRequired());
			
			Element elementMenuTreeEnabled = doc.createElement("link_to_menu_tree_enabled");
			elementMenuTreeEnabled.setTextContent(nodeInfo.getLinkToMenuTreeEnabled());
			
			Element elementMenuTreeFilename = doc.createElement("menu_tree_filename");
			elementMenuTreeFilename.setTextContent(nodeInfo.getMenuTreeFilename());
			
			Element elementTerminal = doc.createElement("terminal_window_required");
			elementTerminal.setTextContent(nodeInfo.getTerminalWindowRequired());

			Element elementHint = doc.createElement("hint");
			elementHint.setTextContent(nodeInfo.getHint());

			Element elementredirectInput = doc.createElement("redirectInput");
			elementredirectInput.setTextContent(nodeInfo.getRedirectInput());

			Element elementredirectOutput = doc.createElement("redirectOutput");
			elementredirectOutput.setTextContent(nodeInfo.getRedirectOutput());

			Element elementparams = doc.createElement("params");

			for (int x = 0; x < nodeInfo.getParameters().size(); x++)
			{
				Element elementparam = doc.createElement("param");
				elementparam.setTextContent(nodeInfo.getParameters().get(x));
				elementparams.appendChild(elementparam);
			}

			nodeElement.appendChild(elementDirectory);
			nodeElement.appendChild(elementCommand);
			nodeElement.appendChild(elementConfirmExecute);
			nodeElement.appendChild(elementShellScript);
			nodeElement.appendChild(elementMenuTreeEnabled);
			nodeElement.appendChild(elementMenuTreeFilename);
			nodeElement.appendChild(elementTerminal);
			nodeElement.appendChild(elementHint);
			nodeElement.appendChild(elementredirectInput);
			nodeElement.appendChild(elementredirectOutput);
			nodeElement.appendChild(elementparams);
		}

		parentElement.appendChild(nodeElement);

		if (treeNode.getChildCount() > 0)
		{
			for (int i = 0; i < treeNode.getChildCount(); i++)
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
				buildNodeElement(doc, nodeElement, child);
			}
		}
	}

	public static void saveXmlToFile(Document doc, File file) throws TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		// Pretty-print XML
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}
}
