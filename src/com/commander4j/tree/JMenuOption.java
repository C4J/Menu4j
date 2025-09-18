package com.commander4j.tree;

import java.util.LinkedList;

import javax.swing.ImageIcon;

import com.commander4j.sys.Common;

public class JMenuOption
{
	private String type = "";
	private String description = "";
	private String directory = "";
	private String command = "";
	private String hint = "";
	private String icon = "";
	private String redirectInput = "";
	private String redirectOutput = "";
	private String shellScriptRequired="";
	private String terminalWindowRequired="";
	private String linkToMenuTreeEnabled = "";
	private String menuTreeFilename = "";
	private boolean confirmExecute = false;
	private LinkedList<String> parameters = new LinkedList<String>();
	
	public String getLinkToMenuTreeEnabled()
	{
		return linkToMenuTreeEnabled;
	}
	
	public String getShellScriptRequired() {
		return shellScriptRequired;
	}
	
	public boolean isConfirmExecute()
	{
		return confirmExecute;
	}

	public void setConfirmExecute(boolean confirmExecute)
	{
		this.confirmExecute = confirmExecute;
	}

	public String getTerminalWindowRequired()
	{
		return terminalWindowRequired;
	}
	
	public boolean isLinkToMenuTreeEnabled()
	{
		if (linkToMenuTreeEnabled.equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isShellScriptRequired() {
		if (shellScriptRequired.equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isTerminalWindowRequired() {
		if (terminalWindowRequired.equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public void setShellScriptRequired(String shell) {
		if (shell.equals(""))
		{
			shell = "true";
		}
		this.shellScriptRequired = shell;
	}
	
	public void setLinkToMenuTreeEnabled(String enabled)
	{
		if (enabled.equals(""))
		{
			enabled = "false";
		}
		this.linkToMenuTreeEnabled = enabled;
	}
	
	public void setTerminalWindowRequired(String term) {
		if (term.equals(""))
		{
			term = "true";
		}
		this.terminalWindowRequired = term;
	}
	
	public void setLinkToMenuTreeEnabled(boolean checked) {
		if (checked)
		{
			this.linkToMenuTreeEnabled = "true";
		}
		else
		{
			this.linkToMenuTreeEnabled = "false";
		}
	}
	
	public void setShellScriptRequiredChecked(boolean checked) {
		if (checked)
		{
			this.shellScriptRequired = "true";
		}
		else
		{
			this.shellScriptRequired = "false";
		}
			
	}
	
	public void setTerminalWindowRequiredChecked(boolean checked) {
		if (checked)
		{
			this.terminalWindowRequired = "true";
		}
		else
		{
			this.terminalWindowRequired = "false";
		}
			
	}
	
	public LinkedList<String> getParameters()
	{
		return parameters;
	}

	public void setParameters(LinkedList<String> parameters)
	{
		this.parameters = parameters;
	}

	public String toString()
	{
		return description;
	}

	public String getMenuTreeFilename()
	{
		return menuTreeFilename;
	}
	
	public String getRedirectInput()
	{
		return redirectInput;
	}

	public void setRedirectInput(String redirectInput)
	{
		this.redirectInput = redirectInput;
	}

	public void setMenuTreeFilename(String menufilename)
	{
		this.menuTreeFilename = menufilename;
	}
	
	public String getRedirectOutput()
	{
		return redirectOutput;
	}

	public void setRedirectOutput(String redirectOutput)
	{
		this.redirectOutput = redirectOutput;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}

	public String getCommand()
	{
		return command;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

	public String getHint()
	{
		return hint;
	}

	public void setHint(String hint)
	{
		this.hint = hint;
	}

	public String getIcon()
	{
		return icon;
	}
	
	public ImageIcon getImageIcon()
	{
		ImageIcon result;
		result = new ImageIcon(Common.iconPath + getIcon());
		return result;
	}

	public void setIcon(String icon)
	{
		if (icon.contains(Common.iconPath))
		{
		System.out.println("setIcon = "+icon);
		}
		this.icon = icon;
	}
	
	public void clear()
	{
		setType("");
		setDescription("");
		setCommand("");
		setConfirmExecute(false);
		setDirectory("");
		setHint("");
		setIcon("");
		setParameters(null);
		setRedirectInput("");
		setRedirectOutput("");
		setShellScriptRequired("false");
		setLinkToMenuTreeEnabled("false");
		setTerminalWindowRequired("false");	
	}
	
	public void clone(JMenuOption opt)
	{
		setType(opt.getType());
		setDescription(opt.getDescription());
		setCommand(opt.getCommand());
		setConfirmExecute(opt.isConfirmExecute());
		setDirectory(opt.getDirectory());
		setHint(opt.getHint());
		setIcon(opt.getIcon());
		setParameters(opt.getParameters());
		setRedirectInput(opt.getRedirectInput());
		setRedirectOutput(opt.getRedirectOutput());
		setShellScriptRequired(opt.getShellScriptRequired());
		setLinkToMenuTreeEnabled(opt.getLinkToMenuTreeEnabled());
		setTerminalWindowRequired(opt.getTerminalWindowRequired());
	}
}
