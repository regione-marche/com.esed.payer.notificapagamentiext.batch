package com.esed.payer.notificapagamentiext.components;

import java.io.StringWriter;

import javax.xml.bind.JAXB;

@SuppressWarnings("unused")
public class BaseServer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final String DBSCHEMACODSOCIETA = "dbSchemaCodSocieta";
	
	public static String getStringXMLofObject(Object obj) {
		String out = ""; 
     	try {
 			StringWriter sw = new StringWriter();
 			JAXB.marshal(obj, sw);
 	    	out = sw.toString();
	 	} catch (Exception e) {
	 		System.err.println("getStringXMLofObject: " + e.getMessage());
	 	}
     	//System.out.println("getStringXMLofObject: " + out);
	    return out;
    }
		
}
