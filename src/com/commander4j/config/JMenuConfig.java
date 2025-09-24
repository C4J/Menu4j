package com.commander4j.config;

import java.awt.Font;
import java.util.LinkedList;
import java.util.Map;

import com.commander4j.db.JDBFont;
import com.commander4j.sys.Common;
import com.commander4j.util.JCipher;
import com.commander4j.util.Utility;

public class JMenuConfig
{

	private Map<String, JDBFont> font_preferences;
	private Map<String, String> environment_variables;
	private LinkedList<String> valid_commands;
	private String tree_filename = "tree.xml";
	private String shell_script_filename = "run-in-user-env.sh";
	private String enable_shell_script = "Y";
	private String color_terminal_background = "#000000";
	private String color_terminal_forground = "#66FF66";
	private String color_leaf_foreground = "#000000";
	private String color_branch_foreground = "#000000";
	private String password = "";
	private JCipher advancedEncryptionStandard = new JCipher(Common.encryptionKey);

	private Utility util = new Utility();

	public String getPassword()
	{
		return password;
	}

	public String encodePassword()
	{

		String result = advancedEncryptionStandard.encode(getPassword());

		return result;
	}

	public String decodedPassword(String pass)
	{
		String result = advancedEncryptionStandard.decode(pass);

		return result;
	}

	public void setPassword(String pass)
	{
		password = pass;
	}

	public String getColorTerminalBackground()
	{
		return color_terminal_background;
	}

	public void setColorTerminalBackground(String terminalBackgroundColour)
	{
		this.color_terminal_background = terminalBackgroundColour;
	}
	
	public String getColorLeafForeground()
	{
		return color_leaf_foreground;
	}

	public void setColorLeafForegound(String leadforegroundColour)
	{
		this.color_leaf_foreground = leadforegroundColour;
	}
	
	public String getColorBranchForeground()
	{
		return color_branch_foreground;
	}

	public void setColorBranchForeground(String branchforegroundColour)
	{
		this.color_branch_foreground = branchforegroundColour;
	}

	public String getColorTerminalForeground()
	{
		return color_terminal_forground;
	}

	public void setColorTerminalForground(String terminalFontColour)
	{
		this.color_terminal_forground = terminalFontColour;
	}

	public String getScriptFilename()
	{
		return shell_script_filename;
	}

	public void setScriptFilename(String filename)
	{
		this.shell_script_filename = filename;
	}

	public String getTreeFilename()
	{
		return tree_filename;
	}

	public void setTreeFilename(String filename)
	{
		this.tree_filename = filename;
	}

	public String getScriptEnabled()
	{
		return enable_shell_script;
	}

	public boolean isScriptEnabled()
	{
		if (enable_shell_script.equals("Y"))
		{
			return true;
		}
		else
		{
			return false;
		}

	}

	public void setScriptEnabled(String use_userEnvScript)
	{
		this.enable_shell_script = use_userEnvScript;
	}

	public void setScriptEnabled(Boolean yesno)
	{
		if (yesno)
		{
			this.enable_shell_script = "Y";
		}
		else
		{
			this.enable_shell_script = "N";
		}
	}

	public Map<String, String> getEnvironmentVariables()
	{
		return environment_variables;
	}

	public Map<String, JDBFont> getFontPreferences()
	{
		return font_preferences;
	}

	public void setEnvironmentVariables(Map<String, String> environmentVariables)
	{
		this.environment_variables = environmentVariables;
	}

	public void setFontPreferences(Map<String, JDBFont> fontPreferences)
	{
		this.font_preferences = fontPreferences;
	}

	public void setEnvironmentVariable(String key, String value)
	{
		this.environment_variables.put(key, value);
	}

	public void setFontPreference(String key, JDBFont value)
	{
		this.font_preferences.put(key, value);
	}

	public String getEnvironmentVariable(String key)
	{
		String result = this.environment_variables.get(key);
		return result;
	}

	public JDBFont getJDBFontPreference(String key)
	{
		JDBFont result = this.font_preferences.get(key);
		return result;
	}

	public Font getFontPreference(String key)
	{
		JDBFont temp = this.font_preferences.get(key);
		Font result = Common.font_input;
		try
		{
			result = new Font(temp.getName(), util.parseFontStyle(temp.getStyle()), temp.getSize());
		}
		catch (Exception ex)
		{

		}
		return result;
	}

	public LinkedList<String> getValidCommands()
	{
		return valid_commands;
	}

	public void setValidCommands(LinkedList<String> commandList)
	{
		this.valid_commands = commandList;
	}

	public void addValidCommand(String command)
	{
		this.valid_commands.add(command);
	}

	public void removeValidCommand(String command)
	{
		this.valid_commands.remove(command);
	}

	public boolean isValidCommand(String command)
	{
		boolean result = false;
		result = this.valid_commands.contains(command);
		return result;
	}

	public void clearEnviroment()
	{
		environment_variables.clear();
	}

	public void clearFontPreferences()
	{
		font_preferences.clear();
	}

	public void clearCommands()
	{
		valid_commands.clear();
	}

}
