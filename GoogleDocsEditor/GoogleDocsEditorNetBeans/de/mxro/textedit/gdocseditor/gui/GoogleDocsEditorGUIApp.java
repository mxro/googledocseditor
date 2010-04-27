/*
 * GoogleDocsEditorApp.java
 */

package de.mxro.textedit.gdocseditor.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class GoogleDocsEditorGUIApp extends SingleFrameApplication {
	
	 public static interface StartUpCallback {
		 public void startedUp(GoogleDocsEditorGUIApp application);
	 }
	
	 public static StartUpCallback startUpCallback; 
	
     public GoogleDocsEditorMainForm view;

     public boolean isReady = false;

     public GoogleDocsEditorMainForm getView() { return view; };

     @Override protected void ready() {
         isReady = true;
     }

    @Override
    public void exit(EventObject event) {
        
        getView().statusPanel.setVisible(true);
        getView().statusMessageLabel.setText("Synchronizing");
        getView().statusAnimationLabel.setVisible(true);
        getView().getFrame().pack();
        getView().getFrame().doLayout();
        getView().getFrame().dispose();
        //this.getView().
        this.getView().app.doSaveEntries();
        super.exit(event);
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        view = new GoogleDocsEditorMainForm(this);
        show(view);


        startUpCallback.startedUp(this);

    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of GoogleDocsEditorApp
     */
    public static GoogleDocsEditorGUIApp getApplication() {
        return Application.getInstance(GoogleDocsEditorGUIApp.class);
    }

    /**
     * Main method launching the application.
     */
   /* public static void main(String[] args) {
        launch(GoogleDocsEditorApp.class, args);
    }*/
}
