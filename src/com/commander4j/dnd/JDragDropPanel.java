package com.commander4j.dnd;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

/**
 * Small Swing panel that accepts a drag & drop of a macOS .app bundle.
 * After a successful drop it parses Info.plist, extracts useful fields,
 * displays a preview, and exposes getters for your dialog to consume.
 *
 * Optional callback: setDropListener(...) to be notified immediately.
 */
public class JDragDropPanel extends JPanel {

    private static final long serialVersionUID = 1L;
	//private final JLabel lblTitle = new JLabel("Drop a .app bundle here");
    private final JLabel message  = new JLabel("Drag & Drop");
    private final JLabel lblIcon  = new JLabel();


    private JDragDropAppInfo appInfo;                     // last parsed result
    private JDragDropMacListenerMac dropListener;        // optional callback

    public JDragDropPanel() {
    	
        super();
        
        setBackground(new Color(144, 238, 144));
        setLayout(null);
        setSize(111,28);
        
        message.setHorizontalAlignment(SwingConstants.CENTER);
        message.setSize(110, 27);
     
        add(message);
        
        message.setLocation(0, 0);

        // Drag & drop handler
        setTransferHandler(new FileDropHandler());

        // Initial “empty” state
        clear();
    }

    /** Optional: only add this panel on macOS */
    public static boolean isMacOS() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("mac");
    }

    /** Returns the last parsed MacAppInfo, or null if none yet. */
    public JDragDropAppInfo getSelectedAppInfo() {
        return appInfo;
    }
    /** Convenience getters your dialog can call after drop: */

    /** e.g. "/Applications" (parent folder of the .app) */
    public String getWorkingDirectory() {
        return appInfo == null ? "" : appInfo.bundlePath.getParent().toString();
    }

    /** e.g. "Safari.app" */
    public String getAppBundleFolderName() {
        return appInfo == null ? "" : appInfo.bundlePath.getFileName().toString();
    }
    
    public String getExecutableFullPath() {
        return appInfo == null ? "" : appInfo.bundlePath.toString();
    }
    
    public String getIconName_PNG() {
        return appInfo == null ? "" : getBundleName()+".png";
    }

    /** e.g. "AppIcon.icns" (may be empty if not found) */
    public String getIconName_ICNS() {
        return (appInfo == null || appInfo.iconIcnsPath == null)
                ? "" : appInfo.iconIcnsPath.getFileName().toString();
    }

    /** Optional extra fields if you want them */
    public String getBundleId() {
        return appInfo == null ? "" : appInfo.bundleId;
    }
    
    /** Optional extra fields if you want them */
    public String getBundleName() {
        return appInfo == null ? "" : appInfo.bundleName;
    }

    public String getExecutableName() {
        return appInfo == null ? "" : appInfo.executableName;
    }

    public Path getExecutablePath() {
        return appInfo == null ? null : appInfo.executablePath;
    }
    

    /** Register an optional callback for when a drop succeeds. */
    public void setDropListener(JDragDropMacListenerMac listener) {
        this.dropListener = listener;
    }

    /** Manually clear the selection & UI. */
    public void clear() {
        appInfo = null;
        lblIcon.setIcon(UIManager.getIcon("FileView.fileIcon"));

       // lblTitle.setText("Drop a .app bundle here");
        repaint();
    }

    /** Update the preview and notify listener. */
    private void setAppInfo(JDragDropAppInfo info) {
        this.appInfo = Objects.requireNonNull(info, "info");


        revalidate();
        repaint();

        if (dropListener != null) {
            // Notify parent dialog immediately that values are ready
            dropListener.macAppDropped(info);
        }
    }

    /* ---------- Drag & drop handler ---------- */

    private final class FileDropHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;

		@Override public boolean canImport(TransferSupport support) {
            boolean ok = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            support.setDropAction(DnDConstants.ACTION_COPY);
            return ok;
        }

        @SuppressWarnings("unchecked")
        @Override public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            try {
                List<File> files = (List<File>) support.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                for (File f : files) {
                	
                	//APP BUNDLE
                    if (f.isDirectory() && f.getName().toLowerCase().endsWith(".app")) {
                        try {
                            JDragDropAppInfo info = JDragDropAppInfo.fromBundle(f.toPath());
                            setAppInfo(info);
                            return true; // take first valid .app
                        } catch (Exception ex) {
                            showError("Failed to read bundle:\n" + f + "\n\n" + ex.getMessage());
                        }
                    }
                    
                    //BASH SCRIPT
                    if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".sh")) {
                        try {
                            JDragDropAppInfo info = JDragDropAppInfo.fromScript(f.toPath());
                            setAppInfo(info);
                            return true; // take first valid .app
                        } catch (Exception ex) {
                            showError("Failed to read script:\n" + f + "\n\n" + ex.getMessage());
                        }
                    }
                    
                    //WINDOWS EXE
                    if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".exe")) {
                        try {
                            JDragDropAppInfo info = JDragDropAppInfo.fromEXE(f.toPath());
                            setAppInfo(info);
                            return true; // take first valid .app
                        } catch (Exception ex) {
                            showError("Failed to read exe:\n" + f + "\n\n" + ex.getMessage());
                        }
                    }
                }
                showError("Please drop a valid executable file here.");
                return false;
            } catch (Exception e) {
                showError("Drop failed:\n" + e.getMessage());
                return false;
            }
        }

        private void showError(String msg) {
            // Keep it non-modal (we're in a dialog), but you can change this.
            JOptionPane.showMessageDialog(JDragDropPanel.this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
