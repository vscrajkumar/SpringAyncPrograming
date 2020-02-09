package com.flex.adapter.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flex.adapter.client.BeanLocator;
import com.flex.adapter.client.FtpClient;
import com.flex.adapter.constants.AppConstants;
import com.flex.adapter.helper.DateUtil;
import com.flex.adapter.helper.EpiHelper;
import com.flex.adapter.helper.StringUtils;
import com.flex.adapter.helper.json.JSONConvertUtils;
import com.flex.adapter.model.AssemblyFilter;
import com.flex.adapter.model.AttachmentWrapper;
import com.flex.adapter.model.EprBusinessUnit;
import com.flex.adapter.model.ErpAssembly;
import com.flex.adapter.model.ErpBusinessUnitsMap;
import com.flex.adapter.model.ErpLineListMap;
import com.flex.adapter.model.LayoutFilter;
import com.flex.adapter.model.PIDocumentFilter;
import com.flex.adapter.model.UploadAssembly;
import com.flex.adapter.repository.ErpBusinessUnitsMapRepository;
import com.flex.adapter.repository.ErpLineListMapRepository;
import com.flextronics.flexware.ws.client.authentication.UserMasterWs;
import com.flextronics.services.ewi.client.commandcenter.BusinessUnitCompleteWs;
import com.flextronics.services.ewi.client.commandcenter.FindLineResp;
import com.flextronics.services.ewi.client.commandcenter.LineCompleteWs;
import com.flextronics.services.ewi.client.common.FilterList;
import com.flextronics.services.ewi.client.common.FilterType;
import com.flextronics.services.ewi.client.common.FilterWs;
import com.flextronics.services.ewi.client.common.PaginationWs;
import com.flextronics.services.ewi.client.configuration.FtpFilterWs;
import com.flextronics.services.ewi.client.configuration.FtpServerResponse;
import com.flextronics.services.ewi.client.document.AssemblyRevWs;
import com.flextronics.services.ewi.client.document.CreateDocumentResponseWs;
import com.flextronics.services.ewi.client.document.DocTypeWs;
import com.flextronics.services.ewi.client.document.DocumentFiltersWs;
import com.flextronics.services.ewi.client.document.DocumentMasterWs;
import com.flextronics.services.ewi.client.document.DocumentWsComplete;
import com.flextronics.services.ewi.client.document.FtpInfoWs;
import com.flextronics.services.ewi.client.document.assembly.AssemblyFilterWisWs;
import com.flextronics.services.ewi.client.document.assembly.AssemblyList;
import com.flextronics.services.ewi.client.document.assembly.AssemblyWsComplete;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import static java.lang.System.*;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class FlexstationServiceImpl implements FlexstationService {

	private Logger LOG = LoggerFactory.getLogger(FlexstationServiceImpl.class);

	private Map<Integer, List<ErpBusinessUnitsMap>> erpBusinessUnitsListMap;

	private Map<String, List<ErpLineListMap>> erpLineListMap;

	@Autowired
	FlexwareService flexwareService;

	@Autowired
	AsyncService asyncService;
	
	@Resource
	ErpBusinessUnitsMapRepository erpBusinessUnitsMapRepository;

	@Resource
	ErpLineListMapRepository erpLineListMapRepository;

	@Override
	public JSONObject checkAndRemoveEPIDocumentsByConfig(LayoutFilter layoutFilter) throws JSONException {
		LOG.info("checkAndRemoveEPIDocumentsByConfig start");
		DocumentFiltersWs documentFilters = new DocumentFiltersWs();
		documentFilters.setAssytId(layoutFilter.getAssayId());
		documentFilters.setDocumentNumber(layoutFilter.getDocumentNumber());
		documentFilters.setDocumentRevision(layoutFilter.getDocumentRevision());

		out.println("documentFilters.getAssytId--" + documentFilters.getAssytId());
		out.println("documentFilters.getDocumentNumber--" + documentFilters.getDocumentNumber());
		out.println("documentFilters.getDocumentRevision--" + documentFilters.getDocumentRevision());

		try {

			BeanLocator.getDocumentWSBean(AppConstants.FLEXSTATION_TARGET).checkAndRemoveEPIDocumentsByConfig(
					documentFilters, flexwareService.getWebToken(layoutFilter.getSecurityToken()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "success");

		return response;

	}

	@Override
	public JSONObject getAssemblyList(AssemblyFilter assemblyFilter) throws JSONException {
		LOG.info("getAssemblyList start");
		AssemblyList resultList = new AssemblyList();

		AssemblyFilterWisWs assemblyFilters = new AssemblyFilterWisWs();
		assemblyFilters.setErpId(assemblyFilter.getErpId());
		assemblyFilters.setCustomerId(assemblyFilter.getCustomerId());
		assemblyFilters.setFetchAssyRevisions("true");

		/*
		 * if (getConfigurations().containsKey("EWICATCONF009")) {
		 * 
		 * timeZone = (String) getConfigurations().get("EWICATCONF009");
		 * 
		 * if (timeZone == null || timeZone.equals("")) { timeZone =
		 * "America/Los_Angeles"; }
		 * 
		 * } else { timeZone = "America/Los_Angeles";
		 * 
		 * }
		 */

		assemblyFilters.setTimeZone("America/Los_Angeles");

		PaginationWs paginationWs = new PaginationWs();
		paginationWs.setMaxNumberRecords(5000);
		paginationWs.setStartRecord(1);

		try {
			resultList = BeanLocator.getAssemblyParWSBean(AppConstants.FLEXSTATION_TARGET).filterAssembliesPis(
					assemblyFilters, paginationWs, flexwareService.getWebToken(assemblyFilter.getSecurityToken()));
			if (resultList.getAssembly().size() > 0) {

				JSONArray assemblyArray = new JSONArray();
				JSONObject assembly;

				for (AssemblyWsComplete index : resultList.getAssembly()) {

					assembly = new JSONObject();
					assembly.put("assemblyId", index.getAssyId());
					assembly.put("assemblyNumber", index.getAssyNumber());
					assembly.put("assemblyRevision", index.getAssyRevision());
					assemblyArray.put(assembly);
				}

				JSONObject response = new JSONObject();
				response.put("assembly", assemblyArray);
				response.put("status", "success");

				return response;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");

		return response;
	}


	@Override
	public List<LineCompleteWs> getAssemblyLineList(AssemblyFilter assemblyFilter) throws JSONException {
		LOG.info("getAssemblyLineList start");

		FindLineResp findLineResponse;
		List<LineCompleteWs> lineList;

		FilterList filters = new FilterList();

		FilterWs filter = new FilterWs();
		filter.setKey("erpCompanyId");
		filter.setType(FilterType.INTEGER);
		filter.setValue("" + assemblyFilter.getErpId());
		filters.getFilter().add(filter);

		filter = new FilterWs();
		filter.setKey("ewiBusinessUnitId");
		filter.setType(FilterType.INTEGER);
		filter.setValue("" + assemblyFilter.getBusinessUnitId());
		filters.getFilter().add(filter);

		filter = new FilterWs();
		filter.setKey("activeFlag");
		filter.setType(FilterType.STRING);
		filter.setValue("Y");
		filters.getFilter().add(filter);

		try {

			findLineResponse = BeanLocator.getCommandCenterWSBean(AppConstants.FLEXSTATION_TARGET).findLine(filters,
					flexwareService.getWebToken(assemblyFilter.getSecurityToken()));
			lineList = findLineResponse.getLineList();

			if (lineList.size() > 0) {
				/*
				 * JSONArray assemblyLinesArray = new JSONArray();
				 * 
				 * JSONObject assemblyLine; for (LineCompleteWs index :
				 * lineList) { assemblyLine = new JSONObject();
				 * assemblyLine.put("lineId", index.getLineId());
				 * assemblyLine.put("lineDescription", index.getDescription());
				 * assemblyLine.put("activeFlag", index.getActiveFlag());
				 * assemblyLine.put("erpId", index.getBusinessUnitId());
				 * assemblyLine.put("businessUnitId",
				 * index.getBusinessUnitId());
				 * 
				 * assemblyLinesArray.put(assemblyLine); }
				 * 
				 * JSONObject response = new JSONObject(); response.put("lines",
				 * assemblyLinesArray); response.put("status", "success");
				 * 
				 * LOG.info(response.toString());
				 */

				return lineList;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");

		return null;

	}

	@Override
	public JSONObject getAssemblySummaryListWithFutureTask(AssemblyFilter assemblyFilter) throws JSONException {
		LOG.info("inside getAssemblySummaryListWithFutureTask start");

		try {
			String searchValue = assemblyFilter.getSearchValue().trim().replace('*', '%').toUpperCase();
			if (!searchValue.endsWith("%")) {
				searchValue = searchValue + "%";
			}

			String timeZone = "America/Los_Angeles";
			int maxNumberRecords = 50;
			int startRecord = 1;

			CompletableFuture<List<ErpLineListMap>> lineListCallback = asyncService
					.getErpLineListByErpIdAndBUId(assemblyFilter.getErpId(), assemblyFilter.getBusinessUnitId(), "Y");

			CompletableFuture<List<ErpAssembly>> assemblyListCallback = asyncService
					.getProcessInstructionAssembliesListByFilter(searchValue, assemblyFilter.getErpId(),
							assemblyFilter.getCustomerId(), timeZone, startRecord, maxNumberRecords);

			CompletableFuture.allOf(lineListCallback, assemblyListCallback).join();

			List<ErpLineListMap> erpLineList = lineListCallback.get();
			List<ErpAssembly> erpAssemblyList = assemblyListCallback.get();

			if (erpAssemblyList.size() > 0) {

				JSONObject assemblySummary = new JSONObject();

				JSONArray assemblyList = new JSONArray();
				JSONObject assembly;

				JSONArray assemblyLineList = new JSONArray();
				for (ErpLineListMap index : erpLineList) {
					assemblyLineList.put(new JSONObject().put("description", index.getDescription()));
				}

				for (ErpAssembly index : erpAssemblyList) {

					assembly = new JSONObject();
					assembly.put("assemblyNumber", index.getAssemblyNumber());
					assembly.put("assemblyRevision", index.getAssemblyRevision());
					assemblyList.put(assembly);
				}
				assemblySummary.put("erpId", assemblyFilter.getErpId());
				assemblySummary.put("bunit", assemblyFilter.getBusinessUnitId());
				assemblySummary.put("customerId", assemblyFilter.getCustomerId());
				assemblySummary.put("assemblyList", assemblyList);
				assemblySummary.put("assemblyLineList", assemblyLineList);

				CompletableFuture<JSONArray> assemblyLineCountCallback = asyncService
						.getDocumentLinesCountByAssembly(assemblySummary);

				CompletableFuture<JSONArray> assemblyDocumentCountCallback = asyncService
						.getDocumentCountByAssembly(assemblySummary);

				CompletableFuture.allOf(assemblyLineCountCallback, assemblyDocumentCountCallback).join();

				JSONArray documentCountArray = assemblyDocumentCountCallback.get();
				JSONArray documentLineCountArray = assemblyLineCountCallback.get();

				JSONArray assemblyArray = new JSONArray();

				String documentCnt = "0";
				int documentLinesCnt = 0;

				for (ErpAssembly index : erpAssemblyList) {
					assembly = new JSONObject();
					assembly.put("assemblyId", index.getEwiAssemblyPartId());
					assembly.put("assemblyNumber", index.getAssemblyNumber());
					assembly.put("assemblyRevision", index.getAssemblyRevision());
					assembly.put("effectiveDate", index.getCreatedDate());
					documentLinesCnt = 0;
					if (documentLineCountArray.length() > 0) {
						for (int idx = 0; idx < documentLineCountArray.length(); idx++) {
							JSONObject docSummary = documentLineCountArray.getJSONObject(idx);
							JSONObject id = docSummary.getJSONObject("_id");
							if ((index.getAssemblyNumber().equalsIgnoreCase(id.getString("assemblyNumber"))) && (index
									.getAssemblyRevision().equalsIgnoreCase(id.getString("assemblyRevision")))) {
								documentLinesCnt += 1;
							}
						}
					}
					assembly.put("noOfLines", documentLinesCnt);
					documentCnt = "0";
					if (documentCountArray != null) {
						if (documentCountArray.length() > 0) {
							for (int idx = 0; idx < documentCountArray.length(); idx++) {
								JSONObject docSummary = documentCountArray.getJSONObject(idx);
								JSONObject id = docSummary.getJSONObject("_id");
								if ((index.getAssemblyNumber().equalsIgnoreCase(id.getString("assemblyNumber")))
										&& (index.getAssemblyRevision()
												.equalsIgnoreCase(id.getString("assemblyRevision")))) {
									documentCnt = docSummary.get("count").toString();
								}
							}
						}
					}
					assembly.put("noOfDocuments", documentCnt);

					assemblyArray.put(assembly);
				}

				JSONObject response = new JSONObject();
				response.put("assembly", assemblyArray);
				response.put("status", "success");

				// LOG.info(response.toString());
				return response;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("assembly", "Hello");
		response.put("status", "success");
		return response;
	}

	/**************************************************
	@Override
	public JSONObject getAssemblySummaryListWithFutureTask(AssemblyFilter assemblyFilter) throws JSONException {
		LOG.info("inside getAssemblySummaryList start");

		// Getting the line list to the ERP and the Businees Unit

		String searchValue = assemblyFilter.getSearchValue().trim().replace('*', '%').toUpperCase();
		if (!searchValue.endsWith("%")) {
			searchValue = searchValue + "%";
		}

		assemblyFilter.setSearchValue(searchValue);

		List<LineCompleteWs> lineList = getAssemblyLineList(assemblyFilter);

		AssemblyList resultList = new AssemblyList();

		AssemblyFilterWisWs assemblyFilters = new AssemblyFilterWisWs();
		assemblyFilters.setErpId(assemblyFilter.getErpId());
		assemblyFilters.setCustomerId(assemblyFilter.getCustomerId());
		assemblyFilters.setFetchAssyRevisions("true");

		
//		 if (getConfigurations().containsKey("EWICATCONF009")) {
//		 
//		 timeZone = (String) getConfigurations().get("EWICATCONF009");
//		  
//		  if (timeZone == null || timeZone.equals("")) { timeZone =
//		  "America/Los_Angeles"; }
//		  
//		  } else { timeZone = "America/Los_Angeles";
//		  
//		  }
//		

		assemblyFilters.setTimeZone("America/Los_Angeles");

		if (!"".equals(assemblyFilter.getSearchValue().trim())) {
			assemblyFilters.setAssyNumber(searchValue);
		}

		PaginationWs paginationWs = new PaginationWs();
		paginationWs.setMaxNumberRecords(5000);
		paginationWs.setStartRecord(1);

		try {
			resultList = BeanLocator.getAssemblyParWSBean(AppConstants.FLEXSTATION_TARGET).filterAssembliesPis(
					assemblyFilters, paginationWs, flexwareService.getWebToken(assemblyFilter.getSecurityToken()));
			// LOG.info("...resultList1....."+resultList.getResponseCode());
			// LOG.info("...resultList1....."+resultList.getMessage());

			if (resultList.getAssembly().size() > 0) {

				JSONObject assemblySummary = new JSONObject();

				JSONArray assemblyList = new JSONArray();
				JSONObject assembly;

				JSONArray assemblyLineList = new JSONArray();
				for (LineCompleteWs index : lineList) {
					assemblyLineList.put(new JSONObject().put("description", index.getDescription()));
				}
				for (AssemblyWsComplete index : resultList.getAssembly()) {

					assembly = new JSONObject();
					assembly.put("assemblyNumber", index.getAssyNumber());
					assembly.put("assemblyRevision", index.getAssyRevision());
					assemblyList.put(assembly);
				}
				assemblySummary.put("erpId", assemblyFilter.getErpId());
				assemblySummary.put("bunit", assemblyFilter.getBusinessUnitId());
				assemblySummary.put("customerId", assemblyFilter.getCustomerId());
				assemblySummary.put("assemblyList", assemblyList);
				assemblySummary.put("assemblyLineList", assemblyLineList);

				// Will need to convert this spring boot @async
				DocumentCountByAssemblyAsyncImpl documentCountByAssemblyImpl = new DocumentCountByAssemblyAsyncImpl(
						assemblySummary);

				DocumentLineCountByAssemblyAsyncImpl documentLineCountByAssemblyImpl = new DocumentLineCountByAssemblyAsyncImpl(
						assemblySummary);

				FutureTask<JSONArray> futureTask1 = new FutureTask<JSONArray>(documentCountByAssemblyImpl);
				FutureTask<JSONArray> futureTask2 = new FutureTask<JSONArray>(documentLineCountByAssemblyImpl);

				ExecutorService executor = Executors.newFixedThreadPool(2);
				executor.execute(futureTask1);
				executor.execute(futureTask2);

				JSONArray documentCountArray;

				JSONArray documentLineCountArray;

				while (true) {
					try {
						if (futureTask1.isDone() && futureTask2.isDone()) {
							documentCountArray = futureTask1.get();
							documentLineCountArray = futureTask2.get();
							executor.shutdown();
							break;
						}

					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}

				JSONArray assemblyArray = new JSONArray();

				String documentCnt = "0";
				int documentLinesCnt = 0;

				for (AssemblyWsComplete index : resultList.getAssembly()) {
					assembly = new JSONObject();
					assembly.put("assemblyId", index.getAssyId());
					assembly.put("assemblyNumber", index.getAssyNumber());
					assembly.put("assemblyRevision", index.getAssyRevision());
					assembly.put("effectiveDate", index.getCreatedDate());
					documentLinesCnt = 0;
					if (documentLineCountArray.length() > 0) {
						for (int idx = 0; idx < documentLineCountArray.length(); idx++) {
							JSONObject docSummary = documentLineCountArray.getJSONObject(idx);
							JSONObject id = docSummary.getJSONObject("_id");
							if ((index.getAssyNumber().equalsIgnoreCase(id.getString("assemblyNumber")))
									&& (index.getAssyRevision().equalsIgnoreCase(id.getString("assemblyRevision")))) {
								documentLinesCnt += 1;
							}
						}
					}
					assembly.put("noOfLines", documentLinesCnt);
					documentCnt = "0";
					if (documentCountArray != null) {
						if (documentCountArray.length() > 0) {
							for (int idx = 0; idx < documentCountArray.length(); idx++) {
								JSONObject docSummary = documentCountArray.getJSONObject(idx);
								JSONObject id = docSummary.getJSONObject("_id");
								if ((index.getAssyNumber().equalsIgnoreCase(id.getString("assemblyNumber"))) && (index
										.getAssyRevision().equalsIgnoreCase(id.getString("assemblyRevision")))) {
									documentCnt = docSummary.get("count").toString();
								}
							}
						}
					}
					assembly.put("noOfDocuments", documentCnt);

					assembly.put("workOrderNumber", index.getWorkOrderNumber());

					assembly.put("workOrderNumber", index.getWorkOrderNumber());

					assemblyArray.put(assembly);
				}

				JSONObject response = new JSONObject();
				response.put("assembly", assemblyArray);
				response.put("status", "success");

				// LOG.info(response.toString());
				return response;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");

		return response;

	}
	**************************************************/

	@Override
	@PostConstruct
	public void constructErpListListMap() throws JSONException {
		LOG.info("constructErpListListMap start");

		try {

			if (erpLineListMap == null) {

				List<ErpLineListMap> erpLineListMapList = new ArrayList<ErpLineListMap>();

				List<ErpLineListMap> tempErpLineListMapList;

				erpLineListMap = new HashMap<String, List<ErpLineListMap>>();

				erpLineListMapList = erpLineListMapRepository.getErpLineListMap();

				for (ErpLineListMap erpLineMap : erpLineListMapList) {

					String key = String.valueOf(erpLineMap.getErpCompanyId()) + "#"
							+ String.valueOf(erpLineMap.getEwiBusinessUnitId()) + "#" + erpLineMap.getActiveFlag();
					if (erpLineListMap.containsKey(key)) {

						tempErpLineListMapList = erpLineListMap.get(key);
						tempErpLineListMapList.add(erpLineMap);
						erpLineListMap.put(key, tempErpLineListMapList);
					} else {
						tempErpLineListMapList = new ArrayList<ErpLineListMap>();
						tempErpLineListMapList.add(erpLineMap);
						erpLineListMap.put(key, tempErpLineListMapList);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("constructErpBusinessUnitMap end");
	}

	/***************************

	@Override
	public JSONObject getLineList(AssemblyFilter assemblyFilter) {
		LOG.info("getLineList start");

		List<ErpLineListMap> tempErpLineListMap;
		JSONArray lineArray = new JSONArray();
		JSONObject response = new JSONObject();
		JSONObject lineObject;
		
		try{
			tempErpLineListMap = new ArrayList<ErpLineListMap>();
			String key=String.valueOf(assemblyFilter.getErpId())+"#"+String.valueOf(assemblyFilter.getBusinessUnitId()) + "#Y";
			
			if (erpLineListMap.containsKey(key)) {
				tempErpLineListMap = erpLineListMap.get(key);
				
				for(ErpLineListMap obj : tempErpLineListMap){
					lineObject = new JSONObject();

					lineObject.put("description", obj.getDescription());
					lineObject.put("lineId", obj.getEwiLineId());
					lineArray.put(lineObject);
				}
				
			}
			response.put("lineList", lineArray);
			LOG.info("inside line");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;
	}
	***************************/

	@Override
	public JSONObject getLineList(AssemblyFilter assemblyFilter) {
		LOG.info("getLineList start");

		FilterList filters = new FilterList();

		FilterWs filter = new FilterWs();
		filter.setKey("erpCompanyId");
		filter.setType(FilterType.INTEGER);
		filter.setValue("" + assemblyFilter.getErpId());
		filters.getFilter().add(filter);

		filter = new FilterWs();
		filter.setKey("ewiBusinessUnitId");
		filter.setType(FilterType.INTEGER);
		filter.setValue("" + assemblyFilter.getBusinessUnitId());
		filters.getFilter().add(filter);

		filter = new FilterWs();
		filter.setKey("activeFlag");
		filter.setType(FilterType.STRING);
		filter.setValue("Y");
		filters.getFilter().add(filter);

		FindLineResp findLine = BeanLocator.getCommandCenterWSBean(AppConstants.FLEXSTATION_TARGET).findLine(filters,
				flexwareService.getWebToken(assemblyFilter.getSecurityToken()));
		List<LineCompleteWs> lineList = findLine.getLineList();
		JSONArray lineArray = new JSONArray();
		JSONObject response = new JSONObject();
		JSONObject lineObject;
		try {
			for (LineCompleteWs lineCompleteWs : lineList) {
				lineObject = new JSONObject();

				lineObject.put("description", lineCompleteWs.getDescription());
				lineObject.put("lineId", lineCompleteWs.getLineId());
				lineArray.put(lineObject);
			}
			response.put("lineList", lineArray);
			LOG.info("inside line");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;

	}

	@Override
	public JSONObject getErpBusinessUnitList(EprBusinessUnit eprBusinessUnit) throws JSONException {
		LOG.info("getErpBusinessUnitList start");

		FilterList filters = new FilterList();

		FilterWs filter1 = new FilterWs();
		filter1.setKey("erpCompanyId");
		filter1.setType(FilterType.INTEGER);
		filter1.setValue("" + eprBusinessUnit.getErpId());
		filters.getFilter().add(filter1);

		filter1 = new FilterWs();
		filter1.setKey("activeFlag");
		filter1.setType(FilterType.STRING);
		filter1.setValue("Y");
		filters.getFilter().add(filter1);

		try {
			LOG.info("eprBusinessUnit.getSecurityToken()--" + eprBusinessUnit.getSecurityToken().toString());
			List<BusinessUnitCompleteWs> businessUnitList = BeanLocator
					.getCommandCenterWSBean(AppConstants.FLEXSTATION_TARGET)
					.findBusinessUnit(filters, flexwareService.getWebToken(eprBusinessUnit.getSecurityToken()));

			if (businessUnitList.size() > 0) {
				JSONArray businessunitArray = new JSONArray();

				JSONObject businessunit;
				for (BusinessUnitCompleteWs index : businessUnitList) {
					businessunit = new JSONObject();
					businessunit.put("businessUnitId", index.getBusinessUnitId());
					businessunit.put("businessUnitDesc", index.getDescription());
					businessunit.put("activeFlag", index.getActiveFlag());
					businessunit.put("erpId", index.getErpId());
					businessunit.put("erpCode", index.getErpCode());

					businessunitArray.put(businessunit);
				}

				JSONObject response = new JSONObject();
				response.put("businessunit", businessunitArray);
				response.put("status", "success");

				// LOG.info(response.toString());
				return response;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");

		return response;

	}

	@Override
	@PostConstruct
	public void constructErpBusinessUnitMap() throws JSONException {
		LOG.info("constructErpBusinessUnitMap start");

		try {

			if (erpBusinessUnitsListMap == null) {

				List<ErpBusinessUnitsMap> erpBusinessUnitsMapList = new ArrayList<ErpBusinessUnitsMap>();

				List<ErpBusinessUnitsMap> tempErpBusinessUnitsMapList;

				erpBusinessUnitsListMap = new HashMap<Integer, List<ErpBusinessUnitsMap>>();

				erpBusinessUnitsMapList = erpBusinessUnitsMapRepository.getErpBusinessUnitMap();

				for (ErpBusinessUnitsMap erpBusinessUnitsMap : erpBusinessUnitsMapList) {

					if (erpBusinessUnitsListMap
							.containsKey(Integer.valueOf((int)erpBusinessUnitsMap.getErpCompanyId()))) {

						tempErpBusinessUnitsMapList = erpBusinessUnitsListMap
								.get(Integer.valueOf((int)erpBusinessUnitsMap.getErpCompanyId()));
						tempErpBusinessUnitsMapList.add(erpBusinessUnitsMap);
						erpBusinessUnitsListMap.put(Integer.valueOf((int)erpBusinessUnitsMap.getErpCompanyId()),
								tempErpBusinessUnitsMapList);
					} else {
						tempErpBusinessUnitsMapList = new ArrayList<ErpBusinessUnitsMap>();
						tempErpBusinessUnitsMapList.add(erpBusinessUnitsMap);
						erpBusinessUnitsListMap.put(Integer.valueOf((int)erpBusinessUnitsMap.getErpCompanyId()),
								tempErpBusinessUnitsMapList);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("constructErpBusinessUnitMap end");
	}

	@Override
	public JSONObject getBusinessUnit(int erpId) throws JSONException {

		LOG.info("getBusinessUnit start");

		List<ErpBusinessUnitsMap> tempErpBusinessUnitsMapList;

		try {
			tempErpBusinessUnitsMapList = new ArrayList<ErpBusinessUnitsMap>();

			JSONArray businessunitArray = new JSONArray();
			JSONObject response = new JSONObject();

			LOG.info("erpBusinessUnitsListMap.size()--"+erpBusinessUnitsListMap.size());
			LOG.info("erpBusinessUnitsListMap-erpId--"+erpId);
			LOG.info("Integer.valueOf(erpId)--"+Integer.valueOf(erpId));
			LOG.info("erpBusinessUnitsListMap.containsKey--"+erpBusinessUnitsListMap.containsKey(Integer.valueOf(erpId)));
			if (erpBusinessUnitsListMap.containsKey(Integer.valueOf(erpId))) {
				tempErpBusinessUnitsMapList = erpBusinessUnitsListMap.get(Integer.valueOf(erpId));
				JSONObject businessunit;
				for (ErpBusinessUnitsMap erpBusinessUnitsMap : tempErpBusinessUnitsMapList) {
					businessunit = new JSONObject();
					businessunit.put("businessUnitId", erpBusinessUnitsMap.getEwiBusinessUnitId());
					businessunit.put("businessUnitDesc", erpBusinessUnitsMap.getDescription());
					businessunit.put("activeFlag", erpBusinessUnitsMap.getActiveFlag());
					businessunit.put("erpId", erpBusinessUnitsMap.getErpCompanyId());
					businessunit.put("erpCode", erpBusinessUnitsMap.getErpCompanyCode());
					businessunitArray.put(businessunit);
				}
			}

			response.put("businessunit", businessunitArray);
			response.put("status", "success");
			LOG.info("getBusinessUnit end");
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getBusinessUnit end");
		return response;
	}


	@Override
	public JSONObject approveDisableProcessInstruction(PIDocumentFilter documentFilter) throws JSONException {

		LOG.info("approveDisableProcessInstruction start");

		JSONObject response = new JSONObject();

		String documentRevision = documentFilter.getDocumentRevision();
		LOG.info("documentRevision--" + documentRevision);
		String processInstructionStatus = documentFilter.getLineStatus();

		JSONObject previousApprovedProcessInstruction = null;
		UserMasterWs userMasterWs = new UserMasterWs();
		userMasterWs.setFirstName(documentFilter.getFirstName());
		userMasterWs.setLastName(documentFilter.getLastName());
		userMasterWs.setEmail(documentFilter.getEmail());
		userMasterWs.setUserName(documentFilter.getUserName());
		userMasterWs.setUserId(documentFilter.getUserId());

		JSONObject currentProcessInstruction = getCurrentProcessInstruction(documentFilter);
		if (currentProcessInstruction == null) {
			response.put("status", "failure");
			response.put("messgae", "Error: Not Able to get the process instruction From DB");
			return response;
		}

		LOG.info("currentProcessInstruction--" + currentProcessInstruction.toString());

		JSONArray processInstructionsArray = getProcessInstructionsForLineSide(documentFilter);

		LOG.info("processInstructionsArray--" + processInstructionsArray.toString());

		boolean finalLevelApproval = EpiHelper.isFinalLevelApproval(currentProcessInstruction);
		LOG.info("finalLevelApproval--" + finalLevelApproval);
		int currentApproverLevel = EpiHelper.getCurrentApprovalLevel(currentProcessInstruction);
		Integer eWIdocumentId = 0;
		JSONObject procesInstructionToUpdateJson = null;
		previousApprovedProcessInstruction = getPreviousApprovedProcessInstruction(processInstructionsArray,
				documentRevision);
		if (previousApprovedProcessInstruction != null) {
			LOG.info("previousApprovedProcessInstruction--" + previousApprovedProcessInstruction.toString());
			if (previousApprovedProcessInstruction.getJSONObject("lines").has("status")) {
			} else {
			}
		}

		if (processInstructionStatus.equalsIgnoreCase(EpiHelper.PENDING_DISABLE_STATUS)) {
			if (finalLevelApproval) {
				EpiHelper.expireEwiProcessInstruction(currentProcessInstruction.getInt("ewiDocumentId"), documentFilter,
						flexwareService.getWebToken(documentFilter.getSecurityToken()));

				procesInstructionToUpdateJson = updateProcessInstruction(currentProcessInstruction,
						EpiHelper.DISABLE_STATUS, 0, userMasterWs);

				sendDisableConfirmation(currentProcessInstruction,
						procesInstructionToUpdateJson.getJSONObject("disabledBy"), userMasterWs,
						flexwareService.getWebToken(documentFilter.getSecurityToken()));

				previousApprovedProcessInstruction = getPreviousApprovedProcessInstruction(processInstructionsArray,
						documentRevision);
				if (previousApprovedProcessInstruction != null
						&& previousApprovedProcessInstruction.getInt("ewiDocumentId") != 0) {
					EpiHelper.activateEwiProcessInstruction(previousApprovedProcessInstruction.getInt("ewiDocumentId"),
							documentFilter, flexwareService.getWebToken(documentFilter.getSecurityToken()));
				}

				response.put("status", "success");
				response.put("messgae", "Approved Pending disable instruction");
				return response;
			} else {
				procesInstructionToUpdateJson = updateProcessInstruction(currentProcessInstruction,
						EpiHelper.PENDING_DISABLE_STATUS, eWIdocumentId, userMasterWs);
				LOG.info("after update Pending Disable");

				LOG.info("before debug--1--send mail");
				if (procesInstructionToUpdateJson != null) {
					// Con approval confirmation email to the PI creator
					LOG.info("before debug--1--send sendApprovalNotification");

					sendApprovalNotification(currentProcessInstruction, userMasterWs, ++currentApproverLevel,
							flexwareService.getWebToken(documentFilter.getSecurityToken()), true);
					response.put("status", "Success");
					response.put("messgae",
							"Approved and Approval Notification has been sent to the next level Approvers");
					return response;

				} else {
					response.put("status", "failure");
					response.put("messgae", "Unable to send Approval Notification to the next level Approvers");
					return response;
				}
			}
		}
		return response;
	}

	@Override
	public JSONObject approveProcessInstruction(PIDocumentFilter documentFilter) throws JSONException {

		LOG.info("approveProcessInstruction start");

		JSONObject response = new JSONObject();

		String documentRevision = documentFilter.getDocumentRevision();
		LOG.info("documentRevision--" + documentRevision);
		String processInstructionStatus = documentFilter.getLineStatus();

		JSONObject previousApprovedProcessInstruction = null;
		String updatedProcessInstructionStatus = null;
		UserMasterWs userMasterWs = new UserMasterWs();
		userMasterWs.setFirstName(documentFilter.getFirstName());
		userMasterWs.setLastName(documentFilter.getLastName());
		userMasterWs.setEmail(documentFilter.getEmail());
		userMasterWs.setUserName(documentFilter.getUserName());
		userMasterWs.setUserId(documentFilter.getUserId());

		JSONObject currentProcessInstruction = getCurrentProcessInstruction(documentFilter);
		if (currentProcessInstruction == null) {
			response.put("status", "failure");
			response.put("messgae", "Error: Not Able to get the process instruction From DB");
			return response;
		}

		LOG.info("currentProcessInstruction--" + currentProcessInstruction.toString());

		JSONArray processInstructionsArray = getProcessInstructionsForLineSide(documentFilter);

		LOG.info("processInstructionsArray--" + processInstructionsArray.toString());

		boolean finalLevelApproval = EpiHelper.isFinalLevelApproval(currentProcessInstruction);
		LOG.info("finalLevelApproval--" + finalLevelApproval);
		int currentApproverLevel = EpiHelper.getCurrentApprovalLevel(currentProcessInstruction);
		Integer eWIdocumentId = 0;
		JSONObject procesInstructionToUpdateJson = null;
		previousApprovedProcessInstruction = getPreviousApprovedProcessInstruction(processInstructionsArray,
				documentRevision);
		String prevProcessInstructionStatus = null;
		if (previousApprovedProcessInstruction != null) {
			LOG.info("previousApprovedProcessInstruction--" + previousApprovedProcessInstruction.toString());
			if (previousApprovedProcessInstruction.getJSONObject("lines").has("status")) {
				prevProcessInstructionStatus = previousApprovedProcessInstruction.getJSONObject("lines")
						.getString("status");
			} else {
				prevProcessInstructionStatus = previousApprovedProcessInstruction.getJSONArray("lines").getJSONObject(0)
						.getString("status");
			}
		}

		if (processInstructionStatus.equalsIgnoreCase(EpiHelper.PENDING_STATUS)
				|| processInstructionStatus.equalsIgnoreCase(EpiHelper.PARTIALLY_APPROVED_STATUS)) {

			if (checkPreviousPendingInProgressInstructions(processInstructionsArray, documentRevision)) {

				response.put("status", "failure");
				response.put("messgae", "Error: Please approve previous revisions!");
				return response;

			}

			if (!checkAssemblyValidation(documentFilter, currentProcessInstruction, processInstructionsArray)) {
				LOG.info("Inside within !checkAssemblyValidation");
				try {
					if (finalLevelApproval)
						eWIdocumentId = generateEwiDocument(currentProcessInstruction, documentFilter, userMasterWs);
				} catch (Exception e) {
					response.put("status", "failure");
					response.put("messgae", "Error: Creation of eWI document failed!");
					return response;

				}

			} else {
				LOG.info("Inside within !checkAssemblyValidation..else");
				try {
					if (finalLevelApproval)
						eWIdocumentId = generateEwiDocument(currentProcessInstruction, documentFilter, userMasterWs);
				} catch (Exception e) {
					response.put("status", "failure");
					response.put("messgae", "Error: Creation of eWI document failed!");
					return response;

				}

			}

			LOG.info("finalLevelApproval--" + finalLevelApproval);
			if (finalLevelApproval) {
				procesInstructionToUpdateJson = updateProcessInstruction(currentProcessInstruction,
						EpiHelper.APPROVED_STATUS, eWIdocumentId, userMasterWs);
				LOG.info("after update approved");
			} else {
				procesInstructionToUpdateJson = updateProcessInstruction(currentProcessInstruction,
						EpiHelper.PARTIALLY_APPROVED_STATUS, eWIdocumentId, userMasterWs);
				LOG.info("after update partially approved");
			}
			if (previousApprovedProcessInstruction != null) {
				updatedProcessInstructionStatus = procesInstructionToUpdateJson.getJSONArray("lines").getJSONObject(0)
						.getString("status");
				LOG.info("updatedProcessInstructionStatus" + updatedProcessInstructionStatus);
				if (updatedProcessInstructionStatus.equalsIgnoreCase(EpiHelper.APPROVED_STATUS)
						&& prevProcessInstructionStatus.equalsIgnoreCase(EpiHelper.APPROVED_STATUS)) {
					{
						previousApprovedProcessInstruction = obsulutePrevProcessInstruction(EpiHelper.OBSULUTE_STATUS,
								previousApprovedProcessInstruction, userMasterWs);
					}
				}
			}
			LOG.info("before debug--1--send mail");
			if (procesInstructionToUpdateJson != null) {
				// Con approval confirmation email to the PI creator
				if (finalLevelApproval) {
					LOG.info("before debug--1--send sendApprovalConfirmation");
					sendApprovalConfirmation(currentProcessInstruction, userMasterWs,
							procesInstructionToUpdateJson.getJSONObject("approvedBy"),
							flexwareService.getWebToken(documentFilter.getSecurityToken()), eWIdocumentId);
					response.put("status", "Success");
					response.put("messgae", "Approved and Approval confirmation send");
					return response;

				} else {
					LOG.info("before debug--1--send sendApprovalNotification");

					sendApprovalNotification(currentProcessInstruction, userMasterWs, ++currentApproverLevel,
							flexwareService.getWebToken(documentFilter.getSecurityToken()), false);
					LOG.info("testetstets...");
					response.put("status", "Success");
					response.put("messgae",
							"Approved and Approval Notification has been sent to the next level Approvers");
					return response;

				}

			} else {
				response.put("status", "failure");
				response.put("messgae", "Unable to send Approval Notification to the next level Approvers");
				return response;
			}

		} else if (processInstructionStatus.equalsIgnoreCase(EpiHelper.PENDING_DISABLE_STATUS)) {

			EpiHelper.expireEwiProcessInstruction(currentProcessInstruction.getInt("ewiDocumentId"), documentFilter,
					flexwareService.getWebToken(documentFilter.getSecurityToken()));

			procesInstructionToUpdateJson = updateProcessInstruction(currentProcessInstruction,
					EpiHelper.DISABLE_STATUS, 0, userMasterWs);

			sendDisableConfirmation(currentProcessInstruction,
					procesInstructionToUpdateJson.getJSONObject("disabledBy"), userMasterWs,
					flexwareService.getWebToken(documentFilter.getSecurityToken()));

			previousApprovedProcessInstruction = getPreviousApprovedProcessInstruction(processInstructionsArray,
					documentRevision);
			if (previousApprovedProcessInstruction != null
					&& previousApprovedProcessInstruction.getInt("ewiDocumentId") != 0) {
				EpiHelper.activateEwiProcessInstruction(previousApprovedProcessInstruction.getInt("ewiDocumentId"),
						documentFilter, flexwareService.getWebToken(documentFilter.getSecurityToken()));
			}

			response.put("status", "success");
			response.put("messgae", "Approved Pending disable instruction");
			return response;
		}
		return response;
	}

	private JSONObject getPreviousApprovedProcessInstruction(JSONArray processInstructionsArray,
			String selectedRevision) {
		LOG.info("getPreviousApprovedProcessInstruction start");

		LOG.info("processInstructionsArray--" + processInstructionsArray.toString());
		JSONObject processInstructionJson = null;
		String previousRevision = null;

		try {
			while (StringUtils.generatePreviousLetter(selectedRevision) != null) {
				previousRevision = StringUtils.generatePreviousLetter(selectedRevision);
				for (int i = 0; i < processInstructionsArray.length(); i++) {
					processInstructionJson = processInstructionsArray.getJSONObject(i);
					String docRevision = (String) parseJsonObject(processInstructionJson, "documentRevision");
					String processInstructionStatus;
					if (processInstructionJson.getJSONObject("lines").has("status")) {
						processInstructionStatus = processInstructionJson.getJSONObject("lines").getString("status");
					} else {
						processInstructionStatus = processInstructionJson.getJSONArray("lines").getJSONObject(0)
								.getString("status");
					}
					if (docRevision.equalsIgnoreCase(previousRevision)
							&& processInstructionStatus.equalsIgnoreCase(EpiHelper.APPROVED_STATUS)) {
						return processInstructionJson;
					} else {
						processInstructionJson = null;
					}

				}
				selectedRevision = previousRevision;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return processInstructionJson;
	}

	private Object parseJsonObject(JSONObject objectJsonToParse, String key) throws JSONException {
		LOG.info("parseJsonObject start");

		return objectJsonToParse.get(key);
	}

	private boolean checkPreviousPendingInProgressInstructions(JSONArray processInstructionsArray,
			String selectedRevision) throws JSONException {

		LOG.info("checkPreviousPendingInProgressInstructions start");

		JSONObject processInstructionJson = new JSONObject();
		for (int i = 0; i < processInstructionsArray.length(); i++) {
			processInstructionJson = processInstructionsArray.getJSONObject(i);
			String docRevision = (String) parseJsonObject(processInstructionJson, "documentRevision");

			String processInstructionStatus = processInstructionJson.getJSONObject("lines").getString("status");
			LOG.info("processInstructionJson-" + processInstructionJson.toString());

			if (docRevision.equalsIgnoreCase(selectedRevision)) {
				continue;
			}

			if (processInstructionStatus.equalsIgnoreCase(EpiHelper.IN_PROGRESS_STATUS)
					|| processInstructionStatus.equalsIgnoreCase(EpiHelper.PENDING_STATUS)
					|| processInstructionStatus.equalsIgnoreCase(EpiHelper.PARTIALLY_APPROVED_STATUS)) {

				if (compareDocRevisions(selectedRevision, docRevision) > 0) {
					LOG.info("return true");
					return true;
				}
			}

		}

		return false;
	}

	private int compareDocRevisions(String firstRevision, String secondRevision) {
		LOG.info("compareDocRevisions start");

		if (NumberUtils.isNumber(firstRevision) && NumberUtils.isNumber(secondRevision)) {
			return Integer.valueOf(firstRevision).compareTo(Integer.valueOf(secondRevision));
		}

		if (NumberUtils.isNumber(firstRevision)) {
			return 1;
		}

		if (NumberUtils.isNumber(secondRevision)) {
			return -1;
		}

		return firstRevision.compareTo(secondRevision);

	}

	public boolean checkAssemblyValidation(PIDocumentFilter documentFilter, JSONObject currentProcessInstruction,
			JSONArray processInstructionArray) throws JSONException {

		LOG.info("checkAssemblyValidation start");
		LOG.info("currentProcessInstruction--" + currentProcessInstruction.toString());
		LOG.info("processInstructionArray--" + processInstructionArray.toString());

		AssemblyList resultList = new AssemblyList();
		AssemblyFilterWisWs assemblyFilters = new AssemblyFilterWisWs();

		/*
		 * assemblyFilters.setErpId(Integer.parseInt(currentProcessInstruction.
		 * getJSONObject("erp").getString("id")));
		 * assemblyFilters.setErpId(Integer.parseInt(currentProcessInstruction.
		 * getJSONObject("customer").getString("id")));
		 */

		LOG.info("erpId--" + currentProcessInstruction.getJSONObject("erp").getInt("id"));
		LOG.info("customerID--" + currentProcessInstruction.getJSONObject("customer").getInt("id"));

		assemblyFilters.setErpId(currentProcessInstruction.getJSONObject("erp").getInt("id"));
		assemblyFilters.setCustomerId(currentProcessInstruction.getJSONObject("customer").getInt("id"));

		assemblyFilters.setDocumentNumber(
				getEwiDocumentNumber(currentProcessInstruction.getString("documentNumber"), documentFilter.getLineId())
						.toUpperCase());
		assemblyFilters.setDocumentRevision(documentFilter.getDocumentRevision().toUpperCase());

		String timeZone = "America/Los_Angeles";
		/********
		 * if (epiMainScreen.getConfigurations().containsKey("EWICATCONF009")) {
		 * 
		 * timeZone = (String)
		 * epiMainScreen.getConfigurations().get("EWICATCONF009"); }
		 ***************/
		assemblyFilters.setTimeZone(timeZone);

		assemblyFilters.setActive("Yes");

		PaginationWs paginationWs = new PaginationWs();
		paginationWs.setMaxNumberRecords(50);
		paginationWs.setStartRecord(0);

		LOG.info("documentFilter.getSecurityToken()--" + documentFilter.getSecurityToken());
		try {
			resultList = BeanLocator.getAssemblyParWSBean(AppConstants.FLEXSTATION_TARGET).filterAssembliesWis(
					assemblyFilters, paginationWs, flexwareService.getWebToken(documentFilter.getSecurityToken()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null != resultList.getAssembly() && resultList.getAssembly().size() > 0) {

			return true;
		} else {
			return false;
		}
	}

	private Integer generateEwiDocument(JSONObject processInstructionJson, PIDocumentFilter documentFilter,
			UserMasterWs userMasterWs) throws Exception {

		LOG.info("generateEwiDocument start");

		LOG.info("processInstructionJson--" + processInstructionJson.toString());
		System.out.println("processInstructionJson:");
		System.out.println(processInstructionJson.toString());

		try {

			DocumentFiltersWs documentFilters = new DocumentFiltersWs();

			String assemblyId = processInstructionJson.getJSONObject("assembly").has("assemblyId")
					? processInstructionJson.getJSONObject("assembly").get("assemblyId").toString()
					: processInstructionJson.getJSONObject("assembly").get("id").toString();

			documentFilters.setAssytId(Integer.parseInt(assemblyId));
			documentFilters.setDocumentNumber(processInstructionJson.get("documentNumber") + "_"
					+ processInstructionJson.getJSONArray("lines").getJSONObject(0).getInt("id"));
			documentFilters.setDocumentRevision(processInstructionJson.get("documentRevision").toString());

			LOG.info("documentFilters.getAssytId--" + documentFilters.getAssytId());
			LOG.info("documentFilters.getDocumentNumber--" + documentFilters.getDocumentNumber());
			LOG.info("documentFilters.getDocumentRevision--" + documentFilters.getDocumentRevision());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info(e.getMessage());
			throw e;

		}

		Integer eWIDocumentId = 0;
		CreateDocumentResponseWs createResponse = null;
		LOG.info("before call generateDocumentWsComplete");
		DocumentWsComplete documentWsComplete = generateDocumentWsComplete(processInstructionJson, userMasterWs);
		DocumentMasterWs documentMasterWs = generateDocumentMasterWs(processInstructionJson);
		documentWsComplete.setDocumentMasterWs(documentMasterWs);

		try {
			createResponse = BeanLocator.getDocumentWSBean(AppConstants.FLEXSTATION_TARGET)
					.createDocument(documentWsComplete, flexwareService.getWebToken(documentFilter.getSecurityToken()));

		} catch (Exception e) {
			e.printStackTrace();
			LOG.info(e.getMessage());
			throw e;
		}

		eWIDocumentId = createResponse != null ? createResponse.getDocumentWs().getDocumentId() : 0;

		LOG.info("Inside called generateEwiDocument complete");

		return eWIDocumentId;
	}

	private JSONObject updateProcessInstruction(JSONObject processInstructionJson, String processInstructionStatus,
			Integer documentId, UserMasterWs userMasterWs) throws JSONException {
		HttpResponse<JsonNode> httpResponseSaveProcessInstruction = null;
		JSONObject objectContainerJson = new JSONObject();
		LOG.info("updateProcessInstruction start");

		LOG.info("processInstructionStatus--" + processInstructionStatus);

		LOG.info("processInstructionJson--" + processInstructionJson.toString());
		String processInstructionId = processInstructionJson.getString("_id");

		JSONObject procesInstructionToUpdateJson = EpiHelper.pullCompleteProcessInstruction(processInstructionId);

		LOG.info("procesInstructionToUpdateJson--1111" + procesInstructionToUpdateJson.toString());

		if (procesInstructionToUpdateJson.has("_id"))
			procesInstructionToUpdateJson.remove("_id");
		LOG.info("procesInstructionToUpdateJson--222");
		JSONArray linesJsonArray = procesInstructionToUpdateJson.getJSONArray("lines");
		LOG.info("procesInstructionToUpdateJson--333");

		LOG.info("linesJsonArray.length()--333" + linesJsonArray.length());

		for (int i = 0; i < linesJsonArray.length(); i++) {
			JSONObject lineJSON = linesJsonArray.getJSONObject(i);

			if (lineJSON.has("id")) {
				LOG.info("Inside has id");
				if (lineJSON.getInt("id") == processInstructionJson.getJSONArray("lines").getJSONObject(0)
						.getInt("id")) {
					lineJSON.put("status", processInstructionStatus);
				}
			} else {
				LOG.info("Inside not has id..description");

				if (lineJSON.getString("description").equalsIgnoreCase(
						processInstructionJson.getJSONArray("lines").getJSONObject(0).getString("description"))) {
					lineJSON.put("status", processInstructionStatus);
				}
			}
		}

		LOG.info("updateProcessInstruction--debug--1");

		LOG.info("processInstructionStatus--debug--2--" + processInstructionStatus);

		if (processInstructionStatus.equals(EpiHelper.APPROVED_STATUS)) {
			JSONObject approvedBy = JSONConvertUtils.createApproverJson(userMasterWs);
			procesInstructionToUpdateJson = updateApprovalStatus(procesInstructionToUpdateJson, userMasterWs);
			procesInstructionToUpdateJson.put("approvedBy", approvedBy);
			procesInstructionToUpdateJson.put("ewiDocumentId", documentId);

		} else if (processInstructionStatus.equals(EpiHelper.REJECTED_STATUS)) {
			JSONObject rejectedBy = JSONConvertUtils.createRejectJson(userMasterWs);

			procesInstructionToUpdateJson.put("rejectedBy", rejectedBy);
		} else if (processInstructionStatus.equals(EpiHelper.DISABLE_STATUS)) {
			JSONObject disabledBy = JSONConvertUtils.createDisabledJson(userMasterWs);

			procesInstructionToUpdateJson.put("disabledBy", disabledBy);
		} else if (processInstructionStatus.equals(EpiHelper.PARTIALLY_APPROVED_STATUS)
				|| processInstructionStatus.equals(EpiHelper.PENDING_DISABLE_STATUS)) {
			procesInstructionToUpdateJson = updateApprovalStatus(procesInstructionToUpdateJson, userMasterWs);
		}

		objectContainerJson.put("processInstruction", procesInstructionToUpdateJson);
		objectContainerJson.put("_id", processInstructionId);

		LOG.info("procesInstructionToUpdateJson..final--" + procesInstructionToUpdateJson.toString());

		try {
			httpResponseSaveProcessInstruction = Unirest
					.post(AppConstants.EPI_TARGET + AppConstants.POST_PROCESS_INSTRUCTIONS_EDIT)
					.headers(AppConstants.setupRequestHeaders()).body(objectContainerJson.toString()).asJson();

			if (httpResponseSaveProcessInstruction.getStatus() == 200) {
				LOG.info("Inside success---return procesInstructionToUpdateJson");
				LOG.info("procesInstructionToUpdateJson--" + procesInstructionToUpdateJson.toString());
				return procesInstructionToUpdateJson;
			}

		} catch (UnirestException e1) {
			LOG.info("Error while trying to save  the process instruction " + e1.getMessage());
		}

		LOG.info("return null");
		return null;
	}

	private Boolean sendApprovalConfirmation(JSONObject processInstructionJson, UserMasterWs userMasterWs,
			JSONObject approvedBy, String sec, Integer eWIdocumentId) throws JSONException {

		LOG.info("sendApprovalConfirmation start");

		JSONObject containerJson = new JSONObject();

		JSONObject mailParametersJson = new JSONObject();

		mailParametersJson.put("documentNumber", processInstructionJson.getString("documentNumber"));

		mailParametersJson.put("documentRevision", processInstructionJson.getString("documentRevision"));

		mailParametersJson.put("approverName", (userMasterWs.getFirstName() + " " + userMasterWs.getLastName()));

		mailParametersJson.put("approvalDate", approvedBy.getString("approvalDate"));

		if (processInstructionJson.has("effectivityDate")) {
			mailParametersJson.put("effectivityDate", processInstructionJson.getString("effectivityDate"));
		} else {
			mailParametersJson.put("effectivityDate", DateUtil.dateToStr("yyyy-MM-dd", new Date()));
		}

		JSONObject processInstructionCreator = JSONConvertUtils.extractUserInfoJsonObject(processInstructionJson,
				"createdOn", "createdBy");
		JSONObject processInstructionUpdate = JSONConvertUtils.extractUserInfoJsonObject(processInstructionJson,
				"updatedOn", "updatedBy");
		// mailParametersJson.put("processInstructionCreatorMail",
		// processInstructionCreator.getString("email"));

		String email;
		try {
			if (processInstructionUpdate != null) {
				email = processInstructionCreator.getString("email") + ','
						+ processInstructionUpdate.getString("email");
			} else {
				email = processInstructionCreator.getString("email");
			}

			mailParametersJson.put("processInstructionCreatorMail", email);

		} catch (Exception e1) {
			LOG.info("Error while split mail emailid -  " + e1);
			e1.printStackTrace();
		}

		StringBuilder emailSubject = new StringBuilder();
		emailSubject.append("Your EPI ");
		emailSubject.append(processInstructionJson.getString("documentNumber"));
		emailSubject.append(" / ");
		emailSubject.append(processInstructionJson.getString("documentRevision"));
		emailSubject.append(" has been approved ");

		mailParametersJson.put("subject", emailSubject.toString());

		// String flexstationUrl = AppConstants.EPI_TARGET + "/flexstation";
		String flexstationUrl = AppConstants.AEPI_TARGET + "/#/link-instruction/" + eWIdocumentId + "/01/"
				+ userMasterWs.getUserName().toUpperCase() + ":" + sec;
		mailParametersJson.put("URL", flexstationUrl);

		containerJson.put("mailParameters", mailParametersJson);

		HttpResponse<JsonNode> httpResponse = null;

		try {
			httpResponse = Unirest.post(AppConstants.EPI_TARGET + AppConstants.POST_EMAIL_APPROVE_CONFIRMATION)
					.headers(AppConstants.setupRequestHeaders()).body(containerJson.toString()).asJson();
		} catch (UnirestException e1) {
			LOG.info("Error while trying to save  the process instruction " + e1);
		}

		if (httpResponse != null && httpResponse.getStatus() == 200) {

			return true;

		} else {

			return false;

		}

	}

	private void sendApprovalNotification(JSONObject processInstructionJSON, UserMasterWs userMasterWs,
			int approvalLevel, String sec, boolean disableNotification) throws JSONException {

		LOG.info("sendApprovalNotification start");

		JSONObject containerJson = new JSONObject();
		JSONObject mailParametersJson = new JSONObject();
		String documentNumber = processInstructionJSON.getString("documentNumber");

		String documentRevision = processInstructionJSON.getString("documentRevision");

		LOG.info("approverEmailListForNextLevelApprover - before");
		String approver = EpiHelper.approverEmailListForNextLevelApprover(processInstructionJSON, "approvers",
				approvalLevel);
		LOG.info("approverEmailListForNextLevelApprover - after--" + approver);

		mailParametersJson.put("documentNumber", documentNumber);
		mailParametersJson.put("documentRevision", documentRevision);
		mailParametersJson.put("changeDescription", "");
		mailParametersJson.put("changeReason", "");
		mailParametersJson.put("fullName", (userMasterWs.getFirstName() + " " + userMasterWs.getLastName()));
		mailParametersJson.put("username", userMasterWs.getUserName().toLowerCase());
		mailParametersJson.put("comments", "");

		mailParametersJson.put("approver", approver);
		StringBuilder emailSubject = new StringBuilder();
		StringBuilder flexstationUrl = new StringBuilder();
		if (!disableNotification) {

			emailSubject.append("Your approval is required for EPI ");
			emailSubject.append(documentNumber);
			emailSubject.append(" / ");
			emailSubject.append(documentRevision);

			// String flexstationUrl = AppConstants.EPI_TARGET + "/flexstation";
			flexstationUrl.append(AppConstants.AEPI_TARGET);
			flexstationUrl.append("/#/link-instruction/");
			flexstationUrl.append(processInstructionJSON.getString("_id"));
			flexstationUrl.append("/02/ENDUSER:");
			flexstationUrl.append(sec);
		} else {
			emailSubject.append("Your approval is required to disable EPI ");
			emailSubject.append(documentNumber);
			emailSubject.append(" / ");
			emailSubject.append(documentRevision);

			// String flexstationUrl = AppConstants.EPI_TARGET + "/flexstation";
			flexstationUrl.append(AppConstants.AEPI_TARGET);
			flexstationUrl.append("/#/link-instruction/");
			flexstationUrl.append(processInstructionJSON.getString("_id"));
			flexstationUrl.append("/03/ENDUSER:");
			flexstationUrl.append(sec);
		}
		mailParametersJson.put("subject", emailSubject.toString());
		mailParametersJson.put("URL", flexstationUrl.toString());

		containerJson.put("mailParameters", mailParametersJson);

		String notificationApiUrl = AppConstants.EPI_TARGET
				+ (disableNotification ? AppConstants.POST_EMAIL_DISABLE_INSTRUCTION_NOTIFICATION
						: AppConstants.POST_EMAIL_APPROVE_NOTIFICATION);

		HttpResponse<JsonNode> httpResponse = null;

		try {
			httpResponse = Unirest.post(notificationApiUrl).headers(AppConstants.setupRequestHeaders())
					.body(containerJson.toString()).asJson();
		} catch (UnirestException e1) {
			LOG.info("Error while trying to save  the process instruction " + e1);
		}

		if (httpResponse != null && httpResponse.getStatus() == 200) {

		} else {
			// LOG.error("Error while sending approval notification");

		}
		LOG.info("after  method sendApprovalNotification");

	}

	private JSONObject obsulutePrevProcessInstruction(String processInstructionStatus,
			JSONObject previousApprovedProcessInstruction, UserMasterWs userMasterWs) throws JSONException {
		LOG.info("obsulutePrevProcessInstruction start");

		HttpResponse<JsonNode> httpResponseSaveProcessInstruction = null;
		JSONObject objectContainerJson = new JSONObject();

		String processInstructionId = previousApprovedProcessInstruction.getString("_id");

		JSONObject procesInstructionToUpdateJson = EpiHelper.pullCompleteProcessInstruction(processInstructionId);
		LOG.info("procesInstructionToUpdateJson" + procesInstructionToUpdateJson);
		procesInstructionToUpdateJson.remove("_id");

		if (procesInstructionToUpdateJson.getJSONArray("lines").getJSONObject(0).has("status")) {
			LOG.info("inside if");
			JSONObject lineJSON = procesInstructionToUpdateJson.getJSONArray("lines").getJSONObject(0);
			LOG.info("procesInstructionToUpdateJson ID" + lineJSON.getInt("id"));
			LOG.info("previousApprovedProcessInstruction ID"
					+ previousApprovedProcessInstruction.getJSONObject("lines").getInt("id"));
			if (lineJSON.getInt("id") == previousApprovedProcessInstruction.getJSONObject("lines").getInt("id")) {
				lineJSON.put("status", processInstructionStatus);
			}
		} else {
			JSONArray linesJsonArray = procesInstructionToUpdateJson.getJSONArray("lines");
			for (int i = 0; i < linesJsonArray.length(); i++) {
				JSONObject lineJSON = linesJsonArray.getJSONObject(i);
				if (lineJSON.getInt("id") == previousApprovedProcessInstruction.getJSONArray("lines").getJSONObject(0)
						.getInt("id")) {
					lineJSON.put("status", processInstructionStatus);
				}
			}
		}

		if (processInstructionStatus.equals(EpiHelper.APPROVED_STATUS)) {
			JSONObject approvedBy = JSONConvertUtils.createApproverJson(userMasterWs);
			procesInstructionToUpdateJson.put("approvedBy", approvedBy);
			if (previousApprovedProcessInstruction.has("ewiDocumentId")) {
				procesInstructionToUpdateJson.put("ewiDocumentId",
						previousApprovedProcessInstruction.getInt("ewiDocumentId"));
			}
		} else if (processInstructionStatus.equals(EpiHelper.REJECTED_STATUS)) {
			JSONObject rejectedBy = JSONConvertUtils.createRejectJson(userMasterWs);
			procesInstructionToUpdateJson.put("rejectedBy", rejectedBy);
		} else if (processInstructionStatus.equals(EpiHelper.DISABLE_STATUS)) {
			JSONObject disabledBy = JSONConvertUtils.createRejectJson(userMasterWs);
			procesInstructionToUpdateJson.put("disabledBy", disabledBy);
		} else if (processInstructionStatus.equals(EpiHelper.PARTIALLY_APPROVED_STATUS)) {
			procesInstructionToUpdateJson = updateApprovalStatus(procesInstructionToUpdateJson, userMasterWs);
		}
		objectContainerJson.put("processInstruction", procesInstructionToUpdateJson);
		objectContainerJson.put("_id", processInstructionId);
		try {
			httpResponseSaveProcessInstruction = Unirest
					.post(AppConstants.EPI_TARGET + AppConstants.POST_PROCESS_INSTRUCTIONS_EDIT)
					.headers(AppConstants.setupRequestHeaders()).body(objectContainerJson.toString()).asJson();
			if (httpResponseSaveProcessInstruction.getStatus() == 200) {
				return procesInstructionToUpdateJson;
			}
		} catch (UnirestException e1) {
			LOG.info("Error while trying to save  the process instruction " + e1);
		}
		return null;
	}

	private Boolean sendDisableConfirmation(JSONObject processInstructionJSON, JSONObject approvedBy,
			UserMasterWs userMasterWs, String sec) throws JSONException {
		LOG.info("sendDisableConfirmation start");

		JSONObject containerJson = new JSONObject();

		JSONObject mailParametersJson = new JSONObject();

		mailParametersJson.put("documentNumber", processInstructionJSON.getString("documentNumber"));

		mailParametersJson.put("documentRevision", processInstructionJSON.getString("documentRevision"));

		mailParametersJson.put("approverName", (userMasterWs.getFirstName() + " " + userMasterWs.getLastName()));

		mailParametersJson.put("approvalDate", approvedBy.getString("disabledDate"));

		if (processInstructionJSON.has("effectivityDate")) {
			mailParametersJson.put("effectivityDate", processInstructionJSON.getString("effectivityDate"));
		} else {
			mailParametersJson.put("effectivityDate", DateUtil.dateToStr("yyyy-MM-dd", new Date()));
		}

		JSONObject processInstructionCreator = JSONConvertUtils.extractUserInfoJsonObject(processInstructionJSON,
				"createdOn", "createdBy");
		// mailParametersJson.put("processInstructionCreatorMail",
		// processInstructionCreator.getString("email"));

		String email;
		try {
			email = processInstructionCreator.getString("email");
			String[] emailUsersourceArray = email.split("\\|");
			if (emailUsersourceArray.length > 0) {
				mailParametersJson.put("processInstructionCreatorMail", emailUsersourceArray[0]);
			} else {
				mailParametersJson.put("processInstructionCreatorMail", email);
			}
		} catch (Exception e1) {
			LOG.info("Error while split mail emailid -  " + e1);
			e1.printStackTrace();
		}

		StringBuilder emailSubject = new StringBuilder();
		emailSubject.append("Your EPI ");
		emailSubject.append(processInstructionJSON.getString("documentNumber"));
		emailSubject.append(" / ");
		emailSubject.append(processInstructionJSON.getString("documentRevision"));
		emailSubject.append(" has been Disabled ");

		mailParametersJson.put("subject", emailSubject.toString());

		String flexstationUrl = AppConstants.AEPI_TARGET + "/#/link-instruction/"
				+ processInstructionJSON.getInt("ewiDocumentId") + "/01/" + userMasterWs.getUserName().toUpperCase()
				+ ":" + sec;
		// String flexstationUrl = AppConstants.AEPI_TARGET + "/#/assembly";
		mailParametersJson.put("URL", flexstationUrl);

		containerJson.put("mailParameters", mailParametersJson);

		HttpResponse<JsonNode> httpResponse = null;

		try {
			httpResponse = Unirest
					.post(AppConstants.EPI_TARGET + AppConstants.POST_EMAIL_DISABLE_INSTRUCTION_CONFIRMATION)
					.headers(AppConstants.setupRequestHeaders()).body(containerJson.toString()).asJson();
		} catch (UnirestException e1) {
			LOG.info("Error while trying to save  the process instruction " + e1);
		}

		if (httpResponse != null && httpResponse.getStatus() == 200) {

			return true;

		} else {

			return false;

		}
	}

	private DocumentWsComplete generateDocumentWsComplete(JSONObject processInstruction, UserMasterWs userMasterWs) {
		DocumentWsComplete documentWsComplete = new DocumentWsComplete();
		String effecivityTimeStr = "00:00:00";

		LOG.info("generateDocumentWsComplete method start");
		try {
			int lineId = processInstruction.getJSONArray("lines").getJSONObject(0).getInt("id");
			String eWIdocumentNumber = getEwiDocumentNumber(processInstruction.getString("documentNumber"), lineId);

			documentWsComplete.setDocumentNumber(eWIdocumentNumber);
			documentWsComplete.setDocumentRevision(processInstruction.getString("documentRevision"));

			String timeZoneStr = "America/Los_Angeles";
			/*
			 * if (EwiUserSession.getInstance().getConfigurations().containsKey(
			 * "EWICATCONF009")) { timeZoneStr = (String)
			 * EwiUserSession.getInstance().getConfigurations().get(
			 * "EWICATCONF009"); }
			 */
			documentWsComplete.setTimeZone(timeZoneStr);

			if (processInstruction.has("description")) {
				documentWsComplete.setWiDescription(processInstruction.getString("description"));
			}

			if (processInstruction.has("expireDate")) {

				String currentTime = DateUtil.dateToStr("HH:mm:ss", new Date());
				Date expireDate = DateUtil.strToDate("yyyy-MM-dd  HH:mm:ss",
						processInstruction.getString("expireDate") + "  " + currentTime);

				Date expireDateGMT = DateUtil.cvtToGmt(expireDate);

				documentWsComplete.setExpiryDateTime(DateUtil.dateToXMLGregorianComplete(expireDateGMT));
			}

			if (processInstruction.has("effectivityDate")) {
				try {

					String effectivityDateStr = processInstruction.getString("effectivityDate");

					TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);

					XMLGregorianCalendar clientCalendar = DateUtil.convertDatesToClientTimeZone(effectivityDateStr,
							effecivityTimeStr, timeZone);

					documentWsComplete.setEffectivityDateTime(clientCalendar);
				} catch (Exception de) {
					LOG.info(de.getMessage());
				}

			} else {
				try {

					TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);

					XMLGregorianCalendar clientCalendar = DateUtil.convertDatesToClientTimeZone(null, effecivityTimeStr,
							timeZone);

					documentWsComplete.setEffectivityDateTime(clientCalendar);

				} catch (Exception e) {

					LOG.info(e.getMessage());
				}
			}
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}

		AssemblyRevWs assemblyInfo = new AssemblyRevWs();
		try {
			assemblyInfo.setAssemblyNumber(processInstruction.getJSONObject("assembly").getString("assemblyNumber"));
			assemblyInfo
					.setAssemblyRevision(processInstruction.getJSONObject("assembly").getString("assemblyRevision"));

			String assemblyId = processInstruction.getJSONObject("assembly").has("assemblyId")
					? processInstruction.getJSONObject("assembly").get("assemblyId").toString()
					: processInstruction.getJSONObject("assembly").get("id").toString();

			assemblyInfo.setAssPartId(new Integer(assemblyId).longValue());

			documentWsComplete.setAssemblyInfo(assemblyInfo);

			DocTypeWs docType = new DocTypeWs();
			docType.setDocTypeId(3L);
			documentWsComplete.setDocType(docType);

			FtpInfoWs ftpInfo = new FtpInfoWs();
			ftpInfo.setFtpId(85);
			documentWsComplete.setFtpInfo(ftpInfo);

			documentWsComplete.setCreatedBy(userMasterWs.getUserId());
			documentWsComplete.setUpdatedBy(userMasterWs.getUserId());

			documentWsComplete.setNotificationType("EPI");
			documentWsComplete.setFolderName("/");
			documentWsComplete.setFileSize("0");
			documentWsComplete.setRestrictedFlag("N");
			documentWsComplete.setCurrentFlag("Y");

		} catch (Exception e1) {

		}
		/*******
		 * LOG.info("documentWsComplete.setDocumentNumber--"+documentWsComplete.getDocumentNumber());
		 * LOG.info("documentWsComplete.setDocumentRevision--"+documentWsComplete.getDocumentRevision());
		 * LOG.info("documentWsComplete.setTimeZone--"+documentWsComplete.getTimeZone());
		 * LOG.info("documentWsComplete.setWiDescription--"+documentWsComplete.getWiDescription());
		 * LOG.info("documentWsComplete.setExpiryDateTime--"+documentWsComplete.getExpiryDateTime());
		 * LOG.info("documentWsComplete.setEffectivityDateTime--"+documentWsComplete.getEffectivityDateTime());
		 * LOG.info("documentWsComplete.setDocType--"+documentWsComplete.getDocType());
		 * LOG.info("documentWsComplete.setFtpInfo--"+documentWsComplete.getFtpInfo());
		 * LOG.info("documentWsComplete.setCreatedBy--"+documentWsComplete.getCreatedBy());
		 * LOG.info("documentWsComplete.setUpdatedBy--"+documentWsComplete.getUpdatedBy());
		 * LOG.info("documentWsComplete.setNotificationType--"+documentWsComplete.getNotificationType());
		 * LOG.info("documentWsComplete.setFolderName--"+documentWsComplete.getFolderName());
		 * LOG.info("documentWsComplete.setFileSize--"+documentWsComplete.getFileSize());
		 * LOG.info("documentWsComplete.setRestrictedFlag--"+documentWsComplete.getRestrictedFlag());
		 * LOG.info("documentWsComplete.setCurrentFlag--"+documentWsComplete.getCurrentFlag());
		 * 
		 * LOG.info("assemblyInfo.getAssemblyNumber--"+assemblyInfo.getAssemblyNumber());
		 * LOG.info("assemblyInfo.getAssemblyRevision--"+assemblyInfo.getAssemblyRevision());
		 * LOG.info("assemblyInfo.getAssPartId--"+assemblyInfo.getAssPartId());
		 * 
		 * LOG.info("documentWsComplete.setCurrentFlag--"+documentWsComplete.getCurrentFlag());
		 * 
		 * LOG.info("generateDocumentWsComplete method end");
		 *******/

		return documentWsComplete;
	}

	private DocumentMasterWs generateDocumentMasterWs(JSONObject processInstruction) throws JSONException {

		LOG.info("generateDocumentMasterWs method start");

		DocumentMasterWs documentMasterWs = new DocumentMasterWs();

		documentMasterWs.setDocumentRevision(processInstruction.getString("documentRevision"));

		int lineId = processInstruction.getJSONArray("lines").getJSONObject(0).getInt("id");
		String eWIdocumentNumber = getEwiDocumentNumber(processInstruction.getString("documentNumber"), lineId);

		documentMasterWs.setDocumentNumber(eWIdocumentNumber);
		documentMasterWs.setCurrentFlag("Y");
		documentMasterWs.setNotificationType("EPI");
		documentMasterWs.setRestrictedFlag("N");
		documentMasterWs.setFileSize("0");

		LOG.info("generateDocumentMasterWs method end");

		return documentMasterWs;
	}

	private JSONObject updateApprovalStatus(JSONObject procesInstructionToUpdateJson, UserMasterWs userMasterWs)
			throws JSONException {
		LOG.info("updateApprovalStatus start");

		int currentLevel = EpiHelper.getCurrentApprovalLevel(procesInstructionToUpdateJson);
		JSONArray approversJsonArray = procesInstructionToUpdateJson.getJSONArray("approvers");
		JSONArray currentLevelApprovers = EpiHelper.getSelectedApproversForLevel(approversJsonArray, currentLevel);
		for (int i = 0; i < currentLevelApprovers.length(); i++) {
			JSONObject approverJson = currentLevelApprovers.getJSONObject(i);
			if (approverJson.has("username")) {
				if (approverJson.getString("username").equalsIgnoreCase(userMasterWs.getUserName())) {
					approverJson.put("approvalStatus", EpiHelper.APPROVED_STATUS);
				}
			} else {
				approverJson.put("approvalStatus", EpiHelper.NOT_APPLICABLE_STATUS);
			}
		}
		return procesInstructionToUpdateJson;

	}

	private JSONObject getCurrentProcessInstruction(PIDocumentFilter documentFilter) {

		LOG.info("getCurrentProcessInstruction start");

		HashMap<String, Object> filtersMap = new HashMap<String, Object>();
		filtersMap.put("_id", documentFilter.getInstructionId());

		HttpResponse<JsonNode> responseProcessIntruction = null;

		try {

			JSONObject objectJson = new JSONObject();
			objectJson.put("filters", filtersMap);
			objectJson.put("limit", 1);

			LOG.info("objectJson--" + objectJson.toString());
			LOG.info("EPI_TARGET--" + AppConstants.EPI_TARGET);

			responseProcessIntruction = Unirest.post(AppConstants.EPI_TARGET + AppConstants.POST_PROCESS_INSTRUCTION)
					.headers(AppConstants.setupRequestHeaders()).body(objectJson.toString()).asJson();

			int responseCode = responseProcessIntruction.getStatus();

			if (responseCode == 200) {

				JSONObject completeProcessInstruction = responseProcessIntruction.getBody().getObject()
						.getJSONObject("processInstruction");

				return completeProcessInstruction;

			} else {
				LOG.info("Error while trying to get the process instruction : " + documentFilter.getInstructionId());
				return null;
			}

		} catch (UnirestException e1) {
			LOG.info("Error while trying to get the process instruction List:" + e1);
			return null;
		} catch (JSONException e2) {
			LOG.info("Error while trying to get the process instruction List:" + e2);
			return null;
			// return null;
		} catch (Exception e3) {
			LOG.info("Error while trying to get the process instruction List:" + e3);
			return null;
			// return null;
		}

	}

	private JSONArray getProcessInstructionsForLineSide(PIDocumentFilter documentFilter) {
		LOG.info("getProcessInstructionsForLineSide start");

		JSONArray processInstructionsArray = null;

		HashMap<String, Object> filtersMap = new HashMap<String, Object>();
		filtersMap.put("assembly.assemblyNumber", documentFilter.getAssemblyNumber());
		filtersMap.put("assembly.assemblyRevision", documentFilter.getAssemblyRevision());
		filtersMap.put("erp.code", documentFilter.getErpCode());
		filtersMap.put("customer.code", documentFilter.getCustomerCode());
		filtersMap.put("businessUnit.description", documentFilter.getBusinessUnitDesc());
		filtersMap.put("lines.id", documentFilter.getLineId());
		filtersMap.put("side", documentFilter.getSide());

		HttpResponse<JsonNode> response = null;

		try {

			JSONObject objectJson = new JSONObject();
			objectJson.put("filters", filtersMap);
			objectJson.put("limit", 1);
			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.POST_PROCESS_INSTRUCTIONS_LIST)
					.headers(AppConstants.setupRequestHeaders()).body(objectJson.toString()).asJson();

			int responseCode = response.getStatus();

			if (responseCode == 200) {
				processInstructionsArray = response.getBody().getObject().getJSONArray("processInstructions");
			} else {
				LOG.info("Error while trying to get the process instruction List: ");
			}

		} catch (UnirestException e1) {
			LOG.info("Error while trying to get the process instruction List: " + e1);
		} catch (JSONException e2) {
			LOG.info("Error while trying to get the process instruction List:" + e2);
		} catch (Exception e3) {
			LOG.info("Error while trying to get the process instruction List:" + e3);
		}

		JSONArray filteredArray = new JSONArray();
		JSONObject processInstructionJson = new JSONObject();

		try {

			for (int i = 0; i < processInstructionsArray.length(); i++) {
				processInstructionJson = processInstructionsArray.getJSONObject(i);
				LOG.info("processInstructionJson...111--" + processInstructionJson.toString());
				Integer lineId = processInstructionJson.getJSONObject("lines").getInt("id");
				String side = (String) parseJsonObject(processInstructionJson, "side");
				if (lineId.equals(documentFilter.getLineId()) && side.equalsIgnoreCase(documentFilter.getSide())) {
					filteredArray.put(processInstructionJson);
				}

			}
		} catch (JSONException e2) {
			LOG.info("Error while trying to get the process instruction List:" + e2);
		} catch (Exception e3) {
			LOG.info("Error while trying to get the process instruction List:" + e3);
		}

		return filteredArray;
	}

	private String getEwiDocumentNumber(String epiDocumentNumber, int lineId) {
		LOG.info("getEwiDocumentNumber start");
		return new StringBuilder(epiDocumentNumber).append("_").append(lineId).toString();
	}

	@Override
	public JSONObject copyAttachments(List<AttachmentWrapper> attachmentWrapperList) throws JSONException {
		LOG.info("copyAttachments start");

		int successUploaddedDocumentCount, failureUploaddedDocumentCount, totalAttachmentCount;
		successUploaddedDocumentCount = failureUploaddedDocumentCount = totalAttachmentCount = 0;

		try {

			totalAttachmentCount = attachmentWrapperList.size();

			for (AttachmentWrapper attachmentWrapper : attachmentWrapperList) {

				HttpResponse<InputStream> response;
				byte[] attachmentData;
				File attachmentFile;
				try {
					response = Unirest.get(AppConstants.EPI_TARGET + AppConstants.GET_ATTACHMENT_BY_ID)
							.queryString("_id", attachmentWrapper.get_id()).asBinary();

					attachmentData = IOUtils.toByteArray(response.getRawBody());

					if (attachmentData != null && attachmentData.length > 0) {
						attachmentFile = new File(AppConstants.EPI_HOME_DIR + attachmentWrapper.getFilename());
						org.apache.commons.io.FileUtils.writeByteArrayToFile(attachmentFile, attachmentData);

						MultipartBody mb = Unirest.post(AppConstants.EPI_TARGET + AppConstants.POST_UPLOAD_ATTACHMENT)
								.header("accept", "application/json")
								.field("mimeType", attachmentWrapper.getContentType())
								.field("lineProcess", attachmentWrapper.getMetadata().getLineProcess())
								.field("lineLayout", attachmentWrapper.getMetadata().getLineLayout())
								.field("processInstruction", attachmentWrapper.getMetadata().getProcessInstruction());
						if (attachmentWrapper.getMetadata().getDescription() != null)
							mb = mb.field("description", attachmentWrapper.getMetadata().getDescription());
						if (attachmentWrapper.getMetadata().getProcedureName() != null)
							mb = mb.field("procedureName", attachmentWrapper.getMetadata().getProcedureName());
						if (attachmentWrapper.getMetadata().getDocumentType() != null)
							mb = mb.field("documentType", attachmentWrapper.getMetadata().getDocumentType());

						mb = mb.field("createdOn", attachmentWrapper.getMetadata().getCreatedOn().toString());

						mb = mb.field("file", attachmentFile);

						HttpResponse<JsonNode> jsonResponse = null;
						jsonResponse = mb.asJson();

						if (jsonResponse.getStatus() != 200) {
							failureUploaddedDocumentCount++;
						} else {
							successUploaddedDocumentCount++;
						}
					}
				} catch (UnirestException ex) {
					LOG.error("UnirestException -- " + ex.getMessage());
				}
			}
		} catch (Exception e) {
			LOG.error("Exception -- " + e.getMessage());
		}

		JSONObject obj = new JSONObject();

		obj.put("status", "success");
		obj.put("successUploaddedDocumentCount", successUploaddedDocumentCount);
		obj.put("failureUploaddedDocumentCount", failureUploaddedDocumentCount);
		obj.put("totalAttachmentCount", totalAttachmentCount);

		return obj;
	}

	@Override
	public JSONObject UploadFile(String fileStorageLocation, BufferedReader reader, UploadAssembly uploadAssembly) {
		LOG.info("UploadFile start");

		String error_msg = null;
		boolean error_file = false;

		File selectedFile = new File(fileStorageLocation);
		String fileName = selectedFile.getName();
		JSONObject response = new JSONObject();
		if (reader != null) {
			int line_no = 0;
			String line = "";
			String erp_company = uploadAssembly.getErpCode();
			try {
				/// ^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/
				String regex = "(\\d{4})-(\\d{2})-(\\d{2})_(\\d{2})-(\\d{2})-(\\d{2})";
				String name = fileName.split("\\.")[0];
				int startIndex = name.length() - 19;

				String date = name.substring(startIndex);

				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(date);
				if (!matcher.find()) {
					error_file = true;
					error_msg = "Please upload a file name which ends up with date format YYYY-MM-DD_HH-MM-SS ";
					response.put("status", "failure");
					response.put("message", error_msg);

				} else {
					while ((line = reader.readLine()) != null) {
						LOG.info(line);
						line_no++;
						// fileName.endsWith(arg0)

						if (line_no != 1) {
							if (!(line.split(AppConstants.PIPE_DELIMITER).length >= 31)
									|| !((line.split(AppConstants.PIPE_DELIMITER))[21].contains("@"))
									|| !(line.split(AppConstants.PIPE_DELIMITER))[19].equals("EPI")
									|| !(line.split(AppConstants.PIPE_DELIMITER))[18].equals("EPI")) {
								error_file = true;
								error_msg = "Please check ECO Originator/Change Type/Notification Type at Line no "
										+ line_no;
								response.put("status", "failure");
								response.put("message", error_msg);

								break;
							} else if ((line.split(AppConstants.PIPE_DELIMITER)[0].trim() == null
									&& line.split(AppConstants.PIPE_DELIMITER)[0].trim().equalsIgnoreCase(erp_company))
									|| line.split(AppConstants.PIPE_DELIMITER)[1].trim() == null
									|| line.split(AppConstants.PIPE_DELIMITER)[2].trim() == null
									|| line.split(AppConstants.PIPE_DELIMITER)[4].trim() == null
									|| line.split(AppConstants.PIPE_DELIMITER)[30].trim() == null) {
								error_file = true;
								error_msg = "ERP Company Code/Customer/Assembly Number/New Assembly Revision/Assembly Effective Date should not be null at Line no  "
										+ line_no;
								response.put("status", "failure");
								response.put("message", error_msg);
								break;
							} else if (!line.split(AppConstants.PIPE_DELIMITER)[0].trim()
									.equalsIgnoreCase(erp_company)) {
								error_file = true;
								error_msg = "The ERP Code is not matching with Selected EPR Code at line no " + line_no;
								response.put("status", "failure");
								response.put("message", error_msg);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			LOG.info("error_file--" + error_file);
			LOG.info("error_msg--" + error_msg);
			try {
				if (!error_file) {

					FtpInfoWs ftpServer = getPdfFtpServerDetails(uploadAssembly);
					StringBuilder pdfRemoteFolder = new StringBuilder();
					pdfRemoteFolder.append("epi");
					pdfRemoteFolder.append(AppConstants.SEPERATOR);
					pdfRemoteFolder.append("metadata");
					if ("10.201.15.217".equalsIgnoreCase(AppConstants.METADATA_IMPORTER_SERVER_IP_ADDRESS)
							|| "ewiqa".equalsIgnoreCase(AppConstants.EWI_TARGET_ENV))

						uploadPDFFile(selectedFile, ftpServer, "/QA", fileName);
					else
						uploadPDFFile(selectedFile, ftpServer, "", fileName);

					error_msg = fileName + " is uploaded.";
					response.put("status", "success");
					response.put("message", error_msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	private FtpInfoWs getPdfFtpServerDetails(UploadAssembly uploadAssembly) throws Exception {
		LOG.info("getPdfFtpServerDetails start");

		String ftpConfig = AppConstants.EPI_IMPORTER_CODE;

		FtpFilterWs ftpFilters = new FtpFilterWs();
		ftpFilters.setFtpDevCode(ftpConfig);
		FtpServerResponse ftpServerResponse;
		ftpServerResponse = BeanLocator.getConfigurationWSBean(AppConstants.FLEXSTATION_TARGET)
				.findFtpServer(ftpFilters, flexwareService.getWebToken(uploadAssembly.getSecurityToken()));
		return ftpServerResponse.getFtpServerWs();

	}

	public void uploadPDFFile(File file, FtpInfoWs ftpServer, String remoteFolder, String remoteFileName) {
		LOG.info("uploadPDFFile start");

		try {
			FtpClient ftp = new FtpClient(ftpServer);
			ftp.connect();
			boolean status = ftp.uploadFile(file, remoteFolder, remoteFileName, true);
			if (!status) {
				throw new Exception("Failed to upload the document");
			}
			ftp.disconnect();
		} catch (FTPConnectionClosedException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void insertUserToken(UserMasterWs userMasterWs, String token) throws JSONException {

		LOG.info("insertUserToken start");

		Map<String, Object> authFiltersMap = new HashMap<String, Object>();
		authFiltersMap.put("userName", userMasterWs.getUserName());
		authFiltersMap.put("userId", userMasterWs.getUserId());
		authFiltersMap.put("token", token);

		Map<String, Object> auditFiltersMap = new HashMap<String, Object>();
		auditFiltersMap.put("module", AppConstants.LOGIN);
		auditFiltersMap.put("action", AppConstants.LOGIN);
		auditFiltersMap.put("accessedOn", JSONConvertUtils.createAccessJson(userMasterWs));

		HttpResponse<JsonNode> response = null;

		try {

			JSONObject objectJson = new JSONObject();
			objectJson.put("authFilter", authFiltersMap);
			objectJson.put("auditFilter", auditFiltersMap);

			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.POST_USER_TOKEN_INSERT)
					.headers(AppConstants.setupRequestHeaders()).body(objectJson.toString()).asJson();

			JSONObject obj = (JSONObject) response.getBody().getObject();

			int responseCode = 200;

			if (responseCode == 200) {
				LOG.info("TOKEN Inserted");
			} else {
				LOG.info("TOKEN Not Inserted");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeUserToken(String userName, UserMasterWs userMasterWs) {

		LOG.info("removeUserToken start");

		JSONObject authFiltersMap = new JSONObject();
		HttpResponse<JsonNode> response = null;

		try {
			authFiltersMap.put("userName", userName);
			Map<String, Object> auditFiltersMap = new HashMap<String, Object>();
			auditFiltersMap.put("module", AppConstants.LOGOUT);
			auditFiltersMap.put("action", AppConstants.LOGOUT);
			auditFiltersMap.put("accessedOn", JSONConvertUtils.createAccessJson(userMasterWs));
			JSONObject objectJson = new JSONObject();
			objectJson.put("authFilter", authFiltersMap);
			objectJson.put("auditFilter", auditFiltersMap);

			System.out.println("objectJson " + objectJson);
			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.POST_USER_TOKEN_REMOVE)
					.headers(AppConstants.setupRequestHeaders()).body(objectJson.toString()).asJson();

			int responseCode = response.getStatus();

			if (responseCode == 200) {
				LOG.info("TOKEN removed");
			} else {
				LOG.info("TOKEN Not removed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean isAuthorised(int userId) {
		
		HttpResponse<JsonNode> response = null;
		try {

			JSONObject objectJson = new JSONObject();
			objectJson.put("userId", userId);
			
			JSONObject authFilter = new JSONObject();
			authFilter.put("authFilter", objectJson);

			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.Is_AUTHORISED)
					.headers(AppConstants.setupRequestHeaders()).body(authFilter.toString()).asJson();
			if(response.getBody().getObject().get("status").toString().equalsIgnoreCase("success"))
				return true;
			else
				return false;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	/***************************************************************************************
	@Override
	public JSONObject getAssemblySummaryList(AssemblyFilter assemblyFilter) throws JSONException {
		LOG.info("inside getAssemblySummaryList start");

		// Getting the line list to the ERP and the Businees Unit

		String searchValue = assemblyFilter.getSearchValue().trim().replace('*', '%').toUpperCase();
		if (!searchValue.endsWith("%")) {
			searchValue = searchValue + "%";
		}

		assemblyFilter.setSearchValue(searchValue);

		List<LineCompleteWs> lineList = getAssemblyLineList(assemblyFilter);

		AssemblyList resultList = new AssemblyList();

		AssemblyFilterWisWs assemblyFilters = new AssemblyFilterWisWs();
		assemblyFilters.setErpId(assemblyFilter.getErpId());
		assemblyFilters.setCustomerId(assemblyFilter.getCustomerId());
		assemblyFilters.setFetchAssyRevisions("true");

		
		assemblyFilters.setTimeZone("America/Los_Angeles");

		if (!"".equals(assemblyFilter.getSearchValue().trim())) {
			assemblyFilters.setAssyNumber(searchValue);
		}

		PaginationWs paginationWs = new PaginationWs();
		paginationWs.setMaxNumberRecords(5000);
		paginationWs.setStartRecord(1);

		try {
			resultList = BeanLocator.getAssemblyParWSBean(AppConstants.FLEXSTATION_TARGET).filterAssembliesPis(
					assemblyFilters, paginationWs, flexwareService.getWebToken(assemblyFilter.getSecurityToken()));
			// LOG.info("...resultList1....."+resultList.getResponseCode());
			// LOG.info("...resultList1....."+resultList.getMessage());

			if (resultList.getAssembly().size() > 0) {

				JSONObject assemblySummary = new JSONObject();

				JSONArray assemblyList = new JSONArray();
				JSONObject assembly;

				JSONArray assemblyLineList = new JSONArray();
				for (LineCompleteWs index : lineList) {
					assemblyLineList.put(new JSONObject().put("description", index.getDescription()));
				}
				for (AssemblyWsComplete index : resultList.getAssembly()) {

					assembly = new JSONObject();
					assembly.put("assemblyNumber", index.getAssyNumber());
					assembly.put("assemblyRevision", index.getAssyRevision());
					assemblyList.put(assembly);
				}
				assemblySummary.put("erpId", assemblyFilter.getErpId());
				assemblySummary.put("bunit", assemblyFilter.getBusinessUnitId());
				assemblySummary.put("customerId", assemblyFilter.getCustomerId());
				assemblySummary.put("assemblyList", assemblyList);
				assemblySummary.put("assemblyLineList", assemblyLineList);

				JSONArray documentCountArray = getDocumentCountByAssembly(assemblySummary);

				JSONArray documentLineCountArray = getDocumentLinesCountByAssembly(assemblySummary);

				// LOG.info("documentCountArray--"+documentCountArray.toString());

				// LOG.info("documentLineCountArray--"+documentLineCountArray.toString());

				JSONArray assemblyArray = new JSONArray();

				String documentCnt = "0";
				int documentLinesCnt = 0;

				for (AssemblyWsComplete index : resultList.getAssembly()) {
					assembly = new JSONObject();
					assembly.put("assemblyId", index.getAssyId());
					assembly.put("assemblyNumber", index.getAssyNumber());
					assembly.put("assemblyRevision", index.getAssyRevision());
					assembly.put("effectiveDate", index.getCreatedDate());
					documentLinesCnt = 0;
					if (documentLineCountArray.length() > 0) {
						for (int idx = 0; idx < documentLineCountArray.length(); idx++) {
							JSONObject docSummary = documentLineCountArray.getJSONObject(idx);
							JSONObject id = docSummary.getJSONObject("_id");
							if ((index.getAssyNumber().equalsIgnoreCase(id.getString("assemblyNumber")))
									&& (index.getAssyRevision().equalsIgnoreCase(id.getString("assemblyRevision")))) {
								documentLinesCnt += 1;
							}
						}
					}
					assembly.put("noOfLines", documentLinesCnt);
					documentCnt = "0";
					if (documentCountArray != null) {
						if (documentCountArray.length() > 0) {
							for (int idx = 0; idx < documentCountArray.length(); idx++) {
								JSONObject docSummary = documentCountArray.getJSONObject(idx);
								JSONObject id = docSummary.getJSONObject("_id");
								if ((index.getAssyNumber().equalsIgnoreCase(id.getString("assemblyNumber"))) && (index
										.getAssyRevision().equalsIgnoreCase(id.getString("assemblyRevision")))) {
									documentCnt = docSummary.get("count").toString();
								}
							}
						}
					}
					assembly.put("noOfDocuments", documentCnt);

					assembly.put("workOrderNumber", index.getWorkOrderNumber());

					assembly.put("workOrderNumber", index.getWorkOrderNumber());

					assemblyArray.put(assembly);
				}

				JSONObject response = new JSONObject();
				response.put("assembly", assemblyArray);
				response.put("status", "success");

				// LOG.info(response.toString());
				return response;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");

		return response;

	}

	@Override
	public JSONArray getDocumentCountByAssembly(JSONObject documentFilter) throws JSONException {
		LOG.info("getDocumentCountByAssembly start");

		HttpResponse<JsonNode> response = null;

		try {

			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.GET_DOCUMENT_CNT)
					.headers(AppConstants.setupRequestHeaders()).body(documentFilter.toString()).asJson();

		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

		return response.getBody().getObject().getJSONArray("summary");

	}

	@Override
	public JSONArray getDocumentLinesCountByAssembly(JSONObject documentFilter) throws JSONException {
		LOG.info("getDocumentLinesCountByAssembly start");

		HttpResponse<JsonNode> response = null;

		try {

			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.GET_DOCUMENT_LINE_CNT)
					.headers(AppConstants.setupRequestHeaders()).body(documentFilter.toString()).asJson();

		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		LOG.info("summary--" + response.getBody().getObject().getJSONArray("summary").toString());
		return response.getBody().getObject().getJSONArray("summary");

	}

	***************************************************************************************/


}
