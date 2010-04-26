/*
 * A simple JavaBean based on NetBeans tutorial.
 */

package de.mxro.textedit.gdocseditor.gui;

import java.beans.*;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author mx
 */
public class GDocsSettings implements Serializable {

    
    protected String password;
    

    
    
    private static class EncryptUtils {
   public static final String DEFAULT_ENCODING="UTF-8"; 
   static BASE64Encoder enc=new BASE64Encoder();
   static BASE64Decoder dec=new BASE64Decoder();

   public static String base64encode(String text){
      try {
         String rez = enc.encode( text.getBytes( DEFAULT_ENCODING ) );
         return rez;         
      }
      catch ( UnsupportedEncodingException e ) {
         return null;
      }
   }//base64encode

   public static String base64decode(String text){

         try {
            return new String(dec.decodeBuffer( text ),DEFAULT_ENCODING);
         }
         catch ( IOException e ) {
           return null;
         }

      }//base64decode

      public static void main(String[] args){
       String txt="some text to be encrypted" ;
       String key="key phrase used for XOR-ing";
       System.out.println(txt+" XOR-ed to: "+(txt=xorMessage( txt, key )));
       String encoded=base64encode( txt );       
       System.out.println( " is encoded to: "+encoded+" and that is decoding to: "+ (txt=base64decode( encoded )));
       System.out.print( "XOR-ing back to original: "+xorMessage( txt, key ) );

      }

      public static String xorMessage(String message, String key){
       try {
          if (message==null || key==null ) return null;

         char[] keys=key.toCharArray();
         char[] mesg=message.toCharArray();

         int ml=mesg.length;
         int kl=keys.length;
         char[] newmsg=new char[ml];

         for (int i=0; i<ml; i++){
            newmsg[i]=(char)(mesg[i]^keys[i%kl]);
         }//for i
         mesg=null; keys=null;
         return new String(newmsg);
      }
      catch ( Exception e ) {
         return null;
       }  
      }//xorMessage

}//class

     public String getPassword() {
        System.out.println("get "+this.password);
         return this.password;

     }

    static private String key="not a very strong key!!@£@£@@!@!";
    /**
     * Get the value of password
     *
     * @return the value of password
     */
    public String getPasswordUnenc() {
       String txt = this.password;
       if (this.password==null) return null;
       if (this.password.equals("")) return null;

       txt=EncryptUtils.base64decode( txt );
       txt =EncryptUtils.xorMessage( txt, key );
       // System.out.println("getUnenc "+txt);
       return txt;

       
    }


    private boolean first=true;
    /**
     * Set the value of password
     *
     * @param password new value of password
     */
    public void setPassword(String password) {
        if (first) {
            this.password = password;
            this.first =false;
            //System.out.println("set first "+password);
            return;
        }
        String txt=password;
        txt=EncryptUtils.xorMessage( txt, key );
        txt=EncryptUtils.base64encode( txt );
       //System.out.println("set "+txt);
        this.password = txt;
               
      
        
    }

    private PropertyChangeSupport propertySupport;

    public GDocsSettings() {
       
            propertySupport = new PropertyChangeSupport(this);
          

    }

    protected String username;

    /**
     * Get the value of username
     *
     * @return the value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the value of username
     *
     * @param username new value of username
     */
    public void setUsername(String username) {
        this.username = username;
    }



    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
