package com.commander4j.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.io.FilenameUtils;

import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.gui.JTextField4j;
import com.commander4j.sys.Common;
import com.commander4j.tree.JMenuOption;
import com.commander4j.util.JFileFilterLOG;
import com.commander4j.util.Utility;

public class JTerminalOutput extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JTextPane textPane;
	private StyledDocument doc;
	private SimpleAttributeSet defaultStyle;
	private SimpleAttributeSet currentStyle;

	private Utility utils = new Utility();
	private final JToolBar toolBarNorth = new JToolBar();
	private final JToolBar toolBarSouth = new JToolBar();

	JButton4j btnClose = new JButton4j(Common.icon_exit);
	JButton4j btnSave = new JButton4j(Common.icon_save);
	JButton4j btnRespond = new JButton4j(Common.icon_button_key);

	private static int widthadjustment = 0;
	private static int heightadjustment = 0;
	private final JLabel4j_std lblStatus = new JLabel4j_std("Status");
	private final JLabel4j_std lblReturnCode = new JLabel4j_std("");
	private final JLabel4j_std lblInputText = new JLabel4j_std("Input : ");
	private JTextField4j inputText = new JTextField4j();
	Process process;
	int result = -1;

	public JTerminalOutput()
	{
		super("Console Output");
		setResizable(false);

		Color fg = utils.fromHex(Common.config.getColorTerminalForeground());
		Color bg = utils.fromHex(Common.config.getColorTerminalBackground());

		textPane = new JTextPane();
		textPane.setBackground(bg);

		defaultStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(defaultStyle, fg);
		currentStyle = defaultStyle;

		doc = textPane.getStyledDocument();
		textPane.setFont(Common.config.getFontPreference("terminal"));
		textPane.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(textPane);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(toolBarNorth, BorderLayout.NORTH);
		getContentPane().add(toolBarSouth, BorderLayout.SOUTH);

		toolBarNorth.setBorder(BorderFactory.createEmptyBorder());
		toolBarNorth.setBackground(Common.color_app_window);
		toolBarNorth.setFloatable(false);
		
		toolBarSouth.setBorder(BorderFactory.createEmptyBorder());
		toolBarSouth.setBackground(Common.color_app_window);
		toolBarSouth.setFloatable(false);

		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setBackground(Common.color_app_window);
		lblReturnCode.setHorizontalAlignment(SwingConstants.CENTER);
		lblReturnCode.setBackground(Common.color_app_window);

		toolBarNorth.add(new Separator());
		toolBarNorth.add(lblStatus);
		toolBarNorth.add(new Separator());
		toolBarNorth.add(lblReturnCode);

		inputText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				 if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					 respond(); 
				 }
			}
		});

		inputText.setSize(new Dimension(350, 25));
		inputText.setPreferredSize(new Dimension(350, 25));

		toolBarSouth.add(new Separator());
		toolBarSouth.add(lblInputText);
		toolBarSouth.add(new Separator());
		toolBarSouth.add(inputText);
		btnRespond.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				respond();
			}
		});
		btnRespond.setFocusable(false);
		toolBarSouth.add(btnRespond);
		toolBarNorth.add(new Separator());
		btnSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveConsoleOutput();
			}
		});
		btnSave.setToolTipText("Save Console output to file.");
		btnSave.setFocusable(false);
		
				toolBarNorth.add(btnSave);
		btnClose.setToolTipText("Close Console");
		
				btnClose.setFocusable(false);
				toolBarNorth.add(btnClose);
				
						btnClose.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								dispose();
							}
						});
		toolBarNorth.add(new Separator());
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				inputText.requestFocus();
				inputText.setCaretPosition(inputText.getText().length());
			}
		});

		setSize(1031, 774);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null); // Center window

		// Auto-scroll
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		widthadjustment = Utility.getOSWidthAdjustment();
		heightadjustment = Utility.getOSHeightAdjustment();

		GraphicsDevice gd = Utility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JTerminalOutput.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JTerminalOutput.this.getHeight()) / 2), JTerminalOutput.this.getWidth() + widthadjustment,
				JTerminalOutput.this.getHeight() + heightadjustment);
		setVisible(true);

	}

	public void respond()
	{
		if (process.isAlive())
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			try
			{
				bw.write(inputText.getText());
				bw.newLine();
				bw.flush();
				inputText.setText("");
			}
			catch (Exception ex)
			{
				System.out.println(ex.getMessage());
			}
			finally
			{
				bw=null;
			}
		}
	}
	
	public void runProcessAndCaptureOutput(JMenuOption menuOption,ProcessBuilder processBuilder)
	{
		setTitle("Console Output ["+menuOption.getDescription()+"]");
		
		runProcessAndCaptureOutput(processBuilder);
	}
	
	public void runProcessAndCaptureOutput(ProcessBuilder processBuilder)
	{
		lblStatus.setFont(Common.font_input_large);
		lblReturnCode.setFont(Common.font_input_large);
		lblReturnCode.setOpaque(true);
		lblStatus.setOpaque(true);
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblReturnCode.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setText("Running");
		lblStatus.setIcon(Common.icon_release);
		lblReturnCode.setText("");
		btnClose.setEnabled(false);
		btnSave.setEnabled(false);

		new Thread(() -> {
			try
			{

				process = processBuilder.start();

				InputStreamReader isr = new InputStreamReader(process.getInputStream());
				BufferedReader reader = new BufferedReader(isr);

				String line;
				while ((line = reader.readLine()) != null)
				{
					appendAnsiFormatted(line + "\n");
				}

				result = process.waitFor();
				
				System.out.println(result);

				lblStatus.setText("Finished");
				lblStatus.setIcon(Common.icon_hold);
				lblReturnCode.setText("Exit Code: ["+String.valueOf(result)+"]");

				btnClose.setEnabled(true);
				btnSave.setEnabled(true);
				btnRespond.setEnabled(false);
				inputText.setEnabled(false);
			}
			catch (IOException | InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}).start();

	}

	private void appendAnsiFormatted(String text)
	{
		text = text.replaceAll("\\\\033\\[", "\u001B[");

		int index = 0;
		while (index < text.length())
		{
			int escapeStart = text.indexOf("\u001B[", index);
			if (escapeStart == -1)
			{
				try
				{
					doc.insertString(doc.getLength(), text.substring(index), currentStyle);
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
				break;
			}

			if (escapeStart > index)
			{
				try
				{
					doc.insertString(doc.getLength(), text.substring(index, escapeStart), currentStyle);
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
			}

			int escapeEnd = text.indexOf('m', escapeStart);
			if (escapeEnd == -1)
			{
				break;
			}

			String escapeCode = text.substring(escapeStart + 2, escapeEnd);
			applyAnsiCodes(escapeCode);
			index = escapeEnd + 1;
		}
	}

	// Apply ANSI color codes to the current style
	private void applyAnsiCodes(String codes)
	{
		String[] parts = codes.split(";");
		SimpleAttributeSet newStyle = new SimpleAttributeSet(currentStyle);

		for (String part : parts)
		{
			switch (part)
			{
			case "0": // Reset
				newStyle = new SimpleAttributeSet(defaultStyle);
				break;
			case "1": // Bold (optional enhancement later)
				StyleConstants.setBold(newStyle, true);
				break;
			case "4": // Underline (optional)
				StyleConstants.setUnderline(newStyle, true);
				break;
			case "30":
				StyleConstants.setForeground(newStyle, Color.BLACK);
				break;
			case "31":
				StyleConstants.setForeground(newStyle, Color.RED);
				break;
			case "32":
				StyleConstants.setForeground(newStyle, Color.GREEN);
				break;
			case "33":
				StyleConstants.setForeground(newStyle, Color.ORANGE);
				break;
			case "34":
				StyleConstants.setForeground(newStyle, Color.BLUE);
				break;
			case "35":
				StyleConstants.setForeground(newStyle, new Color(213, 0, 255));
				break; // Purple
			case "36":
				StyleConstants.setForeground(newStyle, Color.CYAN);
				break;
			case "37":
				StyleConstants.setForeground(newStyle, Color.LIGHT_GRAY);
				break;
			case "90":
				StyleConstants.setForeground(newStyle, Color.DARK_GRAY);
				break;
			case "91":
				StyleConstants.setForeground(newStyle, Color.PINK);
				break; // Light Red
			case "92":
				StyleConstants.setForeground(newStyle, Color.GREEN.brighter());
				break; // Light Green
			case "93":
				StyleConstants.setForeground(newStyle, Color.YELLOW);
				break;
			case "94":
				StyleConstants.setForeground(newStyle, Color.CYAN.darker());
				break; // Light Blue variant
			case "95":
				StyleConstants.setForeground(newStyle, Color.MAGENTA);
				break; // Light Purple
			case "96":
				StyleConstants.setForeground(newStyle, Color.CYAN.brighter());
				break; // Light Cyan
			case "97":
				StyleConstants.setForeground(newStyle, Color.WHITE);
				break;
			default:
				break;
			}
		}

		// Save the style for future output
		currentStyle = newStyle;
	}

	private void saveConsoleOutput()
	{
		File result = new File(System.getProperty("user.home"));

		JFileChooser fc = new JFileChooser(result);

		JFileFilterLOG ffi = new JFileFilterLOG();
		fc.setApproveButtonText("Save");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JTerminalOutput.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();

			String extension = FilenameUtils.getExtension(result.getAbsolutePath());
			if (extension.equals(""))
			{
				result = new File(result.getAbsolutePath() + ".log");
			}

			String rawOutput = textPane.getText();

			saveToAsciiFile(rawOutput, result);
		}
		else
		{
			result = null;
		}

	}

	public void saveToAsciiFile(String content, File file)
	{
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
		{
			writer.write(content);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
