package com.commander4j.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.commander4j.db.JDBFont;
import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JCheckBox4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.gui.JList4j;
import com.commander4j.gui.JList4j_dnd;
import com.commander4j.gui.JMenuItem4j;
import com.commander4j.gui.JPasswordField4j;
import com.commander4j.gui.JTextField4j;
import com.commander4j.process.JEnvironmentVariable;
import com.commander4j.sys.Common;
import com.commander4j.util.JFileFilterExecs;
import com.commander4j.util.JFileFilterXML;
import com.commander4j.util.Utility;

public class JDialogSettings extends JDialog
{

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField4j textField_script;
	private JLabel textField_Terminal_Background;
	private JLabel textField_Terminal_Forground;
	private JLabel textField_Branch_Forground;
	private JLabel textField_Leaf_Forground;

	private JList4j_dnd<JEnvironmentVariable> environmentList = new JList4j_dnd<JEnvironmentVariable>();
	private DefaultListModel<JEnvironmentVariable> environmentModel = new DefaultListModel<JEnvironmentVariable>();
	private JList4j<String> commandList = new JList4j<String>();
	private DefaultListModel<String> commandModel = new DefaultListModel<String>();
	private JCheckBox4j chckbx_runScript = new JCheckBox4j();
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;

	private Utility utils = new Utility();
	private JPasswordField4j textField_Password = new JPasswordField4j();
	private JLabel4j_std lbl_BranchFont = new JLabel4j_std();
	private JLabel4j_std lbl_LeafFont = new JLabel4j_std();
	private JLabel4j_std lbl_TerminalFont = new JLabel4j_std();

	private JLabel4j_std lbl_TitleBranchFont = new JLabel4j_std();
	private JLabel4j_std lbl_TitleLeafFont = new JLabel4j_std();
	private JLabel4j_std lbl_TitleTerminalFont = new JLabel4j_std();
	private Utility util = new Utility();
	
	private Color terminalForegroundColor;
	private Color terminalBackgroundColor;
	private Color leafForegroundColor;
	private Color branchForegroundColor;

