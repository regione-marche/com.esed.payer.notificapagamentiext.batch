package com.esed.payer.notificapagamentiext.components;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.esed.log.req.dati.LogWin;
import com.esed.payer.notificapagamentiext.config.NotificaPagamentiEsterniContext;
import com.esed.payer.notificapagamentiext.config.NotificaPagamentiEsterniResponse;
import com.esed.payer.notificapagamentiext.model.NotificaPagamentiEsterniModel;
import com.esed.payer.notificapagamentiext.util.LoggerUtil;
import com.seda.bap.components.core.BapException;
import com.seda.bap.components.core.spi.ClassPrinting;
import com.seda.bap.components.core.spi.PrintCodes;
import com.seda.commons.properties.tree.PropertiesTree;
import com.seda.data.dao.DAOHelper;
import com.seda.data.datasource.DataSourceFactoryImpl;
import com.seda.data.helper.Helper;
import com.seda.emailsender.webservices.dati.EMailSenderRequestType;
import com.seda.emailsender.webservices.dati.EMailSenderResponse;
import com.seda.emailsender.webservices.source.EMailSenderInterface;
import com.seda.emailsender.webservices.source.EMailSenderServiceLocator;
import com.seda.emailsender.webservices.srv.EMailSenderFaultType;
import com.seda.payer.core.bean.NodoSpcRpt;
import com.seda.payer.core.dao.NodoSpcDao;
import com.seda.payer.core.exception.DaoException;
import com.seda.payer.integraente.webservice.dati.PagamentoSpontaneo;
import com.seda.payer.integraente.webservice.dati.TipoCDS;
import com.seda.payer.integraente.webservice.dati.TipoSpontaneo;
import com.seda.payer.pgec.webservice.commons.dati.BeanIV;
import com.seda.payer.pgec.webservice.commons.dati.ConfigPagamentoSingleRequest;
import com.seda.payer.pgec.webservice.commons.dati.ConfigPagamentoSingleResponse;
import com.seda.payer.pgec.webservice.commons.dati.RecuperaTransazioneRequestType;
import com.seda.payer.pgec.webservice.commons.dati.RecuperaTransazioneResponseType;

public class NotificaPagamentiEsterniCore extends BaseServer {
	

//	private Properties config = null;
	private PropertiesTree configTree = null;
	
	private static Logger log = Logger.getLogger(NotificaPagamentiEsterniCore.class);
	private static String myPrintingKeyPAG_REPORT = "REPORT";
	private static String myPrintingKeyPAG_SYSOUT = "SYSOUT";

	Calendar cal = Calendar.getInstance();
	private NotificaPagamentiEsterniContext notificaPagamentiEsterniContext;
	
	DataSource datasource;
	private ClassPrinting classPrinting;
	String schema;
//	DataSource datasourceHost;
//	String schemaHost;
	String jobId;
	int recordAnagraficheLette = 0;

	int totNotifiche = 0;
	int totErroriNotifiche = 0;
	
	private Connection connection;
	//inizio LP PG22XX07
	NodoSpcDao nodoSpcDao = null;
	boolean bAggiornaRTErrate = true;
	//fine LP PG22XX07
	
	private ArrayList<NotificaPagamentiEsterniModel> listNotificaPagamentiEsterniEstratti = new ArrayList<NotificaPagamentiEsterniModel>();
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	String lineSeparator = "============================================================================================";

	//SVILUPPO_001_LUCAP_28.05.2020
	private ArrayList<NotificaPagamentiEsterniModel> listErroriNotificaPagamentiEsterni = new ArrayList<NotificaPagamentiEsterniModel>();
	private HashMap<String, String> listErrori = new HashMap<String, String>();
	private EMailSenderInterface emsCaller = null;
	//FINE SVILUPPO_001_LUCAP_28.05.2020
	
	public NotificaPagamentiEsterniCore() {
		super();
		welcome();
	}

	public NotificaPagamentiEsterniResponse run(String[] params, DataSource datasource, String schema, ClassPrinting classPrinting ,Logger logger, String jobId) throws BapException {
		NotificaPagamentiEsterniResponse notificaPagamentiEsterniResponse = new NotificaPagamentiEsterniResponse();
		notificaPagamentiEsterniResponse.setCode("00");
		notificaPagamentiEsterniResponse.setMessage("");
		try {
			this.datasource = datasource;
			this.schema = schema;


			this.jobId = jobId;
			this.classPrinting = classPrinting;

			preProcess(params);
			processNotificaPagamenti();
 			postProcess(classPrinting);
 			printRow(myPrintingKeyPAG_SYSOUT, "Elaborazione completata con successo ");
 			printRow(myPrintingKeyPAG_SYSOUT, lineSeparator);
		} catch (Exception e) {
			//System.out.println(e);
			e.printStackTrace();
			printRow(myPrintingKeyPAG_SYSOUT, "Elaborazione completata con errori " + e);
 			printRow(myPrintingKeyPAG_SYSOUT, lineSeparator);
 			notificaPagamentiEsterniResponse.setCode("30");	//TODO da verificare se mantenere 30 come per altri processi oppure impostare 12
 			notificaPagamentiEsterniResponse.setMessage("Operazione terminata con errori ");
		}

		return notificaPagamentiEsterniResponse;
	}



