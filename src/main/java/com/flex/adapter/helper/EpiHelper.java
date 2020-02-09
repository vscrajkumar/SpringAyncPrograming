package com.flex.adapter.helper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.client.BeanLocator;
import com.flex.adapter.constants.AppConstants;
import com.flex.adapter.model.PIDocumentFilter;
import com.flextronics.services.ewi.client.document.DocumentWsComplete;
import com.flextronics.services.ewi.client.document.UpdateDocumentResponseWs;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class EpiHelper {

	private static Logger LOG = LoggerFactory.getLogger(EpiHelper.class);

	public final static String IN_PROGRESS_STATUS = "In Progress";
	public final static String APPROVED_STATUS = "Approved";
	public final static String OBSULUTE_STATUS = "Obsolete";
	public final static String PENDING_STATUS = "Pending";
	public final static String PENDING_DISABLE_STATUS = "Pending Disable";
	public final static String REJECTED_STATUS = "Rejected";
	public final static String DISABLE_STATUS = "Disable";
	public final static String PARTIALLY_APPROVED_STATUS = "Partially Approved";
	public final static String NOT_APPLICABLE_STATUS = "Not Applicable";

	public final static String NEW_DOCUMENT_ACTION = "NewDocument";
	public final static String DISABLE_ACTION = "Disable";
	public final static String ASSIGN_LINE_ACTION = "AssignLine";
	public final static String EDIT_LINE_ACTION = "EditLine";

	/******************
	 * public static int getCurrentApprovalLevel(JSONObject obj) {
	 * 
	 * int currentApproverLevel = 0; try { JSONArray approversArray = (JSONArray)
	 * parseJsonObject(obj, "approvers"); for (int i = 0; i <
	 * approversArray.length(); i++) { if
	 * (approversArray.getJSONObject(i).getString("approvalStatus").equalsIgnoreCase(APPROVED_STATUS))
	 * { currentApproverLevel++; } } } catch (JSONException e1) { LOG.error("Error :
	 * isFinalLevelApproval " + e1.toString()); } catch (Exception e2) {
	 * LOG.error("Error : isFinalLevelApproval " + e2.toString()); }
	 * 
	 * return ++currentApproverLevel; }
	 ******************/

	public static int getCurrentApprovalLevel(JSONObject obj) {

		int currentApproverLevel = 0;

		try {
			int approverLevels = (Integer) parseJsonObject(obj, "approverLevels");
			if (approverLevels == -1) {
				currentApproverLevel++;
			} else if (approverLevels == 1) {
				currentApproverLevel++;
			} else if (approverLevels > 1) {
				currentApproverLevel = 1;
				Map<String, List> approverLevelStatusListMap = new HashMap<String, List>();
				JSONArray approversArray = (JSONArray) parseJsonObject(obj, "approvers");

				for (int index = 0; index < approversArray.length(); index++) {
					if(!approversArray.getJSONObject(index).has("approverLevel")){
						return currentApproverLevel;
					}
					/*if (approverLevelStatusListMap
							.containsKey(approversArray.getJSONObject(index).get("approverLevel").toString())) {
						List<String> statusList = new ArrayList<String>();
						statusList = approverLevelStatusListMap
								.get(approversArray.getJSONObject(index).get("approverLevel").toString());
						if (approversArray.getJSONObject(index).has("approvalStatus")) {
							statusList.add(approversArray.getJSONObject(index).getString("approvalStatus"));
							approverLevelStatusListMap.put(
									approversArray.getJSONObject(index).get("approverLevel").toString(), statusList);
						}
					} else {*/
						if (approversArray.getJSONObject(index).has("approvalStatus")) {
							List<String> statusList = new ArrayList<String>();
							statusList.add(0, approversArray.getJSONObject(index).getString("approvalStatus"));
							approverLevelStatusListMap.put(
									approversArray.getJSONObject(index).get("approverLevel").toString(), statusList);
						}
					//}
				}

				for (String key : approverLevelStatusListMap.keySet()) {
					if (approverLevelStatusListMap.get(key).toString().contains(APPROVED_STATUS)) {
						currentApproverLevel++;
					}
				}
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			LOG.error("Error : getCurrentApprovalLevel " + e1.toString());
		} catch (Exception e2) {
			e2.printStackTrace();
			LOG.error("Error : getCurrentApprovalLevel " + e2.toString());
		}
		return currentApproverLevel;
	}

	public static boolean isFinalLevelApproval(JSONObject obj) {

		try {
			int approverLevels = (Integer) parseJsonObject(obj, "approverLevels");
			if (approverLevels == -1) {
				return true;
			}
			if (approverLevels == 1) {
				return true;
			} else if (approverLevels > 1) {
				Map<String, List> approverLevelStatusListMap = new HashMap<String, List>();
				JSONArray approversArray = (JSONArray) parseJsonObject(obj, "approvers");

				for (int index = 0; index < approversArray.length(); index++) {
					if(!approversArray.getJSONObject(index).has("approverLevel")){
						return true;
					}
					/*if (approverLevelStatusListMap
							.containsKey(approversArray.getJSONObject(index).get("approverLevel").toString())) {
						List<String> statusList = new ArrayList<String>();
						statusList = approverLevelStatusListMap
								.get(approversArray.getJSONObject(index).get("approverLevel").toString());
						statusList.add(approversArray.getJSONObject(index).getString("approvalStatus"));
						approverLevelStatusListMap
								.put(approversArray.getJSONObject(index).get("approverLevel").toString(), statusList);
					} else {*/
					
						List<String> statusList = new ArrayList<String>();
						statusList.add(0, approversArray.getJSONObject(index).getString("approvalStatus"));
						approverLevelStatusListMap
								.put(approversArray.getJSONObject(index).get("approverLevel").toString(), statusList);

					}
				//}

				approverLevelStatusListMap.forEach((k, v) -> LOG.info("K--" + k + " v--" + v.toString()));

				for (String key : approverLevelStatusListMap.keySet()) {
					if (Integer.parseInt(key) != approverLevelStatusListMap.size()
							&& !approverLevelStatusListMap.get(key).toString().contains(APPROVED_STATUS)) {
						return false;
					}
				}

				return true;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			LOG.error("Error : isFinalLevelApproval " + e1.toString());
		} catch (Exception e2) {
			e2.printStackTrace();
			LOG.error("Error : isFinalLevelApproval " + e2.toString());
		}

		return false;
	}

	public static Object parseJsonObject(JSONObject objectJsonToParse, String key) throws JSONException {

		return objectJsonToParse.get(key);
	}

	public static void expireEwiProcessInstruction(Integer documentId, PIDocumentFilter documentFilter, String token) {
		DocumentWsComplete documentWsComplete = new DocumentWsComplete();

		String expiryTimeStr = "00:00:00";
		XMLGregorianCalendar clientCalendar = null;
		String timeZoneStr = "America/Los_Angeles";
		/*
		 * if (EwiUserSession.getInstance().getConfigurations().containsKey(
		 * "EWICATCONF009")) { timeZoneStr = (String)
		 * EwiUserSession.getInstance().getConfigurations().get( "EWICATCONF009"); }
		 */

		// USe the current date
		try {

			TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);

			// Get Effectivity Date at client time zone
			clientCalendar = DateUtil.convertDatesToClientTimeZone(null, expiryTimeStr, timeZone);

		} catch (ParseException e) {
			LOG.error(e.getMessage());
		} catch (DatatypeConfigurationException de) {
			LOG.error(de.getMessage());
		}

		documentWsComplete.setExpiryDateTime(clientCalendar);
		documentWsComplete.setTimeZone(timeZoneStr);
		documentWsComplete.setDocumentId(documentId);

		try {
			   UpdateDocumentResponseWs response = BeanLocator.getDocumentWSBean(
	                    AppConstants.FLEXSTATION_TARGET)
	                    .updateDocumentDates(documentWsComplete, 
	                    		token);

		} catch (WebServiceException e) {
			LOG.error("Web Service Exception while creating signature.", e);
		} catch (Exception e) {
			LOG.error("Unknown Exception while creating signature.", e);

		}

	}

	public static void activateEwiProcessInstruction(Integer documentId, PIDocumentFilter documentFilter,
			String token) {

		DocumentWsComplete documentWsComplete = new DocumentWsComplete();

		String effectivityTimeStr = "00:00:00";
		XMLGregorianCalendar clientCalendar = null;
		String timeZoneStr = "America/Los_Angeles";
		/********
		 * if
		 * (EwiUserSession.getInstance().getConfigurations().containsKey("EWICATCONF009"))
		 * { timeZoneStr = (String)
		 * EwiUserSession.getInstance().getConfigurations().get( "EWICATCONF009"); }
		 *************/
		// USe the current date
		try {

			TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);

			// Get Effectivity Date at client time zone
			clientCalendar = DateUtil.convertDatesToClientTimeZone(null, effectivityTimeStr, timeZone);

		} catch (ParseException e) {

			LOG.error(e.getMessage());
		} catch (DatatypeConfigurationException de) {
			LOG.error(de.getMessage());
		}

		documentWsComplete.setEffectivityDateTime(clientCalendar);
		documentWsComplete.setDocumentId(documentId);
		documentWsComplete.setTimeZone(timeZoneStr);

		try {

		} catch (WebServiceException e) {
			LOG.error("Web Service Exception while creating signature." + e);
			e.printStackTrace();

		} catch (Exception e) {
			LOG.error("Unknown Exception while creating signature." + e);
			e.printStackTrace();

		}

	}

	public static JSONObject pullCompleteProcessInstruction(String processIstructionId) {

		Map<String, Object> filtersMap = new HashMap<String, Object>();

		filtersMap.put("_id", processIstructionId);

		JSONObject objectJson = new JSONObject();
		HttpResponse<JsonNode> responseProcessIntruction = null;

		try {
			objectJson.put("filters", filtersMap);
			objectJson.put("limit", 500);

			responseProcessIntruction = Unirest.post(AppConstants.EPI_TARGET + AppConstants.POST_PROCESS_INSTRUCTION)
					.headers(AppConstants.setupRequestHeaders()).body(objectJson.toString()).asJson();

			int responseCode = responseProcessIntruction.getStatus();

			if (responseCode == 200) {

				JSONObject completeProcessInstruction = responseProcessIntruction.getBody().getObject()
						.getJSONObject("processInstruction");

				return completeProcessInstruction;

			} else {
				LOG.error("Error while trying to get the process instruction : " + processIstructionId);
				return null;
			}

		} catch (UnirestException e1) {

			LOG.error("Error while trying to get the process instruction List: " + e1);
			return null;
		} catch (JSONException e2) {
			LOG.error("Error while trying to get the process instruction List: " + e2);
			return null;
		}

	}

	public static String approverEmailListForNextLevelApprover(JSONObject objectJsonToParse, String key,
			int approverLevel) {
		StringBuffer approversStr = new StringBuffer();

		try {
			JSONArray approversArray = objectJsonToParse.getJSONArray(key);
			for (int i = 0; i < approversArray.length(); i++) {
				JSONObject approverJson = (JSONObject) approversArray.get(i);

				if (approverJson.has("approverLevel") && (approverJson.getInt("approverLevel") == approverLevel)) {

					String email;
					try {
						email = approverJson.getString("email").toLowerCase();
						String[] emailUsersourceArray = email.split("\\|");
						if (emailUsersourceArray.length > 0) {
							approversStr.append(emailUsersourceArray[0]);
						} else {
							approversStr.append(email);
						}
					} catch (Exception e1) {
						LOG.info("Error while split mail emailid -  " + e1);
						e1.printStackTrace();
					}

					// approversStr.append(approverJson.getString("email").toLowerCase());

					if (i < approversArray.length() - 1) {
						approversStr.append(", ");
					}
				}
			}
		} catch (JSONException e1) {
			LOG.error("Error approverEmailListForNextLevelApprover : " + e1.toString());
		} catch (Exception e2) {
			LOG.error("Error approverEmailListForNextLevelApprover : " + e2.toString());
		}

		return approversStr.toString();
	}

	public static JSONArray getSelectedApproversForLevel(JSONArray selectedApproversArray, int approverLevel) {

		JSONArray selectedApproversCurrentLevel = new JSONArray();

		try {
			for (int i = 0; i < selectedApproversArray.length(); i++) {

				JSONObject obj = selectedApproversArray.getJSONObject(i);
				if (obj.has("approverLevel") && obj.getInt("approverLevel") == approverLevel)
					selectedApproversCurrentLevel.put(obj);

				if (!obj.has("approverLevel") && approverLevel == 1)
					selectedApproversCurrentLevel.put(obj);

			}
		} catch (JSONException e1) {
			LOG.error("Error getSelectedApproversForLevel : " + e1.toString());

		} catch (Exception e2) {
			LOG.error("Error getSelectedApproversForLevel : " + e2.toString());
		}

		return selectedApproversCurrentLevel;
	}

	/************************
	 * public static JSONObject pullCompleteProcessInstruction(String
	 * processIstructionId) {
	 * 
	 * Map<String, Object> filtersMap = new HashMap<String, Object>();
	 * 
	 * filtersMap.put("_id", processIstructionId);
	 * 
	 * JSONObject objectJson = new JSONObject(); objectJson.put("filters",
	 * filtersMap); objectJson.put("limit", 500);
	 * 
	 * HttpResponse<JsonNode> responseProcessIntruction = null; try {
	 * responseProcessIntruction = Unirest
	 * .post(EwiUserSession.getInstance().getNodeApiUrl() +
	 * EpiRestURIConstants.POST_PROCESS_INSTRUCTION)
	 * .headers(EpiRestURIConstants.setupRequestHeaders())
	 * .body(objectJson.toString()).asJson();
	 * 
	 * int responseCode = responseProcessIntruction.getStatus();
	 * 
	 * if (responseCode == 200) {
	 * 
	 * JSONObject completeProcessInstruction = responseProcessIntruction
	 * .getBody().getObject() .getJSONObject("processInstruction");
	 * 
	 * return completeProcessInstruction;
	 * 
	 * } else { LOG.error(EwiUserSession .getInstance() .getLoggerUtil()
	 * .logMessage( "Error while trying to get the process instruction : " +
	 * processIstructionId));
	 * 
	 * return null; }
	 * 
	 * } catch (UnirestException e1) {
	 * 
	 * LOG.error( EwiUserSession .getInstance() .getLoggerUtil() .logMessage( "Error
	 * while trying to get the process instruction List: ", e1), e1); return null;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public static JSONObject updateProcessInstruction(JSONObject
	 * processInstructionSelected, String processInstructionStatus , Integer
	 * documentId) { HttpResponse<JsonNode> httpResponseSaveProcessInstruction =
	 * null; JSONObject objectContainerJson = new JSONObject();
	 * 
	 * String processInstructionId = processInstructionSelected.getString("_id");
	 * JSONObject procesInstructionToUpdateJson = EpiUtils
	 * .pullCompleteProcessInstruction(processInstructionId);
	 * 
	 * procesInstructionToUpdateJson.remove("_id"); JSONArray linesJsonArray =
	 * procesInstructionToUpdateJson .getJSONArray("lines");
	 * 
	 * for (int i = 0; i < linesJsonArray.length(); i++) { JSONObject lineJSON =
	 * linesJsonArray.getJSONObject(i);
	 * 
	 * if (lineJSON.getInt("id") ==
	 * processInstructionSelected.getJSONObject("lines") .getInt("id")) {
	 * lineJSON.put("status",processInstructionStatus); } }
	 * 
	 * if(processInstructionStatus.equals(EpiUtils.APPROVED_STATUS)) { JSONObject
	 * approvedBy = JSONConvertUtils
	 * .createApproverJson(EwiUserSession.getInstance().getUserRoles()
	 * .getUserMaster());
	 * 
	 * procesInstructionToUpdateJson.put("approvedBy", approvedBy);
	 * procesInstructionToUpdateJson.put("ewiDocumentId", documentId);
	 * 
	 * }else if(processInstructionStatus.equals(EpiUtils.REJECTED_STATUS)) {
	 * JSONObject rejectedBy = JSONConvertUtils
	 * .createRejectJson(EwiUserSession.getInstance().getUserRoles()
	 * .getUserMaster());
	 * 
	 * procesInstructionToUpdateJson.put("rejectedBy", rejectedBy); }
	 * 
	 * objectContainerJson.put("processInstruction", procesInstructionToUpdateJson);
	 * objectContainerJson.put("_id", processInstructionId);
	 * 
	 * try { httpResponseSaveProcessInstruction = Unirest
	 * .post(EwiUserSession.getInstance().getNodeApiUrl() +
	 * EpiRestURIConstants.POST_PROCESS_INSTRUCTIONS_EDIT)
	 * .headers(EpiRestURIConstants.setupRequestHeaders())
	 * .body(objectContainerJson.toString()).asJson();
	 * 
	 * if (httpResponseSaveProcessInstruction.getStatus() == 200) { return
	 * procesInstructionToUpdateJson; }
	 * 
	 * } catch (UnirestException e1) { LOG.error("Error while trying to save the
	 * process instruction " , e1); }
	 * 
	 * return null; }
	 * 
	 * 
	 * public static void expireEwiProcessInstruction(Integer documentId) {
	 * //DocumentWs documentWs = new DocumentWs(); DocumentWsComplete
	 * documentWsComplete = new DocumentWsComplete(); //SimpleDateFormat sdf = new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 * 
	 * 
	 * //ST String expiryTimeStr = "00:00:00"; XMLGregorianCalendar clientCalendar =
	 * null; String timeZoneStr = "America/Los_Angeles"; if
	 * (EwiUserSession.getInstance().getConfigurations().containsKey("EWICATCONF009"))
	 * { timeZoneStr = (String)
	 * EwiUserSession.getInstance().getConfigurations().get( "EWICATCONF009"); }
	 * 
	 * //USe the current date try {
	 * 
	 * TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
	 * 
	 * //Get Effectivity Date at client time zone clientCalendar =
	 * DateUtil.convertDatesToClientTimeZone( null, expiryTimeStr, timeZone);
	 * 
	 * 
	 * } catch (ParseException e) {
	 * 
	 * LOG.error(EwiUserSession.getInstance().getLoggerUtil()
	 * .logMessage(e.getMessage())); } catch (DatatypeConfigurationException de){
	 * LOG.error(EwiUserSession.getInstance().getLoggerUtil()
	 * .logMessage(de.getMessage())); }
	 * 
	 * 
	 * documentWsComplete.setExpiryDateTime(clientCalendar);
	 * documentWsComplete.setTimeZone(timeZoneStr);
	 * 
	 * 
	 * //EN
	 * 
	 * 
	 * 
	 * //documentWs.setEffectivityDate(sdf.format(new Date()));
	 * //documentWs.setExpiryDate(sdf.format(new Date()));
	 * //documentWs.setDocumentId(documentId);
	 * documentWsComplete.setDocumentId(documentId);
	 * 
	 * try {
	 * 
	 * UpdateDocumentResponseWs response = BeanLocator.getDocumentWSBean(
	 * EwiUserSession.getInstance().getEnvTarget())
	 * .updateDocumentDates(documentWsComplete,
	 * EwiUserSession.getInstance().getSecurityToken());
	 * 
	 * } catch (WebServiceException e) { LOG.error( EwiUserSession .getInstance()
	 * .getLoggerUtil() .logMessage( "Web Service Exception while creating
	 * signature.", e), e);
	 * 
	 * } catch (Exception e) { LOG.error( EwiUserSession .getInstance()
	 * .getLoggerUtil() .logMessage( "Unknown Exception while creating signature.",
	 * e), e);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * 
	 * public static void activateEwiProcessInstruction(Integer documentId) {
	 * 
	 * DocumentWsComplete documentWsComplete = new DocumentWsComplete();
	 * 
	 * String effectivityTimeStr = "00:00:00"; XMLGregorianCalendar clientCalendar =
	 * null; String timeZoneStr = "America/Los_Angeles"; if
	 * (EwiUserSession.getInstance().getConfigurations().containsKey("EWICATCONF009"))
	 * { timeZoneStr = (String)
	 * EwiUserSession.getInstance().getConfigurations().get( "EWICATCONF009"); }
	 * 
	 * //USe the current date try {
	 * 
	 * TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
	 * 
	 * //Get Effectivity Date at client time zone clientCalendar =
	 * DateUtil.convertDatesToClientTimeZone( null, effectivityTimeStr, timeZone);
	 * 
	 * 
	 * } catch (ParseException e) {
	 * 
	 * LOG.error(EwiUserSession.getInstance().getLoggerUtil()
	 * .logMessage(e.getMessage())); } catch (DatatypeConfigurationException de){
	 * LOG.error(EwiUserSession.getInstance().getLoggerUtil()
	 * .logMessage(de.getMessage())); }
	 * 
	 * 
	 * documentWsComplete.setEffectivityDateTime(clientCalendar);
	 * documentWsComplete.setDocumentId(documentId);
	 * documentWsComplete.setTimeZone(timeZoneStr);
	 * 
	 * try {
	 * 
	 * UpdateDocumentResponseWs response = BeanLocator.getDocumentWSBean(
	 * EwiUserSession.getInstance().getEnvTarget())
	 * .updateDocumentDates(documentWsComplete,
	 * EwiUserSession.getInstance().getSecurityToken());
	 * 
	 * } catch (WebServiceException e) { LOG.error( EwiUserSession .getInstance()
	 * .getLoggerUtil() .logMessage( "Web Service Exception while creating
	 * signature.", e), e); e.printStackTrace();
	 * 
	 * } catch (Exception e) { LOG.error( EwiUserSession .getInstance()
	 * .getLoggerUtil() .logMessage( "Unknown Exception while creating signature.",
	 * e), e); e.printStackTrace();
	 * 
	 * }
	 * 
	 * } public static boolean compareProcessesWithoutPosition(JSONObject
	 * firstProcess,JSONObject secondProcess) { String firstProcessCode = null;
	 * String secondProcessCode = null; try{ firstProcessCode =
	 * (String)parseJsonObject( firstProcess, "processCode"); secondProcessCode =
	 * (String)parseJsonObject( secondProcess, "processCode");
	 * if(firstProcessCode.equalsIgnoreCase(secondProcessCode)) { return true; }
	 * 
	 * }catch(Exception e){ e.printStackTrace(); return false; }
	 * 
	 * 
	 * return false; } public static boolean compareProcesses(JSONObject
	 * firstProcess, JSONObject secondProcess){
	 * 
	 * String firstProcessCode = null; String secondProcessCode = null; int
	 * firstProcessPosition = 0; int secondProcessPosition = 0;
	 * 
	 * try{ firstProcessCode = (String)parseJsonObject( firstProcess,
	 * "processCode"); secondProcessCode = (String)parseJsonObject( secondProcess,
	 * "processCode"); firstProcessPosition = (Integer)parseJsonObject(
	 * firstProcess, "position"); secondProcessPosition = (Integer)parseJsonObject(
	 * secondProcess, "position");
	 * 
	 * if(firstProcessCode.equalsIgnoreCase(secondProcessCode) &&
	 * firstProcessPosition == secondProcessPosition){ return true; }
	 * 
	 * }catch(Exception e){ e.printStackTrace(); return false; }
	 * 
	 * 
	 * return false; }
	 * 
	 * public static boolean compareLayouts(JSONObject firstLayout, JSONObject
	 * secondLayout){
	 * 
	 * JSONArray firstLayoutProcessArray = (JSONArray)parseJsonObject(firstLayout,
	 * "lineProcess"); JSONArray secondLayoutProcessArray =
	 * (JSONArray)parseJsonObject(secondLayout, "lineProcess"); JSONObject
	 * firstLayoutProcess = null; JSONObject secondLayoutProcess = null; int
	 * processCount = 0;
	 * 
	 * if(firstLayoutProcessArray.length() != secondLayoutProcessArray.length()){
	 * return false; }
	 * 
	 * processCount = firstLayoutProcessArray.length();
	 * 
	 * for(int i = 0 ; i < processCount ; i++){ firstLayoutProcess =
	 * firstLayoutProcessArray.getJSONObject(i); secondLayoutProcess =
	 * secondLayoutProcessArray.getJSONObject(i);
	 * 
	 * if(!compareProcesses(firstLayoutProcess, secondLayoutProcess)){ return false;
	 * }
	 * 
	 * }
	 * 
	 * return true;
	 * 
	 * }
	 * 
	 * public static List<String> getUnAvailableProcess(JSONObject
	 * targetLayout,JSONObject sourceLayout){
	 * 
	 * JSONArray sourceLayoutProcessArray = (JSONArray)parseJsonObject(sourceLayout,
	 * "lineProcess"); JSONArray targetLayoutProcessArray =
	 * (JSONArray)parseJsonObject(targetLayout, "lineProcess"); JSONObject
	 * sourceLayoutProcess = null; JSONObject targetLayoutProcess = null; int
	 * sourceLayoutprocessCount = 0; List<String> processUnAvailable=new
	 * ArrayList(); sourceLayoutprocessCount = sourceLayoutProcessArray.length();
	 * 
	 * LOG.info("sourceLayoutProcessArray--"+sourceLayoutProcessArray.toString());
	 * LOG.info("targetLayoutProcessArray--"+targetLayoutProcessArray.toString());
	 * for(int process = 0 ; process < sourceLayoutprocessCount ; process++){
	 * sourceLayoutProcess = sourceLayoutProcessArray.getJSONObject(process);
	 * if(!checkProcessAvailaility(sourceLayoutProcess,targetLayoutProcessArray))
	 * processUnAvailable.add((String)parseJsonObject(sourceLayoutProcess,
	 * "processCode")); } return processUnAvailable; }
	 * 
	 * public static List<String> getLayoutProcessComparisionCount(JSONObject
	 * targetLayout, JSONObject sourceLayout){
	 * 
	 * JSONArray sourceLayoutProcessArray = (JSONArray)parseJsonObject(sourceLayout,
	 * "lineProcess"); JSONArray targetLayoutProcessArray =
	 * (JSONArray)parseJsonObject(targetLayout, "lineProcess"); JSONObject
	 * sourceLayoutProcess = null; JSONObject targetLayoutProcess = null; int
	 * sourceLayoutprocessCount = 0; int processAvailableCount=0; List<String>
	 * processAvailable=new ArrayList(); sourceLayoutprocessCount =
	 * sourceLayoutProcessArray.length();
	 * 
	 * for(int process = 0 ; process < sourceLayoutprocessCount ; process++){
	 * sourceLayoutProcess = sourceLayoutProcessArray.getJSONObject(process);
	 * if(checkProcessAvailaility(sourceLayoutProcess,targetLayoutProcessArray)){
	 * processAvailableCount++;
	 * processAvailable.add((String)parseJsonObject(sourceLayoutProcess,
	 * "processCode")); } } //return sourceLayoutprocessCount; return
	 * processAvailable; }
	 * 
	 * public static boolean checkProcessAvailaility(JSONObject sourceLayoutProcess,
	 * JSONArray targetLayoutProcessArray) {
	 * 
	 * for (int process = 0; process < targetLayoutProcessArray.length(); process++)
	 * { if (compareProcessesWithoutPosition(sourceLayoutProcess,
	 * targetLayoutProcessArray.getJSONObject(process))) { return true; }
	 * 
	 * } return false; }
	 * 
	 * public static boolean checkSpecficProcessAvailaility(String processCode,
	 * JSONObject layoutProcess) {
	 * 
	 * JSONArray layoutProcessArray = (JSONArray)parseJsonObject(layoutProcess,
	 * "lineProcess");
	 * 
	 * try { for (int process = 0; process < layoutProcessArray.length(); process++)
	 * { String currentProcessCode = (String)
	 * parseJsonObject(layoutProcessArray.getJSONObject(process), "processCode"); if
	 * (currentProcessCode.equalsIgnoreCase(processCode)) { return true; } }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); return false; }
	 * 
	 * return false; }
	 * 
	 * public static JSONObject getProcessFromLayout(JSONObject layout, String
	 * reqdProcessCode, Integer reqdPosition){
	 * 
	 * if(layout == null || reqdProcessCode == null ||
	 * reqdProcessCode.equalsIgnoreCase("")){ return null; }
	 * 
	 * JSONArray processArray = (JSONArray)parseJsonObject(layout, "lineProcess");
	 * 
	 * return getProcessFromProcessArray(processArray, reqdProcessCode,
	 * reqdPosition);
	 * 
	 * }
	 **************************/
	/*
	 * public static JSONObject getProcessFromProcessArray(JSONArray processArray,
	 * String reqdProcessCode, Integer reqdPosition){
	 * 
	 * if(processArray == null || processArray.length() == 0 || reqdProcessCode ==
	 * null || reqdProcessCode.equalsIgnoreCase("")){ return null; }
	 * 
	 * JSONObject processJson = null; String processCode = null; Integer position =
	 * -1; int layoutLength = processArray.length();
	 * 
	 * for(int i = 0 ; i < layoutLength ; i++){ processJson =
	 * processArray.getJSONObject(i); processCode =
	 * (String)parseJsonObject(processJson, "processCode"); position =
	 * (Integer)parseJsonObject(processJson, "position");
	 * if(processCode.equalsIgnoreCase(reqdProcessCode) &&
	 * position.equals(reqdPosition)){ return processJson; }else{ processJson =
	 * null; processCode = null; }
	 * 
	 * }
	 * 
	 * return processJson;
	 * 
	 * 
	 * }
	 */

	/**************************
	 * 
	 * public static JSONObject getProcessFromProcessArray(JSONArray processArray,
	 * String reqdProcessCode, Integer reqdPosition) {
	 * 
	 * if (processArray == null || processArray.length() == 0 || reqdProcessCode ==
	 * null || reqdProcessCode.equalsIgnoreCase("")) { return null; }
	 * 
	 * JSONObject processJson = null; String processCode = null; Integer position =
	 * -1; int layoutLength = processArray.length();
	 * 
	 * for (int i = 0; i < layoutLength; i++) { processJson =
	 * processArray.getJSONObject(i); processCode = (String)
	 * parseJsonObject(processJson, "processCode"); position = (Integer)
	 * parseJsonObject(processJson, "position"); if
	 * (processCode.equalsIgnoreCase(reqdProcessCode)) { return processJson; } else
	 * { processJson = null; processCode = null; }
	 * 
	 * }
	 * 
	 * return processJson;
	 * 
	 * }
	 * 
	 * public static boolean isIdenticalLayoutMatch(JSONArray targetLayoutArray,
	 * JSONObject sourceLayout) { JSONObject targetLayout = null; targetLayout =
	 * targetLayoutArray.getJSONObject(0); if (compareLayouts(sourceLayout,
	 * targetLayout)) { return true; } return false; }
	 * 
	 * public static JSONObject getMatchingLayout(JSONArray targetLayoutArray ,
	 * JSONObject layout){
	 * 
	 * JSONObject matchingLayout = null;
	 * 
	 * for(int i = 0 ; i < targetLayoutArray.length() ; i++){ matchingLayout =
	 * targetLayoutArray.getJSONObject(i); if(compareLayouts(layout,
	 * matchingLayout)){ return matchingLayout; } matchingLayout = null; }
	 * 
	 * return matchingLayout;
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * public static Boolean checkValidApprover(JSONObject obj) { Boolean
	 * isValidApprover = false;
	 * 
	 * JSONArray approversJsonArray = (JSONArray)parseJsonObject( obj, "approvers");
	 * 
	 * for (int index = 0; index < approversJsonArray.length(); index++) {
	 * JSONObject approverJson = (JSONObject) approversJsonArray.get(index);
	 * 
	 * if (approverJson.getString("username").equalsIgnoreCase(
	 * EwiUserSession.getInstance().getUserRoles().getUserMaster() .getUserName()))
	 * { isValidApprover = true; break; }
	 * 
	 * }
	 * 
	 * return isValidApprover; }
	 * 
	 * public static Boolean checkValidApprover(JSONObject obj,int approverLevel) {
	 * Boolean isValidApprover = false;
	 * 
	 * JSONArray approversJsonArray = (JSONArray)parseJsonObject( obj, "approvers");
	 * 
	 * for (int index = 0; index < approversJsonArray.length(); index++) {
	 * JSONObject approverJson = (JSONObject) approversJsonArray.get(index);
	 * 
	 * if(approverJson.has("approverLevel")){
	 * 
	 * if (approverJson.getInt("approverLevel") == approverLevel &&
	 * approverJson.getString("username").equalsIgnoreCase(
	 * EwiUserSession.getInstance().getUserRoles().getUserMaster() .getUserName()))
	 * { isValidApprover = true; break; }
	 * 
	 * }else{
	 * 
	 * if (approverJson.getString("username").equalsIgnoreCase(
	 * EwiUserSession.getInstance().getUserRoles().getUserMaster() .getUserName()))
	 * { isValidApprover = true; break; }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * return isValidApprover; }
	 * 
	 * public static JSONArray getSelectedApproversForLevel( JSONArray
	 * selectedApproversArray, int approverLevel){
	 * 
	 * JSONArray selectedApproversCurrentLevel = new JSONArray();
	 * 
	 * for(int i = 0 ; i < selectedApproversArray.length() ; i++){
	 * 
	 * JSONObject obj = selectedApproversArray.getJSONObject(i);
	 * if(obj.has("approverLevel") && obj.getInt("approverLevel") == approverLevel)
	 * selectedApproversCurrentLevel.put(obj);
	 * 
	 * if(!obj.has("approverLevel") && approverLevel == 1)
	 * selectedApproversCurrentLevel.put(obj);
	 * 
	 * }
	 * 
	 * 
	 * return selectedApproversCurrentLevel; }
	 * 
	 * 
	 **************************/
}