	/**
	 * Create the dialog.
	 */
	public JDialogSettings()
	{
		setResizable(false);
		setTitle("Settings");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		Utility.setLookAndFeel("Nimbus");

		setBounds(0, 0, 737, 585);
		getContentPane().setLayout(null);
		contentPanel.setBackground(Common.color_app_window);
		contentPanel.setBounds(0, 0, 737, 585);
		contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		
		getConfigColors();

		if (Common.config.getEnvironmentVariables() != null)
		{
			LinkedList<JEnvironmentVariable> list = new LinkedList<JEnvironmentVariable>();
			Map<String, String> map = Common.config.getEnvironmentVariables();

			for (Map.Entry<String, String> entry : map.entrySet())
			{
				JEnvironmentVariable env = new JEnvironmentVariable(entry.getKey(), entry.getValue());
				list.add(env);
			}
			environmentModel.addAll(list);
		}

		if (Common.config.getValidCommands() != null)
		{
			commandModel.addAll(Common.config.getValidCommands());
		}


		JPanel panel_ShellScript = new JPanel();
		panel_ShellScript.setBorder(new TitledBorder(null, "Shell Script", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_ShellScript.setBounds(368, 6, 346, 70);
		contentPanel.add(panel_ShellScript);
		panel_ShellScript.setLayout(null);
		panel_ShellScript.setBackground(Common.color_app_window);

		JPanel panel_Security = new JPanel();
		panel_Security.setBorder(new TitledBorder(null, "Security", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_Security.setBounds(333, 401, 381, 65);
		contentPanel.add(panel_Security);
		panel_Security.setBackground(Common.color_app_window);
		panel_Security.setLayout(null);

		JLabel4j_std lbl_AppPassword = new JLabel4j_std();
		lbl_AppPassword.setBounds(23, 22, 62, 22);
		panel_Security.add(lbl_AppPassword);
		lbl_AppPassword.setText("Password");
		lbl_AppPassword.setHorizontalAlignment(SwingConstants.RIGHT);

		textField_Password.setBounds(97, 23, 202, 22);
		panel_Security.add(textField_Password);
		textField_Password.setText((String) null);
		textField_Password.setEnabled(false);
		textField_Password.setText(Common.config.getPassword());

		JButton4j btnPassword = new JButton4j(Common.icon_password);
		btnPassword.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogChangePassword changePassword = new JDialogChangePassword(Common.config.getPassword());
				changePassword.setVisible(true);
				textField_Password.setText(changePassword.enteredPassword);
				Common.config.setPassword(changePassword.enteredPassword);
			}
		});
		btnPassword.setBounds(311, 18, 30, 30);
		panel_Security.add(btnPassword);
		btnPassword.setToolTipText("Application Password");

		JButton4j btnClearPassword = new JButton4j();
		btnClearPassword.setIcon(Common.icon_erase);
		btnClearPassword.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				textField_Password.setText("");
				Common.config.setPassword("");
			}
		});
		btnClearPassword.setBounds(343, 18, 30, 30);
		panel_Security.add(btnClearPassword);
		btnClearPassword.setToolTipText("Clear Password");

		JLabel4j_std lbl_console = new JLabel4j_std("Command");
		lbl_console.setBounds(6, 25, 58, 22);
		panel_ShellScript.add(lbl_console);
		lbl_console.setText("Enable");
		lbl_console.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbx_runScript.setBounds(72, 24, 24, 23);
		panel_ShellScript.add(chckbx_runScript);
		chckbx_runScript.setSelected(Common.config.isScriptEnabled());

		textField_script = new JTextField4j();
		textField_script.setEnabled(false);
		textField_script.setBounds(104, 25, 200, 22);
		panel_ShellScript.add(textField_script);
		textField_script.setText(Common.config.getScriptFilename());

		JButton4j btnShellScript = new JButton4j(Common.icon_select_file);
		btnShellScript.setBounds(308, 21, 30, 30);
		panel_ShellScript.add(btnShellScript);
		btnShellScript.setToolTipText("Utility Scrip to Access Shell Variables");

		JPanel panel_Environment = new JPanel();
		panel_Environment.setBorder(new TitledBorder(null, "Environment Variables", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_Environment.setBounds(6, 78, 708, 154);
		contentPanel.add(panel_Environment);
		panel_Environment.setBackground(Common.color_app_window);
		panel_Environment.setLayout(null);

		JScrollPane scrollPane_Enviroment = new JScrollPane();
		scrollPane_Enviroment.setBounds(17, 23, 650, 114);
		panel_Environment.add(scrollPane_Enviroment);
		environmentList.setToolTipText("Parameters can be resequenced with drag and drop");

		environmentList.setModel(environmentModel);
		environmentList.addMouseListener((new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					editEnvironmentRecord();
				}
			}
		}));

		scrollPane_Enviroment.setViewportView(environmentList);

		final JPopupMenu popupMenuEnvir = new JPopupMenu();
		addPopup(environmentList, popupMenuEnvir);

		{
			final JMenuItem4j popupAddParam = new JMenuItem4j(Common.icon_add);
			popupAddParam.addActionListener(new ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					addEnvironmentRecord();
				}
			});
			popupAddParam.setText("Add");
			popupMenuEnvir.add(popupAddParam);
		}

		{
			final JMenuItem4j popupDeleteParam = new JMenuItem4j(Common.icon_delete);
			popupDeleteParam.addActionListener(new ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					deleteEnvironmentRecord();
				}
			});
			popupDeleteParam.setText("Delete");
			popupMenuEnvir.add(popupDeleteParam);
		}

		{
			final JMenuItem4j popupEditParam = new JMenuItem4j(Common.icon_edit);
			popupEditParam.addActionListener(new ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					editEnvironmentRecord();
				}
			});
			popupEditParam.setText("Edit");

			popupMenuEnvir.add(popupEditParam);
		}

		JButton4j btnAddParameter = new JButton4j(Common.icon_add);
		btnAddParameter.setBounds(670, 23, 30, 30);
		panel_Environment.add(btnAddParameter);
		btnAddParameter.setToolTipText("Input Manual Parameter");

		JButton4j btnDeleteParameter = new JButton4j(Common.icon_delete);
		btnDeleteParameter.setBounds(670, 83, 30, 30);
		panel_Environment.add(btnDeleteParameter);
		btnDeleteParameter.setToolTipText("Delete Parameter");

		JButton4j btnEditParameter = new JButton4j(Common.icon_edit);
		btnEditParameter.setBounds(670, 53, 30, 30);
		panel_Environment.add(btnEditParameter);
		btnEditParameter.setToolTipText("Edit Parameter");
		btnEditParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editEnvironmentRecord();
			}
		});
		btnDeleteParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteEnvironmentRecord();

			}
		});
		btnAddParameter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addEnvironmentRecord();
			}
		});
		btnShellScript.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				File scriptFile = selectCommand(Common.scriptFolder);

				if (scriptFile != null)
				{
					textField_script.setText(scriptFile.getName());
					textField_script.requestFocus();
					textField_script.setCaretPosition(textField_script.getText().length());
				}
			}
		});

		JPanel panel_Tree = new JPanel();
		panel_Tree.setBorder(new TitledBorder(null, "Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_Tree.setBounds(6, 6, 349, 70);
		contentPanel.add(panel_Tree);
		panel_Tree.setBackground(Common.color_app_window);
		panel_Tree.setLayout(null);

		JLabel4j_std lbl_console_1 = new JLabel4j_std("Command");
		lbl_console_1.setText("Default Tree");
		lbl_console_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_console_1.setBounds(21, 25, 76, 22);
		panel_Tree.add(lbl_console_1);

		JTextField4j textField_treeFilename = new JTextField4j();
		textField_treeFilename.setEnabled(false);
		textField_treeFilename.setText(Common.treeFolderFile.getName());
		textField_treeFilename.setBounds(105, 25, 200, 22);
		panel_Tree.add(textField_treeFilename);

		JButton4j btnTreeFilename = new JButton4j(Common.icon_select_file);
		btnTreeFilename.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File treeFile = selectLoadTreeXML(Common.treeFolderFile);

				if (treeFile != null)
				{
					textField_treeFilename.setText(treeFile.getName());
					textField_treeFilename.requestFocus();
					textField_treeFilename.setCaretPosition(textField_treeFilename.getText().length());
				}
			}
		});
		btnTreeFilename.setToolTipText("Default Tree to Load at Startup");
		btnTreeFilename.setBounds(310, 21, 30, 30);
		panel_Tree.add(btnTreeFilename);

		JPanel panel_SystemCommands = new JPanel();
		panel_SystemCommands.setBorder(new TitledBorder(null, "System Commands", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_SystemCommands.setBackground(new Color(241, 241, 241));
		panel_SystemCommands.setBounds(6, 401, 328, 130);
		contentPanel.add(panel_SystemCommands);
		panel_SystemCommands.setLayout(null);

		JScrollPane scrollPane_System = new JScrollPane();
		scrollPane_System.setBounds(17, 23, 260, 87);
		panel_SystemCommands.add(scrollPane_System);

		scrollPane_System.setViewportView(commandList);
		commandList.setModel(commandModel);

		commandList.addMouseListener((new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					editCommandRecord();
				}
			}
		}));

		JPopupMenu popupMenuCommand = new JPopupMenu();
		addPopup(commandList, popupMenuCommand);

		JMenuItem4j popupAddCommand = new JMenuItem4j(Common.icon_add);
		popupAddCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addCommandRecord();
			}
		});
		popupAddCommand.setText("Add");
		popupMenuCommand.add(popupAddCommand);

		JMenuItem4j popupDeleteCommand = new JMenuItem4j(Common.icon_delete);
		popupDeleteCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteCommandRecord();
			}
		});
		popupDeleteCommand.setText("Delete");
		popupMenuCommand.add(popupDeleteCommand);

		JMenuItem4j popupEditCommand = new JMenuItem4j(Common.icon_edit);
		popupEditCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editCommandRecord();
			}
		});
		popupEditCommand.setText("Edit");
		popupMenuCommand.add(popupEditCommand);

		JButton4j btnAddCommand = new JButton4j(Common.icon_add);
		btnAddCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addCommandRecord();
			}
		});
		btnAddCommand.setBounds(285, 23, 30, 30);
		btnAddCommand.setToolTipText("Input Manual Command");
		panel_SystemCommands.add(btnAddCommand);

		JButton4j btnDeleteCommand = new JButton4j(Common.icon_delete);
		btnDeleteCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteCommandRecord();
			}
		});
		btnDeleteCommand.setBounds(285, 83, 30, 30);
		btnDeleteCommand.setToolTipText("Delete Command");
		panel_SystemCommands.add(btnDeleteCommand);

		JButton4j btnEditCommand = new JButton4j(Common.icon_edit);
		btnEditCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editCommandRecord();
			}
		});
		btnEditCommand.setBounds(285, 53, 30, 30);
		btnEditCommand.setToolTipText("Edit Command");
		panel_SystemCommands.add(btnEditCommand);

		JPanel panel_Fonts = new JPanel();
		panel_Fonts.setBorder(new TitledBorder(null, "Fonts", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_Fonts.setBackground(new Color(241, 241, 241));
		panel_Fonts.setBounds(6, 235, 708, 159);
		contentPanel.add(panel_Fonts);
		panel_Fonts.setLayout(null);

		lbl_TitleBranchFont.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_TitleBranchFont.setBounds(10, 17, 75, 31);
		lbl_TitleBranchFont.setText("Branch Font");
		panel_Fonts.add(lbl_TitleBranchFont);

		lbl_BranchFont.setHorizontalAlignment(SwingConstants.LEFT);
		lbl_BranchFont.setBounds(97, 17, 371, 31);
		lbl_BranchFont.setFont(Common.config.getFontPreference("branch"));
		lbl_BranchFont.setText(util.getFontDisplayName(Common.config.getFontPreference("branch")));
		panel_Fonts.add(lbl_BranchFont);

		lbl_TitleLeafFont.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_TitleLeafFont.setBounds(10, 49, 75, 31);
		lbl_TitleLeafFont.setText("Leaf Font");
		panel_Fonts.add(lbl_TitleLeafFont);

		lbl_LeafFont.setHorizontalAlignment(SwingConstants.LEFT);
		lbl_LeafFont.setBounds(97, 49, 371, 31);
		lbl_LeafFont.setFont(Common.config.getFontPreference("leaf"));
		lbl_LeafFont.setText(util.getFontDisplayName(Common.config.getFontPreference("leaf")));
		panel_Fonts.add(lbl_LeafFont);

		lbl_TitleTerminalFont.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_TitleTerminalFont.setBounds(10, 82, 75, 31);
		lbl_TitleTerminalFont.setText("Terminal Font");
		panel_Fonts.add(lbl_TitleTerminalFont);

		lbl_TerminalFont.setHorizontalAlignment(SwingConstants.LEFT);
		lbl_TerminalFont.setBounds(97, 82, 371, 31);
		lbl_TerminalFont.setFont(Common.config.getFontPreference("terminal"));
		lbl_TerminalFont.setText(util.getFontDisplayName(Common.config.getFontPreference("terminal")));
		panel_Fonts.add(lbl_TerminalFont);

		JButton4j btnBranchFont = new JButton4j(Common.icon_font);
		btnBranchFont.setBounds(474, 18, 31, 31);
		panel_Fonts.add(btnBranchFont);
		btnBranchFont.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				lbl_BranchFont.setFont(selectFont(lbl_BranchFont.getFont()));
				lbl_BranchFont.setText(util.getFontDisplayName(lbl_BranchFont.getFont()));
			}
		});

		JButton4j btnLeafFont = new JButton4j(Common.icon_font);
		btnLeafFont.setBounds(474, 49, 31, 31);
		panel_Fonts.add(btnLeafFont);
		btnLeafFont.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				lbl_LeafFont.setFont(selectFont(lbl_LeafFont.getFont()));
				lbl_LeafFont.setText(util.getFontDisplayName(lbl_LeafFont.getFont()));
			}
		});

		JButton4j btnTerminalFont = new JButton4j(Common.icon_font);
		btnTerminalFont.setBounds(474, 80, 31, 31);
		panel_Fonts.add(btnTerminalFont);

		btnTerminalFont.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				lbl_TerminalFont.setFont(selectFont(lbl_TerminalFont.getFont()));
				lbl_TerminalFont.setText(util.getFontDisplayName(lbl_TerminalFont.getFont()));
			}
		});

		JButton4j btnBranchForeground = new JButton4j("Foreground");
		btnBranchForeground.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				branchForegroundColor = JColorChooser.showDialog(JDialogSettings.this, "Choose Foreground Color", Color.BLACK);

				if (branchForegroundColor != null)
				{
					textField_Branch_Forground.setText(utils.toHex(branchForegroundColor));
					applyCurrentColors();
				}
			}
		});
		btnBranchForeground.setBounds(509, 17, 100, 29);
		panel_Fonts.add(btnBranchForeground);

		JButton4j btnLeafForeground = new JButton4j("Foreground");
		btnLeafForeground.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				leafForegroundColor = JColorChooser.showDialog(JDialogSettings.this, "Choose Foreground Color", Color.BLACK);

				if (leafForegroundColor != null)
				{
					textField_Leaf_Forground.setText(utils.toHex(leafForegroundColor));
					applyCurrentColors();
				}
			}
		});
		btnLeafForeground.setBounds(509, 48, 100, 29);
		panel_Fonts.add(btnLeafForeground);

		JButton4j btnForeground = new JButton4j("Foreground");
		btnForeground.setBounds(509, 79, 100, 29);
		panel_Fonts.add(btnForeground);

		JButton4j btnBackground = new JButton4j("Background");
		btnBackground.setBounds(509, 109, 100, 29);
		panel_Fonts.add(btnBackground);

		textField_Terminal_Forground = new JLabel();
		textField_Terminal_Forground.setOpaque(true);
		textField_Terminal_Forground.setBackground(Common.color_app_window);
		textField_Terminal_Forground.setHorizontalAlignment(SwingConstants.CENTER);
		textField_Terminal_Forground.setBounds(614, 79, 75, 29);
		panel_Fonts.add(textField_Terminal_Forground);
		textField_Terminal_Forground.setText(Common.config.getColorTerminalForeground());

		textField_Terminal_Background = new JLabel();
		textField_Terminal_Background.setOpaque(true);
		textField_Terminal_Background.setBackground(Common.color_app_window);
		textField_Terminal_Background.setHorizontalAlignment(SwingConstants.CENTER);
		textField_Terminal_Background.setBounds(614, 109, 75, 29);
		panel_Fonts.add(textField_Terminal_Background);
		textField_Terminal_Background.setText(Common.config.getColorTerminalBackground());

		textField_Branch_Forground = new JLabel();
		textField_Branch_Forground.setOpaque(true);
		textField_Branch_Forground.setBackground(Common.color_app_window);
		textField_Branch_Forground.setHorizontalAlignment(SwingConstants.CENTER);
		textField_Branch_Forground.setText((String) null);
		textField_Branch_Forground.setText(Common.config.getColorBranchForeground());
		textField_Branch_Forground.setBounds(614, 17, 75, 29);
		panel_Fonts.add(textField_Branch_Forground);

		textField_Leaf_Forground = new JLabel();
		textField_Leaf_Forground.setOpaque(true);
		textField_Leaf_Forground.setBackground(Common.color_app_window);
		textField_Leaf_Forground.setHorizontalAlignment(SwingConstants.CENTER);
		textField_Leaf_Forground.setText((String) null);
		textField_Leaf_Forground.setText(Common.config.getColorLeafForeground());
		textField_Leaf_Forground.setBounds(614, 48, 75, 29);
		panel_Fonts.add(textField_Leaf_Forground);


		btnBackground.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				terminalBackgroundColor = JColorChooser.showDialog(JDialogSettings.this, "Choose Background Color", Color.BLACK);

				if (terminalBackgroundColor != null)
				{
					textField_Terminal_Background.setText(utils.toHex(terminalBackgroundColor));
					applyCurrentColors();
				}
			}
		});
		btnForeground.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				terminalForegroundColor = JColorChooser.showDialog(JDialogSettings.this, "Choose Background Color", Color.BLACK);

				if (terminalForegroundColor != null)
				{

					textField_Terminal_Forground.setText(utils.toHex(terminalForegroundColor));
					applyCurrentColors();
				}
			}
		});
		
		JButton4j okButton = new JButton4j(Common.icon_ok);
		okButton.setBounds(411, 492, 103, 30);
		contentPanel.add(okButton);
		okButton.setText("Confirm");
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				saveConfig();
			}
		});
		okButton.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton);

		JButton4j cancelButton = new JButton4j(Common.icon_cancel);
		cancelButton.setBounds(526, 492, 103, 30);
		contentPanel.add(cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		
		applyCurrentColors();

		widthadjustment = Utility.getOSWidthAdjustment();
		heightadjustment = Utility.getOSHeightAdjustment();

		GraphicsDevice gd = Utility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JDialogSettings.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JDialogSettings.this.getHeight()) / 2), JDialogSettings.this.getWidth() + widthadjustment,
				JDialogSettings.this.getHeight() + heightadjustment);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textField_treeFilename.requestFocus();
				textField_treeFilename.setCaretPosition(textField_treeFilename.getText().length());



			}
		});
	}

	private Font selectFont(Font currentFont)
	{
		Font result = currentFont;
		JDialogFonts dialog = new JDialogFonts(JDialogSettings.this, currentFont);
		dialog.setVisible(true);
		result = JDialogFonts.selectedFont;

		return result;
	}

	private File selectCommand(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);

		JFileFilterExecs ffi = new JFileFilterExecs();
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogSettings.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

	private File selectLoadTreeXML(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);
		fc.setSelectedFile(defaultPath);

		JFileFilterXML ffi = new JFileFilterXML();
		fc.setApproveButtonText("Open");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogSettings.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

	private void addCommandRecord()
	{
		String param = JOptionPane.showInputDialog(JDialogSettings.this, "New System Command");

		if (param != null)
		{
			if (param.equals("") == false)
			{
				commandModel.addElement(param);
				commandList.setModel(commandModel);
			}
		}
	}

	private void editCommandRecord()
	{
		if (commandList.isSelectionEmpty() == false)
		{
			int idx = commandList.getSelectedIndex();
			String param = commandModel.getElementAt(idx);
			String result = JOptionPane.showInputDialog(JDialogSettings.this, "Amend System Command", param);
			if (result != null)
			{
				;
				if (result != null)
				{
					if (result.equals("") == false)
					{

						commandModel.set(idx, result);
						commandList.setModel(commandModel);
					}
				}
			}
		}
	}

	private void deleteCommandRecord()
	{
		if (commandList.isSelectionEmpty() == false)
		{
			int idx = commandList.getSelectedIndex();
			int question = JOptionPane.showConfirmDialog(JDialogSettings.this, "Confirm ?", "Remove Command", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
			if (question == 0)
			{
				commandModel.remove(idx);

				commandList.setModel(commandModel);

			}
		}
	}

	private void addEnvironmentRecord()
	{
		String param = JOptionPane.showInputDialog(JDialogSettings.this, "New Environment Variable");

		if (param != null)
		{
			if (param.equals("") == false)
			{
				String[] parts = param.split("=");
				JEnvironmentVariable env = new JEnvironmentVariable(parts[0], parts[1]);
				environmentModel.addElement(env);
				environmentList.setModel(environmentModel);
			}
		}
	}

	private void deleteEnvironmentRecord()
	{
		if (environmentList.isSelectionEmpty() == false)
		{
			int idx = environmentList.getSelectedIndex();
			int question = JOptionPane.showConfirmDialog(JDialogSettings.this, "Confirm ?", "Remove Environment Variable", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
			if (question == 0)
			{
				environmentModel.removeElementAt(idx);
				environmentList.setModel(environmentModel);

			}
		}
	}

	private void editEnvironmentRecord()
	{
		if (environmentList.isSelectionEmpty() == false)
		{
			int idx = environmentList.getSelectedIndex();
			JEnvironmentVariable param = environmentModel.getElementAt(idx);
			String result = JOptionPane.showInputDialog(JDialogSettings.this, "Amend Environment Variable", param);
			if (result != null)
			{
				String[] parts = result.split("=");
				JEnvironmentVariable env = new JEnvironmentVariable(parts[0], parts[1]);
				if (result != null)
				{
					if (result.equals("") == false)
					{

						environmentModel.removeElementAt(idx);
						environmentModel.insertElementAt(env, idx);

					}
				}
			}
		}
	}

	private void saveConfig()
	{
		Common.config.setColorTerminalBackground(textField_Terminal_Background.getText());
		Common.config.setColorTerminalForground(textField_Terminal_Forground.getText());
		Common.config.setColorLeafForegound(textField_Leaf_Forground.getText());
		Common.config.setColorBranchForeground(textField_Branch_Forground.getText());
		Common.config.setScriptEnabled(chckbx_runScript.isSelected());
		Common.config.setScriptFilename(textField_script.getText());
		Common.config.setPassword(new String(textField_Password.getPassword()));

		Common.config.clearEnviroment();
		for (int x = 0; x < environmentModel.getSize(); x++)
		{
			JEnvironmentVariable env = environmentModel.getElementAt(x);
			Common.config.setEnvironmentVariable(env.key, env.variable);
		}

		Common.config.clearCommands();
		for (int x = 0; x < commandModel.getSize(); x++)
		{
			String env = commandModel.getElementAt(x);
			Common.config.addValidCommand(env);
		}

		Map<String, JDBFont> savePrefs = new HashMap<String, JDBFont>();

		savePrefs.put("terminal", new JDBFont(lbl_TerminalFont.getFont().getName(), util.parseFontStyle(lbl_TerminalFont.getFont().getStyle()), lbl_TerminalFont.getFont().getSize()));
		savePrefs.put("leaf", new JDBFont(lbl_LeafFont.getFont().getName(), util.parseFontStyle(lbl_LeafFont.getFont().getStyle()), lbl_LeafFont.getFont().getSize()));
		savePrefs.put("branch", new JDBFont(lbl_BranchFont.getFont().getName(), util.parseFontStyle(lbl_BranchFont.getFont().getStyle()), lbl_BranchFont.getFont().getSize()));

		Common.config.setFontPreferences(savePrefs);

		Common.configSaver.save();

		dispose();
	}
	
	private void getConfigColors()
	{
		terminalForegroundColor = utils.fromHex(Common.config.getColorTerminalForeground());
		terminalBackgroundColor = utils.fromHex(Common.config.getColorTerminalBackground());
		
		leafForegroundColor = utils.fromHex(Common.config.getColorLeafForeground());
	    branchForegroundColor = utils.fromHex(Common.config.getColorBranchForeground());
	}
	
	private void applyCurrentColors()
	{
		textField_Terminal_Background.setForeground(terminalForegroundColor);
		textField_Terminal_Background.setBackground(terminalBackgroundColor);
		
		textField_Terminal_Forground.setForeground(terminalForegroundColor);
		textField_Terminal_Forground.setBackground(terminalBackgroundColor);
		
	    textField_Branch_Forground.setForeground(branchForegroundColor);
		textField_Leaf_Forground.setForeground(leafForegroundColor);
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
}
