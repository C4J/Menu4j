package com.commander4j.dialog;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.gui.JTextField4j;
import com.commander4j.sys.Common;
import com.commander4j.tree.JMenuOption;
import com.commander4j.util.Utility;

public class JDialogBranch extends JDialog
{

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField4j textField_description;
	private JTextField4j textField_type;
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;

	/**
	 * Create the dialog.
	 */
	public JDialogBranch(JMenuOption menuOption)
	{
		setResizable(false);
		setTitle("Menu Branch");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		Utility.setLookAndFeel("Nimbus");
		
		setBounds(100, 100, 618, 160);
		getContentPane().setLayout(null);
		contentPanel.setBackground(Common.color_app_window);
		contentPanel.setBounds(0, 0,getWidth(), getHeight());
		contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		{
			textField_type = new JTextField4j();
			textField_type.setEnabled(false);
			textField_type.setEditable(false);
			textField_type.setBounds(134, 12, 114, 22);
			contentPanel.add(textField_type);
			textField_type.setText(menuOption.getType());
		}
		{
			textField_description = new JTextField4j();
			textField_description.setBounds(134, 43, 452, 22);
			contentPanel.add(textField_description);
			textField_description.setText(menuOption.getDescription());
		}
		
		JLabel4j_std lbl_description = new JLabel4j_std("Description");
		lbl_description.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_description.setBounds(6, 43, 120, 22);
		contentPanel.add(lbl_description);
		
		{
			JButton4j okButton = new JButton4j(Common.icon_ok);
			okButton.setText("Confirm");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					menuOption.setDescription(textField_description.getText());
					dispose();
				}
			});
			okButton.setBounds(196, 77, 103, 30);
			contentPanel.add(okButton);
			okButton.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton);
		}
		{
			JButton4j cancelButton = new JButton4j(Common.icon_cancel);
			cancelButton.setText("Cancel");
			cancelButton.setBounds(307, 77, 103, 30);
			contentPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		
		
		widthadjustment = Utility.getOSWidthAdjustment();
		heightadjustment = Utility.getOSHeightAdjustment();

		GraphicsDevice gd = Utility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JDialogBranch.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JDialogBranch.this.getHeight()) / 2), JDialogBranch.this.getWidth() + widthadjustment, JDialogBranch.this.getHeight() + heightadjustment);
		//setVisible(true);
		
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textField_description.requestFocus();
				textField_description.setCaretPosition(textField_description.getText().length());
				
				JLabel4j_std lbl_type = new JLabel4j_std("Type");
				lbl_type.setBounds(6, 11, 120, 22);
				contentPanel.add(lbl_type);
				lbl_type.setHorizontalAlignment(SwingConstants.RIGHT);



			}
		});
	}
}
