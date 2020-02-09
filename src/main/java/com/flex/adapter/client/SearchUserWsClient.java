package com.flex.adapter.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.client.AbstractWSClient;
import com.flex.adapter.constants.AppConstants;
import com.flextronics.flexware.ws.client.authentication.UserMasterWs;
import com.flextronics.flexware.ws.client.searchuser.SearchUserData;
import com.flextronics.flexware.ws.client.searchuser.SearchUserPort;
import com.flextronics.flexware.ws.client.searchuser.SearchUserReturn;

public class SearchUserWsClient extends AbstractWSClient {
	private Logger LOG = LoggerFactory.getLogger(SearchUserWsClient.class);
	private SearchUserPort searchUserPort;
	public static final String WS_NAME = "SearchUserService?wsdl";

	public SearchUserWsClient() {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.setServiceClass(SearchUserPort.class);
		factory.setAddress(AppConstants.GMSWS_TARGET + WS_NAME);
		if (LOG.isDebugEnabled()) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		searchUserPort = (SearchUserPort) factory.create();

		configurePortTimeOut(AppConstants.WS_REQUEST_TIMEOUT, searchUserPort);

		configureSecureAccess(searchUserPort, AppConstants.GMSWS_TARGET);
	}

	public List<UserMasterWsComparable> serachUsers(SearchUserData filters) throws Exception {
		List<UserMasterWsComparable> userMasterComparableList = new ArrayList<UserMasterWsComparable>();

		SearchUserReturn result = searchUserPort.searchUser(filters);

		for (UserMasterWs userMasterWs : result.getUserMasterList()) {
			UserMasterWsComparable userMasterWsComparable = new UserMasterWsComparable();
			BeanUtils.copyProperties(userMasterWsComparable, userMasterWs);
			userMasterComparableList.add(userMasterWsComparable);
		}

		return userMasterComparableList;
	}

	public SearchUserPort getSearchUserPort() {
		return searchUserPort;
	}

	public void setSearchUserPort(SearchUserPort searchUserPort) {
		this.searchUserPort = searchUserPort;
	}

}
