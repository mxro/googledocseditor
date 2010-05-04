package de.mxro.textedit.gdocseditor;

import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.google.gdata.data.docs.DocumentListEntry;

import com.thoughtworks.xstream.XStream;
import de.mxro.gdocs.GoogleDocsAdapter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.ConsoleHandler;
import org.jdesktop.application.LocalStorage;

public class GDocsEditorData {

    public static class GDocNode implements Comparable {

        public String plainText;
        public final String docId;
        public final DocumentListEntry entry;
        private String documentData;

        public DocumentListEntry getEntry() {
            return entry;
        }

        public GDocNode setDocumentData(String documentData) {
            this.documentData = documentData;
            return this;
        }

        public String getDocumentData() {
            return this.documentData;
        }

        @Override
        public String toString() {
            return plainText;
        }

        public boolean isMoreRecentThan(GDocNode node) {

            //                            Notes                          2010-04-28T09:03:35.505Z compare 2010-04-28T09:03:35.515Z -1
            // a.compareTo(b) if (a < b) -> -1

            // Logger.getLogger(GDocsEditorData.class.getName()).log(Level.INFO, "test sync: " + entry.getTitle().getPlainText() + " " + entry.getUpdated() + " compare " + node.entry.getUpdated() + " " + entry.getUpdated().compareTo(node.entry.getUpdated()), node);
            //System.out.println();

            return this.getEntry().getUpdated().compareTo(node.getEntry().getUpdated()) > 0;

        }

        public GDocNode(DocumentListEntry entry) {
            super();
            this.plainText = entry.getTitle().getPlainText();
            this.docId = entry.getDocId();
            this.entry = entry;
        }

        @Override
        public int compareTo(Object t) {
            if (!(t instanceof GDocNode))
                throw new ClassCastException("A GDocNode object expected.");


            return this.entry.getUpdated().compareTo( ((GDocNode) t).getEntry().getUpdated()  );
        }
    }
    private HashMap<String, GDocNode> local;
    private HashMap<String, GDocNode> remote;
    private static XStream xstream = new XStream();
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

        this.local = new HashMap<String, GDocNode>();
        this.remote = new HashMap<String, GDocNode>();

