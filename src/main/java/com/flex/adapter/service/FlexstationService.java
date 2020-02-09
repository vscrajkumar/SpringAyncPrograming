package com.flex.adapter.service;

import java.io.BufferedReader;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.flex.adapter.model.AssemblyFilter;
import com.flex.adapter.model.AttachmentWrapper;
import com.flex.adapter.model.EprBusinessUnit;
import com.flex.adapter.model.LayoutFilter;
import com.flex.adapter.model.PIDocumentFilter;
import com.flex.adapter.model.UploadAssembly;
import com.flextronics.flexware.ws.client.authentication.UserMasterWs;
import com.flextronics.services.ewi.client.commandcenter.LineCompleteWs;


public interface FlexstationService {
	
	/**
	 * Check and remove the EPI Document
	 * @param layoutFilter - 
	 * @return JSONObject - status.
	 * @throws JSONException
	 */
	public JSONObject checkAndRemoveEPIDocumentsByConfig(LayoutFilter layoutFilter) throws JSONException;

	
	/**
	 * Get assembly List.
	 * @param assemblyFilter
	 * @return JSONObject - assemblyId, assemblyNumber ,assemblyRevision and status.
	 * @throws JSONException
	 */
	public JSONObject getAssemblyList(AssemblyFilter assemblyFilter) throws JSONException;
	
	/**
	 * Get assembly line list.
	 * @param assemblyFilter
	 * @return list - lineId,lineDescription,activeFlag,erpId,businessUnitId.
	 * @throws JSONException
	 */
    
	public List<LineCompleteWs> getAssemblyLineList(AssemblyFilter assemblyFilter) throws JSONException;

	/**
	 * Get assembly summary list.
	 * @param assemblyFilter
	 * @return JSONObject - assemblyId, assemblyNumber,assemblyRevision,effectiveDate,noOfDocuments,workOrderNumber,workOrderNumber.
	 * @throws JSONException
	 */
	//public JSONObject getAssemblySummaryList(AssemblyFilter assemblyFilter) throws JSONException;
 

	/**
	 * Get assembly summary list.
	 * @param assemblyFilter
	 * @return JSONObject - assemblyId, assemblyNumber,assemblyRevision,effectiveDate,noOfDocuments,workOrderNumber,workOrderNumber.
	 * @throws JSONException
	 */
	public JSONObject getAssemblySummaryListWithFutureTask(AssemblyFilter assemblyFilter) throws JSONException;

	/**
	 * Get line list.
	 * @param assemblyFilter
	 * @return JSONObject - description,lineId.
	 */
	public JSONObject getLineList(AssemblyFilter assemblyFilter);

	/**
	 * Get business unit list.
	 * @param erpBusinessUnit
	 * @return JSONObject - businessUnitId,businessUnitDesc,activeFlag,erpId,erpCode.
	 * @throws JSONException
	 */
	public JSONObject getErpBusinessUnitList(EprBusinessUnit erpBusinessUnit) throws JSONException;

	/**
	 * Get business unit list.
	 * @param erpBusinessUnit
	 * @return JSONObject - businessUnitId,businessUnitDesc,activeFlag,erpId,erpCode.
	 * @throws JSONException
	 */
	public void constructErpBusinessUnitMap() throws JSONException;
	
	/**
	 * Get business unit list.
	 * @param erpBusinessUnit
	 * @return JSONObject - businessUnitId,businessUnitDesc,activeFlag,erpId,erpCode.
	 * @throws JSONException
	 */
	public void constructErpListListMap() throws JSONException;

	/**
	 * Get business unit list.
	 * @param erpBusinessUnit
	 * @return JSONObject - businessUnitId,businessUnitDesc,activeFlag,erpId,erpCode.
	 * @throws JSONException
	 */
	public JSONObject getBusinessUnit(int erpId) throws JSONException;
	
	/**
	 * Get document count by assembly.
	 * @param documentFilter
	 * @return JSONArray - erpId,bunit,customerId,assemblyList,assemblyLineList.
	 * @throws JSONException
	 */
	//public JSONArray getDocumentCountByAssembly(JSONObject documentFilter) throws JSONException;
	
	/**
	 * Get documents counts by assembly.
	 * @param documentFilter
	 * @return JSONArray erpId,bunit,customerId,assemblyList,assemblyLineList
	 * @throws JSONException
	 */
	//public JSONArray getDocumentLinesCountByAssembly(JSONObject documentFilter) throws JSONException;
	
	
	/**
	 * Approve disable process instruction.
	 * @param documentFilter
	 * @return JSONObject - status, message.
	 * @throws JSONException
	 */
	public JSONObject approveDisableProcessInstruction(PIDocumentFilter documentFilter) throws JSONException;

    /**
     * Approve Process Instruction.
     * @param processInstruction
     * @return JSONObject - status, message.
     * @throws JSONException
     */

	public JSONObject approveProcessInstruction(PIDocumentFilter processInstruction) throws JSONException;
	
	
	/**
	 * Copy attachment.
	 * @param attachmentWrapper
	 * @return JSONObject - status,successUploaddedDocumentCount,failureUploaddedDocumentCount,totalAttachmentCount.
	 * @throws JSONException
	 */
	public JSONObject copyAttachments(List<AttachmentWrapper> attachmentWrapper) throws JSONException;

	/**
	 * Validate and upload the assembly file of EPI in FTP server location
	 * @param fileStorageLocation
	 * @param file
	 * @param uploadAssembly
	 * @return JSONObject- status and message.
	 */
	public JSONObject UploadFile(String fileStorageLocation, BufferedReader file, UploadAssembly uploadAssembly);

	/**
	 * Validate and upload the assembly file of EPI in FTP server location
	 * @param fileStorageLocation
	 * @param file
	 * @param uploadAssembly
	 * @return JSONObject- status and message.
	 */
	public void insertUserToken(UserMasterWs userMasterWs,String token) throws JSONException;

	/**
	 * Validate and upload the assembly file of EPI in FTP server location
	 * @param fileStorageLocation
	 * @param file
	 * @param uploadAssembly
	 * @return JSONObject- status and message.
	 */
	public void removeUserToken(String userId,UserMasterWs userMasterWs);

	public boolean isAuthorised(int userId);
	

	

}
