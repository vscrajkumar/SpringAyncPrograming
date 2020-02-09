package com.flex.adapter.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.flex.adapter.model.Erp;
import com.flex.adapter.model.LoginUser;
import com.flex.adapter.model.LogoutUser;
import com.flex.adapter.model.UserCustomer;
import com.flex.adapter.model.UsersErp;
import com.flextronics.flexware.ws.client.authentication.UserMasterWs;

public interface FlexwareService {
    
	/**
	 * Get process instruction creator list.
	 * @param erp
	 * @return JSONObject - instructionCreatorsList(userId,username,firstName,lastName,fullName) and status.
	 * @throws JSONException
	 */
	public JSONObject getInstructionCreatorsList(Erp erp ) throws JSONException;
	
	/**
	 * Get process instruction approver list.
	 * @param erp
	 * @return JSONObject - instructionApproversList(userId,username,firstName,lastName,fullName) and status.
	 * @throws JSONException
	 */
	public JSONObject getInstructionApproversList(Erp erp ) throws JSONException;	
	
	public void getAllErpCompanyList();
	
	/**
	 * Get user mapped customer list.
	 * @param userCustomer
	 * @return JSONObject - customerId,customerCode,customerName and status.
	 * @throws JSONException
	 */
	public JSONObject getUserMappedCustomersList(UserCustomer userCustomer) throws JSONException;

	/**
	 * Get user mapped customer list.
	 * @param userCustomer
	 * @return JSONObject - customerId,customerCode,customerName and status.
	 * @throws JSONException
	 */
	public void getAllCustomersList() ;

	/**
	 * Get users mapped erp list.
	 * @param usersErp
	 * @return JSONObject - erpId,erpCode,erpName and status.
	 * @throws JSONException
	 */
	
	public JSONObject getUsersMappedErpListUser(UsersErp usersErp) throws JSONException;
	
	/**
	 * Validate user from flexware.
	 * @param loginUser
	 * @return JSONObject - roleCode,roleName,userId,userName,firstName,lastName,email,token and status.
	 * @throws JSONException
	 */
	public JSONObject validateUser(LoginUser loginUser) throws JSONException;
	
	/**
	 * Validate token.
	 * @param token
	 * @return JSONObject - roleCode,roleName,userId,userName,firstName,lastName,email,token and status.
	 * @throws JSONException
	 */
	public JSONObject validateToken(LoginUser token) throws JSONException;
	/**
	 * Validate and Create web token.
	 * @param str1
	 * @param str2
	 * @return boolean - true.
	 */
	public boolean validateAndCreateWebToken(UserMasterWs userMasterWs,String toekn) throws JSONException;
	
	/**
	 * Get web token.
	 * @param str
	 * @return String -Key name.
	 */
	public String getWebToken(String str);
	
	/**
	 * Remove web token.
	 * @param str
	 * @return boolean- true.
	 */
	public boolean removeWebToken(String str);
	
	/**
	 * Validate user from flexware.
	 * @param loginUser
	 * @return JSONObject - roleCode,roleName,userId,userName,firstName,lastName,email,token and status.
	 * @throws JSONException
	 */
	public JSONObject userLogoutAction(LogoutUser logoutUser) throws JSONException;
	
	/**
	 * Get layout notification users list.
	 * @param erp
	 * @return JSONObject - instructionApproversList(userId,username,firstName,lastName,fullName) and status.
	 * @throws JSONException
	 */
	public JSONObject layoutNotification(Erp erp ) throws JSONException;	
	
}