        Logger.getLogger(GDocsEditorData.class.getName()).setLevel(Level.ALL);
        Logger.getLogger(GDocsEditorData.class.getName()).addHandler(new ConsoleHandler());

    }

    private boolean addEntriesToMap(List<DocumentListEntry> entries, HashMap<String, GDocNode> map) {
        map.clear();
        for (DocumentListEntry e : entries) {
            GDocNode node = new GDocNode(e);
            
            map.put(e.getDocId(), node);
        }
        return true;
    }

    private boolean downloadRemote() {
        // update remote
        LinkedList<DocumentListEntry> entries = this.adapter.downloadDocumentsList();
        if (entries == null) {
            return false;
        }


        this.addEntriesToMap(entries, this.remote);
        return true;
    }

    private GDocNode getRemoteNode(String id) {
        return this.remote.get(id);
    }

    private boolean loadLocal() {
        Collection<GDocNode> nodes = this.loadLocalNodesFromStorage();
        if (nodes == null) {
            return false;
        }

        this.local.clear();

        for (GDocNode node : nodes) {
            this.local.put(node.docId, node);
        }

        return true;
    }

    public boolean initLocalFromStorage() {
        if (!loadLocal()) {
            return false;
        }
        refreshTree();
        return true;
    }

    public boolean synchronize() {
        if (!this.isConnected()) {
            return false;
        }

        if (!this.downloadRemote()) {
            return false;
        }
        for (GDocNode localNode : this.local.values()) {
            GDocNode remoteNode = this.getRemoteNode(localNode.docId);

            if (remoteNode == null) continue;

            if (localNode.isMoreRecentThan(remoteNode)) {
                this.uploadDocumentToGoogleDocs(this.getLocalNode(localNode.docId));
            }

            if (remoteNode.isMoreRecentThan(localNode)) {
                try {
                    this.downloadGoogleDocToLocalFileAndUpdateNode(remoteNode);
                    
                } catch (MalformedURLException ex) {
                    Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

       /* if (!this.downloadRemote()) {
            return false;
        }*/

        for (GDocNode remoteNode : this.remote.values()) {
            GDocNode localNode = this.getLocalNode(remoteNode.docId);

            if (localNode == null /*|| remoteNode.isMoreRecentThan(localNode)*/) {
                this.local.put(remoteNode.docId, remoteNode);

                GDocNode local = this.getLocalNode(remoteNode.docId);
                if (local.getDocumentData() == null) {
                    try {
                        this.downloadGoogleDocToLocalFileAndUpdateNode(local);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ServiceException ex) {
                        Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //this.updateNode(remoteNode);
            }
        }


        refreshTree();

        return true;
    }

    public boolean updateNode(GDocNode node) {
        GDocNode localNode = this.local.get(node.docId);
        if (localNode == null) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, "Node not in local cache", node);
            return false;
        }
        this.local.put(node.docId, node);
        this.updateNodeInTree(node);
        return true;
    }

    public GDocNode getLocalNode(String id) {
        GDocNode node = this.local.get(id);
        if (node == null) return null;
        if (node.getDocumentData() == null) this.loadGoogleDocContentFromLocalFileIfNotOutdated(node);
        return node;
    }

    public boolean isConnected() {
        return this.adapter.isConnected();
    }

    private void updateNodeInTree(GDocNode node) {
        int count = treeModel.getChildCount(googleDocsNode);
        for (int i = 0; i < count; i++) {
            DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

            if (!(treenode.getUserObject() instanceof GDocNode)) {
                continue;
            }

            GDocNode nodeitem = (GDocNode) treenode.getUserObject();

            if (nodeitem.entry.getDocId().equals(node.entry.getDocId())) {
                Logger.getLogger(GDocsEditorData.class.getName()).log(Level.INFO, "update node in tree: " + nodeitem.plainText + " " + nodeitem.entry.getUpdated(), node);
                
                treenode.setUserObject(node);
            }


        }
    }

    public GDocsEditorData clearLocalCache(GDocNode node) throws IOException {

        this.storage.deleteFile(node.docId);


        return this;
    }

    public GDocsEditorData clearLocalCache() throws IOException {
        for (int i = 0; i < treeModel.getChildCount(googleDocsNode); i++) {
            DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

            if (!(treenode.getUserObject() instanceof GDocNode)) {
                continue;
            }
            GDocNode node = (GDocNode) treenode.getUserObject();
            clearLocalCache(node);


        }
        this.storage.deleteFile("documentFeed.xml");
        return this;
    }

    private GDocsEditorData saveLocalFile(GDocNode node) throws IOException {
        if (node.getDocumentData() == null) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, "called save with empty document data "+node.docId, node);
            return this;
        }
        //node.setDocumentData(document);
        String xml = xstream.toXML(node);
        Logger.getLogger(GDocsEditorData.class.getName()).log(Level.INFO, "save document locally: "+node.plainText, node);
       // System.out.println("Save Local File: " + node.plainText + " " + node.docId + " " + node.entry.getUpdated());

        this.storage.save(xml, node.docId);
        return this;
    }

    private GDocsEditorData downloadGoogleDocToLocalFileAndUpdateNode(GDocNode node) throws IOException, MalformedURLException, ServiceException {
        if (this.getAdapter() == null) {
            return this;
        }

        Logger.getLogger(GDocsEditorData.class.getName()).log(Level.INFO, "download document: "+node.plainText, node);

        String downloadedDocument = null;
        try {
            downloadedDocument = this.adapter.downloadDocument(node.entry.getResourceId(), "html");
        } catch (Exception e) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, e);
            return this;
        }
        if (downloadedDocument == null) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.WARNING, "document could not be downloaded: "+node.plainText, node);
            return this;
        }
        node.setDocumentData(this.adapter.prepareHTMLDocumentAfterDownload(downloadedDocument));

        this.updateNode(node);

        return saveLocalFile(node);
    }

    private GDocsEditorData uploadDocumentToGoogleDocs(GDocNode node) {
        return this.uploadDocumentToGoogleDocs(node, false);
    }

    public GDocsEditorData uploadDocumentToGoogleDocs(GDocNode node, boolean forceCache) {
        if (this.getAdapter() == null) {
            return this;
        }

        /*DocumentListEntry entry = this.adapter.getDocumentEntry(node.entry.getDocId());
        if (entry == null) {
        return this;
        }
        //System.out.println(entry.getTitle().getPlainText());
        //System.out.println(entry.getUpdated()+" "+node.entry.getUpdated()+" "+entry.getUpdated().compareTo(node.entry.getUpdated()));
        if (entry.getUpdated() != null && entry.getUpdated().compareTo(node.entry.getUpdated()) != 0) {*/
        Logger.getLogger(GDocsEditorData.class.getName()).log(Level.INFO, "Upload "+node.plainText, node);

        //System.out.println("upload: " + node.plainText);
        if (forceCache || node.getDocumentData() == null) {
            this.loadGoogleDocContentFromLocalFileIfNotOutdated(node);
        }

        if (node.getDocumentData() == null) {
            return this;
        }

        String uploadHTML = getAdapter().prepareHTMLDocumentBeforeUpload(node.getDocumentData());
        
        
        DocumentListEntry e=null;
        try {
            e = getAdapter().updateDocument(node.entry, uploadHTML);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            if (e != null) {
                GDocNode newNode = new GDocNode(e);
                newNode.setDocumentData(uploadHTML);
                this.saveLocalFile(newNode);
                this.updateNode(newNode);
            }
        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }

        //}

        return this;
    }

    /*public GDocsEditorData uploadAll(List<DocumentListEntry> entries, boolean forceCache) {

    for (DocumentListEntry e : entries) {
    GDocNode node = new GDocNode(e.getTitle().getPlainText(), e.getDocId(), e);
    //System.out.println("upload process node: "+node.plainText+" "+node.entry.getUpdated());
    uploadDocumentToGoogleDocs(node, forceCache);
    }
    return this;
    }*/

    /*public GDocsEditorData uploadAll() {
    //int count = ;
    for (int i = 0; i < treeModel.getChildCount(googleDocsNode); i++) {
    DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

    if (!(treenode.getUserObject() instanceof GDocNode)) {
    continue;
    }
    GDocNode node = (GDocNode) treenode.getUserObject();
    uploadDocumentToGoogleDocs(node);


    }
    return this;
    }*/

    /*public GDocsEditorData assureDocumentInSyncWithGoogleDocs(GDocNode node) throws IOException, MalformedURLException, ServiceException {
    loadGoogleDocContentFromLocalFileIfNotOutdated(node);
    if (node.getDocumentData() == null) {
    System.out.println("test sync - download document " + node.plainText);
    downloadGoogleDocToLocalFile(node);


    } else {
    if (this.getAdapter() == null) {
    return this;
    }

    DocumentListEntry entry = this.adapter.getDocumentEntry(node.entry.getDocId());

    //                            Notes                          2010-04-28T09:03:35.505Z compare 2010-04-28T09:03:35.515Z -1
    // a.compareTo(b) if (a < b) -> -1

    System.out.println("test sync: " + entry.getTitle().getPlainText() + " " + entry.getUpdated() + " compare " + node.entry.getUpdated() + " " + entry.getUpdated().compareTo(node.entry.getUpdated()));
    if (node.entry.getUpdated() != null && entry.getUpdated().compareTo(node.entry.getUpdated()) > 0) {
    downloadGoogleDocToLocalFile(node);
    }
    }
    return this;
    }

    public GDocsEditorData assureDocumentsInSyncWithGoogleDocs() throws IOException, MalformedURLException, ServiceException {
    int count = treeModel.getChildCount(googleDocsNode);
    for (int i = 0; i < count; i++) {
    DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

    if (!(treenode.getUserObject() instanceof GDocNode)) {
    continue;
    }
    GDocNode node = (GDocNode) treenode.getUserObject();
    //System.out.println("Cached: "+node.plainText);
    assureDocumentInSyncWithGoogleDocs(node);


    }
    return this;
    }*/
    private GDocsEditorData loadGoogleDocContentFromLocalFileIfNotOutdated(GDocNode node) {
        try {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.INFO, "load document cache: "+node.docId, node);
            Object obj = this.storage.load(node.docId);
            //this.storage.
            if (obj == null || !(obj instanceof String)) {
                return this;
            }

            String xml = (String) obj;
            GDocNode cached = (GDocNode) xstream.fromXML(xml);

            // a.compareTo(b) if (a < b) -> -1

            //  cached.entry.getUpdated()          node.entry.getUpdated()
            //  2010-05-01T09:56:32.262Z compare 2010-05-01T10:06:16.594Z = -1 >= 0
            //System.out.println("Test if restore possible: " + cached.docId + " " + cached.entry.getUpdated() + " compare " + node.entry.getUpdated() + " = " + cached.entry.getUpdated().compareTo(node.entry.getUpdated()) + " >= 0");
            if ((cached.entry.getUpdated().compareTo(node.entry.getUpdated()) >= 0) || this.getAdapter() == null) {
                Logger.getLogger(GDocsEditorData.class.getName()).log(Level.INFO, "document is loaded from local cache: "+node.docId, node);
                node.setDocumentData(cached.getDocumentData());
            }

        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this;
    }

    /*private List<DocumentListEntry> getEntriesFromTree() {
    LinkedList<DocumentListEntry> list = new LinkedList<DocumentListEntry>();
    int count = treeModel.getChildCount(googleDocsNode);
    for (int i = 0; i < count; i++) {
    DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) treeModel.getChild(googleDocsNode, i);

    if (!(treenode.getUserObject() instanceof GDocNode)) {
    continue;
    }
    GDocNode node = (GDocNode) treenode.getUserObject();
    // System.out.println("treeentry: "+node.plainText+" "+node.entry.getUpdated());
    list.add(node.entry);

    }
    return list;
    }*/
    public void saveEntries() {
        if (treeModel == null) {
            return;
        }

        String xml = xstream.toXML(this.local.values());
        try {
            this.storage.save(xml, "documentFeed.xml");
        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Collection<GDocNode> loadLocalNodesFromStorage() {
        try {
            Object obj = this.storage.load("documentFeed.xml");
            if (obj == null) {
                return null;
            }
            String xml = (String) obj;
            Object loaded = xstream.fromXML(xml);
            if (!(loaded instanceof Collection)) {
                return null;
            }
            //List<DocumentListEntry> localentries = (List<DocumentListEntry>) xstream.fromXML(xml);
            return (Collection<GDocNode>) loaded;
        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorData.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private DefaultMutableTreeNode removeAllChildren(DefaultMutableTreeNode rootNode) {
        while (rootNode.getChildCount() > 0) {
            // DefaultMutableTreeNode t = (DefaultMutableTreeNode)rootNode.getChildAt(0);
            //treeModel.removeNodeFromParent(t);
            rootNode.remove(0);
        }
        return rootNode;
    }

    private void refreshTree() {
        this.removeAllChildren(this.googleDocsNode);
        //this.googleDocsNode.removeAllChildren();
        this.googleDocsNode.insert(this.tempNode, 0);
        // treeModel.insertNodeInto(this.tempNode, this.googleDocsNode, 0);
        //this.googleDocsNode.add(this.tempNode);

        Object[] array = this.local.values().toArray();
        Arrays.sort(array);
       

        for (Object o : array) {
            GDocNode node = (GDocNode) o;
            //this.loadCache(newNode);
            googleDocsNode.insert(new DefaultMutableTreeNode(node), 0/*treeModel.getChildCount(googleDocsNode)*/);
            // treeModel.insertNodeInto(new DefaultMutableTreeNode(newNode), googleDocsNode, treeModel.getChildCount(googleDocsNode));

        }
        googleDocsNode.remove(this.tempNode);
        //treeModel.removeNodeFromParent(this.tempNode);
        treeModel.nodeStructureChanged(this.googleDocsNode);
        //this.googleDocsNode.remove(this.tempNode);
    }

    /*public void refreshEntries() {
    if (adapter == null) {
    return;
    }

    this.adapter.downloadDocumentsList();
    entries = adapter.getDocumentList();
    }

    public void loadEntries(boolean loadLocal) {

    if (loadLocal) {
    entries = loadLocalNodesFromStorage();
    if (entries != null) {
    return;
    }
    }

    if (getAdapter() != null) {
    //adapter.loadDocumentsList();
    entries = adapter.getDocumentList();
    } else {
    entries = loadLocalNodesFromStorage();

    }

    refreshTree();

    SwingUtilities.invokeLater(new Runnable() {

    @Override
    public void run() {
    refreshEntries();
    }
    });

    }*/
    public GoogleDocsAdapter getAdapter() {
        return adapter;
    }

    public boolean setConnection(String username, String password) {
        this.adapter = new GoogleDocsAdapter(username, password);
        if (!this.adapter.isConnected()) {
            this.adapter = null;
        }
        return (this.adapter != null);
    }
}
