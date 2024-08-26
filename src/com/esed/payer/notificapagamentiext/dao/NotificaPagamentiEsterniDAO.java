package com.esed.payer.notificapagamentiext.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import com.esed.payer.notificapagamentiext.config.NotificaPagamentiEsterniContext;
import com.esed.payer.notificapagamentiext.model.NotificaPagamentiEsterniModel;
import com.seda.data.helper.HelperException;
import com.seda.payer.core.handler.BaseDaoHandler;

public class NotificaPagamentiEsterniDAO extends BaseDaoHandler {  

	protected CallableStatement callableStatemenLstNEX = null;
	protected CallableStatement callableStatemenUpdNEX = null;
	protected CallableStatement callableStatementSelANE = null;

	Properties attributes = new Properties();

	public int getIntegerAttribute(String name) {
		return Integer.parseInt(attributes.getProperty(name));
	}

	public String getStringAttribute(String name) {
		return attributes.getProperty(name);
	}
	
	public NotificaPagamentiEsterniDAO(Connection connection, String schema){
		super(connection, schema);
	}
	
	public void updateNotificaPagamentiEsterni (
			NotificaPagamentiEsterniModel model,
			String numeroAvvisoPagoPA,
			long calInMillis,
			String resValue,
			String resRevocaValue,
			boolean bFaiNotifica
	) throws Exception {

		CallableStatement callableStatement = null;
		try { 
			if (callableStatemenUpdNEX == null) {
				callableStatemenUpdNEX = prepareCall(false, "PYNEXSP_UPD");
			}
			callableStatement = callableStatemenUpdNEX; 
			callableStatement.setString(1, model.getChiaveTransazione());
			callableStatement.setString(2, model.getChiaveDettaglioTransazione());	//PG1800XX_014 GG
			callableStatement.setString(3, model.getCodiceSocieta());
			callableStatement.setString(4, model.getCodiceUtente());
			callableStatement.setString(5, model.getChiaveEnte());
			if(bFaiNotifica) {
				callableStatement.setInt(6, 0);//0 => incrementa in automatico di 1
			} else {
				callableStatement.setInt(6, model.getNumeroTentativoNotifica());
			}
			callableStatement.setString(7, model.getUrlPortale());
			callableStatement.setString(8, model.getNumeroDocumento());
			callableStatement.setString(9, numeroAvvisoPagoPA);
			callableStatement.setString(10, model.getCodiceFiscale());
			callableStatement.setNull(11, java.sql.Types.TIMESTAMP);
			if(bFaiNotifica) {
				callableStatement.setTimestamp(12, new Timestamp(calInMillis));
				callableStatement.setString(13, resValue);
			} else {
				callableStatement.setNull(12, java.sql.Types.TIMESTAMP);
				callableStatement.setNull(13, java.sql.Types.VARCHAR);
			}
			callableStatement.setBigDecimal(14, model.getImportoPagato());
			callableStatement.setTimestamp(15, new Timestamp(model.getDataPagamento().getTimeInMillis()));
			callableStatement.setString(16, model.getXmlRicevuta());
			if(bFaiNotifica) {
				callableStatement.setInt(17, -1);
				callableStatement.setNull(18, java.sql.Types.TIMESTAMP);
				callableStatement.setNull(19, java.sql.Types.TIMESTAMP);
				callableStatement.setNull(20, java.sql.Types.VARCHAR);
				callableStatement.setString(21, model.getXmlRichiestaRevoca() != null ?  model.getXmlRichiestaRevoca() : "");
			} else {
				callableStatement.setInt(17, 0);//0 => incrementa in automatico di 1
				if(model.getDataInvioNotificaAnnullo() != null) {
					callableStatement.setTimestamp(18, new Timestamp(model.getDataInvioNotificaAnnullo().getTimeInMillis()));
				} else {
					callableStatement.setTimestamp(18, new Timestamp(calInMillis));
				}
				callableStatement.setTimestamp(19, new Timestamp(calInMillis));
				callableStatement.setString(20, resRevocaValue);
				callableStatement.setString(21, model.getXmlRichiestaRevoca() != null ?  model.getXmlRichiestaRevoca() : "");
			}
			callableStatement.execute();
		} catch (SQLException e) {
			throw new Exception(e);
		} catch (IllegalArgumentException e) {
			throw new Exception(e);
		} catch (HelperException e) {
			throw new Exception(e);
		} finally {
		}
	}
	
