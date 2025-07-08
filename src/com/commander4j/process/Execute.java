package com.commander4j.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.commander4j.dialog.JTerminalOutput;
import com.commander4j.sys.Common;
import com.commander4j.tree.JMenuOption;

public class Execute
{

	public void execute(JFrame parentFrame, JMenuOption menuOption)
	{

		if (menuOption.getType().equals("leaf"))
		{
			Path pathDirectory = Paths.get(menuOption.getDirectory());
			boolean directoryValid = Files.exists(pathDirectory);

			if (directoryValid)
			{
				boolean commandValid = false;
				//Is this a system command (not a file)
				commandValid = Common.config.isValidCommand(menuOption.getCommand());

				if (commandValid == false)
				{
					//Is this a file to run
					Path pathCommand = Paths.get(menuOption.getCommand());
					commandValid = Files.exists(pathCommand);
				}
				
				if (commandValid)
				{
					executeCommand(menuOption);
				}
				else
				{
					JOptionPane.showMessageDialog(parentFrame, "Missing Command File [" + menuOption.getCommand() + "]", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(parentFrame, "Missing Directory [" + menuOption.getDirectory() + "]", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private int executeCommand(JMenuOption menuOption)
	{
		int result = 0;

		ProcessBuilder processBuilder = new ProcessBuilder();

		// Setup Enviroment

		Map<String, String> environment = processBuilder.environment();

		environment.putAll(Common.config.getEnvironmentVariables());

		// Setup Command Line

		LinkedList<String> commandAndParams = new LinkedList<String>();

		// If we are using a wrapper script to get the Shell Environment then
		// add that first.

		if (menuOption.isShellScriptRequired())
		{
			if (Common.config.isScriptEnabled())
			{
				// Run Environment shell script and pass command as parameter
				commandAndParams.add(Common.scriptFolder + File.separator + Common.config.getScriptFilename());
			}
		}

		// Add the command file
		commandAndParams.add(menuOption.getCommand());

		// Add any parameters
		commandAndParams.addAll(menuOption.getParameters());

		processBuilder.command(commandAndParams);

		// Set working directory
		processBuilder.directory(new File(menuOption.getDirectory()));

		// Redirect Error output
		processBuilder.redirectErrorStream(true);

		if (menuOption.isTerminalWindowRequired())
		{
			JTerminalOutput viewer = new JTerminalOutput();

			viewer.setVisible(true);

			viewer.runProcessAndCaptureOutput(menuOption,processBuilder);
		}
		else
		{
			try
			{
				Process process = processBuilder.start();
				result = process.waitFor();
				System.out.println(result);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

}
