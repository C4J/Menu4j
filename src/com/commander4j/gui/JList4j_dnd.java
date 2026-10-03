
package com.commander4j.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * Author      : David Garratt
 * Project Name: Commander4j
 * Filename    : JList4j_dnd.java
 * License     : GNU General Public License
 * 
 * Adapted to add Drag and Drop Reorder Support
 */
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import com.commander4j.sys.Common;

public class JList4j_dnd<E> extends JList<E> {

    private static final long serialVersionUID = 1L;

    private int draggedIndex = -1;

    public JList4j_dnd(ListModel<E> items) {
        super(items);
        initialise();
    }

    public JList4j_dnd(E[] items) {
        super(items);
        initialise();
    }

    public JList4j_dnd() {
        super();
        initialise();
    }

    private void initialise() {
        setFont(Common.font_list);
        setBackground(Common.color_listBackground);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);
        setTransferHandler(new ListItemTransferHandler());
    }

    private class ListItemTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;

		@Override
        protected Transferable createTransferable(JComponent c) {
            draggedIndex = getSelectedIndex();
            E draggedValue = getSelectedValue();
            if (draggedValue != null) {
                return new StringSelection(draggedValue.toString());
            }
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
        	
            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
            	
                return false;
            }

            try {
                JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
                int dropIndex = dropLocation.getIndex();

                ListModel<E> model = getModel();
                if (!(model instanceof DefaultListModel)) {
                    // Only works with DefaultListModel
                	
                    return false;
                }

                DefaultListModel<E> listModel = (DefaultListModel<E>) model;

                if (draggedIndex < 0 || draggedIndex >= listModel.getSize() || dropIndex < 0) {
                    // Not a drag which started in this list
                    return false;
                }

                if (dropIndex > draggedIndex) {
                    dropIndex--;
                }

                // Move the element itself - the transferred String is only a
                // text copy and must not be put back into the model.
                E draggedValue = listModel.remove(draggedIndex);
                listModel.add(dropIndex, draggedValue);

                setSelectedIndex(dropIndex);
                return true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            draggedIndex = -1;
        }
    }
}
