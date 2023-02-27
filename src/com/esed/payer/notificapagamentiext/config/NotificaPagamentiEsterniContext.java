package com.esed.payer.notificapagamentiext.config;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.seda.commons.properties.tree.PropertiesTree; 
//import com.seda.payer.estrattoconto.util.CipherHelper;

public class NotificaPagamentiEsterniContext implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1398452067949461001L;


	//inizio YML PG22XX04_02
	//private Properties config;
	private PropertiesTree config;
	//fine YML PG22XX04_02


	private Logger logger;
	protected HashMap<String, List<String>> parameters = new HashMap<String, List<String>>();
	
	
	public NotificaPagamentiEsterniContext() {
	}
	
	//inizio YML PG22XX04_02
		/*
		public NotificaPagamentiEsterniContext(PropertiesTree propertiesTree,
				DataSource dataSource, String schema, Logger logger,
				ClassPrinting printers, String idJob, Properties config) {
			super();
			//this.config = config;
			this.logger = logger;
		}	
		*/
	//fine YML PG22XX04_02
	
	/**
	 * Ritorna il logger
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}
	/**
	 * Setta il logger
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	 
	//inizio YML PG22XX04_02
	/*
	public Properties getProperties() {
		return config;
	}
	public void setConfig(Properties config) {
		this.config = config;
	}
	*/
	public void setConfig(PropertiesTree config) {
		this.config = config;
	}
	//fine YML PG22XX04_02
	
	
	public int addParameter(String name, String value) {
		if(!this.parameters.containsKey(name)) {
			this.parameters.put(name, new LinkedList<String>());
		}
		this.parameters.get(name).add(value); //Aggiunge un valore alla lista delle ripetizioni
		return this.parameters.get(name).size();

	}
	public String getParameter(String name) {
		if(parameters.containsKey(name))
			return (String)parameters.get(name).get(0);
		else
			return "";
	}
	
	
	public void loadSchedeBap(String[] params) {
		for (int i=0;i < params.length; i++ ) {
			String[] p = params[i].split("\\s+");
			//if (p[0].equals("END") || p[0].equals("CONFIGPATH") || p[0].equals("CUTECUTE")|| p[0].equals("ENTE")) {
			if (p[0].equals("END")) {
				if (p[1].trim().equals("")) {
					addParameter(p[0].trim(), "");
				} else {
					addParameter(p[0].trim(), p[1].trim());
				}
			} else {
				addParameter(p[0].trim(), p[1].trim());//Nome parametro - valore(Aggiunge Lista di valori per schede con ripetizione)	
			}
		}
	}
	public String formatDate(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}
	
	/** fornito come parametro BAP */
	public String getCodiceUtente() {
		return getParameter("CUTECUTE");
	}
	
	
//	public String getUrlWsEsterno(String codiceUtente) {
//		return config.getProperty(PropertiesPath.urlWsEsterno.format(codiceUtente));
//	}
	
	public String getCodiceSocieta(String codiceUtente) {
		return config.getProperty(PropertiesPath.codiceSocieta.format(codiceUtente));
	}

	public String getCodiceEnte(String codiceUtente) {
		return config.getProperty(PropertiesPath.codiceEnte.format(codiceUtente));
	}
	
	
	public String geturlPGEC(String codiceUtente) {
		return config.getProperty(PropertiesPath.urlpgec.format(codiceUtente));
	}
	
	/**
	 * Indica il numero massimo dei tentativi di notifica.
	 * @return Ritorna il numero massimo dei tentativi di notifica.
	 */
	public int getMaxTentativi(String codiceUtente) {
		return Integer.parseInt(config.getProperty(PropertiesPath.maxTentativi.format(codiceUtente)));
	}		

	public String getDatasourceJDBCDriver(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCDriver.format(codiceUtente));
	}

	public String getDatasourceJDBCUrl(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCUrl.format(codiceUtente));
	}

	public String getDatasourceJDBCUser(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCUser.format(codiceUtente));
	}

	public String getDatasourceJDBCPassword(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCPassword.format(codiceUtente));
	}

	public String getDatasourceSchema(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceSchema.format(codiceUtente));
	}	
	
	//SVILUPPO_001_LUCAP_28.05.2020
	public String getEmailTo(String codiceUtente) {
		return config.getProperty(PropertiesPath.emailTo.format(codiceUtente));
	}	
	public String getEmailSubject(String codiceUtente) {
		return config.getProperty(PropertiesPath.emailSubject.format(codiceUtente));
	}	
	public String getEmailBody(String codiceUtente) {
		return config.getProperty(PropertiesPath.emailBody.format(codiceUtente));
	}	
	public String getUrlMailSender(String codiceUtente) {
		return config.getProperty(PropertiesPath.urlMailSender.format(codiceUtente));
	}	
	//FINE SVILUPPO_001_LUCAP_28.05.2020
	
	//inizio YML PG22XX04_02
	public String geturlLogWs() {
		String prop = PropertiesPath.urlLogRequest.format(PropertiesPath.defaultNode.format());
		return config.getProperty(prop);
	}

	//fine YML PG22XX04_02
	
}
