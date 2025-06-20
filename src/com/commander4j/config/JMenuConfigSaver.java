package com.commander4j.config;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.commander4j.db.JDBFont;
import com.commander4j.sys.Common;

public class JMenuConfigSaver
{

	public boolean save()
	{

		boolean result = false;

		try
		{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// Root element
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("config");
			doc.appendChild(rootElement);

			// Tree
			Element tree = doc.createElement("tree");
			tree.setAttribute("filename", Common.config.getTreeFilename());
			tree.setAttribute("password", Common.config.encodePassword());
			rootElement.appendChild(tree);

			
			// Shell
			Element shell = doc.createElement("shell");
			Element shellScript = doc.createElement("shell_script");
			shellScript.setAttribute("enabled", Common.config.getScriptEnabled());
			shellScript.setAttribute("filename", Common.config.getScriptFilename());
			shell.appendChild(shellScript);
			rootElement.appendChild(shell);

			// Colors
			Element colors = doc.createElement("colors");
			Element color = doc.createElement("color");
			color.setAttribute("id", "terminal");
			color.setAttribute("foreground", Common.config.getColorTerminalForeground());
			color.setAttribute("background", Common.config.getColorTerminalBackground());
			colors.appendChild(color);
			rootElement.appendChild(colors);

			// Environment
			Element environment = doc.createElement("environment");
			for (Map.Entry<String, String> entry : Common.config.getEnvironmentVariables().entrySet())
			{
				Element variable = doc.createElement("variable");
				variable.setAttribute("key", entry.getKey());
				variable.setAttribute("value", entry.getValue());
				environment.appendChild(variable);
			}
			rootElement.appendChild(environment);
			
			// Valid Commands
			Element validate = doc.createElement("validate");
			for (int x=0;x<Common.config.getValidCommands().size();x++)
			{
				Element system_command = doc.createElement("system_command");
				system_command.setAttribute("name", Common.config.getValidCommands().get(x));
				validate.appendChild(system_command);
			}
			rootElement.appendChild(validate);
			
			// Font Preferences
			Element fontPreferences = doc.createElement("fontPreferences");
			for (Map.Entry<String, JDBFont> entry : Common.config.getFontPreferences().entrySet())
			{
				Element fontSetting = doc.createElement("fontSetting");
				fontSetting.setAttribute("id", entry.getKey());
				fontSetting.setAttribute("name", ((JDBFont) entry.getValue()).getName());
				fontSetting.setAttribute("style", ((JDBFont) entry.getValue()).getStyle());
				fontSetting.setAttribute("size", String.valueOf(((JDBFont) entry.getValue()).getSize()));
				fontPreferences.appendChild(fontSetting);
			}
			rootElement.appendChild(fontPreferences);
			
			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Pretty print
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DOMSource source = new DOMSource(doc);
			StreamResult streamResult = new StreamResult(Common.settingsFolderFile);

			transformer.transform(source, streamResult);

			result = true;
		}
		catch (Exception ex)
		{

		}

		return result;

	}
}
