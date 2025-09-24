package com.commander4j.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.commander4j.db.JDBFont;
import com.commander4j.sys.Common;
import com.commander4j.util.Utility;

public class JMenuConfigLoader
{

	public static JMenuConfig load()
	{
		JMenuConfig config = new JMenuConfig();
		Utility utils = new Utility();

		try
		{
			Map<String, String> envVars = new HashMap<>();
			Map<String, JDBFont> fontPrefs = new HashMap<>();
			
			LinkedList<String> validCommands = new LinkedList<String>();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(Common.settingsFolderFile);

			doc.getDocumentElement().normalize();
			
			NodeList treeNodes = doc.getElementsByTagName("tree");
			Element tree = (Element) treeNodes.item(0);
			config.setPassword(config.decodedPassword(tree.getAttribute("password")));
			
			if (treeNodes.getLength() > 0)
			{
				Element shellScript = (Element) treeNodes.item(0);
				config.setTreeFilename(shellScript.getAttribute("filename"));
				
			}

			// Parse <shell>
			NodeList shellNodes = doc.getElementsByTagName("shell_script");
			if (shellNodes.getLength() > 0)
			{
				Element shellScript = (Element) shellNodes.item(0);
				config.setScriptFilename(shellScript.getAttribute("filename"));
				config.setScriptEnabled(shellScript.getAttribute("enabled"));
			}

			// Parse <colors>
			NodeList colorNodes = doc.getElementsByTagName("color");
			if (colorNodes.getLength() > 0)
			{
				for (int i = 0; i < colorNodes.getLength(); i++)
				{
					Element var = (Element) colorNodes.item(i);
					String id = var.getAttribute("id");
	
					switch (id)
					{
					case "terminal":
						config.setColorTerminalForground(var.getAttribute("foreground"));
						config.setColorTerminalBackground(var.getAttribute("background"));
						break;
						
					case "leaf":
						config.setColorLeafForegound(var.getAttribute("foreground"));
						break;
						
					case "branch":
						config.setColorBranchForeground(var.getAttribute("foreground"));
						break;
					}
					
				}
				
			}
			
			if (utils.replaceNullStringwithBlank(config.getColorLeafForeground()).equals(""))
			{
				config.setColorLeafForegound("#000000");
			}
			
			if (utils.replaceNullStringwithBlank(config.getColorBranchForeground()).equals(""))
			{
				config.setColorBranchForeground("#000000");
			}

			NodeList envNodes = doc.getElementsByTagName("variable");
			for (int i = 0; i < envNodes.getLength(); i++)
			{
				Element var = (Element) envNodes.item(i);
				String key = var.getAttribute("key");
				String value = var.getAttribute("value");
				envVars.put(key, value);
			}

			config.setEnvironmentVariables(envVars);
			
			NodeList commandNodes = doc.getElementsByTagName("system_command");
			for (int i = 0; i < commandNodes.getLength(); i++)
			{
				Element var = (Element) commandNodes.item(i);
				String name = var.getAttribute("name");
				validCommands.add(name);
			}
			config.setValidCommands(validCommands);;
			
			NodeList fontSettingNodes = doc.getElementsByTagName("fontSetting");
			for (int i = 0; i < fontSettingNodes.getLength(); i++)
			{
				Element var = (Element) fontSettingNodes.item(i);
				String id = var.getAttribute("id");
				String name = var.getAttribute("name");
				String style = var.getAttribute("style");
				int size = Integer.valueOf(var.getAttribute("size"));
				
				JDBFont newFont = new JDBFont(name,style,size);
				
				fontPrefs.put(id, newFont);
			}
			
			config.setFontPreferences(fontPrefs);

		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return config;
	}
}
