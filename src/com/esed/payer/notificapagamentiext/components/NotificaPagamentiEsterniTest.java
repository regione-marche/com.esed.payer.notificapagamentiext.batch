package com.esed.payer.notificapagamentiext.components;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.esed.payer.notificapagamentiext.config.NotificaPagamentiEsterniResponse;
import com.seda.bap.components.core.BapException;
import com.seda.bap.components.core.spi.ClassPrinting;
import com.seda.data.dao.DAOHelper;
import com.seda.data.datasource.DataSourceFactoryImpl;


public class NotificaPagamentiEsterniTest {
	public static void main(String[] args) {
		NotificaPagamentiEsterniCore core = new NotificaPagamentiEsterniCore();
		DataSource datasource = null;
		DataSourceFactoryImpl dataSourceFactory = new DataSourceFactoryImpl();
		Properties dsProperties= new Properties();
		dsProperties.put(DAOHelper.JDBC_DRIVER, "com.ibm.db2.jcc.DB2Driver");
		dsProperties.put(DAOHelper.JDBC_URL, "jdbc:db2://10.10.75.135:60000/PAY00DB0:retrieveMessagesFromServerOnGetMessage=true;");
		dsProperties.put(DAOHelper.JDBC_USER, "SE000SV");
		dsProperties.put(DAOHelper.JDBC_PASSWORD, "SV!L09SE");
		dsProperties.put("autocommit", "true");		//TODO da verificare
		dataSourceFactory.setProperties(dsProperties);
		datasource = dataSourceFactory.getDataSource();
		
		
		String schema ="SE000SV";
		ClassPrinting classPrinting = null;
		
		Logger logger = Logger.getLogger(NotificaPagamentiEsterniCore.class);
		String jobId = "";
		String[] params = new String[]{ 
				"CONFIGPATH      D:/ConfigFiles/Payer/pennacchietti/NotificaPagamentiEsterniBatch/notificapagamentiesternibatch.properties"
				,"CUTECUTE      000TO"
//				,"CODENTE      99999"
				};
		
		NotificaPagamentiEsterniResponse res; 
		
		try {
			//res = core.run(params, datasource, schema, classPrinting, logger, jobId);
			res = core.run(params, null, null, classPrinting, logger, jobId);
			if (!res.getCode().equals("00")) {
				System.exit(1);
			} else {
				System.exit(0);
			}
		} catch (BapException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
		
}
