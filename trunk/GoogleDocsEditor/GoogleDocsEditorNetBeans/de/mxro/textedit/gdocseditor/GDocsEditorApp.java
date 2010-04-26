package de.mxro.textedit.gdocseditor;

import java.io.IOException;
import java.net.MalformedURLException;

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
import javax.swing.JScrollPane;

public class GDocsEditorApp {

	GoogleDocsEditorMainForm mainFrame;

	private MxroEkitTextPane jMxroEditorPane;
	private JTree jTree1; 

	private GDocsEditorData data;


	public void doRefresh() {
		//mainFrame.getApplication().getContext().
		
		this.jMxroEditorPane.addFileHandler(new GoogleDocsFileHandler(data.getAdapter()));
		data.loadEntries();
	}

	public void doSave() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        jTree1.getLastSelectedPathComponent();
    	
    	if (node == null) return;
    	if (!(node.getUserObject() instanceof GDocsEditorData.GDocNode)) return;
    	
    	GDocsEditorData.GDocNode gdocnode = (GDocsEditorData.GDocNode) node.getUserObject();
    	 
    	String uploadHTML = data.getAdapter().prepareHTMLDocumentBeforeUpload(jMxroEditorPane.getText());
    	data.getAdapter().updateDocument(gdocnode.entry, uploadHTML);
	}

	public void documentSelected(String docId) {
		try {
			String html = data.getAdapter().downloadDocument(docId, "html");
			//System.out.println("==== downloaded text ====");
            //System.out.println(html);
            jMxroEditorPane.
                    setText(data.getAdapter().prepareHTMLDocumentAfterDownload(html));
		} catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ServiceException e) {

			e.printStackTrace();
		}
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
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
        	documentSelected(gdocnode.docId);
            return true;  // return your result
        }
        @Override protected void succeeded(Object result) {
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
					public void save() {
						doSave();
					}
					
					public void preferencesEdited() {
						if (!data.setConnection(mainFrame.getSettings().getUsername(), mainFrame.getSettings().getPasswordUnenc())) {
							JOptionPane.showMessageDialog(mainFrame.getComponent(), "Could not establish connection to Google Docs", "Connection Error", JOptionPane.ERROR_MESSAGE);
						}
					
					}

				};

				//while (application.getView() == null) { }
				mainFrame = application.getView(); 

				mainFrame.setCallbacks(callbacks);

				data = new GDocsEditorData();
				
				if (mainFrame.getSettings() == null) {
					mainFrame.editPreferences();
				}
				//mainFrame.getApplication().getContext().getLocalStorage().load("settings.xml");
				

                MxroEkitFactory factory = MxroEkitFactory.getInstance();
                //jMxroEditorPane.getT
                mainFrame.jToolBar2.add(factory.getToolBarMain(true));
                mainFrame.jToolBar3.add(factory.getToolBarFormat(true));
                mainFrame.jToolBar4.add(factory.getToolBarStyles(true));
              // mainFrame.jToolBar2.setVisible(false);
               // mainFrame.jToolBar2.setVisible(true);
                        //new MxroEditorPane();
                
                Font text =new Font("Verdana", Font.PLAIN, 14);
                
                MxroEkitTextPane pane = factory.createTextPane();
                pane.setPreferredSize(null);
                 pane
                 .setFont(text);
                 jMxroEditorPane = pane;
                JScrollPane scrollPane =
        new JScrollPane(
            pane,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                //JScrollPane scrollPane = new JScrollPane(jMxroEditorPane);
                //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                mainFrame.jSplitPane1.setRightComponent(scrollPane);


                //mainFrame.jScrollPane2.setViewportView(jMxroEditorPane);
				//mainFrame.getEditorComponent().add(scrollPane);

                jTree1 = mainFrame.getDocumentsTree();
      		jTree1.addTreeSelectionListener(new TreeSelectionListener() {

                    @Override
					public void valueChanged(TreeSelectionEvent e) {

						DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						jTree1.getLastSelectedPathComponent();

						if (node == null)
							return;

						Object nodeInfo = node.getUserObject();
						if (node.isLeaf()) {
							GDocsEditorData.GDocNode gdocnode = (GDocsEditorData.GDocNode) nodeInfo;
							
							SelectDocumentTask task = new SelectDocumentTask(mainFrame.getApplication(), gdocnode);
							mainFrame.getApplication().getContext().getTaskService().execute(task);
							
							
						} 
					}



				});

				jTree1.setModel(data.getTreeModel());

                mainFrame.callbacks.preferencesEdited();

                mainFrame.getApplication().getContext().getTaskService().execute(mainFrame.Refresh());

               /* mainFrame.getApplication().getContext().addTaskService(mainFrame.
                        Refresh().getTaskService());*/
                


			}

		};
		GoogleDocsEditorGUIApp.launch(GoogleDocsEditorGUIApp.class, args);



	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		GDocsEditorApp app = new GDocsEditorApp();
		app.init(args);



	}

}
