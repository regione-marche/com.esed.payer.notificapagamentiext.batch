package com.esed.payer.notificapagamentiext.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import com.esed.payer.notificapagamentiext.config.NotificaPagamentiEsterniContext;
import com.esed.payer.notificapagamentiext.model.NotificaPagamentiEsterniModel;
import com.seda.data.dao.DAOHelper;
import com.seda.data.helper.HelperException;
import com.seda.payer.core.handler.BaseDaoHandler;

public class NotificaPagamentiEsterniDAO extends BaseDaoHandler {  
	//inizio LP 20241002 - PGNTBNPE-1
	//protected CallableStatement callableStatemenLstNEX = null;
	//protected CallableStatement callableStatemenUpdNEX = null;
	//protected CallableStatement callableStatementSelANE = null;
	//fine LP 20241002 - PGNTBNPE-1

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
			callableStatement = prepareCall(false, "PYNEXSP_UPD"); 	//inizio LP 20241002 - PGNTBNPE-1
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
			DAOHelper.closeIgnoringException(callableStatement); //LP 20241002 - PGNTBNPE-1
		}
	}
	
	public ResultSet listNotificaPagamentiEsterni(NotificaPagamentiEsterniContext notificaPagamentiEsterniContext) throws Exception {
		CallableStatement callableStatement = null; //LP 20241002 - PGNTBNPE-1
		ResultSet resultSet = null;
		try {
			callableStatement = prepareCall(false, "PYNEXSP_LST_BATCH"); //LP 20241002 - PGNTBNPE-1
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
			DAOHelper.closeIgnoringException(callableStatement); //LP 20241002 - PGNTBNPE-1
		}
		return resultSet;
	}
	
	public String selAnagraficaEnti(String codiceEnte) {
		//inizio LP 20241002 - PGNTBNPE-1
		CallableStatement callableStatement = null;
		ResultSet data = null;
		//fine LP 20241002 - PGNTBNPE-1
		String ret = null;
		try {
			callableStatement = prepareCall(false, "PYANESP_SEL"); //LP 20241002 - PGNTBNPE-1
			callableStatement.setString(1, codiceEnte);
			if (callableStatement.execute()) {
				data = callableStatement.getResultSet(); //LP 20241002 - PGNTBNPE-1
			    if (data.next())
			    	ret =  data.getString("ANE_CANECENT");
			}
		}  catch (Exception e){
			e.printStackTrace();
			return null;
		} finally {
			//inizio LP 20241002 - PGNTBNPE-1
			DAOHelper.closeIgnoringException(data);
			DAOHelper.closeIgnoringException(callableStatement);
			//fine LP 20241002 - PGNTBNPE-1
		}
		return ret;
	}
}