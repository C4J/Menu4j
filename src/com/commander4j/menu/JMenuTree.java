package com.commander4j.menu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FilenameUtils;

import com.commander4j.config.JMenuConfigLoader;
import com.commander4j.dialog.JDialogAbout;
import com.commander4j.dialog.JDialogBranch;
import com.commander4j.dialog.JDialogLeaf;
import com.commander4j.dialog.JDialogLicenses;
import com.commander4j.dialog.JDialogPassword;
import com.commander4j.dialog.JDialogSettings;
import com.commander4j.dnd.TreeTransferHandler;
import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JList4j;
import com.commander4j.process.Execute;
import com.commander4j.renderer.JMenuTreeRenderer;
import com.commander4j.sys.Common;
import com.commander4j.sys.JLicenseInfo;
import com.commander4j.tree.JMenuOption;
import com.commander4j.util.JFileFilterXML;
import com.commander4j.util.JHelp;
import com.commander4j.util.Utility;

public class JMenuTree extends JFrame
{

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;
	JList4j<JLicenseInfo> list = new JList4j<JLicenseInfo>();
	

	public static String version = "1.62";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		try
		{
			JMenuTree dialog = new JMenuTree();
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setVisible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public JMenuTree()
	{
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Common.iconPath + "home.gif"));
		

		setFrameTitle();
		
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		Utility.setLookAndFeel("Nimbus");
		
		Common.osName = Utility.getOSName();

		Common.commandFolder = new File(System.getProperty("user.dir"));
		Common.iconFolder = new File(System.getProperty("user.dir") + File.separator + "images" + File.separator + "appIcons");
		
