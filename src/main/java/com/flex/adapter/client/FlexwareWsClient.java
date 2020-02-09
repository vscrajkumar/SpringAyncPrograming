package com.flex.adapter.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.constants.AppConstants;
import com.flextronics.services.flexware.client.admin.FlexwarePort;
import com.flextronics.services.flexware.client.admin.MasterdataFilterWs;
import com.flextronics.services.flexware.client.admin.MasterdataWs;

public class FlexwareWsClient extends AbstractWSClient {
	private Logger LOG = LoggerFactory.getLogger(FlexwareWsClient.class);
	private FlexwarePort flexwarePort;
	public static final String WS_NAME = "FlexwareService?wsdl";

	public FlexwareWsClient() {

	}

	public FlexwareWsClient(String envUrl) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.setServiceClass(FlexwarePort.class);
		factory.setAddress(envUrl + WS_NAME);
		if (LOG.isDebugEnabled()) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		flexwarePort = (FlexwarePort) factory.create();

		configurePortTimeOut(AppConstants.WS_REQUEST_TIMEOUT, flexwarePort);

		configureSecureAccess(flexwarePort, envUrl);
	}

	public List<MasterdataWs> getMasterData(MasterdataFilterWs filter) throws Exception {
		List<MasterdataWs> resultList = new ArrayList<MasterdataWs>();

		resultList = flexwarePort.getMasterData(filter);

		return resultList;
	}

	public FlexwarePort getFlexwarePort() {
		return flexwarePort;
	}

	public void setFlexwarePort(FlexwarePort flexwarePort) {
		this.flexwarePort = flexwarePort;
	}

}
