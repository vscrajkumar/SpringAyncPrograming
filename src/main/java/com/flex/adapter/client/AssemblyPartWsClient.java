package com.flex.adapter.client;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.client.AbstractWSClient;
import com.flextronics.services.ewi.client.common.PaginationWs;
import com.flextronics.services.ewi.client.document.assembly.AssemblyFilterWisWs;
import com.flextronics.services.ewi.client.document.assembly.AssemblyFilterWs;
import com.flextronics.services.ewi.client.document.assembly.AssemblyList;
import com.flextronics.services.ewi.client.document.assembly.AssemblyPort;
import com.flextronics.services.ewi.client.document.assembly.AssemblyWs;
import com.flextronics.services.ewi.client.document.assembly.AssemblyWsComplete;
import com.flextronics.services.ewi.client.document.assembly.CreateAssemblyResponseWs;
import com.flextronics.services.ewi.client.document.assembly.UpdateAssemblyResponseWs;
import com.flextronics.services.ewi.client.document.assembly.WorkOrderList;
import com.flextronics.services.ewi.client.faults.InvalidRequestFault;
import com.flextronics.services.ewi.client.faults.ServiceFault_Exception;

import com.flex.adapter.constants.AppConstants;

public class AssemblyPartWsClient extends AbstractWSClient {

	private Logger LOG = LoggerFactory.getLogger(AssemblyPartWsClient.class);
	private String messageException = new String();
	private AssemblyPort assemblyPort;
	public static final String WS_NAME = "AssemblyPartService?wsdl";

	public AssemblyPartWsClient() {

	}

	public AssemblyPartWsClient(String envUrl) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.setServiceClass(AssemblyPort.class);
		factory.setAddress(envUrl + WS_NAME);
		if (LOG.isDebugEnabled()) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		assemblyPort = (AssemblyPort) factory.create();

		configurePortTimeOut(AppConstants.WS_REQUEST_TIMEOUT, assemblyPort);

		configureSecureAccess(assemblyPort, envUrl);
	}

	public AssemblyPort getAssemblyPort() {
		return assemblyPort;
	}

	public void setAssemblyPort(AssemblyPort assemblyPort) {
		this.assemblyPort = assemblyPort;
	}

	public AssemblyList filterAssemblies(AssemblyFilterWs assemblyFilters, PaginationWs paginationWs,
			String securityToken) throws Exception {

		AssemblyList response = null;

		try {
			response = assemblyPort.filterAssemblies(assemblyFilters, paginationWs, securityToken);
		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to filterAssemblies " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		}

		return response;

	}

	public UpdateAssemblyResponseWs updateAssembly(AssemblyWsComplete assemblyWsComplete, String securityToken)
			throws Exception {

		try {
			assemblyPort.updateAssembly(assemblyWsComplete, securityToken);
		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to filterAssemblies " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		}
		return null;
	}

	public CreateAssemblyResponseWs createAssembly(AssemblyWs assemblyWs, String securityToken) throws Exception {

		try {
			assemblyPort.createAssembly(assemblyWs, securityToken);
		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to filterAssemblies " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		}

		return null;
	}

	public UpdateAssemblyResponseWs updateAssembly(AssemblyWs assemblyWs, String securityToken) throws Exception {

		try {
			assemblyPort.updateAssembly(assemblyWs, securityToken);
		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to filterAssemblies " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		}

		return null;
	}

	public AssemblyList filterAssembliesWis(AssemblyFilterWisWs assemblyFilters, PaginationWs paginationWs,
			String securityToken) throws Exception {

		AssemblyList response = null;

		try {
			response = assemblyPort.filterAssembliesWis(assemblyFilters, paginationWs, securityToken);
		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to filterAssemblies " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		}

		return response;
	}

	public AssemblyList filterAssembliesPis(AssemblyFilterWisWs assemblyFilters, PaginationWs paginationWs,
			String securityToken) throws Exception {

		AssemblyList response = null;
		try {
			response = assemblyPort.filterAssembliesPis(assemblyFilters, paginationWs, securityToken);
		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to filterAssemblies " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		}

		return response;
	}

	public WorkOrderList filterWorkOrderByAssy(AssemblyFilterWisWs assemblyFilters, PaginationWs paginationWs,
			String securityToken) throws Exception {

		WorkOrderList response = null;

		try {
			response = assemblyPort.filterWorkOrderByAssy(assemblyFilters, paginationWs, securityToken);
		} catch (ServiceFault_Exception e) {
			messageException = "Service exception " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		} catch (InvalidRequestFault e) {
			messageException = "Invalid data send to filterWorkOrderByAssy " + e.getMessage();
			LOG.error(messageException, e);
			throw new Exception(e.getMessage(), e);
		}

		return response;
	}

}
