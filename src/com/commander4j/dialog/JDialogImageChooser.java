package com.commander4j.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.commander4j.gui.JButton4j;
import com.commander4j.sys.Common;

public class JDialogImageChooser extends JDialog {

    private static final long serialVersionUID = 1L;
	private File selectedImageFile;

    public JDialogImageChooser(Frame owner, File folder) {
        super(owner, "Select an Image", true);
        setResizable(false);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Common.color_app_window);

        java.util.List<File> imageFiles = loadImageFiles(folder);
        Collections.sort(imageFiles);
        JPanel imagePanel = new JPanel(new GridLayout(0, 8, 10, 10)); // 8 columns, adjust as needed
        imagePanel.setToolTipText("Click on the image that you want to use.");
        imagePanel.setBorder(new EmptyBorder(10, 5, 10, 5));
        imagePanel.setBackground(Common.color_app_window);

        for (File file : imageFiles) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(25, 25, Image.SCALE_SMOOTH));
                    JButton4j imgButton = new JButton4j(icon);
                    imgButton.setSize(new Dimension(30,30));
                    imgButton.setToolTipText(file.getName());
                    imgButton.setBorder(BorderFactory.createEmptyBorder());
                    imgButton.setContentAreaFilled(false);

                    imgButton.addActionListener(e -> {
                        selectedImageFile = file;
                        dispose();
                    });

                    imagePanel.add(imgButton);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JButton4j cancel = new JButton4j("Cancel");
        cancel.setIcon(Common.icon_cancel);
        cancel.addActionListener(e -> {
            selectedImageFile = null;
            dispose();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Common.color_app_window);
        bottomPanel.add(cancel);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private java.util.List<File> loadImageFiles(File folder) {
        File[] files = folder.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".png") ||
            name.toLowerCase().endsWith(".jpg") ||
            name.toLowerCase().endsWith(".jpeg") ||
            name.toLowerCase().endsWith(".gif")
        );
        Arrays.sort(files);
        return files != null ? java.util.Arrays.asList(files) : new ArrayList<>();
    }

    public File getSelectedImageFile() {
        return selectedImageFile;
    }

}
