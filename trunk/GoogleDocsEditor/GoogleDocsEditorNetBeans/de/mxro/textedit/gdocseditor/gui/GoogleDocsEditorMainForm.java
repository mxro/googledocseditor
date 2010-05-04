/*
 * GoogleDocsEditorView.java
 */

package de.mxro.textedit.gdocseditor.gui;

import de.mxro.textedit.gdocseditor.GDocsEditorApp;
import de.mxro.textedit.gdocseditor.GDocsEditorData;
import java.awt.dnd.DnDConstants;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * The application's main frame.
 */
public class GoogleDocsEditorMainForm extends FrameView {

   public static String SETTINGSFILENAME = "gdocssettings.xml";

   public GDocsEditorApp app;
   public EditorCallbacks callbacks;
   private GDocsSettings settings;

    public void setSettings(GDocsSettings settings) {

        this.settings = settings;
    }

    public GDocsSettings getSettings() {
        return this.settings;
    }

   public static void init() {
      // GoogleDocsEditorApp.main(null);
   }

   public void setCallbacks(EditorCallbacks callbacks){
       this.callbacks = callbacks;
   }

    public  javax.swing.JTree getDocumentsTree() {
        return this.jTree1;
    }

    public javax.swing.JComponent getEditorComponent() {
        return null;
    }

    public interface EditorCallbacks {
        public void save(GDocsEditorData.GDocNode gdocnode);
        public void refresh();
        public void preferencesEdited();
        public void newDocument();
        public void deleteDocument(GDocsEditorData.GDocNode gdocnode);
        public void clearLocalCache();
        public void initialSetup();
        
    }


