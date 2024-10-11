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
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		NotificaPagamentiEsterniCore core = new NotificaPagamentiEsterniCore();
		DataSource datasource = null;
		DataSourceFactoryImpl dataSourceFactory = new DataSourceFactoryImpl();
		Properties dsProperties= new Properties();
		String schema ="SE000SV";
		String cutecute = "000TO";
		if(args.length == 0 || args[0].equalsIgnoreCase("db2")) {
			dsProperties.put(DAOHelper.JDBC_DRIVER, "com.ibm.db2.jcc.DB2Driver");
			dsProperties.put(DAOHelper.JDBC_URL, "jdbc:db2://svdbdb201.seda.intra:60000/PAY00DB0:retrieveMessagesFromServerOnGetMessage=true;");
			dsProperties.put(DAOHelper.JDBC_USER, "SE000SV");
			dsProperties.put(DAOHelper.JDBC_PASSWORD, "SV!L09SE");
		} else if(args.length == 1 && args[0].equalsIgnoreCase("mysql")) {
			schema = "PAY00DB0_SE000SV";
			dsProperties.put(DAOHelper.JDBC_DRIVER, "com.mysql.jdbc.Driver");
			dsProperties.put(DAOHelper.JDBC_URL, "jdbc:mysql://10.10.82.181:3306/PAY00DB0_SE000SV");
			dsProperties.put(DAOHelper.JDBC_USER, "admin");
			dsProperties.put(DAOHelper.JDBC_PASSWORD, "$myroot");
			dsProperties.put("autocommit", "true");		//TODO da verificare
		} else {
			schema ="se00000";
			dsProperties.put(DAOHelper.JDBC_DRIVER, "org.postgresql.Driver");
			dsProperties.put(DAOHelper.JDBC_URL, "jjdbc:postgresql://10.10.75.120:5432/pay00db0");
			dsProperties.put(DAOHelper.JDBC_USER, "se00000");
			dsProperties.put(DAOHelper.JDBC_PASSWORD, "B4PC8UT$");	
		}
		dataSourceFactory.setProperties(dsProperties);
		datasource = dataSourceFactory.getDataSource();
		
		ClassPrinting classPrinting = null;
		
		Logger logger = Logger.getLogger(NotificaPagamentiEsterniCore.class);
		String jobId = "";
		String[] params = new String[] { 
				//"CONFIGPATH      C:\\work\\giacani_pagonet\\ConfigFiles\\NotificaPagamentiEsterniBatch\\notificapagamentiesternibatch.properties"
				"CONFIGPATH      E:\\ConfigFiles\\NotificaPagamentiEsterniBatch\\notificapagamentiesternibatch" + ((args.length > 0 && args[0].equalsIgnoreCase("postgres")) ? "postgres" : "") + ".properties"
				,"CUTECUTE      " + cutecute
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
