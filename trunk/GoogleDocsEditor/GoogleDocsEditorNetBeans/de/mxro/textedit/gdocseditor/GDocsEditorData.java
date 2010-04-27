package de.mxro.textedit.gdocseditor;

import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.google.gdata.data.docs.DocumentListEntry;

import com.thoughtworks.xstream.XStream;
import de.mxro.gdocs.GoogleDocsAdapter;
import org.jdesktop.application.LocalStorage;

public class GDocsEditorData {
	
	public static class GDocNode {
		public String plainText;
		public final String docId;
		public final DocumentListEntry entry;

        private String documentData;

        public GDocNode setDocumentData(String documentData) {
            this.documentData = documentData;
            return this;
        }

        public String getDocumentData() {
            return this.documentData;
        }

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


    private LocalStorage storage;
	private DefaultMutableTreeNode googleDocsNode;
    private DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode("not loaded");
	
	private GoogleDocsAdapter adapter;
	
	//private final GDocsEditorApp editor;

	private final DefaultTreeModel treeModel;
	
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	public GDocsEditorData(LocalStorage storage) {
		super();
		//this.editor = editor;
		googleDocsNode =
	        new DefaultMutableTreeNode("Google Docs");
		googleDocsNode.add(tempNode);
		this.treeModel = new DefaultTreeModel(googleDocsNode);
        this.storage = storage;
	}

    private static XStream xstream = new XStream();

    public GDocsEditorData saveLocalFile(GDocNode node, String document) throws IOException {
        node.setDocumentData(document);
        String xml = xstream.toXML(node);
        //System.out.println("Save File: "+node.docId+" "+document);
        this.storage.save(xml, node.docId);
        return this;
    }

    public GDocsEditorData downloadToLocalFile(GDocNode node) throws IOException, MalformedURLException, ServiceException {
        if (this.getAdapter() == null) return this;


        String downloadedDocument=null;
        try {
          downloadedDocument=this.adapter.downloadDocument(node.entry.getResourceId(), "html");
        } catch (Exception e) {
            return this;
        }
        if (downloadedDocument == null) return this;
        return saveLocalFile(node, this.adapter.prepareHTMLDocumentAfterDownload(downloadedDocument));
    }

    public GDocsEditorData uploadDocument(GDocNode node) {
        return this.uploadDocument(node, false);
    }

    public GDocsEditorData uploadDocument(GDocNode node, boolean forceCache) {
        if (this.getAdapter() == null) return this;

        DocumentListEntry entry = this.adapter.getDocumentEntry(node.entry.getDocId());
        //System.out.println(entry.getTitle().getPlainText());
        //System.out.println(entry.getUpdated()+" "+node.entry.getUpdated()+" "+entry.getUpdated().compareTo(node.entry.getUpdated()));
        if (entry.getUpdated().compareTo(node.entry.getUpdated()) < 0) {
           System.out.println("upload: "+node.plainText);
           if (forceCache) this.loadCache(node);
           String uploadHTML = getAdapter().prepareHTMLDocumentBeforeUpload(node.getDocumentData());
    	    getAdapter().updateDocument(node.entry, uploadHTML);
        }

        return this;
    }

    public GDocsEditorData uploadAll(List<DocumentListEntry> entries, boolean forceCache) {
        for (DocumentListEntry e : entries) {
            GDocNode node = new GDocNode(e.getTitle().getPlainText(), e.getDocId(), e);
            
            uploadDocument(node,forceCache);
        }
        return this;
    }

    public GDocsEditorData uploadAll() {
        int count = treeModel.getChildCount(googleDocsNode);
        for (int i=0;i < count; i++) {
            DefaultMutableTreeNode treenode = ( DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

            if (!( treenode.getUserObject() instanceof GDocNode)) continue;
            GDocNode node = (GDocNode) treenode.getUserObject();
            uploadDocument(node);


        }
        return this;
    }

    public GDocsEditorData cacheDocument(GDocNode node) throws IOException, MalformedURLException, ServiceException {
        loadCache(node);
        if (node.getDocumentData() ==  null) {
                downloadToLocalFile(node);
                 

            } else {
               if (this.getAdapter() == null) return this;

               DocumentListEntry entry = this.adapter.getDocumentEntry(node.entry.getDocId());

              if (entry.getUpdated().compareTo(node.entry.getUpdated()) > 0) {
                downloadToLocalFile(node);
              }
            }
        return this;
    }

    public GDocsEditorData cacheDocuments() throws IOException, MalformedURLException, ServiceException {
        int count = treeModel.getChildCount(googleDocsNode);
        for (int i=0;i < count; i++) {
             DefaultMutableTreeNode treenode = ( DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

            if (!( treenode.getUserObject() instanceof GDocNode)) continue;
            GDocNode node = (GDocNode) treenode.getUserObject();
            System.out.println("Cached: "+node.plainText);
            cacheDocument(node);


        }
        return this;
    }

    public GDocsEditorData loadCache(GDocNode node) {
        try {
            Object obj = this.storage.load(node.docId);
            if (obj == null) return this;

            String xml = (String) this.storage.load(node.docId);
            GDocNode cached = (GDocNode) xstream.fromXML(xml);
            if ( (cached.entry.getUpdated().compareTo(node.entry.getUpdated()) <= 0) || this.getAdapter() == null) {
                node.setDocumentData(cached.getDocumentData());
            }

        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this;
    }


    public void saveEntries() {
        if (entries == null) return;

        String xml = xstream.toXML(entries);
        try {
            this.storage.save(xml, "documentFeed.xml");
        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public List<DocumentListEntry> entries;

    public void loadLocalEntries() {
        try {
                Object obj = this.storage.load("documentFeed.xml");
                if (obj == null) {
                    return;
                }
                String xml = (String) obj;
                entries = (List<DocumentListEntry>) xstream.fromXML(xml);
            } catch (IOException ex) {
                Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
    }

	public void loadEntries() {
		
        if (getAdapter() != null) {
		  //adapter.loadDocumentsList();
          entries = adapter.getDocumentList();
        } else {
            loadLocalEntries();

         }
        for(int i=0; i<entries.size(); i++) {
          DocumentListEntry entry = new DocumentListEntry(entries.get(i));
                 // entries.get(i);
          GDocNode newNode = new GDocNode(entry.getTitle().getPlainText(), entry.getDocId(), entry);
          this.loadCache(newNode);
          treeModel.insertNodeInto(new DefaultMutableTreeNode(newNode), googleDocsNode, treeModel.getChildCount(googleDocsNode)-1);
          
        }
        

        if (adapter != null) adapter.loadDocumentsList();
	}
	
	public GoogleDocsAdapter getAdapter() {
		return adapter;
	}

	
	public boolean setConnection(String username, String password) {
		this.adapter = new GoogleDocsAdapter(username, password);
        if (!this.adapter.connected) this.adapter = null;
        return (this.adapter != null);
	}
	
}
