package com.flex.adapter.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.client.AbstractWSClient;
import com.flex.adapter.constants.AppConstants;

import com.flextronics.services.ewi.client.configuration.ActiveProcessFilterWs;
import com.flextronics.services.ewi.client.configuration.ActiveProcessResponse;
import com.flextronics.services.ewi.client.configuration.ConfigurationFilterWs;
import com.flextronics.services.ewi.client.configuration.ConfigurationPort;
import com.flextronics.services.ewi.client.configuration.ConfigurationResponse;
import com.flextronics.services.ewi.client.configuration.FtpFilterWs;
import com.flextronics.services.ewi.client.configuration.FtpServerResponse;
import com.flextronics.services.ewi.client.configuration.FwAttributeWs;
import com.flextronics.services.ewi.client.configuration.FwAttributesResponse;
import com.flextronics.services.ewi.client.configuration.ResetPassResponse;
import com.flextronics.services.ewi.client.configuration.SolutionRoleWs;
import com.flextronics.services.ewi.client.faults.InvalidRequestFault;
import com.flextronics.services.ewi.client.faults.ServiceFault_Exception;

public class ConfigurationWsClient extends AbstractWSClient {

	private Logger LOG = LoggerFactory.getLogger(ConfigurationWsClient.class);
	private String messageException = new String();
	private ConfigurationPort configurationPort;
	public static final String WS_NAME = "ConfigurationWebService?wsdl";

	public ConfigurationWsClient() {

	}

	public ConfigurationWsClient(String envUrl) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.setServiceClass(ConfigurationPort.class);
		factory.setAddress(envUrl + WS_NAME);
		if (LOG.isDebugEnabled()) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		configurationPort = (ConfigurationPort) factory.create();

		configurePortTimeOut(AppConstants.WS_REQUEST_TIMEOUT, configurationPort);

		configureSecureAccess(configurationPort, envUrl);
	}

	public ConfigurationResponse getConfigurations(ConfigurationFilterWs configurationFilter, String securityToken)
			throws Exception {

		ConfigurationResponse response = null;

		try {

			response = configurationPort.getConfigurations(configurationFilter, securityToken);

		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to getConfigurations " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		}
		return response;
	}

	public ConfigurationPort getConfigurationPort() {
		return configurationPort;
	}

	public void setConfigurationPort(ConfigurationPort configurationPort) {
		this.configurationPort = configurationPort;
	}

	public ActiveProcessResponse findActiveProcessBySolutionRole(ActiveProcessFilterWs activeProcessFilters,
			String securityToken) throws Exception {

		ActiveProcessResponse response = null;

		try {

			response = configurationPort.findActiveProcessBySolutionRole(activeProcessFilters, securityToken);

		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to findActiveProcessBySolutionRole " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		}
		return response;

	}

	public FtpServerResponse findFtpServer(FtpFilterWs ftpFilters, String securityToken) throws Exception {

		FtpServerResponse response = null;

		try {

			response = configurationPort.findFtpServer(ftpFilters, securityToken);

		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to findActiveProcessBySolutionRole " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		}
		return response;
	}

	public List<SolutionRoleWs> findUserRoles(String username, String solutionCode) throws Exception {
		List<SolutionRoleWs> solutionRoles = null;
		try {
			solutionRoles = configurationPort.findUserRoles(username, solutionCode).getUserRolesWs();
		} catch (InvalidRequestFault e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		} catch (ServiceFault_Exception e) {
			messageException = "Invalid data send to findUserRoles " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		}

		return solutionRoles;
	}

	public List<FwAttributeWs> findFwAttributes(String attributeTypeCode, String attributeDefCode, String securityToken)
			throws Exception {

		FwAttributesResponse response = new FwAttributesResponse();
		List<FwAttributeWs> attributeList = new ArrayList<FwAttributeWs>();

		try {
			response = configurationPort.findFwAttributes(attributeTypeCode, attributeDefCode, securityToken);
			attributeList = response.getFwAttributeList();
		} catch (InvalidRequestFault e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		} catch (ServiceFault_Exception e) {
			messageException = "Invalid data send to findUserRoles " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		}

		return attributeList;
	}

	public ResetPassResponse resetPassword(String userName, String password, String securityToken) throws Exception {
		ResetPassResponse response = new ResetPassResponse();

		try {
			response = configurationPort.resetPassword(userName, password, securityToken);
		} catch (InvalidRequestFault e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		} catch (ServiceFault_Exception e) {
			messageException = "Invalid data send to resetPassword " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(messageException, e);
		}

		return response;

	}

}
