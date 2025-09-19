package com.commander4j.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.commander4j.dnd.JDragDropAppInfo;
import com.commander4j.dnd.JDragDropPanel;
import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JCheckBox4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.gui.JList4j_dnd;
import com.commander4j.gui.JMenuItem4j;
import com.commander4j.gui.JTextField4j;
import com.commander4j.sys.Common;
import com.commander4j.tree.JMenuOption;
import com.commander4j.util.CmdResolver;
import com.commander4j.util.EXEsIconExtractor;
import com.commander4j.util.ICNSIconExporter;
import com.commander4j.util.JFileFilterExecs;
import com.commander4j.util.JFileFilterImages;
import com.commander4j.util.JFileFilterXML;
import com.commander4j.util.Utility;
import com.commander4j.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class JDialogLeaf extends JDialog
{

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField4j textField_description;
	private JTextField4j textField_type;
	private JTextField4j textField_directory;
	private JTextField4j textField_command;
	private JTextField4j textField_tree_filename;
	private JTextField4j textField_hint;
	private JTextField4j textField_icon;
	private JTextField4j textField_redirectIn;
	private JTextField4j textField_redirectOut;
	private JList4j_dnd<String> paramList = new JList4j_dnd<String>();
	private DefaultListModel<String> paramModel = new DefaultListModel<String>();
	private JCheckBox4j chckbx_shell_script = new JCheckBox4j();
	private JCheckBox4j chckbx_link_to_tree_enabled = new JCheckBox4j();
	private JCheckBox4j chckbx_terminal_window = new JCheckBox4j();
	private JCheckBox4j chckbx_confirm_execute = new JCheckBox4j();
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;
	private Utility utils = new Utility();
	private JLabel4j_std lbl_type = new JLabel4j_std("Type");
	private JLabel4j_std lbl_dnd_help = new JLabel4j_std("Drag and Drop target here");
	private JLabel4j_std lbl_description = new JLabel4j_std("Description");
	private JLabel4j_std lbl_directory = new JLabel4j_std("Directory");
	private JLabel4j_std lbl_parameters = new JLabel4j_std("Parameters");
	private JLabel4j_std lbl_command = new JLabel4j_std("Command");
	private JLabel4j_std lbl_shell_script = new JLabel4j_std("Use Shell Script");
	private JLabel4j_std lbl_hint = new JLabel4j_std("Hint");
	private JLabel4j_std lbl_icon = new JLabel4j_std("Icon");
	private JLabel4j_std lbl_link_to_tree_enabled = new JLabel4j_std("Link to Tree");
	private Border blackline = BorderFactory.createLineBorder(Color.black);
	private JLabel4j_std lbl_icon_preview = new JLabel4j_std();
	private JLabel4j_std lbl_redirectIn = new JLabel4j_std("Redirect Input");
	private JLabel4j_std lbl_redirectOut = new JLabel4j_std("Redirect Output");
	private JButton4j btnDirectory = new JButton4j(Common.icon_select_folder);
	private JButton4j btnCommand = new JButton4j(Common.icon_select_file);
	private JButton4j btnLink = new JButton4j(Common.icon_select_file);
	private JButton4j btnIcon = new JButton4j(Common.icon_edit);
	private JButton4j btnRedirectInput = new JButton4j(Common.icon_select_file);
	private JButton4j btnRedirectOutput = new JButton4j(Common.icon_select_file);
	private JButton4j okButton = new JButton4j(Common.icon_ok);
	private JButton4j btnDirectoryParameter = new JButton4j(Common.icon_select_folder);
	private JButton4j btnCommandParameter = new JButton4j(Common.icon_select_file);
	private JButton4j cancelButton = new JButton4j(Common.icon_cancel);
	private JButton4j btnAddParameter = new JButton4j(Common.icon_add);
	private JButton4j btnDeleteParameter = new JButton4j(Common.icon_delete);
	private JButton4j btnEditParameter = new JButton4j(Common.icon_edit);
	private JDragDropPanel DandDpanel;
	JMenuOption mo;

	/**
	 * Create the dialog.
	 */
	public JDialogLeaf(JFrame parent, JMenuOption menuOption)
	{
		this.mo = menuOption;
		setResizable(false);
		setTitle("Menu Leaf");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		Utility.setLookAndFeel("Nimbus");

		setBounds(100, 100, 755, 525);
		getContentPane().setLayout(null);
		contentPanel.setBackground(Common.color_app_window);
		contentPanel.setBounds(0, 0, getWidth(), getHeight());
		contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);

		textField_type = new JTextField4j();
		textField_type.setEnabled(false);
		textField_type.setEditable(false);
		textField_type.setBounds(134, 12, 114, 22);
		contentPanel.add(textField_type);
		textField_type.setText(menuOption.getType());

		textField_description = new JTextField4j();
		textField_description.setBounds(134, 43, 562, 22);
		contentPanel.add(textField_description);
		textField_description.setText(menuOption.getDescription());

		textField_directory = new JTextField4j();
		textField_directory.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				Common.workingFolder = utils.stringToPath(textField_directory.getText());
			}
		});
		textField_directory.setBounds(134, 133, 562, 22);
		contentPanel.add(textField_directory);
		textField_directory.setText(menuOption.getDirectory());
		Common.workingFolder = utils.stringToPath(menuOption.getDirectory());

		textField_command = new JTextField4j();
		textField_command.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				Common.commandFolder = utils.stringToPath(textField_command.getText());
			}
		});
		textField_command.setBounds(134, 74, 562, 22);
		contentPanel.add(textField_command);
		textField_command.setText(menuOption.getCommand());

		textField_tree_filename = new JTextField4j();
		textField_tree_filename.setBounds(475, 357, 221, 22);
		contentPanel.add(textField_tree_filename);
		textField_tree_filename.setText(menuOption.getMenuTreeFilename());

		chckbx_shell_script.setBounds(134, 103, 24, 23);
		contentPanel.add(chckbx_shell_script);
		chckbx_shell_script.setSelected(menuOption.isShellScriptRequired());
		chckbx_link_to_tree_enabled.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				menuOption.setLinkToMenuTreeEnabled(chckbx_link_to_tree_enabled.isSelected());
				setMenuTreeFieldStatus(menuOption);
			}
		});

		chckbx_link_to_tree_enabled.setBounds(443, 357, 24, 23);
		contentPanel.add(chckbx_link_to_tree_enabled);
		chckbx_link_to_tree_enabled.setSelected(menuOption.isLinkToMenuTreeEnabled());

		chckbx_terminal_window.setBounds(410, 103, 24, 23);
		contentPanel.add(chckbx_terminal_window);
		chckbx_terminal_window.setSelected(menuOption.isTerminalWindowRequired());

		chckbx_confirm_execute.setBounds(613, 103, 24, 23);
		contentPanel.add(chckbx_confirm_execute);
		chckbx_confirm_execute.setSelected(menuOption.isConfirmExecute());

		textField_hint = new JTextField4j();
		textField_hint.setBounds(134, 323, 562, 22);
		contentPanel.add(textField_hint);
		textField_hint.setText(menuOption.getHint());

		textField_icon = new JTextField4j();
		textField_icon.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				previewIcon();
			}
		});
		textField_icon.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				previewIcon();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				previewIcon();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				previewIcon();
			}
		});
		textField_icon.setBounds(170, 357, 147, 22);
		contentPanel.add(textField_icon);
		textField_icon.setText(menuOption.getIcon());

		textField_redirectIn = new JTextField4j();
		textField_redirectIn.setBounds(134, 387, 562, 22);
		contentPanel.add(textField_redirectIn);
		textField_redirectIn.setText(menuOption.getRedirectInput());

		textField_redirectOut = new JTextField4j();
		textField_redirectOut.setBounds(134, 418, 562, 22);
		contentPanel.add(textField_redirectOut);
		textField_redirectOut.setText(menuOption.getRedirectOutput());

		lbl_type.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_type.setBounds(6, 12, 120, 22);
		contentPanel.add(lbl_type);
		
		lbl_dnd_help.setBounds(406, 12, 200, 22);
		lbl_dnd_help.setIcon(Common.icon_left_arrow);
		contentPanel.add(lbl_dnd_help);

		lbl_description.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_description.setBounds(6, 43, 120, 22);
		contentPanel.add(lbl_description);

		lbl_directory.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_directory.setBounds(6, 133, 120, 22);
		contentPanel.add(lbl_directory);

		lbl_parameters.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_parameters.setBounds(6, 169, 120, 22);
		contentPanel.add(lbl_parameters);

		lbl_command.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_command.setBounds(6, 74, 120, 22);
		contentPanel.add(lbl_command);

		lbl_shell_script.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_shell_script.setBounds(6, 103, 120, 22);
		contentPanel.add(lbl_shell_script);

		lbl_hint.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_hint.setBounds(6, 323, 120, 22);
		contentPanel.add(lbl_hint);

		lbl_icon.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_icon.setBounds(6, 357, 120, 22);
		contentPanel.add(lbl_icon);

		lbl_link_to_tree_enabled.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_link_to_tree_enabled.setBounds(376, 357, 58, 22);
		contentPanel.add(lbl_link_to_tree_enabled);

		lbl_icon_preview.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_icon_preview.setBounds(134, 353, 28, 28);
		lbl_icon_preview.setBorder(blackline);
		lbl_icon_preview.setIcon(menuOption.getImageIcon());
		contentPanel.add(lbl_icon_preview);

		lbl_redirectIn.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_redirectIn.setBounds(6, 387, 120, 22);
		contentPanel.add(lbl_redirectIn);

		lbl_redirectOut.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_redirectOut.setBounds(6, 418, 120, 22);
		contentPanel.add(lbl_redirectOut);

		btnDirectory.setToolTipText("Select working directory");
		btnDirectory.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Common.workingFolder = utils.stringToPath(textField_directory.getText());
				File directory = selectDirectory(Common.workingFolder);

				if (directory != null)
				{
					textField_directory.setText(directory.getPath());
					Common.workingFolder = directory.getAbsoluteFile();

					textField_directory.requestFocus();
					textField_directory.setCaretPosition(textField_directory.getText().length());
				}
			}
		});
		btnDirectory.setBounds(699, 129, 30, 30);
		btnDirectory.setFocusable(false);
		contentPanel.add(btnDirectory);

		btnCommand.setToolTipText("Select what to run");
		// btnCommand.setMargin(new Insets(5,5,5,5));
		btnCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (Common.config.isValidCommand(textField_command.getText()))
				{
					Common.commandFolder = null;
				}
				else
				{
					Common.commandFolder = utils.stringToPath(textField_command.getText());
				}

				File commandFile = selectCommand(Common.commandFolder);
				if (commandFile != null)
				{
					textField_command.setText(commandFile.getPath());
					textField_directory.setText(commandFile.getParent());
					Common.commandFolder = commandFile.getParentFile();

					textField_command.requestFocus();
					textField_command.setCaretPosition(textField_command.getText().length());
				}
			}
		});
		btnCommand.setBounds(699, 70, 30, 30);
		btnCommand.setFocusable(false);
		contentPanel.add(btnCommand);
		btnLink.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File linkXML = selectLoadTreeXML();
				if (linkXML != null)
				{
					menuOption.setMenuTreeFilename(linkXML.getName());
					textField_tree_filename.setText(linkXML.getName());
				}
			}
		});

		btnLink.setToolTipText("Link this leaf to another JMenuTree");
		btnLink.setBounds(699, 351, 30, 30);
		btnLink.setFocusable(false);
		contentPanel.add(btnLink);

		btnIcon.setToolTipText("Select Icon");
		btnIcon.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				JDialogImageChooser ic = new JDialogImageChooser(parent, Common.iconFolder);
				ic.setVisible(true);

				File iconFile = ic.getSelectedImageFile();
				if (iconFile != null)
				{

					textField_icon.setText(iconFile.getName());
					Common.iconFolder = iconFile.getParentFile();

					textField_icon.requestFocus();
					textField_icon.setCaretPosition(textField_icon.getText().length());

					previewIcon();
				}
			}
		});
		btnIcon.setBounds(322, 353, 30, 30);
		btnIcon.setFocusable(false);
		contentPanel.add(btnIcon);

		btnRedirectInput.setToolTipText("Select Input Redirection");
		btnRedirectInput.setBounds(699, 383, 30, 30);
		btnRedirectInput.setFocusable(false);
		contentPanel.add(btnRedirectInput);

		btnRedirectOutput.setToolTipText("Select Output Redirection");
		btnRedirectOutput.setBounds(699, 414, 30, 30);
		btnRedirectOutput.setFocusable(false);
		contentPanel.add(btnRedirectOutput);

		okButton.setText("Confirm");
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				save(menuOption);

				dispose();
			}
		});
		okButton.setBounds(283, 445, 103, 30);
		contentPanel.add(okButton);
		okButton.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton);

		cancelButton.setText("Cancel");
		cancelButton.setBounds(388, 445, 103, 30);
		contentPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(134, 170, 562, 141);
		contentPanel.add(scrollPane);

		paramModel.addAll(menuOption.getParameters());
		paramList.setToolTipText("Parameters can be resequenced with drag and drop");

		paramList.setModel(paramModel);

		paramList.addMouseListener((new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					editRecord();
				}
			}
		}));

		scrollPane.setViewportView(paramList);

		final JPopupMenu popupMenuParams = new JPopupMenu();
		addPopup(paramList, popupMenuParams);

		final JMenuItem4j paramAddDirectory = new JMenuItem4j(Common.icon_select_folder);
		paramAddDirectory.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				addDirectory();
			}
		});
		paramAddDirectory.setText("Add Directory");
		popupMenuParams.add(paramAddDirectory);

		final JMenuItem4j paramAddFile = new JMenuItem4j(Common.icon_select_file);
		paramAddFile.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				addFile();
			}
		});
		paramAddFile.setText("Add File");
		popupMenuParams.add(paramAddFile);

		final JMenuItem4j paramAdd = new JMenuItem4j(Common.icon_add);
		paramAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				addRecord();
			}
		});
		paramAdd.setText("Add");
		popupMenuParams.add(paramAdd);

		final JMenuItem4j paramDelete = new JMenuItem4j(Common.icon_delete);
		paramDelete.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				deleteRecord();
			}
		});
		paramDelete.setText("Delete");
		popupMenuParams.add(paramDelete);

		final JMenuItem4j paramEdit = new JMenuItem4j(Common.icon_edit);
		paramEdit.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				editRecord();
			}
		});
		paramEdit.setText("Edit");

		popupMenuParams.add(paramEdit);

		btnDirectoryParameter.setToolTipText("Select Directory as Parameter");
		btnDirectoryParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				addDirectory();
			}
		});
		btnDirectoryParameter.setBounds(699, 254, 30, 30);
		contentPanel.add(btnDirectoryParameter);

		btnCommandParameter.setToolTipText("Select File as Parameter");
		btnCommandParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				addFile();
			}
		});
		btnCommandParameter.setBounds(699, 282, 30, 30);
		contentPanel.add(btnCommandParameter);

		btnAddParameter.setToolTipText("Input Manual Parameter");
		btnAddParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addRecord();
			}
		});
		btnAddParameter.setBounds(699, 170, 30, 30);
		contentPanel.add(btnAddParameter);

		btnDeleteParameter.setToolTipText("Delete Parameter");
		btnDeleteParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteRecord();

			}
		});
		btnDeleteParameter.setBounds(699, 226, 30, 30);
		contentPanel.add(btnDeleteParameter);

		btnEditParameter.setToolTipText("Edit Parameter");
		btnEditParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editRecord();
			}
		});
		btnEditParameter.setBounds(699, 198, 30, 30);
		contentPanel.add(btnEditParameter);

		JLabel4j_std lbl_console_1 = new JLabel4j_std("Command");
		lbl_console_1.setText("(see settings)");
		lbl_console_1.setHorizontalAlignment(SwingConstants.LEFT);
		lbl_console_1.setBounds(158, 103, 120, 22);
		contentPanel.add(lbl_console_1);

		JLabel4j_std lbl_terminal = new JLabel4j_std("Command");
		lbl_terminal.setText("Show Terminal Window");
		lbl_terminal.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_terminal.setBounds(248, 103, 155, 22);
		contentPanel.add(lbl_terminal);

		JLabel4j_std lbl_confirm_execute = new JLabel4j_std("Confirm Execute");
		lbl_confirm_execute.setText("Confirm Execute");
		lbl_confirm_execute.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_confirm_execute.setBounds(446, 104, 155, 22);
		contentPanel.add(lbl_confirm_execute);

		// if (utils.isMac())
		// {
		DandDpanel = new JDragDropPanel();
		DandDpanel.setLocation(283, 8);
		contentPanel.add(DandDpanel);

		DandDpanel.setDropListener(info -> {

			if (info.bundleType.equals(JDragDropAppInfo.Type_appBundle))
			{

				try
				{
					ICNSIconExporter xp = new ICNSIconExporter();
					xp.exportAppIconPng(info, Paths.get("." + File.separator + "images" + File.separator + "appIcons"), 24);
					textField_icon.setText(DandDpanel.getIconName_PNG());
				}
				catch (IOException e)
				{
					textField_icon.setText("");
				}

				textField_description.setText(DandDpanel.getBundleName());
				textField_directory.setText(DandDpanel.getWorkingDirectory());

				int foundit = paramModel.indexOf(DandDpanel.getBundleName());

				if (foundit == -1)
				{
					paramModel.clear();
					paramModel.add(0, DandDpanel.getBundleName() + ".app");
				}

				textField_command.setText("open");

				chckbx_terminal_window.setSelected(false);

				chckbx_shell_script.setSelected(true);

				chckbx_confirm_execute.setSelected(true);

			}

			if (info.bundleType.equals(JDragDropAppInfo.Type_bashScript))
			{
				textField_icon.setText("terminal_24x24.png");

				textField_description.setText("Bash Script : " + DandDpanel.getBundleName());
				textField_command.setText(DandDpanel.getExecutableFullPath());
				textField_directory.setText(DandDpanel.getWorkingDirectory());

				chckbx_terminal_window.setSelected(true);

				chckbx_shell_script.setSelected(true);

				chckbx_confirm_execute.setSelected(true);
			}

			if (info.bundleType.equals(JDragDropAppInfo.Type_windowsEXE))
			{
				BufferedImage icon = EXEsIconExtractor.exportAppIconPng(DandDpanel.getExecutableFullPath(), 24);

				if (icon != null)
				{
					try
					{
						File outputfile = new File("."+File.separator+"images"+File.separator+"appIcons"+File.separator +  DandDpanel.getBundleId() + ".png");
						ImageIO.write(icon, "png", outputfile);
					}
					catch (IOException e)
					{
					}
				}

				textField_icon.setText(DandDpanel.getIconName_PNG());

				textField_command.setText(CmdResolver.findCmdExe().toString());

				textField_directory.setText(DandDpanel.getWorkingDirectory());

				chckbx_terminal_window.setSelected(false);

				chckbx_shell_script.setSelected(false);

				chckbx_confirm_execute.setSelected(true);

				paramModel.clear();

				paramModel.add(0, "/c");
				paramModel.add(1, "start");
				paramModel.add(2, "\"\"");
				paramModel.add(3, "/D");
				paramModel.add(4, DandDpanel.getWorkingDirectory());
				paramModel.add(5, DandDpanel.getExecutableName());
				
				
				String desc = WinExeMetadata.getProductName(info.bundlePath);
				if (!desc.isEmpty())
				{
					textField_description.setText(desc);
				}
				else
				{
					textField_description.setText("Windows Executable : " + DandDpanel.getBundleName());
				}
			}
			
			if (info.bundleType.equals(JDragDropAppInfo.Type_windowsCMD))
			{
				textField_icon.setText("terminal_24x24.png");

				textField_command.setText(CmdResolver.findCmdExe().toString());

				textField_directory.setText(DandDpanel.getWorkingDirectory());

				chckbx_terminal_window.setSelected(false);

				chckbx_shell_script.setSelected(false);

				chckbx_confirm_execute.setSelected(true);

				paramModel.clear();

				paramModel.add(0, "/c");
				paramModel.add(1, "start");
				paramModel.add(2, "\"\"");
				paramModel.add(3, "/D");
				paramModel.add(4, DandDpanel.getWorkingDirectory());
				paramModel.add(5, DandDpanel.getExecutableName());
				
				textField_description.setText("Windows Command : " + DandDpanel.getBundleName());
			}
			
			if (info.bundleType.equals(JDragDropAppInfo.Type_windowsBAT))
			{
				textField_icon.setText("terminal_24x24.png");

				textField_command.setText(CmdResolver.findCmdExe().toString());

				textField_directory.setText(DandDpanel.getWorkingDirectory());

				chckbx_terminal_window.setSelected(false);

				chckbx_shell_script.setSelected(false);

				chckbx_confirm_execute.setSelected(true);

				paramModel.clear();

				paramModel.add(0, "/c");
				paramModel.add(1, "start");
				paramModel.add(2, "\"\"");
				paramModel.add(3, "/D");
				paramModel.add(4, DandDpanel.getWorkingDirectory());
				paramModel.add(5, DandDpanel.getExecutableName());
				
				textField_description.setText("Windows Batch File : " + DandDpanel.getBundleName());
			}

			previewIcon();

		});
		// }

		setMenuTreeFieldStatus(menuOption);

		widthadjustment = Utility.getOSWidthAdjustment();
		heightadjustment = Utility.getOSHeightAdjustment();

		GraphicsDevice gd = Utility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JDialogLeaf.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JDialogLeaf.this.getHeight()) / 2), JDialogLeaf.this.getWidth() + widthadjustment,
				JDialogLeaf.this.getHeight() + heightadjustment);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textField_description.requestFocus();
				textField_description.setCaretPosition(textField_description.getText().length());

			}
		});
	}

	private void save(JMenuOption menuOption)
	{
		menuOption.setDescription(textField_description.getText());
		menuOption.setCommand(textField_command.getText());
		menuOption.setConfirmExecute(chckbx_confirm_execute.isSelected());
		menuOption.setDirectory(textField_directory.getText());
		menuOption.setHint(textField_hint.getText());
		menuOption.setIcon(textField_icon.getText());
		menuOption.setRedirectInput(textField_redirectIn.getText());
		menuOption.setRedirectOutput(textField_redirectOut.getText());
		menuOption.setShellScriptRequiredChecked(chckbx_shell_script.isSelected());
		menuOption.setTerminalWindowRequiredChecked(chckbx_terminal_window.isSelected());
		menuOption.setLinkToMenuTreeEnabled(chckbx_link_to_tree_enabled.isSelected());
		menuOption.setMenuTreeFilename(textField_tree_filename.getText());

		LinkedList<String> params = new LinkedList<String>();

		for (int x = 0; x < paramModel.getSize(); x++)
		{
			params.add(paramModel.getElementAt(x));
		}

		menuOption.setParameters(params);

		dispose();
	}

	private File selectCommand(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);

		JFileFilterExecs ffi = new JFileFilterExecs();
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogLeaf.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

	private void setMenuTreeFieldStatus(JMenuOption menuOption)
	{
		textField_tree_filename.setEnabled(menuOption.isLinkToMenuTreeEnabled());
		textField_tree_filename.setEditable(menuOption.isLinkToMenuTreeEnabled());
		textField_command.setEditable(!menuOption.isLinkToMenuTreeEnabled());
		textField_command.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		textField_directory.setEditable(!menuOption.isLinkToMenuTreeEnabled());
		textField_directory.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		textField_redirectIn.setEditable(!menuOption.isLinkToMenuTreeEnabled());
		textField_redirectIn.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		textField_redirectOut.setEditable(!menuOption.isLinkToMenuTreeEnabled());
		textField_redirectOut.setEnabled(!menuOption.isLinkToMenuTreeEnabled());

		chckbx_shell_script.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		chckbx_terminal_window.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		chckbx_confirm_execute.setEnabled(!menuOption.isLinkToMenuTreeEnabled());

		btnAddParameter.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		btnDeleteParameter.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		btnEditParameter.setEnabled(!menuOption.isLinkToMenuTreeEnabled());

		btnDirectoryParameter.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		btnCommandParameter.setEnabled(!menuOption.isLinkToMenuTreeEnabled());

		btnRedirectInput.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
		btnRedirectOutput.setEnabled(!menuOption.isLinkToMenuTreeEnabled());

		paramList.setEnabled(!menuOption.isLinkToMenuTreeEnabled());
	}

	private File selectFile(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);

		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogLeaf.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

	private File selectDirectory(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);

		fc.setApproveButtonText("Select");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogLeaf.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

	@SuppressWarnings("unused")
	private File selectIcon(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);

		JFileFilterImages ffi = new JFileFilterImages();
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogLeaf.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

	private void addDirectory()
	{
		File directory = selectDirectory(Common.workingFolder);
		if (directory != null)
		{
			paramModel.addElement(directory.getPath());
			paramList.setModel(paramModel);
		}
	}

	private void addFile()
	{
		File commandParamFile = selectFile(Common.commandFolder);
		if (commandParamFile != null)
		{
			paramModel.addElement(commandParamFile.getPath());
			paramList.setModel(paramModel);
		}
	}

	private void previewIcon()
	{
		ImageIcon result;

		String filename = textField_icon.getText();

			System.out.println("previewIcon="+filename);

			result = new ImageIcon(Common.iconPath +filename);
			
			System.out.println("ImageIcon="+Common.iconPath +filename);
			
			lbl_icon_preview.setIcon(result);
			lbl_icon_preview.invalidate();
			lbl_icon_preview.revalidate();
			lbl_icon_preview.repaint();

	}

	private void addRecord()
	{
		String param = JOptionPane.showInputDialog(JDialogLeaf.this, "New Parameter");
		if (param != null)
		{
			if (param.equals("") == false)
			{
				paramModel.addElement(param);
				paramList.setModel(paramModel);
			}
		}
	}

	private void deleteRecord()
	{
		if (paramList.isSelectionEmpty() == false)
		{
			int idx = paramList.getSelectedIndex();
			int question = JOptionPane.showConfirmDialog(JDialogLeaf.this, "Confirm ?", "Remove Parameter", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
			if (question == 0)
			{
				paramModel.removeElementAt(idx);
				paramList.setModel(paramModel);

			}
		}
	}

	private void editRecord()
	{
		if (paramList.isSelectionEmpty() == false)
		{
			int idx = paramList.getSelectedIndex();
			String param = paramModel.getElementAt(idx);
			String result = JOptionPane.showInputDialog(JDialogLeaf.this, "Amend Parameter", param);
			if (result != null)
			{
				if (result.equals("") == false)
				{

					paramModel.removeElementAt(idx);
					paramModel.insertElementAt(result, idx);

				}
			}
		}
	}

	private static void addPopup(Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
					showMenu(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
					showMenu(e);
			}

			private void showMenu(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}

		});
	}

	private File selectLoadTreeXML()
	{
		File result = null;

		JFileChooser fc = new JFileChooser(Common.treeFolderFile);
		fc.setSelectedFile(Common.treeFolderFile);

		JFileFilterXML ffi = new JFileFilterXML();
		fc.setApproveButtonText("Open");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogLeaf.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}
}
