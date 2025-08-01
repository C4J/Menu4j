package com.commander4j.util;

/**
 * @author David Garratt
 * 
 * Project Name : Commander4j
 * 
 * Filename     : JFileFilterExecs.java
 * 
 * Package Name : com.commander4j.util
 * 
 * License      : GNU General Public License
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * http://www.commander4j.com/website/license.html.
 * 
 */

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 */
public class JFileFilterExecs extends FileFilter
{
	// Accept all directories and all gif, jpg, tiff, or png files.
	/**
	 * Method accept.
	 * 
	 * @param f
	 *            File
	 * @return boolean
	 */
	public boolean accept(File f) {
		if (f.isDirectory())
		{
			return true;
		}

		String extension = JFileFilterExecTypes.getExtension(f);
		if (extension != null)
		{
			if (extension.equals(JFileFilterExecTypes.BAT) || extension.equals(JFileFilterExecTypes.BIN) || extension.equals(JFileFilterExecTypes.CMD) || extension.equals(JFileFilterExecTypes.COM) || extension.equals(JFileFilterExecTypes.EXE)
					|| extension.equals(JFileFilterExecTypes.SH)|| extension.equals(JFileFilterExecTypes.APP))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		return false;
	}

	// The description of this filter
	/**
	 * Method getDescription.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return "Executables";
	}

}
