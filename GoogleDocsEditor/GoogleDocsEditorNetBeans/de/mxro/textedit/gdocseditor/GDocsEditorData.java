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
import java.util.LinkedList;
import javax.swing.SwingUtilities;
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
	public DefaultMutableTreeNode googleDocsNode;
    private DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode("loading ...");
	
	private GoogleDocsAdapter adapter;
	
	//private final GDocsEditorApp editor;

	public final DefaultTreeModel treeModel;
	
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

    public void updateNode(GDocNode node) {
        int count = treeModel.getChildCount(googleDocsNode);
        for (int i=0;i < count; i++) {
            DefaultMutableTreeNode treenode = ( DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

            if (!( treenode.getUserObject() instanceof GDocNode)) continue;

            GDocNode nodeitem = (GDocNode) treenode.getUserObject();

            if (nodeitem.entry.getDocId().equals(node.entry.getDocId())) {
                System.out.println("updated node: "+nodeitem.plainText+" "+nodeitem.entry.getUpdated());
                treenode.setUserObject(node);
            }


        }
    }

    public GDocsEditorData clearLocalCache(GDocNode node) throws IOException {

        this.storage.deleteFile(node.docId);


        return this;
    }

    public GDocsEditorData clearLocalCache() throws IOException {
       for (int i=0;i < treeModel.getChildCount(googleDocsNode); i++) {
            DefaultMutableTreeNode treenode = ( DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

            if (!( treenode.getUserObject() instanceof GDocNode)) continue;
            GDocNode node = (GDocNode) treenode.getUserObject();
            clearLocalCache(node);


        }
        this.storage.deleteFile("documentFeed.xml");
        return this;
    }

    public GDocsEditorData saveLocalFile(GDocNode node, String document) throws IOException {
        node.setDocumentData(document);
        String xml = xstream.toXML(node);
        //System.out.println("Save File: "+node.docId+" "+document);

        this.storage.save(xml, node.docId);
        return this;
    }

    public GDocsEditorData downloadGoogleDocToLocalFile(GDocNode node) throws IOException, MalformedURLException, ServiceException {
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
        if (entry == null) return this;
        //System.out.println(entry.getTitle().getPlainText());
        //System.out.println(entry.getUpdated()+" "+node.entry.getUpdated()+" "+entry.getUpdated().compareTo(node.entry.getUpdated()));
        if (entry.getUpdated() != null && entry.getUpdated().compareTo(node.entry.getUpdated()) != 0) {
           System.out.println("upload: "+node.plainText);
           if (forceCache || node.getDocumentData() == null) this.loadCache(node);

           if (node.getDocumentData() == null) return this;
           
           String uploadHTML = getAdapter().prepareHTMLDocumentBeforeUpload(node.getDocumentData());
    	    getAdapter().updateDocument(node.entry, uploadHTML);

        }

        return this;
    }

    public GDocsEditorData uploadAll(List<DocumentListEntry> entries, boolean forceCache) {
        
        for (DocumentListEntry e : entries) {
            GDocNode node = new GDocNode(e.getTitle().getPlainText(), e.getDocId(), e);
            //System.out.println("upload process node: "+node.plainText+" "+node.entry.getUpdated());
            uploadDocument(node,forceCache);
        }
        return this;
    }

    public GDocsEditorData uploadAll() {
        //int count = ;
        for (int i=0;i < treeModel.getChildCount(googleDocsNode); i++) {
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
                downloadGoogleDocToLocalFile(node);
                 

            } else {
               if (this.getAdapter() == null) return this;

               DocumentListEntry entry = this.adapter.getDocumentEntry(node.entry.getDocId());

               //                            Notes                          2010-04-28T09:03:35.505Z compare 2010-04-28T09:03:35.515Z -1

               System.out.println("cache: "+entry.getTitle().getPlainText()+" "+entry.getUpdated()+" compare "+node.entry.getUpdated()+" "+entry.getUpdated().compareTo(node.entry.getUpdated()));
              if (node.entry.getUpdated() != null && entry.getUpdated().compareTo(node.entry.getUpdated()) > 0) {
                downloadGoogleDocToLocalFile(node);
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
            //System.out.println("Cached: "+node.plainText);
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
            if ( (cached.entry.getUpdated().compareTo(node.entry.getUpdated()) >= 0) || this.getAdapter() == null) {
                node.setDocumentData(cached.getDocumentData());
            }

        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this;
    }


    public List<DocumentListEntry> getEntriesFromTree() {
        LinkedList<DocumentListEntry> list = new LinkedList<DocumentListEntry>();
        int count = treeModel.getChildCount(googleDocsNode);
        for (int i=0;i < count; i++) {
             DefaultMutableTreeNode treenode = ( DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

            if (!( treenode.getUserObject() instanceof GDocNode)) continue;
            GDocNode node = (GDocNode) treenode.getUserObject();
            // System.out.println("treeentry: "+node.plainText+" "+node.entry.getUpdated());
            list.add(node.entry);

        }
        return list;
    }

    public void saveEntries() {
        if (treeModel == null) return;
        
        String xml = xstream.toXML(getEntriesFromTree());
        try {
            this.storage.save(xml, "documentFeed.xml");
        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public List<DocumentListEntry> entries;

    public List<DocumentListEntry> loadLocalEntries() {
        try {
                Object obj = this.storage.load("documentFeed.xml");
                if (obj == null) {
                    return null;
                }
                String xml = (String) obj;
                List<DocumentListEntry> localentries = (List<DocumentListEntry>) xstream.fromXML(xml);
                return localentries;
            } catch (IOException ex) {
                Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
    }


       private DefaultMutableTreeNode removeAllChildren(DefaultMutableTreeNode rootNode){
         while(rootNode.getChildCount() > 0){
          // DefaultMutableTreeNode t = (DefaultMutableTreeNode)rootNode.getChildAt(0);
          //treeModel.removeNodeFromParent(t);
           rootNode.remove(0);
         }
    return rootNode;
    }


    public void refreshTree() {
        this.removeAllChildren(this.googleDocsNode);
        //this.googleDocsNode.removeAllChildren();
        this.googleDocsNode.insert(this.tempNode, 0);
       // treeModel.insertNodeInto(this.tempNode, this.googleDocsNode, 0);
        //this.googleDocsNode.add(this.tempNode);
        for(int i=0; i<entries.size(); i++) {
          DocumentListEntry entry = new DocumentListEntry(entries.get(i));
                 // entries.get(i);
          GDocNode newNode = new GDocNode(entry.getTitle().getPlainText(), entry.getDocId(), entry);
          //this.loadCache(newNode);
          googleDocsNode.insert(new DefaultMutableTreeNode(newNode), treeModel.getChildCount(googleDocsNode));
         // treeModel.insertNodeInto(new DefaultMutableTreeNode(newNode), googleDocsNode, treeModel.getChildCount(googleDocsNode));

        }
        googleDocsNode.remove(this.tempNode);
        //treeModel.removeNodeFromParent(this.tempNode);
        treeModel.nodeStructureChanged(this.googleDocsNode);
        //this.googleDocsNode.remove(this.tempNode);
    }

	public void loadEntries(boolean loadLocal) {

        if (loadLocal) {
            entries = loadLocalEntries();
            if (entries != null) return;
        }

        if (getAdapter() != null) {
		  //adapter.loadDocumentsList();
          entries = adapter.getDocumentList();
        } else {
            entries = loadLocalEntries();

         }

        refreshTree();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              if (adapter != null) adapter.loadDocumentsList();
            }
        });
        
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
