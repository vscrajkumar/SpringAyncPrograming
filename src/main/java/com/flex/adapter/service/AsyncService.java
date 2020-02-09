package com.flex.adapter.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import com.flex.adapter.model.ErpAssembly;
import com.flex.adapter.model.ErpLineListMap;

public interface AsyncService {

	public CompletableFuture isAllCustomerMapping(String customerCode,String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException;
	
	public CompletableFuture  getCustomerDetails(String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException;
	
	public CompletableFuture isUserMappingAllERPCompany(String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException;
	
	public CompletableFuture  getErpCompany(String solutionCode,int userId,List<String> roleCodeList) throws InterruptedException;

	public CompletableFuture<List<ErpLineListMap>> getErpLineListByErpIdAndBUId(int erpId, int businessUnitId,String string) throws InterruptedException;

	public CompletableFuture<List<ErpAssembly>> getProcessInstructionAssembliesListByFilter(String searchValue,int erpId, int customerId, String timeZone, int startRecord, int maxNumberRecords) throws InterruptedException;

	public CompletableFuture<JSONArray> getDocumentCountByAssembly(JSONObject documentFilter) throws InterruptedException;
	
	public CompletableFuture<JSONArray> getDocumentLinesCountByAssembly(JSONObject documentFilter) throws InterruptedException;

}

