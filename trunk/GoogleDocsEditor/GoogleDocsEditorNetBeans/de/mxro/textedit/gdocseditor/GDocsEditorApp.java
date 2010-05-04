package de.mxro.textedit.gdocseditor;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import java.io.IOException;
import java.net.MalformedURLException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.application.Task;

import com.google.gdata.util.ServiceException;

import de.mxro.gdocs.GoogleDocsFileHandler;
import de.mxro.textedit.MxroEkitFactory;
import de.mxro.textedit.MxroEkitTextPane;
import de.mxro.textedit.gdocseditor.gui.GoogleDocsEditorGUIApp;
import de.mxro.textedit.gdocseditor.gui.GoogleDocsEditorMainForm;
import de.mxro.textedit.gdocseditor.gui.GoogleDocsEditorGUIApp.StartUpCallback;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JScrollPane;

public class GDocsEditorApp {

    public GoogleDocsEditorMainForm mainFrame;
    private MxroEkitTextPane jMxroEditorPane;
    private JTree jTree1;
    private GDocsEditorData data;
    private GDocsEditorData.GDocNode activeNode;
    private String initialText = null;

    public void doSaveEntries() {
        this.data.saveEntries();
        
    }

    public void doClearLocalCache() {
        try {
            this.data.clearLocalCache();
        } catch (IOException ex) {
            Logger.getLogger(GDocsEditorApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void doDeleteDocument(GDocsEditorData.GDocNode gdocnode) {
        if (activeNode == null) {
            return;
        }
        if (this.data.getAdapter() == null) {
            return;
        }

        this.data.getAdapter().trashDocument(activeNode.entry);

        doRefresh();
    }

    public void doRefresh() {
        //mainFrame.getApplication().getContext().
        if (activeNode != null) {
            this.doSave(activeNode);
        }

        if (!data.isConnected()) return;

        data.synchronize();     

    }

    public void doInitialSetup() {
        
            //data.initLocalFromStorage();

            if (data.isConnected()) { data.synchronize(); }
            

            javax.swing.Timer cacheTimer = new javax.swing.Timer(5000, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (!cachingRunning) {
                        Thread t = new Thread() {

                            @Override
                            public void run() {
                                cacheDocument();
                            }
                        };
                        t.setPriority(Thread.MIN_PRIORITY);
                        t.start();
                    }

                }
            });
            cacheTimer.setInitialDelay(10000);

            cacheTimer.setRepeats(false);
            cacheTimer.start();
        

        mainFrame.getApplication().getContext().getTaskService().execute(mainFrame.Refresh());
    }

    public void doNewDocument() {
       // DocumentListEntry entry = new DocumentListEntry();
       // entry.setTitle(new PlainTextConstruct("New *"));
       // entry.setResourceId("document:" + (DateTime.now().getValue()));
        //entry.setKind("document");
        //entry.addExtension(Extension);
        // entry.setContent(new TextContent(new PlainTextConstruct("<html><body></body></html>")));
        // entry.setKind("html");
       // entry.setUpdated(new com.google.gdata.data.DateTime(new Date(),
      //          java.util.TimeZone.getDefault()));

        // GDocsEditorData.GDocNode newNode = new GDocsEditorData.GDocNode(entry.getTitle().getPlainText(), entry.getDocId(), entry);
        //  data.treeModel.insertNodeInto(new DefaultMutableTreeNode(newNode), data.googleDocsNode, 0);

        /*jMxroEditorPane.
        setText("<html><body></body></html>");
        this.mainFrame.jTitleField.setText(newNode.plainText);
        this.activeNode = newNode;
        this.initialText = jMxroEditorPane.getText();*/

        if (this.data.getAdapter() != null) {
            try {
                DocumentListEntry entry = this.data.getAdapter().createNewDocument("New", "document");
                doRefresh();
            //  this.data.getAdapter().loadDocumentsList();
            // entry = this.data.getAdapter().getDocumentEntry(entry.getDocId());
            //  GDocsEditorData.GDocNode newNode = new GDocsEditorData.GDocNode(entry.getTitle().getPlainText(), entry.getDocId(), entry);
            //   data.treeModel.insertNodeInto(new DefaultMutableTreeNode(newNode), data.googleDocsNode, 0);
            //  this.activeNode = new GDocsEditorData.GDocNode(entry.getTitle().getPlainText(), entry.getDocId(), entry);
            //  ((DefaultMutableTreeNode) data.treeModel.getChild(data.googleDocsNode, 0)).setUserObject(this.activeNode);
            } catch (IOException ex) {
                Logger.getLogger(GDocsEditorApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(GDocsEditorApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void doSave(GDocsEditorData.GDocNode gdocnode) {
        // System.out.print("Save: "+gdocnode.plainText+" ...");
        String editorPaneText = jMxroEditorPane.getText();
        if (initialText == null || !initialText.equals(editorPaneText) || !this.mainFrame.jTitleField.getText().equals(gdocnode.entry.getTitle().getPlainText())) {
           // this.data.updateNode(gdocnode);
                System.out.println("Change Text: "+ editorPaneText);
                gdocnode.setDocumentData(editorPaneText);
                gdocnode.entry.setTitle(new PlainTextConstruct(this.mainFrame.jTitleField.getText()));
                gdocnode.plainText = this.mainFrame.jTitleField.getText();
                //System.out.println("Update doc: old updated = "+gdocnode.entry.getUpdated());
                //DateTime now = com.google.gdata.data.DateTime.now();
                //now.setTzShift(gdocnode.entry.getUpdated().getTzShift());
                gdocnode.entry.getUpdated().setValue(gdocnode.entry.getUpdated().getValue()+10);

                this.data.updateNode(gdocnode);
                        //gdocnode.entry.getUpdated().getValue() + 10);
                //System.out.println("Update doc: new updated = "+gdocnode.entry.getUpdated());
                // com.google.gdata.data.DateTime.now());
                //System.out.println("Saved Text: "+gdocnode.getDocumentData());
                
           

        }

    // this.mainFrame.getApplication().getContext().getTaskService().execute(this.mainFrame.Refresh());
    // String uploadHTML = data.getAdapter().prepareHTMLDocumentBeforeUpload(jMxroEditorPane.getText());
    // data.getAdapter().updateDocument(gdocnode.entry, uploadHTML);
    }

    public void documentSelected(GDocsEditorData.GDocNode node) {
        if (this.activeNode != null) {
            this.doSave(activeNode);
        }


            

            jMxroEditorPane.setText(node.getDocumentData());
            this.mainFrame.jTitleField.setText(node.plainText);
            this.activeNode = node;
            this.initialText = jMxroEditorPane.getText();
        
    }

    public void cacheDocument() {
        cachingRunning = true;
        
        cachingRunning = false;
    }

    private class SelectDocumentTask extends org.jdesktop.application.Task<Object, Void> {

        GDocsEditorData.GDocNode gdocnode;

        SelectDocumentTask(org.jdesktop.application.Application app, GDocsEditorData.GDocNode gdocnode) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to RefreshTask fields, here.
            super(app);
            this.gdocnode = gdocnode;
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            documentSelected(gdocnode);
            return true;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    public void init(String[] args) {

        GoogleDocsEditorGUIApp.startUpCallback = new StartUpCallback() {

            @Override
            public void startedUp(GoogleDocsEditorGUIApp application) {

                //while (GoogleDocsEditorApp.getApplication() == null) {}

                //while (!application.isReady) { }

                GoogleDocsEditorMainForm.EditorCallbacks callbacks = new GoogleDocsEditorMainForm.EditorCallbacks() {

                    @Override
                    public void refresh() {
                        doRefresh();
                    }

                    @Override
                    public void save(GDocsEditorData.GDocNode gdocnode) {
                        doSave(gdocnode);
                    }

                    public void preferencesEdited() {
                        if (!data.setConnection(mainFrame.getSettings().getUsername(), mainFrame.getSettings().getPasswordUnenc())) {
                            JOptionPane.showMessageDialog(mainFrame.getComponent(), "Could not establish connection to Google Docs", "Connection Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }

                    @Override
                    public void newDocument() {
                        doNewDocument();

                    }

                    @Override
                    public void deleteDocument(GDocsEditorData.GDocNode gdocnode) {
                        doDeleteDocument(gdocnode);
                    }

                    @Override
                    public void clearLocalCache() {
                        doClearLocalCache();
                    }

                    public void initialSetup() {
                        doInitialSetup();
                    }
                };

                //while (application.getView() == null) { }
                mainFrame = application.getView();
                mainFrame.app = GDocsEditorApp.this;
                mainFrame.setCallbacks(callbacks);

                data = new GDocsEditorData(mainFrame.getApplication().getContext().getLocalStorage());


                //mainFrame.getApplication().getContext().getLocalStorage().load("settings.xml");


                MxroEkitFactory factory = MxroEkitFactory.getInstance();
                //jMxroEditorPane.getT
                mainFrame.jToolBar2.add(factory.getToolBarMain(true));
                mainFrame.jToolBar3.add(factory.getToolBarFormat(true));
                mainFrame.jToolBar4.add(factory.getToolBarStyles(true));
                // mainFrame.jToolBar2.setVisible(false);
                // mainFrame.jToolBar2.setVisible(true);
                //new MxroEditorPane();

                Font text = new Font("Verdana", Font.PLAIN, 14);

                MxroEkitTextPane pane = factory.createTextPane();
                pane.setPreferredSize(null);
                pane.setFont(text);
                jMxroEditorPane = pane;
                JScrollPane scrollPane =
                        new JScrollPane(
                        pane,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                //JScrollPane scrollPane = new JScrollPane(jMxroEditorPane);
                //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                mainFrame.jPanel6.add(scrollPane);
                //mainFrame.jSplitPane1.setRightComponent(scrollPane);


                //mainFrame.jScrollPane2.setViewportView(jMxroEditorPane);
                //mainFrame.getEditorComponent().add(scrollPane);

                jTree1 = mainFrame.getDocumentsTree();
                jTree1.addTreeSelectionListener(new TreeSelectionListener() {

                    @Override
                    public void valueChanged(TreeSelectionEvent e) {



                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();

                        if (node == null) {
                            return;
                        }

                        Object nodeInfo = node.getUserObject();
                        if (node.isLeaf()) {
                            GDocsEditorData.GDocNode gdocnode = (GDocsEditorData.GDocNode) nodeInfo;

                            SelectDocumentTask task = new SelectDocumentTask(mainFrame.getApplication(), gdocnode);
                            mainFrame.getApplication().getContext().getTaskService().execute(task);


                        }
                    }
                });

                jTree1.setModel(data.getTreeModel());

                // in order to establish connection to GDocs
                if (mainFrame.getSettings() == null) {
                    mainFrame.editPreferences();
                } else {
                    mainFrame.callbacks.preferencesEdited();
                }

                

                jMxroEditorPane.addFileHandler(new GoogleDocsFileHandler(data.getAdapter()));


                data.initLocalFromStorage();

               // data.loadEntries(true);

                //data.refreshTree();

                mainFrame.getApplication().getContext().getTaskService().execute(mainFrame.InitialSetUp());

               




            /* mainFrame.getApplication().getContext().addTaskService(mainFrame.
            Refresh().getTaskService());*/




            }
        };
        GoogleDocsEditorGUIApp.launch(GoogleDocsEditorGUIApp.class, args);



    }
    public static boolean cachingRunning = false;
    public static GDocsEditorApp app;

    /**
     * @param args
     */
    public static void main(String[] args) {
        app = new GDocsEditorApp();
        app.init(args);



    }
}
