/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mxro.textedit.gdocseditor.gui;

import de.mxro.textedit.gdocseditor.GDocsEditorData;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.io.IOException;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author mx
 */
public class TreeDragSource implements DragSourceListener, DragGestureListener {


  public static class URITransferable implements Transferable {

      public static DataFlavor String_FLAVOR = DataFlavor.stringFlavor;

      String data;
  DataFlavor flavors[] = { String_FLAVOR };

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(String_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return data;
        }

        public URITransferable(String data){
            this.data = data;
        }
  }

  DragSource source;

  DragGestureRecognizer recognizer;

  URITransferable transferable;

  DefaultMutableTreeNode oldNode;

  JTree sourceTree;

  public TreeDragSource(JTree tree, int actions) {
    sourceTree = tree;
    source = new DragSource();
    recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
        actions, this);
  }

  /*
   * Drag Gesture Handler
   */
    @Override
  public void dragGestureRecognized(DragGestureEvent dge) {
    TreePath path = sourceTree.getSelectionPath();


    if ((path == null) || (path.getPathCount() <= 1)) {
      // We can't move the root node or an empty selection
      return;
    }
    //oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();

    DefaultMutableTreeNode lastComponent = (DefaultMutableTreeNode) sourceTree.getLastSelectedPathComponent();
    Object userObject = lastComponent.getUserObject();

    GDocsEditorData.GDocNode gdocnode = (GDocsEditorData.GDocNode) userObject;

    transferable = new URITransferable("http://docs.google.com/Doc?id="+gdocnode.entry.getDocId());
    source.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);

    // If you support dropping the node anywhere, you should probably
    // start with a valid move cursor:
    //source.startDrag(dge, DragSource.DefaultMoveDrop, transferable,
    // this);
  }

  /*
   * Drag Event Handlers
   */
  public void dragEnter(DragSourceDragEvent dsde) {
  }

  public void dragExit(DragSourceEvent dse) {
  }

  public void dragOver(DragSourceDragEvent dsde) {
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {
    System.out.println("Action: " + dsde.getDropAction());
    System.out.println("Target Action: " + dsde.getTargetActions());
    System.out.println("User Action: " + dsde.getUserAction());
  }

  public void dragDropEnd(DragSourceDropEvent dsde) {
    /*
     * to support move or copy, we have to check which occurred:
     */
    System.out.println("Drop Action: " + dsde.getDropAction());
    if (dsde.getDropSuccess()
        && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
      ((DefaultTreeModel) sourceTree.getModel())
          .removeNodeFromParent(oldNode);
    }

    /*
     * to support move only... if (dsde.getDropSuccess()) {
     * ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode); }
     */
  }
}