		Common.settingsFolderFile = new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "config.xml");
		
		
		initFiles(new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "init" + File.separator+ "config.xml"),Common.settingsFolderFile);

		Common.scriptFolder = new File(System.getProperty("user.dir") + File.separator + "script");
		Common.config = JMenuConfigLoader.load();
		

		Common.treeFolderFile = new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "tree" + File.separator + Common.config.getTreeFilename());

		Common.treeFolderPath = new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "tree" + File.separator + ".");

		initFiles(new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "tree" + File.separator + "init" + File.separator+ "tree.xml"),Common.treeFolderFile);
		initFiles(new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "tree" + File.separator + "init" + File.separator+ "tree.xml.state"),new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "tree" + File.separator + "tree.xml.state"));
	
		
		if (Common.config.getPassword().equals("") == false)
		{
			boolean success = false;

			int attempt = 1;

			while (attempt <= 3)
			{
				attempt++;

				JDialogPassword password = new JDialogPassword(Common.config.getPassword());
				password.setVisible(true);

				if (password.action.equals("OK"))
				{
				
				if (password.enteredPassword.equals(Common.config.getPassword()))
				{
					success = true;
					break;
				}
				}
				else
				{
					attempt = 4;
					success = false;
				}
			}

			if (success == false)
			{
				System.exit(attempt);
			}

		}

		setUndecorated(false);

		addWindowListener(new WindowListener());

		Common.tree = new JTree();
		Common.tree.setToolTipText("<html>Double click item to execute.<br/>Items can be reordered using drag and drop.</html>");
		Common.tree.setDragEnabled(true);
		Common.tree.setDropMode(DropMode.ON_OR_INSERT);
		Common.tree.setTransferHandler(new TreeTransferHandler());
		Common.tree.setBackground(Common.color_app_window);
		Common.tree.setToggleClickCount(0);
		Common.tree.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) Common.tree.getLastSelectedPathComponent();

					if (node == null)
						return;

					if (node.getUserObject().getClass() == JMenuOption.class)
					{
						JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());

						if (nodeInfo.getType().equals("leaf"))
						{
							execute();
						}
						else
						{
							toggleBranchExpansion();
						}
					}
				}
			}
		});

		Common.treeLoader.loadTree(JMenuTree.this);
		Common.tree.setCellRenderer(new JMenuTreeRenderer());
		ToolTipManager.sharedInstance().registerComponent(Common.tree);
		Common.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		setBounds(100, 100, 500, 845);
		getContentPane().setLayout(new BorderLayout());

		contentPanel.setBorder(new CompoundBorder());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		scrollPane.setViewportView(Common.tree);

		JToolBar toolBarTop = new JToolBar();
		toolBarTop.setFloatable(false);
		toolBarTop.setBorder(BorderFactory.createEmptyBorder());
		toolBarTop.setBackground(Common.color_app_window);
		contentPanel.add(toolBarTop, BorderLayout.NORTH);

		JButton4j btnExpandAll = new JButton4j(Common.icon_expandAll);
		btnExpandAll.setToolTipText("Expand entire Tree");
		btnExpandAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				expandTree();
			}
		});
		btnExpandAll.setPreferredSize(new Dimension(32, 32));
		btnExpandAll.setFocusable(false);
		toolBarTop.add(btnExpandAll);

		JButton4j btnExpand = new JButton4j(Common.icon_expandNode);
		btnExpand.setToolTipText("Expand selected branch of Tree");
		btnExpand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath path = Common.tree.getSelectionPath();
				expandAll(Common.tree, path, true);
			}
		});
		btnExpand.setPreferredSize(new Dimension(32, 32));
		btnExpand.setFocusable(false);
		toolBarTop.add(btnExpand);

		JButton4j btnCollapseAll = new JButton4j(Common.icon_collapseAll);
		btnCollapseAll.setToolTipText("Collapse entire Tree");
		btnCollapseAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				expandAll(Common.tree, false);
			}
		});
		btnCollapseAll.setPreferredSize(new Dimension(32, 32));
		btnCollapseAll.setFocusable(false);
		toolBarTop.add(btnCollapseAll);

		JButton4j btnCollapse = new JButton4j(Common.icon_collapeNode);
		btnCollapse.setToolTipText("Collapse selected branch of menu.");
		btnCollapse.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TreePath path = Common.tree.getSelectionPath();
				expandAll(Common.tree, path, false);
			}
		});
		btnCollapse.setPreferredSize(new Dimension(32, 32));
		btnCollapse.setFocusable(false);
		toolBarTop.add(btnCollapse);

		JToolBar toolBarSide = new JToolBar();
		toolBarSide.setBorder(BorderFactory.createEmptyBorder());
		toolBarSide.setOrientation(SwingConstants.VERTICAL);
		contentPanel.add(toolBarSide, BorderLayout.EAST);
		toolBarSide.setBackground(Common.color_app_window);
		toolBarSide.setFloatable(false);

		JButton4j btnExecute = new JButton4j(Common.icon_execute);
		btnExecute.setToolTipText("Execute Menu Item");
		toolBarSide.add(btnExecute);
		btnExecute.setPreferredSize(new Dimension(32, 32));
		btnExecute.setFocusable(false);
		btnExecute.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				execute();
			}
		});

		JButton4j btnSettings = new JButton4j(Common.icon_settings);
		btnSettings.setToolTipText("Settings");
		toolBarSide.add(btnSettings);
		btnSettings.setPreferredSize(new Dimension(32, 32));
		btnSettings.setFocusable(false);
		btnSettings.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogSettings settings = new JDialogSettings();
				settings.setVisible(true);
			}
		});

		JButton4j btnNewTree = new JButton4j(Common.icon_new);
		btnNewTree.setToolTipText("New Menu Tree");
		toolBarSide.add(btnNewTree);
		btnNewTree.setPreferredSize(new Dimension(32, 32));
		btnNewTree.setFocusable(false);
		btnNewTree.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveChanges();

				File saveXML = selectNewTreeXML();

				if (saveXML != null)
				{
					Common.config.setTreeFilename(saveXML.getName());
					Common.treeFolderFile = saveXML;
					DefaultMutableTreeNode root = (DefaultMutableTreeNode) Common.treeModel.getRoot();

					if (root.getUserObject().getClass() == JMenuOption.class)
					{
						JMenuOption opt = ((JMenuOption) root.getUserObject());
						opt.clear();
						opt.setType("root");
						opt.setDescription("New Tree");
					}

					root.removeAllChildren();

					Common.treeModel.reload();

					Common.treeSaver.saveTree(JMenuTree.this);
					setFrameTitle();

				}

			}
		});

		JButton4j btnOpen = new JButton4j(Common.icon_open);
		btnOpen.setToolTipText("Open Menu Tree");
		toolBarSide.add(btnOpen);
		btnOpen.setPreferredSize(new Dimension(32, 32));
		btnOpen.setFocusable(false);
		btnOpen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveChanges();
				File saveXML = selectLoadTreeXML();
				if (saveXML != null)
				{
					Common.treeState.saveTreeState();
					Common.treeFolderFile = saveXML;
					Common.config.setTreeFilename(Common.treeFolderFile.getName());
					Common.treeLoader.loadTree(JMenuTree.this);
					Common.configSaver.save();
				}

			}
		});

		JButton4j btnRefresh = new JButton4j(Common.icon_reload);
		btnRefresh.setToolTipText("Reload Menu Tree");
		toolBarSide.add(btnRefresh);
		btnRefresh.setPreferredSize(new Dimension(32, 32));
		btnRefresh.setFocusable(false);
		btnRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveChanges();
				Common.treeLoader.loadTree(JMenuTree.this);
			}
		});

		JButton4j btnSave = new JButton4j(Common.icon_save);
		btnSave.setToolTipText("Save Menu Tree");
		btnSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File saveXML = selectSaveTreeXML();

				if (saveXML != null)
				{
					Common.treeFolderFile = saveXML;
					Common.treeSaver.saveTree(JMenuTree.this);
					setFrameTitle();
				}
			}
		});

		toolBarSide.add(btnSave);
		btnSave.setPreferredSize(new Dimension(32, 32));
		btnSave.setFocusable(false);

		JButton4j btnEdit = new JButton4j(Common.icon_edit);
		btnEdit.setToolTipText("Edit Menu Item");
		btnEdit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editNode();
			}
		});

		JButton4j btnAdd = new JButton4j(Common.icon_add);
		btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String[] options =
				{ "branch", "leaf" };

				String result = (String) JOptionPane.showInputDialog(JMenuTree.this, "Select type of node to add", "Add to Tree", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if (result != null)
				{
					addNode(result);
				}

			}
		});
		btnAdd.setToolTipText("Add Menu item");
		toolBarSide.add(btnAdd);
		btnAdd.setPreferredSize(new Dimension(32, 32));
		btnAdd.setFocusable(false);
		toolBarSide.add(btnEdit);
		btnEdit.setPreferredSize(new Dimension(32, 32));
		btnEdit.setFocusable(false);

		JButton4j btnDelete = new JButton4j(Common.icon_delete);
		btnDelete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteNode();
			}
		});
		btnDelete.setToolTipText("Delete Menu Item");
		toolBarSide.add(btnDelete);
		btnDelete.setPreferredSize(new Dimension(32, 32));
		btnDelete.setFocusable(false);

		JButton4j btnDuplicate = new JButton4j(Common.icon_duplicate);
		btnDuplicate.setPreferredSize(new Dimension(32, 32));
		btnDuplicate.setFocusable(false);
		btnDuplicate.setToolTipText("Duplicate item");
		toolBarSide.add(btnDuplicate);
		btnDuplicate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				duplicateNode();
			}
		});

		JButton4j btnHelp = new JButton4j(Common.icon_help);
		btnHelp.setPreferredSize(new Dimension(32, 32));
		btnHelp.setFocusable(false);
		btnHelp.setToolTipText("Help");
		toolBarSide.add(btnHelp);
		
		final JHelp help = new JHelp();
		help.enableHelpOnButton(btnHelp, "https://wiki.commander4j.com/index.php?title=Menu4j");

		JButton4j btnAbout = new JButton4j(Common.icon_about);
		btnAbout.setPreferredSize(new Dimension(32, 32));
		btnAbout.setFocusable(false);
		btnAbout.setToolTipText("About");
		btnAbout.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogAbout about = new JDialogAbout();
				about.setVisible(true);
			}
		});
		toolBarSide.add(btnAbout);
		
		JButton4j btnLicense = new JButton4j(Common.icon_license);
		btnLicense.setPreferredSize(new Dimension(32, 32));
		btnLicense.setFocusable(false);
		btnLicense.setToolTipText("Licences");
		btnLicense.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogLicenses dl = new  JDialogLicenses(JMenuTree.this);
				dl.setVisible(true);
			}
		});

		toolBarSide.add(btnLicense);

		JButton4j btnClose = new JButton4j(Common.icon_exit);
		btnClose.setToolTipText("Exit Application");
		toolBarSide.add(btnClose);
		btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				confirmExit();
			}
		});
		btnClose.setPreferredSize(new Dimension(32, 32));
		btnClose.setFocusable(false);

		// expandTree();

		widthadjustment = Utility.getOSWidthAdjustment();
		heightadjustment = Utility.getOSHeightAdjustment();

		GraphicsDevice gd = Utility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JMenuTree.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JMenuTree.this.getHeight()) / 2), JMenuTree.this.getWidth() + widthadjustment,
				JMenuTree.this.getHeight() + heightadjustment);
		setVisible(true);
	}

	private void expandTree()
	{
		if (Common.tree.getModel().getRoot() != null)
		{
			int count = getNumberOfNodes(Common.tree.getModel());
			expandAllNodes(Common.tree, 0, count);
		}
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand)
	{
		// Traverse children
		try
		{
			TreeNode node = (TreeNode) parent.getLastPathComponent();
			if (node.getChildCount() >= 0)
			{
				for (Enumeration<?> e = node.children(); e.hasMoreElements();)
				{
					TreeNode n = (TreeNode) e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					expandAll(tree, path, expand);
				}
			}
			// Expansion or collapse must be done bottom-up
			if (expand)
			{
				tree.expandPath(parent);
			}
			else
			{
				tree.collapsePath(parent);
			}
		}
		catch (Exception e)
		{

		}
	}

	private void expandAllNodes(JTree tree, int startingIndex, int rowCount)
	{
		for (int i = startingIndex; i < rowCount; ++i)
		{
			tree.expandRow(i);
		}

		if (tree.getRowCount() != rowCount)
		{
			expandAllNodes(tree, rowCount, tree.getRowCount());
		}
	}

	public int getNumberOfNodes(TreeModel model)
	{
		return getNumberOfNodes(model, model.getRoot());
	}

	private int getNumberOfNodes(TreeModel model, Object node)
	{
		int count = 1;
		int nChildren = model.getChildCount(node);
		for (int i = 0; i < nChildren; i++)
		{
			count += getNumberOfNodes(model, model.getChild(node, i));
		}
		return count;
	}

	public void expandAll(JTree tree, boolean expand)
	{
		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private void execute()
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) Common.tree.getLastSelectedPathComponent();
		if (node != null)
		{
			JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());

			if (nodeInfo.getType().equals("leaf"))
			{

				if (nodeInfo.isLinkToMenuTreeEnabled())
				{
					saveChanges();
					
					if (nodeInfo.getMenuTreeFilename().equals("") == false)
					{
						String temp = Common.treeFolderPath.getAbsolutePath();
						temp = temp.substring(0, temp.length()-1);
						temp = temp +nodeInfo.getMenuTreeFilename();
						File linkedXML = new File (temp);
						if (linkedXML.exists())
						{
							Common.treeState.saveTreeState();
							Common.treeFolderFile = linkedXML;
							Common.config.setTreeFilename(Common.treeFolderFile.getName());
							Common.treeLoader.loadTree(JMenuTree.this);
							Common.configSaver.save();
						}
					}

				}
				else
				{
					boolean confirm = true;
					if (nodeInfo.isConfirmExecute())
					{
						int question = JOptionPane.showConfirmDialog(JMenuTree.this, "Execute\n\n" + nodeInfo.getDescription() + " ?", "Confirm", JOptionPane.YES_NO_OPTION, 0, Common.icon_confirm);
						if (question == 0)
						{
							confirm = true;
						}
						else
						{
							confirm = false;
						}
					}

					if (confirm)
					{
						Execute exec = new Execute();
						exec.execute(JMenuTree.this, nodeInfo);
					}
				}

			}
		}

	}

	private void toggleBranchExpansion()
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) Common.tree.getLastSelectedPathComponent();
		if (node != null)
		{
			JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());

			if ((nodeInfo.getType().equals("branch")) || (nodeInfo.getType().equals("root")))
			{
				TreePath path = Common.tree.getSelectionPath();
				if (Common.tree.isExpanded(path))
				{
					expandAll(Common.tree, path, false);
				}
				else
				{
					expandAll(Common.tree, path, true);
				}
			}
		}
	}

	private void addNode(String type)
	{
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) Common.tree.getLastSelectedPathComponent();
		if (selectedNode == null)
		{
			return;
		}

		DefaultTreeModel model = (DefaultTreeModel) Common.tree.getModel();

		DefaultMutableTreeNode parentNode = selectedNode;

		JMenuOption selectedMenuOption = (JMenuOption) selectedNode.getUserObject();

		if (selectedMenuOption.getType().equalsIgnoreCase("leaf"))
		{

			parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
			if (parentNode == null)
			{
				return;
			}
		}

		JMenuOption newMenuOption = new JMenuOption();
		newMenuOption.setType(type);
		newMenuOption.setDescription("New " + type);

		DefaultMutableTreeNode newBranchNode = new DefaultMutableTreeNode(newMenuOption);

		model.insertNodeInto(newBranchNode, parentNode, parentNode.getChildCount());
		Common.tree.scrollPathToVisible(new TreePath(newBranchNode.getPath()));
	}

	private void duplicateNode()
	{
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) Common.tree.getLastSelectedPathComponent();

		if (selectedNode != null)
		{

			DefaultTreeModel model = (DefaultTreeModel) Common.tree.getModel();

			JMenuOption selectedMenuOption = (JMenuOption) selectedNode.getUserObject();

			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();

			if (parentNode == null)
			{
				return;
			}

			JMenuOption newMenuOption = new JMenuOption();

			newMenuOption.clone(selectedMenuOption);
			newMenuOption.setDescription(selectedMenuOption.getDescription() + " Copy");

			DefaultMutableTreeNode newBranchNode = new DefaultMutableTreeNode(newMenuOption);

			model.insertNodeInto(newBranchNode, parentNode, parentNode.getChildCount());

			Common.tree.scrollPathToVisible(new TreePath(newBranchNode.getPath()));
		}
	}

	private void editNode()
	{

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) Common.tree.getLastSelectedPathComponent();
		if (node != null)
		{

			DefaultTreeModel model = (DefaultTreeModel) Common.tree.getModel();

			JMenuOption nodeInfo = (JMenuOption) (node.getUserObject());

			if (nodeInfo.getType().equals("leaf"))
			{
				JDialogLeaf dol = new JDialogLeaf(JMenuTree.this, nodeInfo);
				dol.setVisible(true);
			}

			if (nodeInfo.getType().equals("branch"))
			{
				JDialogBranch dob = new JDialogBranch(nodeInfo);
				dob.setVisible(true);
			}

			if (nodeInfo.getType().equals("root"))
			{
				JDialogBranch dob = new JDialogBranch(nodeInfo);
				dob.setVisible(true);
			}

			model.nodeChanged((TreeNode) Common.tree.getLastSelectedPathComponent());

		}
	}

	private void deleteNode()
	{
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) Common.tree.getLastSelectedPathComponent();
		if (selectedNode == null)
		{
			return; // No selection, nothing to delete
		}

		DefaultTreeModel model = (DefaultTreeModel) Common.tree.getModel();

		// Optional: Prevent deleting the root node
		if (selectedNode.isRoot())
		{
			JOptionPane.showMessageDialog(Common.tree, "Cannot delete the root node.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Ask for confirmation
		int result = JOptionPane.showConfirmDialog(Common.tree, "Are you sure you want to delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION)
		{
			model.removeNodeFromParent(selectedNode);
		}
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

		int returnVal = fc.showOpenDialog(JMenuTree.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

	private File selectSaveTreeXML()
	{
		File result = null;

		JFileChooser fc = new JFileChooser(Common.treeFolderFile);
		fc.setSelectedFile(Common.treeFolderFile);

		JFileFilterXML ffi = new JFileFilterXML();
		fc.setApproveButtonText("Save");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JMenuTree.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}
		else
		{
			result = null;
		}

		return result;
	}

	private File selectNewTreeXML()
	{
		File result = null;

		JFileChooser fc = new JFileChooser(Common.treeFolderPath);
		fc.setSelectedFile(Common.treeFolderPath);

		JFileFilterXML ffi = new JFileFilterXML();
		fc.setApproveButtonText("Create");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showSaveDialog(JMenuTree.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
			String extension = FilenameUtils.getExtension(result.getAbsolutePath());
			if (extension.equals(""))
			{
				result = new File(result.getAbsolutePath() + ".xml");
			}
		}
		else
		{
			result = null;
		}

		return result;
	}

	public void setFrameTitle()
	{
		if (Common.treeFolderFile == null)
		{
			this.setTitle("JMenuTree " + version);
		}
		else
		{
			this.setTitle("JMenuTree " + version + " - [" + Common.treeFolderFile.getName() + "]");
		}
	}

	class WindowListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			confirmExit();
		}
	}

	private void saveChanges()
	{
		if (Common.treeChanged)
		{
			int question = JOptionPane.showConfirmDialog(JMenuTree.this, "Tree modified - save changes ?", "Confirm", JOptionPane.YES_NO_OPTION, 0, Common.icon_confirm);
			if (question == 0)
			{
				Common.treeSaver.saveTree(JMenuTree.this);
			}
		}
	}

	private void confirmExit()
	{
		saveChanges();

		int question = JOptionPane.showConfirmDialog(JMenuTree.this, "Exit application ?", "Confirm", JOptionPane.YES_NO_OPTION, 0, Common.icon_confirm);
		if (question == 0)
		{
			Common.treeState.saveTreeState();
			System.exit(0);
		}
	}
	
	private boolean initFiles(File sourceFile,File destinationFile)
	{
		boolean result = true;
		
		
		if (destinationFile.exists()==false)
		{
			try
			{				
				System.out.println("Copying ["+sourceFile.getAbsoluteFile()+"] to ["+destinationFile.getAbsoluteFile()+"]");
				org.apache.commons.io.FileUtils.copyFile(sourceFile,destinationFile);	
	
			}
			catch (Exception ex)
			{
				result = false;
			}
		}
				
		return result;
	}

}
