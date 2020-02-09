package com.flex.adapter.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.constants.AppConstants;
import com.flextronics.services.ewi.client.commandcenter.BusinessUnitCompleteWs;
import com.flextronics.services.ewi.client.commandcenter.CommandCenterPort;
import com.flextronics.services.ewi.client.commandcenter.FindBusinessUnitResp;
import com.flextronics.services.ewi.client.commandcenter.FindLineResp;
import com.flextronics.services.ewi.client.common.FilterList;
import com.flextronics.services.ewi.client.faults.InvalidRequestFault;
import com.flextronics.services.ewi.client.faults.ServiceFault_Exception;

public class CommandCenterWsClient extends AbstractWSClient {

	private Logger LOG = LoggerFactory.getLogger(CommandCenterWsClient.class);
	private String messageException = new String();
	private CommandCenterPort commandCenterPort;
	public static final String WS_NAME = "CommandCenterWebService?wsdl";

	public CommandCenterWsClient() {

	}

	public CommandCenterWsClient(String envUrl) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.setServiceClass(CommandCenterPort.class);
		factory.setAddress(envUrl + WS_NAME);
		if (LOG.isDebugEnabled()) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		commandCenterPort = (CommandCenterPort) factory.create();

		configurePortTimeOut(AppConstants.WS_REQUEST_TIMEOUT, commandCenterPort);

		configureSecureAccess(commandCenterPort, envUrl);

	}

	// **************************************************************************
	// BusinessUnit
	// **************************************************************************

	public List<BusinessUnitCompleteWs> findBusinessUnit(FilterList filters, String securityToken) {
		FindBusinessUnitResp response = new FindBusinessUnitResp();
		List<BusinessUnitCompleteWs> businessUnitList = new ArrayList<BusinessUnitCompleteWs>();

		try {

			response = commandCenterPort.findBusinessUnit(filters, securityToken);
			businessUnitList = response.getBusinessUnitList();

		} catch (ServiceFault_Exception e) {
			messageException = "Service exception findBusinessUnit " + e.getMessage();
			LOG.error(messageException, e);

		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to findBusinessUnit " + e.getMessage();
			LOG.error(messageException, e);
		}

		return businessUnitList;
	}

	// **************************************************************************
	// Lines
	// **************************************************************************

	public FindLineResp findLine(FilterList filters, String securityToken) {

		FindLineResp response = new FindLineResp();

		try {

			response = commandCenterPort.findLine(filters, securityToken);

		} catch (ServiceFault_Exception e) {
			messageException = "Service exception findBusinessUnit " + e.getMessage();
			LOG.error(messageException, e);

		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to findBusinessUnit " + e.getMessage();
			LOG.error(messageException, e);
		}

		return response;

	}

}
