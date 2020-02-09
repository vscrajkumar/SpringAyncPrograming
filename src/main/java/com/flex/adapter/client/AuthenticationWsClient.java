package com.flex.adapter.client;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.client.AbstractWSClient;
import com.flextronics.flexware.ws.client.authentication.AuthenticationPort;
import com.flextronics.flexware.ws.client.authentication.CredentialsWs;
import com.flextronics.flexware.ws.client.authentication.FlexwareTokenWs;
import com.flextronics.flexware.ws.client.authentication.UserRolesWs;
import com.flextronics.flexware.ws.client.faults.AccessDeniedFault;
import com.flextronics.flexware.ws.client.faults.InvalidCredentialsFault;
import com.flextronics.flexware.ws.client.faults.InvalidRequestFault;
import com.flextronics.flexware.ws.client.faults.InvalidTokenFault;
import com.flextronics.flexware.ws.client.faults.ServiceFault_Exception;

import com.flex.adapter.constants.AppConstants;

@SuppressWarnings("deprecation")
public class AuthenticationWsClient extends AbstractWSClient {

	private Logger LOG = LoggerFactory.getLogger(AuthenticationWsClient.class);
	private AuthenticationPort authenticationPort;
	public static final String WS_NAME = "AuthenticationService?wsdl";

	public AuthenticationWsClient() {

	}

	public AuthenticationWsClient(String envUrl) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.setServiceClass(AuthenticationPort.class);
		factory.setAddress(envUrl + WS_NAME);
		if (LOG.isDebugEnabled()) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		authenticationPort = (AuthenticationPort) factory.create();

		configurePortTimeOut(AppConstants.WS_REQUEST_TIMEOUT, authenticationPort);

		configureSecureAccess(authenticationPort, envUrl);
	}

	public FlexwareTokenWs authenticate(CredentialsWs credentialsWS) throws Exception {

		LOG.info("Inside class AuthenticationWsClient.java---------------");
		FlexwareTokenWs token = null;

		try {
			token = getAuthenticationPort().authenticate(credentialsWS);
		} catch (InvalidRequestFault ex) {
			LOG.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		} catch (InvalidCredentialsFault ex) {
			LOG.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		} catch (ServiceFault_Exception ex) {
			LOG.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		} catch (AccessDeniedFault ex) {
			LOG.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		}

		return token;

	}

	public UserRolesWs getUserRoles(FlexwareTokenWs token) throws Exception {
		UserRolesWs roles = null;

		try {
			roles = getAuthenticationPort().getUserRoles(token);
		} catch (InvalidTokenFault ex) {
			LOG.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		} catch (InvalidRequestFault ex) {
			LOG.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		} catch (ServiceFault_Exception ex) {
			LOG.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		}

		return roles;

	}

	public AuthenticationPort getAuthenticationPort() {
		return authenticationPort;
	}

	public void setAuthenticationPort(AuthenticationPort authenticationPort) {
		this.authenticationPort = authenticationPort;
	}

}
