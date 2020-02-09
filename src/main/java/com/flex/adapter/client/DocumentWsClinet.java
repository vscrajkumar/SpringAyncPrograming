package com.flex.adapter.client;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.constants.AppConstants;
import com.flextronics.services.ewi.client.document.CreateDocumentResponseWs;
import com.flextronics.services.ewi.client.document.DocumentFiltersWs;
import com.flextronics.services.ewi.client.document.DocumentPort;
import com.flextronics.services.ewi.client.document.DocumentWsComplete;
import com.flextronics.services.ewi.client.document.ServiceCheckAndRemoveEPIDocumentsByConfigResponseWs;
import com.flextronics.services.ewi.client.document.UpdateDocumentResponseWs;

public class DocumentWsClinet extends AbstractWSClient {
	private Logger LOG = LoggerFactory.getLogger(DocumentWsClinet.class);
	private String messageException = new String();
	private DocumentPort documentPort;
	public static final String WS_NAME = "DocumentWebService?wsdl";

	public DocumentWsClinet() {

	}

	public DocumentWsClinet(String envUrl) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.setServiceClass(DocumentPort.class);
		factory.setAddress(envUrl + WS_NAME);
		if (LOG.isDebugEnabled()) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		documentPort = (DocumentPort) factory.create();

		configurePortTimeOut(AppConstants.WS_REQUEST_TIMEOUT, documentPort);

		configureSecureAccess(documentPort, envUrl);
	}

	public ServiceCheckAndRemoveEPIDocumentsByConfigResponseWs checkAndRemoveEPIDocumentsByConfig(
			DocumentFiltersWs documentFilters, String securityToken) throws Exception {

		ServiceCheckAndRemoveEPIDocumentsByConfigResponseWs response = null;

		try {
			response = documentPort.serviceCheckAndRemoveEPIDocumentsByConfig(documentFilters, securityToken);
		} catch (Exception e) {
			messageException = "Service exception serviceCheckAndRemoveEPIDocumentsByConfig " + e.getMessage();
			LOG.error(messageException);
		}

		return response;
	}

	public CreateDocumentResponseWs createDocument(DocumentWsComplete documentWsComplete, String securityToken)
			throws Exception {
		CreateDocumentResponseWs createResponse = null;
		try {
			createResponse = documentPort.createDocument(documentWsComplete, securityToken);
		} catch (Exception e) {
			messageException = "Error ServiceFault_Exception while saving the  Document  information:  "
					+ e.getMessage();
			LOG.error(messageException);
		}
		return createResponse;
	}

	public DocumentPort getDocumentPort() {
		return documentPort;
	}

	public void setDocumentPort(DocumentPort documentPort) {
		this.documentPort = documentPort;
	}

	public UpdateDocumentResponseWs updateDocumentDates(DocumentWsComplete documentWsComplete, String securityToken)
			throws Exception {

		UpdateDocumentResponseWs updateDocumentResponseWs = null;

		try {
			updateDocumentResponseWs = documentPort.updateDocumentDates(documentWsComplete, securityToken);

		} catch (Exception e3) {
			messageException = "Error updating effctivity and expiry dates: " + e3.getMessage();
			LOG.error(messageException);
			throw new Exception(messageException, e3);
		}

		return updateDocumentResponseWs;

	}

}
