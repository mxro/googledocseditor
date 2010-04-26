package de.mxro.textedit.gdocseditor;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.google.gdata.data.docs.DocumentListEntry;

import de.mxro.gdocs.GoogleDocsAdapter;

public class GDocsEditorData {
	
	public static class GDocNode {
		public final String plainText;
		public final String docId;
		public final DocumentListEntry entry;
		
		public String toString() {
			return plainText;
		}

		public GDocNode(String plainText, String docId, DocumentListEntry entry) {
			super();
			this.plainText = plainText;
			this.docId = docId;
			this.entry = entry;
		}
		
		
	}
	
	private DefaultMutableTreeNode googleDocsNode;
	
	private GoogleDocsAdapter adapter;
	
	//private final GDocsEditorApp editor;

	private final DefaultTreeModel treeModel;
	
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	public GDocsEditorData() {
		super();
		//this.editor = editor;
		googleDocsNode =
	        new DefaultMutableTreeNode("Google Docs");
		googleDocsNode.add(new DefaultMutableTreeNode("not loaded"));
		this.treeModel = new DefaultTreeModel(googleDocsNode);
	}
	
	public void loadEntries() {
		if (getAdapter() == null) return;
		List<DocumentListEntry> entries = adapter.getDocumentList();
        for(int i=0; i<entries.size(); i++) {
          DocumentListEntry entry = entries.get(i);
          GDocNode newNode = new GDocNode(entry.getTitle().getPlainText(), entry.getDocId(), entry);
          treeModel.insertNodeInto(new DefaultMutableTreeNode(newNode), googleDocsNode, treeModel.getChildCount(googleDocsNode)-1);
          
        }
	}
	
	public GoogleDocsAdapter getAdapter() {
		return adapter;
	}

	
	public boolean setConnection(String username, String password) {
		this.adapter = new GoogleDocsAdapter(username, password);
		return (this.adapter != null);
	}
	
}