	private void postProcess(ClassPrinting classPrinting) {
		printRow(myPrintingKeyPAG_SYSOUT, " ");
		printRow(myPrintingKeyPAG_SYSOUT, "Notifica pagamenti esterni completata - Numero di notifiche: " + totNotifiche);
		printRow(myPrintingKeyPAG_SYSOUT, "Numero di notifiche NON riuscite: " + totErroriNotifiche);
		if (classPrinting!=null)
			try {
				classPrinting.print(myPrintingKeyPAG_REPORT, "Notifica pagamenti esterni completata - Numero di notifiche: " + totNotifiche);
				classPrinting.print(myPrintingKeyPAG_REPORT, "Numero di notifiche NON riuscite: " + totErroriNotifiche);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	private void welcome() {
		StringBuffer w = new StringBuffer("");    	
		w.append("" +" Notifica Pagamenti Esterni "+ "\n");
		w.append(System.getProperties().get("java.specification.vendor") + " ");
		w.append(System.getProperties().get("java.version") + "\n");  
		//inizio LP PG22XX07
		//w.append("(C) Copyright 2017 di e-SED"  + "\n");
		w.append("(C) Copyright 2021 di Maggioli spa"  + "\n");
		//inizio LP PG22XX07
		w.append("\n");
		System.out.println(w.toString());
		w=null;
		System.out.println(lineSeparator);
		System.out.println("Avvio " + "Notifica Pagamenti Esterni " + "");
		System.out.println(lineSeparator);
	}
	
	public void preProcess(String[] params) throws Exception {
		notificaPagamentiEsterniContext =  new NotificaPagamentiEsterniContext();
		notificaPagamentiEsterniContext.loadSchedeBap(params);

		String fileConf = notificaPagamentiEsterniContext.getParameter("CONFIGPATH");
		
//		try {
//			config = PropertiesLoader.load(fileConf);
		configTree = new PropertiesTree(fileConf);
		if( configTree == null) {
			printRow(myPrintingKeyPAG_SYSOUT, "Errore settaggio da file di configurazione " + fileConf );
			throw new Exception();
		}
//		} catch (FileNotFoundException e) {
//			printRow(myPrintingKeyPAG_SYSOUT, "File di configurazione " + fileConf + " non trovato");
//			throw new Exception();
//		} catch (IOException e) {
//			printRow(myPrintingKeyPAG_SYSOUT, "Errore file di configurazione " + fileConf + " " + e);
//			throw new Exception();
//		}	
		notificaPagamentiEsterniContext.setConfig(configTree);
		
//		if (notificaPagamentiEsterniContext.getUrlWsEsterno(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
//			printRow(myPrintingKeyPAG_SYSOUT, "Url WS Esterno non configurata"); 
//			throw new Exception();
//		}

		
		if (notificaPagamentiEsterniContext.getDatasourceJDBCDriver(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "JDBCDriver non configurato"); 
			throw new Exception();
		}
		if (notificaPagamentiEsterniContext.getDatasourceJDBCUrl(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "JDBCUrl non configurato"); 
			throw new Exception();
		}
		if (notificaPagamentiEsterniContext.getDatasourceJDBCUser(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "JDBCUSer non configurato"); 
			throw new Exception();
		}
		if (notificaPagamentiEsterniContext.getDatasourceJDBCPassword(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "JDBCPassword non configurato"); 
			throw new Exception();
		}
		if (notificaPagamentiEsterniContext.getDatasourceSchema(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "Datasource Schema non configurato"); 
			throw new Exception();
		}
		
		//SVILUPPO_001_LUCAP_28.05.2020
		if (notificaPagamentiEsterniContext.getEmailTo(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "Destinatario mail non configurato"); 
		}
		if (notificaPagamentiEsterniContext.getEmailSubject(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "Soggetto mail Schema non configurato"); 
		}
		if (notificaPagamentiEsterniContext.getEmailBody(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "Corpo mail non configurato"); 
		}
		if (notificaPagamentiEsterniContext.getUrlMailSender(notificaPagamentiEsterniContext.getCodiceUtente())==null) {
			printRow(myPrintingKeyPAG_SYSOUT, "Url mailSender non configurato"); 
		} else {
			//inizializzazione mailSender
			EMailSenderServiceLocator emsService = new EMailSenderServiceLocator();
			emsService.setEMailSenderPortEndpointAddress(notificaPagamentiEsterniContext.getUrlMailSender(notificaPagamentiEsterniContext.getCodiceUtente()));
			try  {
				emsCaller = (EMailSenderInterface)emsService.getEMailSenderPort();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}
		//FINE SVILUPPO_001_LUCAP_28.05.2020
		
		if(this.datasource == null) {
			//Recupero da file di configurazione
			DataSourceFactoryImpl dataSourceFactory = new DataSourceFactoryImpl();
			Properties dsProperties= new Properties();
			dsProperties.put(DAOHelper.JDBC_DRIVER, notificaPagamentiEsterniContext.getDatasourceJDBCDriver(notificaPagamentiEsterniContext.getCodiceUtente()));
			dsProperties.put(DAOHelper.JDBC_URL, notificaPagamentiEsterniContext.getDatasourceJDBCUrl(notificaPagamentiEsterniContext.getCodiceUtente()));
			dsProperties.put(DAOHelper.JDBC_USER, notificaPagamentiEsterniContext.getDatasourceJDBCUser(notificaPagamentiEsterniContext.getCodiceUtente()));
			dsProperties.put(DAOHelper.JDBC_PASSWORD, notificaPagamentiEsterniContext.getDatasourceJDBCPassword(notificaPagamentiEsterniContext.getCodiceUtente()));
			//inizio LP PG200060
			if(!(notificaPagamentiEsterniContext.getCodiceUtente().equals("000LP") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000RM"))) {
			//fine LP PG200060
			dsProperties.put("autocommit", "true");		//TODO da verificare
			//inizio LP PG200060
			}
			//fine LP PG200060
			dataSourceFactory.setProperties(dsProperties);
			this.datasource = dataSourceFactory.getDataSource();
		}
		if(schema == null) {
			//Recupero da file di configurazione
			this.schema = notificaPagamentiEsterniContext.getDatasourceSchema(notificaPagamentiEsterniContext.getCodiceUtente());
		}		

//		ConfigPagamentoSingleRequest configPagamentoSingleRequest = new ConfigPagamentoSingleRequest(notificaPagamentiEsterniContext.getCodiceSocieta(notificaPagamentiEsterniContext.getCodiceUtente()),
//				notificaPagamentiEsterniContext.getCodiceUtente(), notificaPagamentiEsterniContext.getCodiceEnte(notificaPagamentiEsterniContext.getCodiceUtente()),
//				"SAN", "WEB");
//		ConfigPagamentoSingleResponse  configPagamentoSingleResponse = recuperaFunzioneEnte(configPagamentoSingleRequest );
//		if(configPagamentoSingleResponse.getConfigPagamento().getFlagNotificaPagamento().equalsIgnoreCase("Y")) {
//			String integraente_endpointurl = configPagamentoSingleResponse.getConfigPagamento().getUrlServizioWebNotificaPagamento();
//		}
//				
				
		printRow(myPrintingKeyPAG_SYSOUT, "Configurazione caricata da " + fileConf);
		connection = this.datasource.getConnection();
		//inizio LP PG200060
		if(!(notificaPagamentiEsterniContext.getCodiceUtente().equals("000LP") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000RM"))) {
		//fine LP PG200060
		connection.setAutoCommit(true);
		//inizio LP PG200060
		}
		//fine LP PG200060
		//inizio LP PG200060
		try {
			nodoSpcDao = new NodoSpcDao(connection, schema);
		} catch (Exception e) {
			printRow(myPrintingKeyPAG_SYSOUT, "Errore in new nodoSpcDao");
			nodoSpcDao = null;
		}
		//fine LP PG200060

	}
	
//	private ConfigPagamentoSingleResponse recuperaFunzioneEnte(
//			ConfigPagamentoSingleRequest in) throws java.rmi.RemoteException,
//			com.seda.payer.pgec.webservice.commons.srv.FaultType {
//		ConfigPagamentoSingleResponse result = new ConfigPagamentoSingleResponse();
//
//		String jindi_context = env.getProperty(Context.INITIAL_CONTEXT_FACTORY);
//		String jindi_provider = env.getProperty(Context.PROVIDER_URL);
//		ConfigPagamento conf = null;
//
//		try {
//			CommonsFacadeRemoteHome serviceRemoteHome;
//			serviceRemoteHome = (CommonsFacadeRemoteHome) ServiceLocator.getInstance().getRemoteHome(jindi_provider, jindi_context, null, null, CommonsFacadeRemoteHome.JNDI_NAME, CommonsFacadeRemoteHome.class);
//
//			CommonsFacade service = serviceRemoteHome.create();
//
//			ConfigPagamentoDto confDto = service.getConfigPagamento(in.getCodSocieta(), in.getCodUtente(), in.getChiaveEnte(), in.getCodTipologiaServizio(), in.getCanalePagamento(), dbSchemaCodSocieta);
//
//			if (confDto != null) {
//				conf = beanToBeanConfigPagamento(confDto);
//
//				result.setRetCode("00");
//				result.setRetMessage("FUNZIONE RECUPERATA CORRETTAMENTE PER L'ENTE SELEZIONATO: "
//						+ in.getCodSocieta()
//						+ " - "
//						+ in.getCodUtente()
//						+ " - "
//						+ in.getChiaveEnte()
//						+ ". TIPOLOGIA SERVIZIO: "
//						+ in.getCodTipologiaServizio());
//			} else {
//				result.setRetCode("01");
//				result.setRetMessage("FUNZIONE NON TROVATA PER L'ENTE SELEZIONATO: "
//						+ in.getCodSocieta()
//						+ " - "
//						+ in.getCodUtente()
//						+ " - "
//						+ in.getChiaveEnte()
//						+ ". TIPOLOGIA SERVIZIO: "
//						+ in.getCodTipologiaServizio());
//			}
//		} catch (Exception e) {
//			result.setRetCode("02");
//			result.setRetMessage("SI E' VERIFICATO UN ERRORE DURANTE IL RECUPERO DELLA LISTA FUNZIONI PER L'ENTE SELEZIONATO: "
//					+ in.getCodSocieta()
//					+ " - "
//					+ in.getCodUtente()
//					+ " - "
//					+ in.getChiaveEnte()
//					+ ". TIPOLOGIA SERVIZIO: "
//					+ in.getCodTipologiaServizio());
//			e.printStackTrace();
//		}
//
//		result.setConfigPagamento(conf);
//
//		return result;
//	}

	public  void processNotificaPagamenti() throws Exception {	
		
		listNotificaPagamentiEsterniEstratti.clear();
		printRow(myPrintingKeyPAG_SYSOUT, lineSeparator);
		printRow(myPrintingKeyPAG_SYSOUT, "Process " + "Notifica Pagamenti"+ "");
		printRow(myPrintingKeyPAG_SYSOUT, lineSeparator);
		
		try {
			printRow(myPrintingKeyPAG_SYSOUT, "Estrazione Pagamenti Esterni non notificate ");
			CallableStatement callableStatement;
			
			try {
				callableStatement = Helper.prepareCall(connection, schema, "PYNEXSP_LST_BATCH");
				callableStatement.setString(1, notificaPagamentiEsterniContext.getCodiceSocieta(notificaPagamentiEsterniContext.getCodiceUtente()));//				IN I_NEX_CSOCCSOC CHAR(5),
				callableStatement.setString(2, notificaPagamentiEsterniContext.getCodiceUtente());//				IN I_NEX_CUTECUTE CHAR(5),
				callableStatement.setString(3, notificaPagamentiEsterniContext.getCodiceEnte(notificaPagamentiEsterniContext.getCodiceUtente()));//				IN I_NEX_KANEKENT CHAR(10),
				callableStatement.setString(4, "");//				IN I_NEX_KTRAKTRA VARCHAR(64),
				callableStatement.setString(5, "");//				IN I_NEX_KTDTKTDT VARCHAR(64), --PG180110 FB
				callableStatement.setInt(6, notificaPagamentiEsterniContext.getMaxTentativi(notificaPagamentiEsterniContext.getCodiceUtente()));//				IN I_NEX_NNEXCORR_MAX INTEGER,
				callableStatement.setString(7, ""); //notificaPagamentiEsterniContext.getUrlWsEsterno(notificaPagamentiEsterniContext.getCodiceUtente()));//				IN I_NEX_CNEXPORT VARCHAR(256),
				callableStatement.setString(8, "");//				IN I_NEX_CNEXNDOC VARCHAR(20),
				callableStatement.setString(9, "");//				IN I_NEX_CNEXNAVV VARCHAR(18),
				callableStatement.setString(10, "");//				IN I_NEX_CNEXCFIS VARCHAR(64)	
				
				
				callableStatement.execute();
				
				
				printRow(myPrintingKeyPAG_SYSOUT, "Estrazione Pagamenti Esterni non notificate eseguita con PYNEXSP_LST_BATCH e con i parametri:");
				printRow(myPrintingKeyPAG_SYSOUT, "getCodiceSocieta = " + notificaPagamentiEsterniContext.getCodiceSocieta(notificaPagamentiEsterniContext.getCodiceUtente()));
				printRow(myPrintingKeyPAG_SYSOUT, "getCodiceUtente = " + notificaPagamentiEsterniContext.getCodiceUtente());
				printRow(myPrintingKeyPAG_SYSOUT, "getCodiceEnte = " + notificaPagamentiEsterniContext.getCodiceEnte(notificaPagamentiEsterniContext.getCodiceUtente()));
				
				
			}  catch (Exception e){
				e.printStackTrace();
				throw e;
			} finally {
			}
			
			//System.out.println("Post estrazione pagamenti esterni - " + new java.util.Date(System.currentTimeMillis()));
			//System.out.println("Pre elaborazione resultset pagamenti estratti - " + new java.util.Date(System.currentTimeMillis()));
			ResultSet listNotificaPagamenti = callableStatement.getResultSet();
			
			if (listNotificaPagamenti.next()){
				do{
					String transazioneCorrente = "";
					NotificaPagamentiEsterniModel model = null; //PREJAVA18_LUCAP_04092020
					try {
						//NotificaPagamentiEsterniModel model = new NotificaPagamentiEsterniModel(); //PREJAVA18_LUCAP_04092020
						model = new NotificaPagamentiEsterniModel(); //PREJAVA18_LUCAP_04092020
						transazioneCorrente = listNotificaPagamenti.getString(1);
						model.setChiaveTransazione(listNotificaPagamenti.getString(1));
						model.setCodiceSocieta(listNotificaPagamenti.getString(2));
						model.setCodiceUtente(listNotificaPagamenti.getString(3));
						model.setChiaveEnte(listNotificaPagamenti.getString(4).trim());
						model.setNumeroTentativoNotifica(listNotificaPagamenti.getInt(5));
						model.setUrlPortale(listNotificaPagamenti.getString(6));
						model.setNumeroDocumento(listNotificaPagamenti.getString(7));
						model.setNumeroAvviso(listNotificaPagamenti.getString(8));
						model.setCodiceFiscale(listNotificaPagamenti.getString(9));
						Calendar cal1 = Calendar.getInstance();
						cal1.setTimeInMillis(listNotificaPagamenti.getTimestamp(10).getTime());
						model.setDataInvioNotifica(cal1);
						if(listNotificaPagamenti.getTimestamp(11) != null) {
							Calendar cal2 = Calendar.getInstance();
							cal2.setTimeInMillis(listNotificaPagamenti.getTimestamp(11).getTime());
							model.setDataRispostaNotifica(cal2);
						}
						model.setUltimoEsitoNotifica(listNotificaPagamenti.getString(12));
						model.setImportoPagato(listNotificaPagamenti.getBigDecimal(13));
						Calendar cal3 = Calendar.getInstance();
						cal3.setTimeInMillis(listNotificaPagamenti.getTimestamp(14).getTime());
						model.setDataPagamento(cal3);
						
						//System.out.println("XML da notificare dentro il DB = " + listNotificaPagamenti.getString(15).toString());
						
						model.setXmlRicevuta(listNotificaPagamenti.getString(15));					
						model.setChiaveDettaglioTransazione(listNotificaPagamenti.getString(16));	//PG1800XX_014
						//inizio LP PG190220
						model.setNumeroTentativoNotificaAnnullo(listNotificaPagamenti.getInt("NEX_NNEXCOAN"));
			        	if(listNotificaPagamenti.getTimestamp("NEX_GNEXDIAN") != null) {
			            	Calendar calAi = Calendar.getInstance();
			            	calAi.setTimeInMillis(listNotificaPagamenti.getTimestamp("NEX_GNEXDIAN").getTime());
			            	model.setDataInvioNotificaAnnullo(calAi);
			        	}
			        	if(listNotificaPagamenti.getTimestamp("NEX_GNEXDRAN") != null) {
			            	Calendar calAr = Calendar.getInstance();
			            	calAr.setTimeInMillis(listNotificaPagamenti.getTimestamp("NEX_GNEXDRAN").getTime());
			            	model.setDataRispostaNotificaAnnullo(calAr);
			        	}
			        	model.setUltimoEsitoNotificaAnnullo(listNotificaPagamenti.getString("NEX_CNEXUEAN"));
			        	model.setXmlRichiestaRevoca(listNotificaPagamenti.getString("NEX_CNEXRRAN"));
						//fine LP PG190220
						
						listNotificaPagamentiEsterniEstratti.add(model);
						totNotifiche++;
					}
					catch (Exception e) {
						listErroriNotificaPagamentiEsterni.add(model);  //PREJAVA18_LUCAP_04092020
						listErrors(transazioneCorrente, String.format("Errore in Notifica a Portale Esterni per transazione: %s ;<br> Errore: %s ;",transazioneCorrente, e.getMessage()));  //PREJAVA18_LUCAP_04092020
						printRow(myPrintingKeyPAG_SYSOUT, "Errore in creare NotificaPagamentiEsterniModel per transazione: " + transazioneCorrente);						
						printRow(myPrintingKeyPAG_SYSOUT, "Errore: " + e.getMessage());						
						totErroriNotifiche++;
						continue;
					}
				} while(listNotificaPagamenti.next());
			}
			
			if (listNotificaPagamenti!=null){
				listNotificaPagamenti.close();
				listNotificaPagamenti = null;
			}
			
			//inizio LP PG190220
			boolean bFaiNotifica = true;
			com.seda.payer.integraente.webservice.dati.NotificaPagamentoResponse response = null;
			com.seda.payer.integraente.webservice.dati.NotificaRevocaPagamentoResponse responseRevoca = null;
			//fine LP PG190220
			
			//Per ogni Notifica Pagamento estratta re-interrogo WS Esterno di Notifica
			for (int i = 0; i < listNotificaPagamentiEsterniEstratti.size(); i++) {
				String transazioneCorrente = "";
				//inizio LP PG190220
				bFaiNotifica = true;
				//fine LP PG190220
				try {
					NotificaPagamentiEsterniModel model = listNotificaPagamentiEsterniEstratti.get(i);
					transazioneCorrente = model.getChiaveTransazione();
					//inizio LP PG190220
					bFaiNotifica = model.DaEseguireNotifica();
					
					if(bFaiNotifica) {
						//fine LP PG190220
						printRow(myPrintingKeyPAG_SYSOUT, sdf.format(new java.util.Date(System.currentTimeMillis())) + " - " + "Rinotifica Pagamento esterno per Codice Fiscale: " + model.getCodiceFiscale() + " - Numero Avviso Pagamento: " + model.getNumeroAvviso() + " - Transazione: " + model.getChiaveTransazione());
						//inizio LP PG190220
					} else {
						printRow(myPrintingKeyPAG_SYSOUT, sdf.format(new java.util.Date(System.currentTimeMillis())) + " - " + "Rinotifica Annullo Tecnico esterno per Codice Fiscale: " + model.getCodiceFiscale() + " - Numero Avviso Pagamento: " + model.getNumeroAvviso() + " - Transazione: " + model.getChiaveTransazione());
					}
					//fine LP PG190220
					printRow(myPrintingKeyPAG_SYSOUT, "Portale esterno: " + model.getUrlPortale());	
					
					
					printRow(myPrintingKeyPAG_SYSOUT, "recuperaTransazione: " + model.getChiaveTransazione());	
					printRow(myPrintingKeyPAG_SYSOUT, "DettaglioTransazione: " + model.getChiaveDettaglioTransazione());
				
					RecuperaTransazioneResponseType res = recuperaTransazione(model.getChiaveTransazione());
					BeanIV[] listIV = res.getListIV();
					

					
					for (BeanIV beanIV : listIV) {
				       
						// devo inviare la notifica solamente al bollettino che sto ciclando sulla tabella NEX
						if(model.getChiaveDettaglioTransazione().equals(beanIV.getChiave_transazione_dettaglio()))
						{ 
							System.err.println("beanIV.getChiave_transazione_dettaglio() = "+ beanIV.getChiave_transazione_dettaglio());
							String numeroAvvisoPagoPA = "";
							PagamentoSpontaneo pagamentoSpontaneo = null;
							if(beanIV.getTipo_bollettino().equals("SPOM") 
									|| beanIV.getTipo_bollettino().equals("CDSM") 
									|| (beanIV.getTipo_bollettino().equals("CDSA") && (beanIV.getCodice_bollettino_premarcato_mav() == null || beanIV.getCodice_bollettino_premarcato_mav().length() < 15))
								) { 
								
								//Blindatura su codice bollettino valorizzato erroneamente
								if(beanIV.getTipo_bollettino().equals("CDSA") && beanIV.getCodice_bollettino_premarcato_mav() != null && beanIV.getCodice_bollettino_premarcato_mav().length() < 15)
									beanIV.setCodice_bollettino_premarcato_mav("");
								
								printRow(myPrintingKeyPAG_SYSOUT, "Trovato bollettino SPONTANEO o MULTA SENZA AVVISO: ");	
								
								
								//notificaPagamentoRequest.setPagamentoSpontaneo(pagamentoSpontaneo);
								pagamentoSpontaneo = new PagamentoSpontaneo();
								String denominazione = beanIV.getDenominazione();
								String indirizzo = beanIV.getIndirizzo();
				                String codiceBelfioreComune = beanIV.getCodice_ente_comune_domicilio_fiscale();
				                String siglaProvincia = beanIV.getProvincia();
				                String CAP = beanIV.getCap();
				                
				                pagamentoSpontaneo.setDenominazione(denominazione);
				                pagamentoSpontaneo.setIndirizzo(indirizzo);
				                if(!codiceBelfioreComune.trim().equals("")) {
				                	pagamentoSpontaneo.setCodiceBelfioreComune(codiceBelfioreComune);
				                }
				                pagamentoSpontaneo.setSiglaProvincia(siglaProvincia);
				                pagamentoSpontaneo.setCAP(CAP);
				                //30082018 GG - inizio
				                ConfigPagamentoSingleRequest configPagamentoSingleRequest = new ConfigPagamentoSingleRequest(beanIV.getCodice_societa(), beanIV.getCodice_utente(), beanIV.getChiave_ente_con(), beanIV.getCodice_tipologia_servizio(), "WEB");
								ConfigPagamentoSingleResponse  configPagamentoSingleResponse = recuperaFunzioneEnte(configPagamentoSingleRequest);
				                String descrizioneTipologiaServizio = (configPagamentoSingleResponse.getConfigPagamento()!=null && configPagamentoSingleResponse.getConfigPagamento().getDescFunzionePagamento()!=null)?configPagamentoSingleResponse.getConfigPagamento().getDescFunzionePagamento():"";
				                pagamentoSpontaneo.setDescrizioneTipologiaServizio(descrizioneTipologiaServizio);
				                //30082018 GG - fine
				                //07102022 SB - inizio
				                if(!model.getUrlPortale().toLowerCase().contains("integraentesanita")) {
					                String codiceTipologiaServizio = (configPagamentoSingleResponse.getConfigPagamento()!=null && configPagamentoSingleResponse.getConfigPagamento().getDescFunzionePagamento()!=null)?configPagamentoSingleResponse.getConfigPagamento().getCodTipologiaServizio():"";
	                                pagamentoSpontaneo.setCodiceTipologiaServizio(codiceTipologiaServizio);
				                }
                                //07102022 SB - fine
								if (beanIV.getTipo_bollettino().equals("CDSM")) { // PG180110 (aggiunto "CDSA" nella condizione)
									String numeroVerbale = beanIV.getCodice_bollettino_premarcato_mav(); // len 15
									String targa = beanIV.getTarga(); // len 10
									SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
									
									
									//Calendar dataVerbale = beanIV.getData_sanzione(); // len 14
									TipoCDS tipoCDS = new TipoCDS(numeroVerbale, sdf.format(beanIV.getData_sanzione().getTime()), targa);
									pagamentoSpontaneo.setTipoCDS(tipoCDS);
								}
								//inizio PG180110
								else if (beanIV.getTipo_bollettino().equals("CDSA")) {
									String numeroVerbale = beanIV.getNumero_documento();
									String targa = beanIV.getTarga();
									//Calendar dataVerbale = beanIV.getData_sanzione();
									SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
									TipoCDS tipoCDS = new TipoCDS(numeroVerbale, sdf.format(beanIV.getData_sanzione().getTime()), targa);
									pagamentoSpontaneo.setTipoCDS(tipoCDS);
									printRow(myPrintingKeyPAG_SYSOUT, "Trovato CDSA: " + numeroVerbale + " - " +  sdf.format(beanIV.getData_sanzione().getTime())+ " - " + targa);
								}
								//fine PG180110
								else {
								  printRow(myPrintingKeyPAG_SYSOUT, "inizio Spontaneo");
								  //transazioniIV.setNote_premarcato(boll.getCausaleservizio() + "|" + boll.getAnnorif() + "|" + boll.getCespite());
								  String note = beanIV.getNote_premarcato();
								  
								  System.out.println("note = " + note);
								  //String cosa = "\\|";
								  
								  String annoRiferimento = "";
								  String cespite ="";
								  
								  String cosa = "\\|";
								  String[] notesplit = note.split(cosa);

								  String causaleServizio = notesplit[0]; //len 256
								  
								  if(notesplit.length>1)
									  annoRiferimento = notesplit[1]; // len 4
								  if(notesplit.length>2)
									  cespite = notesplit[2]; //len 256
								  
							     
							      
							      printRow(myPrintingKeyPAG_SYSOUT, "annoRiferimento = " + annoRiferimento);
							      printRow(myPrintingKeyPAG_SYSOUT, "causaleServizio = " + causaleServizio);
							      printRow(myPrintingKeyPAG_SYSOUT, "cespite = " + cespite);
							      
							      
							      
								  TipoSpontaneo tipoSpontaneo = new TipoSpontaneo();
								  if(!annoRiferimento.trim().equals("")) {
									  tipoSpontaneo.setAnnoRiferimento(annoRiferimento.trim());
								  }
								  if(!causaleServizio.trim().equals("")) {
									  tipoSpontaneo.setCausaleServizio(causaleServizio.trim());
								  }
								  if(!cespite.trim().equals("")) {
									  tipoSpontaneo.setCespite(cespite.trim());
								  }
								  pagamentoSpontaneo.setTipoSpontaneo(tipoSpontaneo);
		
								}
							}else {
								numeroAvvisoPagoPA = beanIV.getCodice_bollettino_premarcato_mav();
							}
							
							
							//inizio LP PG190220
							if(!bFaiNotifica) {
								printRow(myPrintingKeyPAG_SYSOUT, "inizio Notifica WS");
								//Preparo chiamata a WS Esterno Notifica 
								com.seda.payer.integraente.webservice.source.IntegraEnteNotificaSOAPBindingStub serviceIntegraEnteNotifica = new com.seda.payer.integraente.webservice.source.IntegraEnteNotificaSOAPBindingStub(new java.net.URL(model.getUrlPortale()), null);
								com.seda.payer.integraente.webservice.dati.NotificaRevocaPagamentoRequest notificaRevocaPagamentoRequest = new com.seda.payer.integraente.webservice.dati.NotificaRevocaPagamentoRequest();
								notificaRevocaPagamentoRequest.setCodiceTransazionePagonet(model.getChiaveTransazione());
								notificaRevocaPagamentoRequest.setCodiceDettaglioTransazionePagonet(beanIV.getChiave_transazione_dettaglio());//In update non serve aggiornare questo campo
								notificaRevocaPagamentoRequest.setCodiceEnte(model.getChiaveEnte().trim());
								notificaRevocaPagamentoRequest.setCodiceFiscale(model.getCodiceFiscale());
								notificaRevocaPagamentoRequest.setNumeroDocumento(model.getNumeroDocumento());
								
								notificaRevocaPagamentoRequest.setImportoPagato(model.getImportoPagato().longValue());
								SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
								notificaRevocaPagamentoRequest.setDataPagamento(sdf1.format(model.getDataPagamento().getTime()));
								if(pagamentoSpontaneo!=null)
									notificaRevocaPagamentoRequest.setPagamentoSpontaneo(pagamentoSpontaneo);
								else
									notificaRevocaPagamentoRequest.setNumeroAvvisoPagoPA(numeroAvvisoPagoPA);
									
								notificaRevocaPagamentoRequest.setRtXML(model.getXmlRicevuta());
								notificaRevocaPagamentoRequest.setRrXML(model.getXmlRichiestaRevoca());
								
								System.out.println("notificaRevocaPagamentoRequest.getCodiceDettaglioTransazionePagonet = " + notificaRevocaPagamentoRequest.getCodiceDettaglioTransazionePagonet());
								
								// metodo integraente
								responseRevoca = serviceIntegraEnteNotifica.notificaRevocaPagamento(notificaRevocaPagamentoRequest);
								//fine LP PG190220
								
								printRow(myPrintingKeyPAG_SYSOUT, "Notifica revoca pagamento esterno eseguita con esito: " + responseRevoca.getRisultato().getRetcode().getValue());	
								
								pagamentoSpontaneo = null;
								notificaRevocaPagamentoRequest = null;
								serviceIntegraEnteNotifica = null;
							} else {
							//fine LP PG190220
							//Preparo chiamata a WS Esterno (Sanità)
							com.seda.payer.integraente.webservice.source.IntegraEnteSanitaSOAPBindingStub serviceIntegraEnteSanita = new com.seda.payer.integraente.webservice.source.IntegraEnteSanitaSOAPBindingStub(new java.net.URL(model.getUrlPortale()), null);
							com.seda.payer.integraente.webservice.dati.NotificaPagamentoRequest notificaPagamentoRequest = new com.seda.payer.integraente.webservice.dati.NotificaPagamentoRequest();
							notificaPagamentoRequest.setCodiceTransazionePagonet(model.getChiaveTransazione());
							notificaPagamentoRequest.setCodiceDettaglioTransazionePagonet(beanIV.getChiave_transazione_dettaglio());//In update non serve aggiornare questo campo
							notificaPagamentoRequest.setCodiceEnte(model.getChiaveEnte().trim());
							notificaPagamentoRequest.setCodiceFiscale(model.getCodiceFiscale());
							notificaPagamentoRequest.setNumeroDocumento(model.getNumeroDocumento());
							
							notificaPagamentoRequest.setImportoPagato(model.getImportoPagato().longValue());
							SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
							notificaPagamentoRequest.setDataPagamento(sdf1.format(model.getDataPagamento().getTime()));
							if(pagamentoSpontaneo!=null)
								notificaPagamentoRequest.setPagamentoSpontaneo(pagamentoSpontaneo);				
							
							else
								notificaPagamentoRequest.setNumeroAvvisoPagoPA(numeroAvvisoPagoPA);
							
							if(!model.getUrlPortale().toLowerCase().contains("integraentesanita")) {
							  notificaPagamentoRequest.setIdentificativoUnivocoRiscossione(beanIV.getCodiceIUR());
							  notificaPagamentoRequest.setTassonomia(beanIV.getTassonomia());  
							  notificaPagamentoRequest.setIdentificativoUnivocoVersamento(beanIV.getNodoSpcIuv());
							}
							
							//PG22XX09_SB3 - inizio
							if(model.getUrlPortale().contains("jcitygov-pagopa")) {
								notificaPagamentoRequest.setIuvRpt(beanIV.getNodoSpcIuv());
							}
							//PG22XX09_SB3 - fine
							
							//inizio LP PG22XX07 - Bug RT non conformi per AGID
							//notificaPagamentoRequest.setRtXML(model.getXmlRicevuta());
							//forzatura UTF-8
							//notificaPagamentoRequest.setRtXML(new String(model.getXmlRicevuta().getBytes("UTF-8"), "UTF-8"));
							String RTXML = new String(model.getXmlRicevuta().getBytes("UTF-8"), "UTF-8");
							if(RTXML.indexOf("generate=\"PAGONET\"") != -1) {
								String iuv = beanIV.getNodoSpcIuv();
//								//Se auxdigit != 0  ==> iuv e' senza auxdigit di lunghezza 17
//					    		String iuv = numeroAvviso.substring(1);
//								//Se auxdigit == 0 ==> iuv e' senza applicationcode quindi di lungohezza 15
//					    		if(numeroAvviso.substring(0, 1).equals("0"))
//					    			iuv = iuv.substring(2);
								String tassonomia = beanIV.getTassonomia();
								System.out.println("inzio conversione RT con generate=\"PAGONET\"");
								RTXML = generateRT(model.getChiaveTransazione(), iuv, tassonomia, RTXML);
								if(bAggiornaRTErrate)
									model.setXmlRicevuta(RTXML);
							}
							notificaPagamentoRequest.setRtXML(RTXML);
							System.out.println("RT presente in DB e letta in java = " + notificaPagamentoRequest.getRtXML());
							//System.out.println("notificaPagamentoRequest.getRtXML() = " + notificaPagamentoRequest.getRtXML());
							
							//07102022 SB - inizio
							if(!model.getUrlPortale().toLowerCase().contains("integraentesanita")) {
								notificaPagamentoRequest.setIdentificativoUnivocoVersamento(beanIV.getNodoSpcIuv());
								notificaPagamentoRequest.setIdentificativoUnivocoRiscossione(beanIV.getCodiceIUR());
								notificaPagamentoRequest.setTassonomia(beanIV.getTassonomia());
							}
							//07102022 SB - fine
							
							System.out.println("notificaPagamentoRequest.getCodiceDettaglioTransazionePagonet = " + notificaPagamentoRequest.getCodiceDettaglioTransazionePagonet());

							//ini YLM PG22XX05_26 
				    		java.util.Date dataInizio = new java.util.Date();
				            Timestamp dIni = new Timestamp(dataInizio.getTime());
				            
				    		LogWin notificaLog = new LogWin();
				            String esitoLog = "";
				            String errorMessageLog = "";
				            String xmlInputLog = "";
				            String xmlOutputLog = "";
				            //fine YLM PG22XX05_26 
				            
				            
					        try {
					        	
					        	// inizio LM PAGONET-396
					        	xmlInputLog = getStringXMLofObject(notificaPagamentoRequest) != null ? getStringXMLofObject(notificaPagamentoRequest) :  "getStringXMLofObject(notificaPagamentoRequest) vuota";
					        	// fine LM PAGONET-396
					        	
								// metodo integraente
								//inizio LP PG190220
								//com.seda.payer.integraente.webservice.dati.NotificaPagamentoResponse response = serviceIntegraEnteSanita.notificaPagamento(notificaPagamentoRequest);
								response = serviceIntegraEnteSanita.notificaPagamento(notificaPagamentoRequest);
								//fine LP PG190220
								
								//ini YLM PG22XX05_26 
								esitoLog = response.getRisultato().getRetcode().getValue() + " : " + response.getRisultato().getRetmessage().toString(); 
								
						        //fine YLM PG22XX05_26 
					    		
								printRow(myPrintingKeyPAG_SYSOUT, "Notifica pagamento esterno eseguita con esito: " + response.getRisultato().getRetcode().getValue());	
								
								pagamentoSpontaneo = null;
								notificaPagamentoRequest = null;
								serviceIntegraEnteSanita = null;
							
							} catch (Exception e) {
								esitoLog= "KO : Exception";
								errorMessageLog = e.getMessage();
							} finally {

								//ini YLM PG22XX05_26 
								xmlOutputLog = response != null && getStringXMLofObject(response) != null ? getStringXMLofObject(response) : "Errore: mancanza xlm output";
								
								notificaLog.setDataInizioChiamata(dIni);
								notificaLog.setTipoChiamata("notificaPagamento");
								notificaLog.setOperatoreInserimento("notificaPagamentoBatch");
								notificaLog.setEsito(esitoLog);
								notificaLog.setMessaggioErrore(errorMessageLog);
								notificaLog.setXmlRequest(xmlInputLog);
								notificaLog.setXmlResponse(xmlOutputLog);
								
								
								inizializzaSalvaLoggingWinProcessNotificaPagamenti(response, notificaLog, model.getCodiceUtente());
						        //fine YLM PG22XX05_26 
							}
							
							//inizio LP PG190220
							}
							//fine LP PG190220
							
							//Aggiorno tabella delle notifiche
							try {
								long calInMillis = Calendar.getInstance().getTimeInMillis();
		//						1 IN I_NEX_KTRAKTRA VARCHAR(64),
		//						2 IN I_NEX_KTDTKTDT VARCHAR(64), 
		//						3 IN I_NEX_CSOCCSOC CHAR(5),
		//						4 IN I_NEX_CUTECUTE CHAR(5),
		//						5 IN I_NEX_KANEKENT CHAR(10),
		//						6 IN I_NEX_NNEXCORR INTEGER,
		//						7 IN I_NEX_CNEXPORT VARCHAR(256),
		//						8 IN I_NEX_CNEXNDOC VARCHAR(20),
		//						9 IN I_NEX_CNEXNAVV VARCHAR(18),
		//						10 IN I_NEX_CNEXCFIS VARCHAR(64),
		//						11 IN I_NEX_GNEXDNOT TIMESTAMP,
		//						12 IN I_NEX_GNEXDRNO TIMESTAMP,
		//						13 IN I_NEX_CNEXUESI CHAR(3),
		//						14 IN I_NEX_INEXIMPO DECIMAL(10 , 2),
		//						15 IN I_NEX_GNEXDPAG TIMESTAMP,
		//						16 IN I_NEX_CNEXRXML VARCHAR(20)
								//inizio LP PG190220    	
//						    	17 IN I_NEX_NNEXCOAN" INTEGER,
//						    	18 IN I_NEX_GNEXDIAN" TIMESTAMP,
//						    	19 IN I_NEX_GNEXDRAN" TIMESTAMP,	
//						    	20 IN I_NEX_CNEXUEAN" VARCHAR(2),
//						    	21 IN I_NEX_CNEXRRAN" CLOB(1M)
								//fine LP PG190220    	
								
								callableStatement = Helper.prepareCall(connection, schema, "PYNEXSP_UPD");
								callableStatement.setString(1, model.getChiaveTransazione());
								callableStatement.setString(2, model.getChiaveDettaglioTransazione());	//PG1800XX_014 GG //TODO
								callableStatement.setString(3, model.getCodiceSocieta());
								callableStatement.setString(4, model.getCodiceUtente());
								callableStatement.setString(5, model.getChiaveEnte());
								//inizio LP PG190220
								//callableStatement.setInt(6, 0);//0 => incrementa in automatico di 1
								if(bFaiNotifica) {
									callableStatement.setInt(6, 0);//0 => incrementa in automatico di 1
								} else {
									callableStatement.setInt(6, model.getNumeroTentativoNotifica());
								}
								//fine LP PG190220
								callableStatement.setString(7, model.getUrlPortale());
								callableStatement.setString(8, model.getNumeroDocumento());
								callableStatement.setString(9, numeroAvvisoPagoPA);
								callableStatement.setString(10, model.getCodiceFiscale());
								callableStatement.setNull(11, java.sql.Types.TIMESTAMP);
								//callableStatement.setTimestamp(12, new Timestamp(Calendar.getInstance().getTimeInMillis()));
								if(bFaiNotifica) {
									callableStatement.setTimestamp(12, new Timestamp(calInMillis));
									callableStatement.setString(13, response.getRisultato().getRetcode().getValue());
								} else {
									callableStatement.setNull(12, java.sql.Types.TIMESTAMP);
									callableStatement.setNull(13, java.sql.Types.VARCHAR);
								}
								callableStatement.setBigDecimal(14, model.getImportoPagato());
								callableStatement.setTimestamp(15, new Timestamp(model.getDataPagamento().getTimeInMillis()));
								callableStatement.setString(16, model.getXmlRicevuta());
								//inizio LP PG190220
								if(bFaiNotifica) {
									callableStatement.setInt(17, -1);
									callableStatement.setNull(18, java.sql.Types.TIMESTAMP);
									callableStatement.setNull(19, java.sql.Types.TIMESTAMP);
									callableStatement.setNull(20, java.sql.Types.VARCHAR);
									callableStatement.setString(21, model.getXmlRichiestaRevoca() != null ?  model.getXmlRichiestaRevoca() : "");
								} else {
									callableStatement.setInt(17, 0);//0 => incrementa in automatico di 1
									if(model.getDataInvioNotificaAnnullo() != null)
										callableStatement.setTimestamp(18, new Timestamp(model.getDataInvioNotificaAnnullo().getTimeInMillis()));
									else
										callableStatement.setTimestamp(18, new Timestamp(calInMillis));
									callableStatement.setTimestamp(19, new Timestamp(calInMillis));
									callableStatement.setString(20, responseRevoca.getRisultato().getRetcode().getValue());
									callableStatement.setString(21, model.getXmlRichiestaRevoca() != null ?  model.getXmlRichiestaRevoca() : "");
								}
								//fine LP PG190220
								callableStatement.execute();
								printRow(myPrintingKeyPAG_SYSOUT, "Elaborazione completata con successo");	
		
							}  catch (Exception e){
								e.printStackTrace();
								throw e;
							} 
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					listErroriNotificaPagamentiEsterni.add(listNotificaPagamentiEsterniEstratti.get(i)); //SVILUPPO_001_LUCAP_28.05.2020
					listErrors(listNotificaPagamentiEsterniEstratti.get(i).getChiaveTransazione(), String.format("Errore in Notifica a Portale Esterni per transazione: %s ;<br> Errore: %s ;",transazioneCorrente, e.getMessage()));  //PREJAVA18_LUCAP_04092020
					printRow(myPrintingKeyPAG_SYSOUT, "Errore in Notifica a Portale Esterni per transazione: " + transazioneCorrente);						
					printRow(myPrintingKeyPAG_SYSOUT, "Errore: " + e.getMessage());						
					totErroriNotifiche++;
				}
			}	
			
			listNotificaPagamentiEsterniEstratti = null;

			printRow(myPrintingKeyPAG_SYSOUT, "Elaborazione completata con successo");	
		} catch (Exception e) {
			e.printStackTrace();
			printRow(myPrintingKeyPAG_SYSOUT, "Elaborazione completata con errori");
			throw new Exception(e.getMessage());
		} finally {
			connection.commit();
			//inizio LP PG200060
			if(!(notificaPagamentiEsterniContext.getCodiceUtente().equals("000LP") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000RM"))) {
			//fine LP PG200060
			connection.setAutoCommit(true);
			//inizio LP PG200060
			}
			//fine LP PG200060
			connection.close();
			//connectionHost.close();
			
			//SVILUPPO_001_LUCAP_28.05.2020
			if(listErroriNotificaPagamentiEsterni.size()>0 
					&& notificaPagamentiEsterniContext.getEmailTo(notificaPagamentiEsterniContext.getCodiceUtente())!=null
					&& notificaPagamentiEsterniContext.getUrlMailSender(notificaPagamentiEsterniContext.getCodiceUtente())!=null) {
				segnalaErroriViaMail();
			}
			//FINE SVILUPPO_001_LUCAP_28.05.2020
			
		}
	}


	private RecuperaTransazioneResponseType recuperaTransazione(String chiaveTransazione) throws Exception {
    	RecuperaTransazioneResponseType sRes = null;
    	try {
			WSCache.initiatePgEcCommonsServer(notificaPagamentiEsterniContext.geturlPGEC(notificaPagamentiEsterniContext.getCodiceUtente()));
			RecuperaTransazioneRequestType req = new RecuperaTransazioneRequestType();
			req.setChiave_transazione(chiaveTransazione);
			if(notificaPagamentiEsterniContext.getCodiceUtente().equals("000BZ") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000P6"))
				sRes = WSCache.pgEcCommonsServer.recuperaTransazione("000P6", req);
			else if(notificaPagamentiEsterniContext.getCodiceUtente().equals("000S1") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000M1"))
				sRes = WSCache.pgEcCommonsServer.recuperaTransazione("000S1", req);
			else if(notificaPagamentiEsterniContext.getCodiceUtente().equals("SACDS") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000TR"))
				sRes = WSCache.pgEcCommonsServer.recuperaTransazione("000P5", req);
			else
				sRes = WSCache.pgEcCommonsServer.recuperaTransazione(notificaPagamentiEsterniContext.getCodiceUtente(), req);
			
    	} catch (Exception e) {
    		throw new Exception("Errore in recuperaTransazione: " + e.getMessage());
    	}
    	return sRes;
    }
	
	private ConfigPagamentoSingleResponse recuperaFunzioneEnte(ConfigPagamentoSingleRequest in) throws Exception {
		ConfigPagamentoSingleResponse sRes = null;
    	try {
			WSCache.initiatePgEcCommonsServer(notificaPagamentiEsterniContext.geturlPGEC(notificaPagamentiEsterniContext.getCodiceUtente()));
			
			if(notificaPagamentiEsterniContext.getCodiceUtente().equals("000BZ") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000P6"))
				sRes = WSCache.pgEcCommonsServer.recuperaFunzioneEnte("000P6", in);
			if(notificaPagamentiEsterniContext.getCodiceUtente().equals("000S1") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000M1"))
				sRes = WSCache.pgEcCommonsServer.recuperaFunzioneEnte("000S1", in);
			else if(notificaPagamentiEsterniContext.getCodiceUtente().equals("SACDS") || notificaPagamentiEsterniContext.getCodiceUtente().equals("000TR"))
				sRes = WSCache.pgEcCommonsServer.recuperaFunzioneEnte("000P5", in);
			else
				sRes = WSCache.pgEcCommonsServer.recuperaFunzioneEnte(notificaPagamentiEsterniContext.getCodiceUtente(), in);
				
    	} catch (Exception e) {
    		throw new Exception("Errore in recuperaFunzioneEnte: " + e.getMessage());
    	}
    	return sRes;
    }
	
	public void printRow(String printer, String row) {
		System.out.println(row);	
		if (classPrinting!=null)
			try {
				classPrinting.print(printer, row);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	public void printRow(String printer, String row, PrintCodes printCodes) {
		System.out.println(row);	
		if (classPrinting!=null)
			try {
				classPrinting.print(printer, row, printCodes);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	//SVILUPPO_001_LUCAP_28.05.2020
	public void segnalaErroriViaMail() { //da controllare
		String destinatarioMail = notificaPagamentiEsterniContext.getEmailTo(notificaPagamentiEsterniContext.getCodiceUtente());
		String oggettoMail=notificaPagamentiEsterniContext.getEmailSubject(notificaPagamentiEsterniContext.getCodiceUtente());
		String testoMail=notificaPagamentiEsterniContext.getEmailBody(notificaPagamentiEsterniContext.getCodiceUtente());
		for(int i=0; i<listErroriNotificaPagamentiEsterni.size(); i++) {
			testoMail += String.format("<br> Numero documento : %s ;<br> Numero avviso : %s ;<br> URL Portale : %s ;<br> XML Ricevuta : %s ;<br> %s;<br>",
					listErroriNotificaPagamentiEsterni.get(i).getNumeroDocumento().trim().equals("")?"Assente":listErroriNotificaPagamentiEsterni.get(i).getNumeroDocumento(), 
					listErroriNotificaPagamentiEsterni.get(i).getNumeroAvviso().trim().equals("")?"Assente":listErroriNotificaPagamentiEsterni.get(i).getNumeroAvviso(),
					listErroriNotificaPagamentiEsterni.get(i).getUrlPortale().trim().equals("")?"Assente":listErroriNotificaPagamentiEsterni.get(i).getUrlPortale(),
					listErroriNotificaPagamentiEsterni.get(i).getXmlRicevuta().trim().equals("")?"Assente":listErroriNotificaPagamentiEsterni.get(i).getXmlRicevuta(),
					listErrori.get(listErroriNotificaPagamentiEsterni.get(i).getChiaveTransazione())==null||listErrori.get(listErroriNotificaPagamentiEsterni.get(i).getChiaveTransazione()).trim().equals("")?"Dettaglio Errore Assente":listErrori.get(listErroriNotificaPagamentiEsterni.get(i).getChiaveTransazione()).trim()	//PREJAVA18_LUCAP_04092020
					);
		}
		String dbSchemaSocieta = notificaPagamentiEsterniContext.getCodiceSocieta(notificaPagamentiEsterniContext.getCodiceUtente());
		sendEMail("", destinatarioMail, "", "", oggettoMail, testoMail, "", dbSchemaSocieta);
	}
	
	public String sendEMail( String EMailSender, String EMailDataTOList, String EMailDataCCList,
			String EMailDataCCNList, String EMailDataSubject, String EMailDataText,
			String EMailDataAttacchedFileList, String dbSchemaSocieta) {
		EMailSenderResponse emsRes = null;
		EMailSenderRequestType emsBean = new EMailSenderRequestType();
		emsBean.setEMailSender(EMailSender);
		emsBean.setEmailCuteCute(dbSchemaSocieta);
		emsBean.setEMailDataTOList(EMailDataTOList);
		emsBean.setEMailDataCCList(EMailDataCCList);
		emsBean.setEMailDataCCNList(EMailDataCCNList);
		emsBean.setEMailDataSubject(EMailDataSubject);
		emsBean.setEMailDataText(EMailDataText);
		emsBean.setEMailDataAttacchedFileList(EMailDataAttacchedFileList);
		try {
			emsRes = (EMailSenderResponse)emsCaller.getEMailSender(emsBean);
		} catch(EMailSenderFaultType e) {
			System.out.println("Remote exception: " + e.getMessage());
			System.out.println("EMailSenderFaultType: " + e.getMessage1());
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
		return emsRes.getValue();
	}
	//SVILUPPO_001_LUCAP_28.05.2020
	
	//PREJAVA18_LUCAP_04092020
	private void listErrors(String chiaveTransazione, String Errore) {
		if(listErrori.containsKey(chiaveTransazione)) {
			listErrori.put(chiaveTransazione, listErrori.get(chiaveTransazione) + Errore);
		} else {
			listErrori.put(chiaveTransazione, Errore);
		}
	}
	//FINE PREJAVA18_LUCAP_04092020
	
	// inizio YML PG22XX04_02
	public void inizializzaSalvaLoggingWinProcessNotificaPagamenti( Object res, LogWin notificaLog , String modelCodUtente)  {

		CallableStatement callableStatement;
		ResultSet data = null;
		
		String codiceFiscale = "";
		String cutecute = "";
		String codiceEnte = "";
		String numeroBollettino = "";
		String codiceSocieta = "";

		try {
			
			java.util.Date dataFine = new java.util.Date();
			Timestamp dFin = new Timestamp(dataFine.getTime());
			
			if (res != null ) {
				Method[] methods = res.getClass().getMethods();
				
				for (int i = 0; i < methods.length; i++) { 
					Method m = methods[i];
	//				printRow(myPrintingKeyPAG_SYSOUT, "METODO: " + m.getName());
					
					if (m.getName() == "getCodiceSocieta" || m.getName() == "getCodice_societa") {
						
						Object c = m.invoke(res);
						codiceSocieta = c.toString().trim().length()>0 && c.toString() != null
								? c.toString().trim() 
								: "";
					}
	
					if (m.getName() == "getCodiceUtente" || m.getName() == "getCodice_utente") {
						
						Object c = m.invoke(res);
						cutecute =  c.toString().trim().length()>5 
								? c.toString().trim().substring(0, 5) 
								: c.toString().trim();
					}
	//				attenzione chiave a 5 cifre necessaria questo è il codice pubblico
					if (m.getName() == "getCodiceEnte" || m.getName() == "getChiave_ente_ent") {
						
						Object c = m.invoke(res);
						codiceEnte =  c.toString().trim().length()>0 && c.toString() != null
								? c.toString().trim()
								: "";
					}
					if (m.getName() == "getCodiceFiscale" || m.getName() == "getCodice_fiscale") {
						
						Object c = m.invoke(res);
						codiceFiscale =  c.toString().trim().length()>0 && c.toString() != null
								? c.toString().trim() 
								: "";
					}
					if (m.getName() == "getIdentificativoBollettino" || m.getName() == "getCodice_bollettino_premarcato_mav") {
						
						Object c = m.invoke(res);
						numeroBollettino =  c.toString().trim().length()>0 && c.toString() != null
								? c.toString().trim() 
								: "";
					}
				
				
				}
			} else {
				cutecute = modelCodUtente;
			}
		
			
			if ( codiceEnte != null && codiceEnte != "" && codiceEnte.length() == 10) {

				callableStatement = Helper.prepareCall(connection, schema, "PYANESP_SEL");
				callableStatement.setString(1, codiceEnte);
				
				if (callableStatement.execute()) {
				    data = callableStatement.getResultSet();
				    if (data.next())
				    	codiceEnte =  data.getString("ANE_CANECENT");
				}
				
				
			} else {
				// altrimenti controllo i parametri opzionali della req
				if ( notificaPagamentiEsterniContext.getCodiceEnte(notificaPagamentiEsterniContext.getCodiceUtente()) != null ) {
					codiceEnte = notificaPagamentiEsterniContext.getCodiceEnte(notificaPagamentiEsterniContext.getCodiceUtente());
				}
			}
			
			
			LogWin logWin = new LogWin();
			logWin.setTipoChiamata(notificaLog.getTipoChiamata());
			logWin.setCodiceUtente(cutecute);
			logWin.setCodiceSocieta(codiceSocieta);
			logWin.setCodiceEnte(codiceEnte);
			logWin.setIdDominio("");
			logWin.setBollettino(numeroBollettino);
			logWin.setCodiceFiscale(codiceFiscale);
			logWin.setXmlRequest(notificaLog.getXmlRequest());
			logWin.setXmlResponse(notificaLog.getXmlResponse());
			logWin.setDataInizioChiamata(notificaLog.getDataInizioChiamata());
			logWin.setDataFineChiamata(dFin);
			logWin.setMessaggioErrore(notificaLog.getMessaggioErrore() != null && notificaLog.getMessaggioErrore().trim().length() > 0? notificaLog.getMessaggioErrore().trim() : "");
			logWin.setEsito(notificaLog.getEsito());
			logWin.setOperatoreInserimento("notificaPagamentoBatch");
			logWin.setDbSchemaCodSocieta(cutecute);

			LoggerUtil winLogger = new LoggerUtil();
			winLogger.saveWinLog(logWin, configTree);
		    
		}  
		catch (Exception e) {
		    
		    e.printStackTrace();
		    printRow(myPrintingKeyPAG_SYSOUT, "Errore in saveWinLog: " + e.getMessage());	
		}catch (Throwable e) {
		    
		    e.printStackTrace();
		    printRow(myPrintingKeyPAG_SYSOUT, "Errore in saveWinLog: " + e.getMessage());	
		} 
	}
	
	// fine YML PG22XX04_02
	
	
	//inizio LP PG22XX07
	private String getTagToXml(String xmlin, String tag) {
    	String init = "<" + tag + ">";
    	String end = "</" + tag + ">";
    	int inizio = xmlin.indexOf(init); 
    	int fine = xmlin.indexOf(end); 
		if(inizio != -1 && fine != -1) {
			String id = xmlin.substring(inizio + init.length(), fine);
			return id;
		}
    	return null;
	}

	private String generateRT(String chiaveTra, String codiceIuv, String tassonomia, String rt)
	{
		String idDominio = getTagToXml(rt, "identificativoDominio");
		String codiceEsitoPagamento = getTagToXml(rt, "codiceEsitoPagamento");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String riferimentoMessaggioRichiesta = "PG" + dateFormat.format(Calendar.getInstance().getTime());
		String esitoSingoloPagamento = 	codiceEsitoPagamento.equals("0") ? "ESEGUITO" : "NON ESEGUITO";
		String dataEsitoSingoloPagamento = getTagToXml(rt, "dataEsitoSingoloPagamento");
		String dataEsitoSingoloPagamentoShort = dataEsitoSingoloPagamento.trim().length() > 10 ? dataEsitoSingoloPagamento.trim().substring(0,10) : dataEsitoSingoloPagamento.trim();
		String rtOut = rt;

		rtOut = rtOut.replaceFirst("generate=\"PAGONET\"", "xmlns=\"http://www.digitpa.gov.it/schemas/2011/Pagamenti/\"");
		int ik = rtOut.indexOf("<dominio>");
		if(rtOut.indexOf("<versioneOggetto>") == -1)
			rtOut = rtOut.substring(0, ik) + "<versioneOggetto>6.2.0</versioneOggetto>" + rtOut.substring(ik); 
		//ik = rtOut.indexOf("<identificativoMessaggioRicevuta>");
		//rtOut = rtOut.substring(0, ik) + "<riferimentoMessaggioRichiesta>" + riferimentoMessaggioRichiesta + "</riferimentoMessaggioRichiesta>" + rtOut.substring(ik);
		ik = rtOut.indexOf("</dataOraMessaggioRicevuta>");
		if(rtOut.indexOf("<riferimentoMessaggioRichiesta>") == -1) {
			rtOut = rtOut.substring(0, ik+27) + "<riferimentoMessaggioRichiesta>" + riferimentoMessaggioRichiesta + "</riferimentoMessaggioRichiesta>" + rtOut.substring(ik+27);
		}
		if (!dataEsitoSingoloPagamento.equals(dataEsitoSingoloPagamentoShort)) {
			rtOut = rtOut.replaceAll("<dataEsitoSingoloPagamento>"+dataEsitoSingoloPagamento+"</dataEsitoSingoloPagamento>", "<dataEsitoSingoloPagamento>"+dataEsitoSingoloPagamentoShort+"</dataEsitoSingoloPagamento>");
		}
		//20220707 GG - inizio sistemazione posizionamento tag
		String identificativoMessaggioRicevuta = getTagToXml(rt, "identificativoMessaggioRicevuta");
		String riferimentoDataRichiesta = getTagToXml(rt, "riferimentoDataRichiesta");
		int jk = rtOut.indexOf("</dominio>");
		int ak = rtOut.indexOf("<identificativoMessaggioRicevuta>");
		int bk = rtOut.indexOf("<riferimentoDataRichiesta>");
		if (bk < ak) {
			rtOut = rtOut.replaceAll("<identificativoMessaggioRicevuta>"+identificativoMessaggioRicevuta+"</identificativoMessaggioRicevuta>", "");
			rtOut = rtOut.replaceAll("<riferimentoDataRichiesta>"+riferimentoDataRichiesta+"</riferimentoDataRichiesta>", "");
			rtOut = rtOut.substring(0, jk+10) + "<identificativoMessaggioRicevuta>" + identificativoMessaggioRicevuta + "</identificativoMessaggioRicevuta>" + rtOut.substring(jk+10);
			jk = rtOut.indexOf("<istitutoAttestante>");
			rtOut = rtOut.substring(0, jk) + "<riferimentoDataRichiesta>" + riferimentoDataRichiesta + "</riferimentoDataRichiesta>" + rtOut.substring(jk);
		}
		//20220707 GG - fine sistemazione posizionamento tag
		rtOut = rtOut.replaceAll("codiceContestoPagamento", "CodiceContestoPagamento");
		
		rtOut = rtOut.replaceAll("<esitoSingoloPagamento>PAGAMENTO ESEGUITO NUOVE FUNZIONI PAGOPA</esitoSingoloPagamento>",
				                 "<esitoSingoloPagamento>" + esitoSingoloPagamento + "</esitoSingoloPagamento>");
		
		if(rtOut.indexOf("<datiSpecificiRiscossione>") == -1) {
			ik = rtOut.indexOf("</causaleVersamento>") + "</causaleVersamento>".length();
			rtOut = rtOut.substring(0, ik) + "<datiSpecificiRiscossione>9/" + tassonomia + "/</datiSpecificiRiscossione>" + rtOut.substring(ik);
		}
		
		if(rtOut.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") != -1)
			rtOut = rtOut.replaceFirst(" encoding=\"UTF-8\"", " encoding=\"UTF-8\" standalone=\"yes\"");
		else
			rtOut = rtOut.replaceFirst(" standalone=\"no\"", " standalone=\"yes\"");
		System.out.println("RT XML post correzione: \n" + rtOut);
		
		if(bAggiornaRTErrate && nodoSpcDao != null) {
			try {
				List<NodoSpcRpt> lst = nodoSpcDao.recuperaRPT(null, chiaveTra, codiceIuv, null, idDominio, null);
				NodoSpcRpt oldRPT = lst.get(0);
				String oldRT = new String(oldRPT.getRt().getBytes("UTF-8"), "UTF-8");
				if(oldRT.indexOf("generate=\"PAGONET\"") != -1) {
					oldRPT.setRt(rtOut);
					nodoSpcDao.updateRptNodoSpc(oldRPT);
				}
			} catch (DaoException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return rtOut;
	}
	//fine LP PG22XX07
}