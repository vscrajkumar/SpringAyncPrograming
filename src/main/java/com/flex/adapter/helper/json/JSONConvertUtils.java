package com.flex.adapter.helper.json;

import com.flextronics.flexware.ws.client.authentication.UserMasterWs;
import com.flex.adapter.helper.DateUtil;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class JSONConvertUtils {

	public static JSONObject createApproverJson(UserMasterWs userMasterWs) throws JSONException {
		JSONObject approverJson = new JSONObject();
		approverJson.put("username", userMasterWs.getUserName());

		String[] meailParts = userMasterWs.getEmail().split("\\|");

		approverJson.put("email", meailParts[0]);
		approverJson.put("fullName", userMasterWs.getFirstName() + " " + userMasterWs.getLastName());
		approverJson.put("approvalDate", DateUtil.dateToStr("yyyy-MM-dd  HH:mm:ss", new Date()));

		return approverJson;
	}

	public static JSONObject createRejectJson(UserMasterWs userMasterWs) throws JSONException {

		JSONObject approverJson = new JSONObject();
		approverJson.put("username", userMasterWs.getUserName());

		String[] meailParts = userMasterWs.getEmail().split("\\|");

		approverJson.put("email", meailParts[0]);
		approverJson.put("fullName", userMasterWs.getFirstName() + " " + userMasterWs.getLastName());
		approverJson.put("rejectDate", DateUtil.dateToStr("yyyy-MM-dd  HH:mm:ss", new Date()));

		return approverJson;
	}

	public static JSONObject createDisabledJson(UserMasterWs userMasterWs) throws JSONException {

		JSONObject approverJson = new JSONObject();
		approverJson.put("username", userMasterWs.getUserName());

		String[] meailParts = userMasterWs.getEmail().split("\\|");

		approverJson.put("email", meailParts[0]);
		approverJson.put("fullName", userMasterWs.getFirstName() + " " + userMasterWs.getLastName());
		approverJson.put("disabledDate", DateUtil.dateToStr("yyyy-MM-dd  HH:mm:ss", new Date()));

		return approverJson;
	}

	public static JSONObject extractUserInfoJsonObject(JSONObject objectJsonToParse, String keyRoot, String userInfoKey)
			throws JSONException {
		if (objectJsonToParse.has(keyRoot)) {
			JSONObject jsonObjectDate = objectJsonToParse.getJSONObject(keyRoot);

			JSONObject jsonObjectUserInfo = jsonObjectDate.getJSONObject(userInfoKey);

			return jsonObjectUserInfo;
		}
		return null;
	}

	public static JSONObject createAccessJson(UserMasterWs userMasterWs) throws JSONException {
		JSONObject accessJson = new JSONObject();
		accessJson.put("username", userMasterWs.getUserName());
		accessJson.put("firstName", userMasterWs.getFirstName());
		accessJson.put("lastName", userMasterWs.getLastName());
		accessJson.put("fullName", userMasterWs.getFirstName() + " " + userMasterWs.getLastName());
		accessJson.put("email", userMasterWs.getEmail().split("\\|")[0]);
		accessJson.put("accessedDate", DateUtil.dateToStr("yyyy-MM-dd  HH:mm:ss", new Date()));
		return accessJson;
	}
}
