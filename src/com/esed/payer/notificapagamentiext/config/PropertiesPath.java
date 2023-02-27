package com.esed.payer.notificapagamentiext.config;

import java.text.MessageFormat;
import java.util.ResourceBundle;
 

/**
 * PG130100
 */
public enum PropertiesPath {
			defaultNode, // YML PG22XX04_02
			//urlWsEsterno, 
			codiceSocieta, 
			codiceEnte, 
			maxTentativi, 
			datasourceJDBCDriver, 
			datasourceJDBCUrl,
			datasourceJDBCUser,
			datasourceJDBCPassword, 
			datasourceSchema,
			urlpgec,
			emailTo,		//SVILUPPO_001_LUCAP_28.05.2020
			emailSubject,	//SVILUPPO_001_LUCAP_28.05.2020
			emailBody,		//SVILUPPO_001_LUCAP_28.05.2020
			urlMailSender, 	//SVILUPPO_001_LUCAP_28.05.2020
			urlLogRequest, // YML PG22XX04_02
			dbSchemaLogRequest // YML PG22XX04_02
			;
	
    private static ResourceBundle rb;

    public String format( Object... args ) {
        synchronized(PropertiesPath.class) {
            if(rb==null)
                rb = ResourceBundle.getBundle(PropertiesPath.class.getName());
            return MessageFormat.format(rb.getString(name()),args);
        }
    }
}
