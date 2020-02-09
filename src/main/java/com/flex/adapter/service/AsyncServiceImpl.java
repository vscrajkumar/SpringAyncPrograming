package com.flex.adapter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

import com.flex.adapter.constants.AppConstants;
import com.flex.adapter.model.ErpAssembly;
import com.flex.adapter.model.ErpCompany;
import com.flex.adapter.model.ErpCustomer;
import com.flex.adapter.model.ErpLineListMap;
import com.flex.adapter.repository.ErpAssemblyRepository;
import com.flex.adapter.repository.ErpCompanyRepository;
import com.flex.adapter.repository.ErpCustomerRepository;
import com.flex.adapter.repository.ErpLineListMapRepository;
import com.flex.adapter.repository.JdbcTemplateAssemblyRepository;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.springframework.stereotype.Service;

@Service
public class AsyncServiceImpl implements AsyncService {

	private Logger LOG = LoggerFactory.getLogger(FlexwareServiceImpl.class);

	@Resource
	ErpCustomerRepository erpCustomerRepository;

	@Resource
	ErpCompanyRepository erpCompanyRepository;

	@Resource
	ErpAssemblyRepository erpAssemblyRepository;
	
	@Resource
	ErpLineListMapRepository erpLineListMapRepository;

	@Resource
	JdbcTemplateAssemblyRepository jdbcTemplateAssemblyRepository;

	
    @Async
	public CompletableFuture isAllCustomerMapping(String customerCode,String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException {

		List<ErpCustomer> customerList=null;

		boolean returnVal=false;

		try {

			customerList=erpCustomerRepository.isAllCustomerMapping(customerCode,solutionCode,userId,roleCodeList);

			if(customerList.size()>0){
				returnVal=true;
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return CompletableFuture.completedFuture(returnVal);
	}
    
	@Async
	public CompletableFuture  getCustomerDetails(String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException{

		List<ErpCustomer> customerList=new ArrayList<ErpCustomer>();

		try {

			customerList=erpCustomerRepository.getErpCustomer(solutionCode,userId,roleCodeList);		
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return CompletableFuture.completedFuture(customerList);
	}

    @Async
	public CompletableFuture isUserMappingAllERPCompany(String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException {

		List<ErpCompany> companyList=null;

		boolean erpReturnVal=false;

		try {

			companyList=erpCompanyRepository.isUserMappingAllERPCompany(solutionCode,userId,roleCodeList);

			if(companyList.size()>0){
				erpReturnVal=true;
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return CompletableFuture.completedFuture(erpReturnVal);
	}

	@Async
	public CompletableFuture  getErpCompany(String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException{

		List<ErpCompany> erpCompanyList=new ArrayList<ErpCompany>();

		try {

			erpCompanyList=erpCompanyRepository.getErpCompany(solutionCode,userId,roleCodeList);		
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return CompletableFuture.completedFuture(erpCompanyList);
	}

	@Async
	public CompletableFuture  getErpLineListByErpIdAndBUId(int erpId,int businessUnitId,String activeFlag) throws InterruptedException{

		LOG.info("inside async...getErpLineListByErpIdAndBUId..start");

		List<ErpLineListMap> erpLineListMapList = new ArrayList<ErpLineListMap>();
		
		
		try {

			erpLineListMapList = erpLineListMapRepository.getErpLineListByErpIdAndBUId(erpId,businessUnitId,activeFlag);
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		LOG.info("inside async...getErpLineListByErpIdAndBUId..complete");

		return CompletableFuture.completedFuture(erpLineListMapList);
	}
	
	@Async
	public CompletableFuture  getProcessInstructionAssembliesListByFilter(String assemblyNumber,int erpCompanyId,int customerId,String timeZone,int startRecord,int maxNumberRecords) throws InterruptedException{

		LOG.info("inside async...getProcessInstructionAssembliesListByFilter..start--"+assemblyNumber);

		List<ErpAssembly> erpAssemblyList = new ArrayList<ErpAssembly>();
		
		
		try {

			erpAssemblyList = erpAssemblyRepository.getProcessInstructionAssembliesListByFilter(assemblyNumber,erpCompanyId,customerId,startRecord,maxNumberRecords);
			//erpAssemblyList =jdbcTemplateAssemblyRepository.getProcessInstructionAssembliesListByFilter(assemblyNumber,erpCompanyId,customerId,timeZone,startRecord,maxNumberRecords);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		LOG.info("inside async...getProcessInstructionAssembliesListByFilter..complete--erpAssemblyList.size--"+erpAssemblyList.size());

		return CompletableFuture.completedFuture(erpAssemblyList);
	}
	
	@Async
	public CompletableFuture getDocumentCountByAssembly(JSONObject documentFilter) {

		HttpResponse<JsonNode> response = null;

		JSONArray responseDocumentArry = new JSONArray();
		try {

			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.GET_DOCUMENT_CNT)
					.headers(AppConstants.setupRequestHeaders()).body(documentFilter.toString()).asJson();

			responseDocumentArry = response.getBody().getObject().getJSONArray("summary");

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		System.out.println("getDocumentCountByAssembly is completed");

		return CompletableFuture.completedFuture(responseDocumentArry);
	}
	
	@Async
	public CompletableFuture getDocumentLinesCountByAssembly(JSONObject documentFilter) {

		HttpResponse<JsonNode> response = null;

		JSONArray responseLineArry = new JSONArray();
		try {

			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.GET_DOCUMENT_LINE_CNT)
					.headers(AppConstants.setupRequestHeaders()).body(documentFilter.toString()).asJson();

			responseLineArry = response.getBody().getObject().getJSONArray("summary");

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		System.out.println("getDocumentLinesCountByAssembly is completed");

		return CompletableFuture.completedFuture(responseLineArry);
	}


}

