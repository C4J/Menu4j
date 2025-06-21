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
import com.commander4j.gui.JPasswordField4j;
import com.commander4j.sys.Common;
import com.commander4j.util.Utility;

public class JDialogPassword extends JDialog
{

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JPasswordField4j textField_Password;
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;
	public String enteredPassword = "";
	public String action = "";

	/**
	 * Create the dialog.
	 */
	public JDialogPassword(String password)
	{
		setResizable(false);
		setTitle("Enter Password");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		Utility.setLookAndFeel("Nimbus");

		setBounds(100, 100, 330, 147);
		getContentPane().setLayout(null);
		contentPanel.setBackground(Common.color_app_window);
		contentPanel.setBounds(0, 0, 755, 160);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		{
			textField_Password = new JPasswordField4j();
			textField_Password.setBounds(134, 18, 167, 22);
			contentPanel.add(textField_Password);
			
		}

		JLabel4j_std lbl_Password = new JLabel4j_std("Description");
		lbl_Password.setText("Password");
		lbl_Password.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_Password.setBounds(6, 18, 120, 22);
		contentPanel.add(lbl_Password);

		{
			JButton4j okButton = new JButton4j(Common.icon_ok);
			okButton.setText("Confirm");
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action = "OK";
					enteredPassword = new String(textField_Password.getPassword());
					dispose();
				}
			});
			okButton.setBounds(46, 60, 103, 30);
			contentPanel.add(okButton);
			okButton.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton);
		}
		{
			JButton4j cancelButton = new JButton4j(Common.icon_cancel);
			cancelButton.setText("Cancel");
			cancelButton.setBounds(157, 60, 103, 30);
			contentPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action = "Cancel";
					enteredPassword = "";
					dispose();
				}
			});
		}

		widthadjustment = Utility.getOSWidthAdjustment();
		heightadjustment = Utility.getOSHeightAdjustment();

		GraphicsDevice gd = Utility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JDialogPassword.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JDialogPassword.this.getHeight()) / 2), JDialogPassword.this.getWidth() + widthadjustment,
				JDialogPassword.this.getHeight() + heightadjustment);
		// setVisible(true);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textField_Password.requestFocus();

			}
		});
	}
}