	public ResultSet listNotificaPagamentiEsterni(NotificaPagamentiEsterniContext notificaPagamentiEsterniContext) throws Exception {
		CallableStatement callableStatement;
		ResultSet resultSet = null;
		try {
			if(callableStatemenLstNEX == null) {
				callableStatemenLstNEX = prepareCall(false, "PYNEXSP_LST_BATCH");
			}
			callableStatement = callableStatemenLstNEX;
			callableStatement.setString(1, notificaPagamentiEsterniContext.getCodiceSocieta(notificaPagamentiEsterniContext.getCodiceUtente()));//				IN I_NEX_CSOCCSOC CHAR(5),
			callableStatement.setString(2, notificaPagamentiEsterniContext.getCodiceUtente());													//				IN I_NEX_CUTECUTE CHAR(5),
			callableStatement.setString(3, notificaPagamentiEsterniContext.getCodiceEnte(notificaPagamentiEsterniContext.getCodiceUtente()));	//				IN I_NEX_KANEKENT CHAR(10),
			callableStatement.setString(4, "");																									//				IN I_NEX_KTRAKTRA VARCHAR(64),
			callableStatement.setString(5, "");																									//				IN I_NEX_KTDTKTDT VARCHAR(64), --PG180110 FB
			callableStatement.setInt(6, notificaPagamentiEsterniContext.getMaxTentativi(notificaPagamentiEsterniContext.getCodiceUtente()));	//				IN I_NEX_NNEXCORR_MAX INTEGER,
			callableStatement.setString(7, ""); 																								//				IN I_NEX_CNEXPORT VARCHAR(256),
			callableStatement.setString(8, "");																									//				IN I_NEX_CNEXNDOC VARCHAR(20),
			callableStatement.setString(9, "");																									//				IN I_NEX_CNEXNAVV VARCHAR(18),
			callableStatement.setString(10, "");																								//				IN I_NEX_CNEXCFIS VARCHAR(64)	
			if(callableStatement.execute()) {
				resultSet = callableStatement.getResultSet(); 
			}
		}  catch (Exception e){
			e.printStackTrace();
			throw e;
		} finally {
		}
		return resultSet;
	}
	
	public String selAnagraficaEnti(String codiceEnte) {
		CallableStatement callableStatement;
		String ret = null;
		try {
			if(callableStatementSelANE ==  null) {
				callableStatementSelANE = prepareCall(false, "PYANESP_SEL");
			}
			callableStatement = callableStatementSelANE; 
			callableStatement.setString(1, codiceEnte);
			if (callableStatement.execute()) {
				ResultSet data = callableStatement.getResultSet();
			    if (data.next())
			    	ret =  data.getString("ANE_CANECENT");
			}
		}  catch (Exception e){
			e.printStackTrace();
			return null;
		} finally {
		}
		return ret;
	}
	
	@Override
	public void destroy() {
		if(callableStatemenUpdNEX !=  null) {
			try {
				callableStatemenUpdNEX.close();
			} catch (Exception e) {
			}
		}
		if(callableStatemenLstNEX !=  null) {
			try {
				callableStatemenLstNEX.close();
			} catch (Exception e) {
			}
		}
		if(callableStatementSelANE !=  null) {
			try {
				callableStatementSelANE.close();
			} catch (Exception e) {
			}
		}
	}	
}