    public GoogleDocsEditorMainForm(SingleFrameApplication app) {
        super(app);

        try {
             Object obj = this.getApplication().getContext().getLocalStorage().load(SETTINGSFILENAME);
             if (obj instanceof GDocsSettings) {
                 this.setSettings((GDocsSettings) obj);
             }
             // this.setSettings(
        } catch (IOException e) {
            e.printStackTrace();
        }
        initComponents();



        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        jTree1.setRootVisible(false);
        jTree1.getSelectionModel().setSelectionMode
            				(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.setDragEnabled(false);
        new TreeDragSource(jTree1, DnDConstants.ACTION_COPY_OR_MOVE);

        
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = GoogleDocsEditorGUIApp.getApplication().getMainFrame();
            aboutBox = new GoogleDocsEditorAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        GoogleDocsEditorGUIApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jToolBar4 = new javax.swing.JToolBar();
        jPanel7 = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jTitleField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        FormListener formListener = new FormListener();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setMinimumSize(new java.awt.Dimension(627, 114));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(211, 94));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

        jPanel8.setName("jPanel8"); // NOI18N
        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.mxro.textedit.gdocseditor.gui.GoogleDocsEditorGUIApp.class).getContext().getActionMap(GoogleDocsEditorMainForm.class, this);
        jButton1.setAction(actionMap.get("Refresh")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.mxro.textedit.gdocseditor.gui.GoogleDocsEditorGUIApp.class).getContext().getResourceMap(GoogleDocsEditorMainForm.class);
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(78, 70));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        jButton4.setAction(actionMap.get("NewDocumentAsync")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton4);

        jButton5.setAction(actionMap.get("DeleteDocumentAsync")); // NOI18N
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton5);

        jButton3.setAction(actionMap.get("Save")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton3);

        jButton6.setAction(actionMap.get("ClearCacheAsync")); // NOI18N
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton6);

        jButton2.setAction(actionMap.get("editPreferences")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        jPanel8.add(jToolBar1);

        jPanel1.add(jPanel8);

        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel10.setName("jPanel10"); // NOI18N
        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N
        jPanel10.add(jToolBar2);

        jToolBar4.setRollover(true);
        jToolBar4.setName("jToolBar4"); // NOI18N
        jPanel10.add(jToolBar4);

        jPanel9.add(jPanel10);

        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar3.setRollover(true);
        jToolBar3.setName("jToolBar3"); // NOI18N
        jPanel7.add(jToolBar3);

        jPanel9.add(jPanel7);

        jPanel1.add(jPanel9);

        mainPanel.add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel5.setMinimumSize(new java.awt.Dimension(280, 0));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setName("jScrollPane1"); // NOI18N
        jScrollPane1.setOpaque(false);
        jScrollPane1.setRequestFocusEnabled(false);

        jTree1.setDragEnabled(true);
        jTree1.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jTree1.setMinimumSize(null);
        jTree1.setName("jTree1"); // NOI18N
        jTree1.setPreferredSize(null);
        jTree1.setRequestFocusEnabled(false);
        jTree1.addTreeSelectionListener(formListener);
        jScrollPane1.setViewportView(jTree1);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel5);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel4.setMaximumSize(new java.awt.Dimension(32767, 30));
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(30, 30));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        jTitleField.setText(resourceMap.getString("jTitleField.text")); // NOI18N
        jTitleField.setName("jTitleField"); // NOI18N
        jPanel4.add(jTitleField);

        jPanel3.add(jPanel4);

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel3.add(jPanel6);

        jSplitPane1.setRightComponent(jPanel3);

        jPanel2.add(jSplitPane1);

        mainPanel.add(jPanel2, java.awt.BorderLayout.CENTER);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 603, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements javax.swing.event.TreeSelectionListener {
        FormListener() {}
        public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
            if (evt.getSource() == jTree1) {
                GoogleDocsEditorMainForm.this.jTree1ValueChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jTree1ValueChanged

    @Action
    public Task Save() {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        jTree1.getLastSelectedPathComponent();

    	if (node == null) return null;
    	if (!(node.getUserObject() instanceof GDocsEditorData.GDocNode)) return null;

    	GDocsEditorData.GDocNode gdocnode = (GDocsEditorData.GDocNode) node.getUserObject();

        return new SaveTask(getApplication(), gdocnode);
    }

    public SaveTask createSaveTask(GDocsEditorData.GDocNode gdocsnode) {
        return new SaveTask(getApplication(), gdocsnode);
    }

    public class SaveTask extends org.jdesktop.application.Task<Object, Void> {
        GDocsEditorData.GDocNode gdocnode;
        
        SaveTask(org.jdesktop.application.Application app, GDocsEditorData.GDocNode gdocnode) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to SaveTask fields, here.
            super(app);

            this.gdocnode = gdocnode;
        }
        @Override protected Object doInBackground() {
            callbacks.save(gdocnode);
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public Task Refresh() {
        return new RefreshTask(getApplication());
    }

    private class RefreshTask extends org.jdesktop.application.Task<Object, Void> {
        RefreshTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to RefreshTask fields, here.
            super(app);

            this.setMessage("Synchronizing with Google Docs");
        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            callbacks.refresh();
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public void editPreferences() {
       // new GDocsSettingsBeanInfo().
        // = new GDocsSettings();
        //        settings.setPassword("testpsw");
         //       settings.setUsername("testusr");
    			if (this.getSettings() == null) this.setSettings(new GDocsSettings());
                final GDocSettingsDialog dialog = new GDocSettingsDialog(new javax.swing.JFrame(), true, this.getSettings());
                dialog.setModal(true);
                dialog.setVisible(true);

                this.setSettings(dialog.getSettings());
                //System.out.println(dialog.gDocsSettingsPanel1.gDocsSettings1.getUsername());
                try {
                this.getApplication().getContext().getLocalStorage().save(this.getSettings(), SETTINGSFILENAME);
                System.out.println("Settings saved at: "+this.getApplication().getContext().getLocalStorage().getDirectory());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // System.out.println(dialog.gDocsSettingsPanel1.gDocsSettings1.getUsername()+" : "+dialog.gDocsSettingsPanel1.gDocsSettings1.getPassword());
                callbacks.preferencesEdited();
    }

    @Action
    public void NewDocument() {


    }

    @Action
    public Task NewDocumentAsync() {
        return new NewDocumentAsyncTask(getApplication());
    }

    private class NewDocumentAsyncTask extends org.jdesktop.application.Task<Object, Void> {
        NewDocumentAsyncTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to NewDocumentAsyncTask fields, here.
            super(app);
        }
        @Override protected Object doInBackground() {
            callbacks.newDocument();
            return null;
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public Task DeleteDocumentAsync() {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        jTree1.getLastSelectedPathComponent();

    	if (node == null) return null;
    	if (!(node.getUserObject() instanceof GDocsEditorData.GDocNode)) return null;

    	GDocsEditorData.GDocNode gdocnode = (GDocsEditorData.GDocNode) node.getUserObject();

        ((DefaultTreeModel) jTree1.getModel()).removeNodeFromParent(node);

        return new DeleteDocumentAsyncTask(getApplication(), gdocnode);
    }

    private class DeleteDocumentAsyncTask extends org.jdesktop.application.Task<Object, Void> {
        GDocsEditorData.GDocNode gdocnode;
        DeleteDocumentAsyncTask(org.jdesktop.application.Application app, GDocsEditorData.GDocNode gdocnode) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to DeleteDocumentAsyncTask fields, here.

            super(app);

            this.gdocnode = gdocnode;
        }
        @Override protected Object doInBackground() {
            callbacks.deleteDocument(gdocnode);
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public Task ClearCacheAsync() {
        return new ClearCacheAsyncTask(getApplication());
    }

    private class ClearCacheAsyncTask extends org.jdesktop.application.Task<Object, Void> {
        ClearCacheAsyncTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ClearCacheAsyncTask fields, here.
            super(app);
        }
        @Override protected Object doInBackground() {
            callbacks.clearLocalCache();
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public Task InitialSetUp() {
        return new InitialSetUpTask(getApplication());
    }

    private class InitialSetUpTask extends org.jdesktop.application.Task<Object, Void> {
        InitialSetUpTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to InitialSetUpTask fields, here.
            super(app);
        }
        @Override protected Object doInBackground() {
            callbacks.initialSetup();
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }







    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton2;
    public javax.swing.JButton jButton3;
    public javax.swing.JButton jButton4;
    public javax.swing.JButton jButton5;
    public javax.swing.JButton jButton6;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel10;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JPanel jPanel4;
    public javax.swing.JPanel jPanel5;
    public javax.swing.JPanel jPanel6;
    public javax.swing.JPanel jPanel7;
    public javax.swing.JPanel jPanel8;
    public javax.swing.JPanel jPanel9;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JTextField jTitleField;
    public javax.swing.JToolBar jToolBar1;
    public javax.swing.JToolBar jToolBar2;
    public javax.swing.JToolBar jToolBar3;
    public javax.swing.JToolBar jToolBar4;
    public javax.swing.JTree jTree1;
    public javax.swing.JPanel mainPanel;
    public javax.swing.JMenuBar menuBar;
    public javax.swing.JProgressBar progressBar;
    public javax.swing.JLabel statusAnimationLabel;
    public javax.swing.JLabel statusMessageLabel;
    public javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
