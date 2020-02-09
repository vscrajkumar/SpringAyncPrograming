package com.flex.adapter.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flex.adapter.client.BeanLocator;
import com.flex.adapter.client.UserMasterWsComparable;
import com.flex.adapter.constants.AppConstants;
import com.flex.adapter.model.Erp;
import com.flex.adapter.model.ErpCompany;
import com.flex.adapter.model.ErpCustomer;
import com.flex.adapter.model.ErpUser;
import com.flex.adapter.model.LoginUser;
import com.flex.adapter.model.LogoutUser;
import com.flex.adapter.model.UserCustomer;
import com.flex.adapter.model.UsersErp;
import com.flex.adapter.repository.ErpBusinessUnitsMapRepository;
import com.flex.adapter.repository.ErpCompanyRepository;
import com.flex.adapter.repository.ErpCustomerRepository;
import com.flex.adapter.repository.ErpUserRepository;
import com.flextronics.flexware.ws.client.authentication.CredentialsWs;
import com.flextronics.flexware.ws.client.authentication.FlexwareTokenWs;
import com.flextronics.flexware.ws.client.authentication.SolutionRoleWs;
import com.flextronics.flexware.ws.client.authentication.UserMasterWs;
import com.flextronics.flexware.ws.client.authentication.UserRolesWs;
import com.flextronics.flexware.ws.client.searchuser.MasterDataElementWs;
import com.flextronics.flexware.ws.client.searchuser.MasterDataObjectWs;
import com.flextronics.flexware.ws.client.searchuser.SearchUserData;
import com.flextronics.flexware.ws.client.searchuser.SolutionCodeWs;
import com.flextronics.flexware.ws.client.searchuser.SolutionRoleCodeWs;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Scope;


@Service
public class FlexwareServiceImpl implements FlexwareService {
	private Logger LOG = LoggerFactory.getLogger(FlexwareServiceImpl.class);
	private UserRolesWs userRoles;
	private Map<String, String> flexToken = new HashMap<String, String>();
	private JSONArray allCustomerArray = new JSONArray();
	private JSONArray allErpCompanyArray = new JSONArray();

	@Autowired
	FlexstationService flexstationService;
	
	@Autowired
	AsyncService asyncService;
	
	@Resource
	ErpCompanyRepository erpCompanyRepository;
	
	@Resource
	ErpCustomerRepository erpCustomerRepository;
	
	@Resource
	ErpUserRepository erpUserRepository;
	
	@Override
	public JSONObject validateUser(LoginUser loginUser) throws JSONException {
		LOG.info("validateUser start");
		HttpResponse<JsonNode> httpResponseSaveProcessInstruction = null;
		CredentialsWs credentialsWS = new CredentialsWs();
		credentialsWS.setUsername(loginUser.getUserName());
		credentialsWS.setPassword(loginUser.getPassWord());
		credentialsWS.setSolutionCode(AppConstants.EWI_SOLUTION_CODE);

		try {

			FlexwareTokenWs flexwareTokenWs = BeanLocator.getAuthenticationWSBean(AppConstants.FLEXWARE_TARGET)
					.authenticate(credentialsWS);
			if (flexwareTokenWs != null) {
				LOG.info("UnserService -- after getting the flexwareTokenWs");
				FlexwareTokenWs token = new FlexwareTokenWs();
				token.setToken(flexwareTokenWs.getToken());
				try {
					LOG.info("Getting user roles! ");
					userRoles = BeanLocator.getAuthenticationWSBean((AppConstants.FLEXWARE_TARGET)).getUserRoles(token);
				} catch (Exception e) {

					LOG.info("Error while getting user profile roles -  " + e);
					e.printStackTrace();
				}

				// iterate thru lamda is not working....
				// userRoles.getRoles().forEach(l-> { roles.put ( new
				// JSONObject().put( l.getCode(),l.getName()) )} );

				JSONObject user = new JSONObject();
				user.put("userId", userRoles.getUserMaster().getUserId());
				user.put("userName", userRoles.getUserMaster().getUserName());
				user.put("firstName", userRoles.getUserMaster().getFirstName());
				user.put("lastName", userRoles.getUserMaster().getLastName());
				// user.put("email", userRoles.getUserMaster().getEmail());
				String email;
				try {
					email = userRoles.getUserMaster().getEmail();
					String[] emailUsersourceArray = email.split("\\|");
					if (emailUsersourceArray.length > 0) {
						user.put("email", emailUsersourceArray[0]);
					} else {
						user.put("email", email);
					}
				} catch (Exception e1) {
					LOG.info("Error while split mail emailid -  " + e1);
					e1.printStackTrace();
				}

				user.put("token", " ");
				Map<String, String> userRolesmap = new HashMap<>();

				JSONObject role = new JSONObject();
				for (SolutionRoleWs index : userRoles.getRoles()) {
					/*
					 * role = new JSONObject(); role.put("roleCode", index.getCode());
					 * role.put("roleName", index.getName());
					 */
					userRolesmap.put(index.getCode(), index.getName());
					// roles.put(role);
				}
				if (userRolesmap.containsKey("EPIADMIN")) {

					role.put("roleCode", "EPIADMIN");
					role.put("roleName", userRolesmap.get("EPIADMIN"));
					LOG.info("EPIADMIN ");
				} else if (userRolesmap.containsKey("EPIAPPROV")) {

					role.put("roleCode", "EPIAPPROV");
					role.put("roleName", userRolesmap.get("EPIAPPROV"));
					LOG.info("EPIAPPROV ");
				} else if (userRolesmap.containsKey("EPIENG")) {

					role.put("roleCode", "EPIENG");
					role.put("roleName", userRolesmap.get("EPIENG"));
					LOG.info("EPIENG ");
				}

				validateAndCreateWebToken(userRoles.getUserMaster(),flexwareTokenWs.getToken());

				JSONObject response = new JSONObject();
				response.put("roles", role);
				response.put("user", user);
				response.put("status", "success");
				try {
					JSONObject roleCode=new JSONObject();
					roleCode.put("roleCode",role.get("roleCode"));
					httpResponseSaveProcessInstruction = Unirest
							.post(AppConstants.EPI_TARGET + AppConstants.GET_ROLE_WISE_ACCESS)
							.headers(AppConstants.setupRequestHeaders()).body(roleCode.toString()).asJson();
					JSONObject roleWiseAccessDetails=new JSONObject();

					if (httpResponseSaveProcessInstruction.getStatus() == 200) {
						LOG.info("Inside success---return procesInstructionToUpdateJson");
						roleWiseAccessDetails=httpResponseSaveProcessInstruction.getBody().getObject()
								.getJSONObject("roleWiseAccessDetails");
						
					//	response.put("roleWiseAccessDetails",httpResponseSaveProcessInstruction.getBody().getObject()
						//.getJSONObject("roleWiseAccessDetails"));
						//return procesInstructionToUpdateJson;
					}
					JSONObject processInstruction=(JSONObject) roleWiseAccessDetails.get("processInstruction"); 
					JSONObject piStatusValidation=(JSONObject)  processInstruction.get("pIStatus");
					
					piStatusValidation.put("In Progress", piStatusValidation.get("inProgress"));
					piStatusValidation.remove("inProgress");
					piStatusValidation.put("Approved", piStatusValidation.get("approved"));
					piStatusValidation.remove("approved");
					piStatusValidation.put("Pending", piStatusValidation.get("pending"));
					piStatusValidation.remove("pending");
					piStatusValidation.put("Partially Approved", piStatusValidation.get("partiallyApproved"));
					piStatusValidation.remove("partiallyApproved");
					piStatusValidation.put("Obsolete", piStatusValidation.get("obsolete"));
					piStatusValidation.remove("obsolete");
					piStatusValidation.put("Pending Disable", piStatusValidation.get("pendingDisable"));
					piStatusValidation.remove("pendingDisable");
					piStatusValidation.put("Rejected", piStatusValidation.get("rejected"));
					piStatusValidation.remove("rejected");
					piStatusValidation.put("Disable", piStatusValidation.get("disable"));
					piStatusValidation.remove("disable");
					
					response.put("roleWiseAccessDetails",roleWiseAccessDetails);
				} catch (UnirestException e1) {
					LOG.info("Error while trying to save  the process instruction " + e1.getMessage());
				}
				
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
	public JSONObject validateToken(LoginUser token) throws JSONException {
		LOG.info("validateToken start");

		try {

			LOG.info("UnserService -- after getting the flexwareTokenWs");
			FlexwareTokenWs token1 = new FlexwareTokenWs();
			token1.setToken(token.getToken());
			LOG.info("Token -- " + token.getToken());
			try {
				LOG.info("Getting user roles! ");
				userRoles = BeanLocator.getAuthenticationWSBean((AppConstants.FLEXWARE_TARGET)).getUserRoles(token1);
			} catch (Exception e) {

				LOG.info("Error while getting user profile roles -  " + e);
				e.printStackTrace();
			}

			// iterate thru lamda is not working....
			// userRoles.getRoles().forEach(l-> { roles.put ( new
			// JSONObject().put( l.getCode(),l.getName()) )} );

			JSONObject user = new JSONObject();
			user.put("userId", userRoles.getUserMaster().getUserId());
			user.put("userName", userRoles.getUserMaster().getUserName());
			user.put("firstName", userRoles.getUserMaster().getFirstName());
			user.put("lastName", userRoles.getUserMaster().getLastName());
			// user.put("email", userRoles.getUserMaster().getEmail());
			String email;
			try {
				email = userRoles.getUserMaster().getEmail();
				String[] emailUsersourceArray = email.split("\\|");
				if (emailUsersourceArray.length > 0) {
					user.put("email", emailUsersourceArray[0]);
				} else {
					user.put("email", email);
				}
			} catch (Exception e1) {
				LOG.info("Error while split mail emailid -  " + e1);
				e1.printStackTrace();
			}

			user.put("token", " ");
			Map<String, String> userRolesmap = new HashMap<>();

			JSONObject role = new JSONObject();
			for (SolutionRoleWs index : userRoles.getRoles()) {
				/*
				 * role = new JSONObject(); role.put("roleCode", index.getCode());
				 * role.put("roleName", index.getName());
				 */
				userRolesmap.put(index.getCode(), index.getName());
				// roles.put(role);
			}
			if (userRolesmap.containsKey("EPIADMIN")) {

				role.put("roleCode", "EPIADMIN");
				role.put("roleName", userRolesmap.get("EPIADMIN"));
				LOG.info("EPIADMIN ");
			} else if (userRolesmap.containsKey("EPIAPPROV")) {

				role.put("roleCode", "EPIAPPROV");
				role.put("roleName", userRolesmap.get("EPIAPPROV"));
				LOG.info("EPIAPPROV ");
			} else if (userRolesmap.containsKey("EPIENG")) {

				role.put("roleCode", "EPIENG");
				role.put("roleName", userRolesmap.get("EPIENG"));
				LOG.info("EPIENG ");
			}

			validateAndCreateWebToken(userRoles.getUserMaster(),token.getToken());

			JSONObject response = new JSONObject();
			response.put("roles", role);
			response.put("user", user);
			response.put("status", "success");

			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");

		return response;
	}

	@Override
	@Scope(value="globalSession")
	public boolean validateAndCreateWebToken(UserMasterWs userMasterWs,String value) throws JSONException{
		LOG.info("validateAndCreateWebToken start");

		flexstationService.insertUserToken(userMasterWs,value);
		
		if (flexToken.containsKey(userMasterWs.getUserName().toLowerCase())) {
			flexToken.remove(userMasterWs.getUserName().toLowerCase());
			flexToken.put(userMasterWs.getUserName().toLowerCase(), value);
			return true;
		} else {
			flexToken.put(userMasterWs.getUserName().toLowerCase(), value);
			return true;
		}
	}

	@Override
	@Scope(value="globalSession")
	public String getWebToken(String keyName) {
		LOG.info("getWebToken start");

		return flexToken.get(keyName.toLowerCase());
	}

	@Override
	@Scope(value="globalSession")
	public boolean removeWebToken(String keyName) {
		LOG.info("removeWebToken start");

		if (flexToken.containsKey(keyName.toLowerCase())) {
			flexToken.remove(keyName.toLowerCase());
			return true;
		} else {
			return true;
		}
	}
	
	@Override
	public JSONObject userLogoutAction(LogoutUser logoutUser) throws JSONException {
		System.out.println("below userLogoutAction ");
		flexstationService.removeUserToken(logoutUser.getUserName(),userRoles.getUserMaster());
		flexToken = new HashMap<String, String>();
		JSONObject status=new JSONObject();
		status.put("status", "success");
		return status;
	}

	@Override
	@Scheduled(cron = "0 10 12 * * ?")
	@PostConstruct
	public void getAllErpCompanyList() {
		
		LOG.info("getAllErpCompanyList Start");
		
		try {
			
			allErpCompanyArray = new JSONArray();
			List<ErpCompany> erpCompanyList = null;
			erpCompanyList = erpCompanyRepository.getAllErpCompany();

			JSONObject erp;
			for (ErpCompany index : erpCompanyList) {
				erp = new JSONObject();
				erp.put("erpId", index.getErpId());
				erp.put("erpCode", index.getErpCode());
				erp.put("erpName", index.getErpName());
				allErpCompanyArray.put(erp);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("getAllErpCompanyList End");

	}


	@Override
	public JSONObject getUsersMappedErpListUser(UsersErp usersErp) throws JSONException {

		LOG.info("getUsersMappedErpListUser start");
		
		List<ErpCompany> erpList ;
		List<String> rolesList;

		try {
			
			rolesList=new ArrayList<String>(Arrays.asList(AppConstants.EWI_SOLUTION_ROLE_CODE,AppConstants.EPI_PROCESS_ENGINEER,AppConstants.EPI_ADMINISTRATOR));
			
			CompletableFuture<Boolean> isAllErpCompanyMappingCallback=asyncService.isUserMappingAllERPCompany(AppConstants.EWI_SOLUTION_CODE,usersErp.getUserId(),rolesList);
			CompletableFuture<List<ErpCompany>> userMappingErpCompanyDetailsCallback=asyncService.getErpCompany(AppConstants.EWI_SOLUTION_CODE,usersErp.getUserId(),rolesList);

			
			if(isAllErpCompanyMappingCallback.get()==false){
		        CompletableFuture.allOf(isAllErpCompanyMappingCallback, userMappingErpCompanyDetailsCallback).join();
		        erpList=userMappingErpCompanyDetailsCallback.get();
				JSONObject erp;
				JSONArray erps = new JSONArray();
		        for (ErpCompany index : erpList) {
		        	erp = new JSONObject();
					erp.put("erpId", index.getErpId());
					erp.put("erpCode", index.getErpCode());
					erp.put("erpName", index.getErpName());
					erps.put(erp);
				}
		        
		        JSONObject response = new JSONObject();
		        response.put("erps", erps);
				response.put("status", "success");
				LOG.info("getUsersMappedErpListUser end");
				return response;
				
			}else{
				userMappingErpCompanyDetailsCallback.cancel(true);
				JSONObject response = new JSONObject();
				response.put("erps", allErpCompanyArray);
				response.put("status", "success");
				LOG.info("getUsersMappedErpListUser end");
				return response;
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getUsersMappedErpListUser end");
		return response;
	}

	@Override
	@Scheduled(cron = "0 10 12 * * ?")
	@PostConstruct
	public void getAllCustomersList() {
		
		LOG.info("getAllCustomersList Start");
		
		try {
			
			allCustomerArray = new JSONArray();
			List<ErpCustomer> customerList = null;
			customerList = erpCustomerRepository.getAllErpCustomer();

			JSONObject customer;
			for (ErpCustomer index : customerList) {
				customer = new JSONObject();
				customer.put("customerId", index.getCustomerId());
				customer.put("customerCode", index.getCustomerCode());
				customer.put("customerName", index.getCustomerName());
				allCustomerArray.put(customer);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("getAllCustomersList End");

	}
	
	@Override
	public JSONObject getUserMappedCustomersList(UserCustomer userCustomer) throws JSONException {

		LOG.info("getUserMappedCustomersList start");

		List<ErpCustomer> customerList;
		List<String> rolesCodeList;

		try {
		
			rolesCodeList = new ArrayList<String>(Arrays.asList(AppConstants.EWI_SOLUTION_ROLE_CODE,
					AppConstants.EPI_PROCESS_ENGINEER, AppConstants.EPI_ADMINISTRATOR));

			CompletableFuture<Boolean> isAllCustomerMappingCallback=asyncService.isAllCustomerMapping(AppConstants.ALL_CUSTOMER_CODE,AppConstants.EWI_SOLUTION_CODE,userCustomer.getUserId(),rolesCodeList);
			CompletableFuture<List<ErpCustomer>> userMappingCustomerDetailsCallback=asyncService.getCustomerDetails(AppConstants.EWI_SOLUTION_CODE,userCustomer.getUserId(),rolesCodeList);
		
			if(isAllCustomerMappingCallback.get()==false){
		        CompletableFuture.allOf(isAllCustomerMappingCallback, isAllCustomerMappingCallback).join();
		        customerList=userMappingCustomerDetailsCallback.get();
				JSONObject customer;
				JSONArray customerArray = new JSONArray();
		        for (ErpCustomer index : customerList) {
					customer = new JSONObject();
					customer.put("customerId", index.getCustomerId());
					customer.put("customerCode", index.getCustomerCode());
					customer.put("customerName", index.getCustomerName());
					customerArray.put(customer);
				}
		        
		        JSONObject response = new JSONObject();
				response.put("customer", customerArray);
				response.put("status", "success");
				LOG.info("getUserMappedCustomersList end");
				return response;
				
			}else{
				userMappingCustomerDetailsCallback.cancel(true);
				JSONObject response = new JSONObject();
				response.put("customer", allCustomerArray);
				response.put("status", "success");
				LOG.info("getUserMappedCustomersList end");
				return response;
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getUserMappedCustomersList end");
		return response;
	}

	@Override
	public JSONObject getInstructionCreatorsList(Erp erp) throws JSONException {
		
		LOG.info("getInstructionCreatorsList start");
		
		List<String> rolesCodeList;
		List<ErpUser> erpInstructionsCreatorList=null;
		JSONArray instructionCreatorsArray = new JSONArray();
		JSONObject instructionCreator=null;
		List<String> userNameAganistCreator = new ArrayList<String>();

		try {
			rolesCodeList = new ArrayList<String>(Arrays.asList(AppConstants.EWI_SOLUTION_ROLE_CODE,
					AppConstants.EPI_PROCESS_ENGINEER, AppConstants.EPI_ADMINISTRATOR));
			erpInstructionsCreatorList = erpUserRepository.getErpUserList(AppConstants.EWI_SOLUTION_CODE, erp.getErpCode(), rolesCodeList);
			
			if(erpInstructionsCreatorList.size() > 0) {
				for (ErpUser user : erpInstructionsCreatorList) {
					if (!userNameAganistCreator.contains(user.getUsername())) {
						userNameAganistCreator.add(user.getUsername());
						
						instructionCreator = new JSONObject();
						instructionCreator.put("userId", user.getUserId());
						instructionCreator.put("username", user.getUsername());
						instructionCreator.put("firstName", user.getFirstName());
						instructionCreator.put("lastName", user.getLastName());
						instructionCreator.put("fullName", user.getFirstName() + " " + user.getLastName());
						String email;
						try {
							email = user.getEmail();
							String[] emailUsersourceArray = email.split("\\|");
							if (emailUsersourceArray.length > 0) {
								instructionCreator.put("email", emailUsersourceArray[0]);
							} else {
								instructionCreator.put("email", email);
							}
						} catch (Exception e1) {
							LOG.info("Error while split mail emailid -  " + e1);
							e1.printStackTrace();
						}
					}
					instructionCreatorsArray.put(instructionCreator);
				}
				JSONObject response = new JSONObject();
				response.put("instructionCreatorsList", instructionCreatorsArray);
				response.put("status", "success");
				LOG.info("getInstructionCreatorsList end");
				return response;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getInstructionCreatorsList end");
		return response;
	}

	@Override
	public JSONObject getInstructionApproversList(Erp erp) throws JSONException {
		
		LOG.info("getInstructionApproversList start");
		
		List<String> rolesCodeList;
		List<String> erpCodeList;
		List<ErpUser> erpInstructionsApproverList=null;
		JSONArray instructionApproversArray = new JSONArray();
		JSONObject instructionApprover=null;
		List<String> userNameAganistCreator = new ArrayList<String>();

		try {
			rolesCodeList = new ArrayList<String>(Arrays.asList(AppConstants.EPI_APPROVER));
			erpCodeList = new ArrayList<String>(Arrays.asList(erp.getErpCode(), "GMSALL"));

			erpInstructionsApproverList = erpUserRepository.getErpApproverList(AppConstants.EWI_SOLUTION_CODE, erpCodeList, rolesCodeList, erp.getUsername());
			
			if(erpInstructionsApproverList.size() > 0) {
				for (ErpUser user : erpInstructionsApproverList) {
					if (!userNameAganistCreator.contains(user.getUsername())) {
						userNameAganistCreator.add(user.getUsername());
						
						instructionApprover = new JSONObject();
						instructionApprover.put("userId", user.getUserId());
						instructionApprover.put("username", user.getUsername());
						instructionApprover.put("firstName", user.getFirstName());
						instructionApprover.put("lastName", user.getLastName());
						instructionApprover.put("fullName", user.getFirstName() + " " + user.getLastName());
						String email;
						try {
							email = user.getEmail();
							String[] emailUsersourceArray = email.split("\\|");
							if (emailUsersourceArray.length > 0) {
								instructionApprover.put("email", emailUsersourceArray[0]);
							} else {
								instructionApprover.put("email", email);
							}
						} catch (Exception e1) {
							LOG.info("Error while split mail emailid -  " + e1);
							e1.printStackTrace();
						}
					}
					instructionApproversArray.put(instructionApprover);
				}
				JSONObject response = new JSONObject();
				response.put("instructionApproversList", instructionApproversArray);
				response.put("status", "success");
				LOG.info("getInstructionApproversList end");
				return response;
			}
		}catch(Exception e){
			e.printStackTrace();
			
		}
		
		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getInstructionCreatorsList end");
		return response;
	}

	@Override
	public JSONObject layoutNotification(Erp erp) throws JSONException {
		LOG.info("getInstructionCreatorsList start");

		List<String> userNameAganistCreator = new ArrayList<String>();
		List<String> roleList = new ArrayList<String>();
		roleList.add(AppConstants.EWI_SOLUTION_ROLE_CODE);
		roleList.add(AppConstants.EPI_PROCESS_ENGINEER);
		roleList.add(AppConstants.EPI_ADMINISTRATOR);

		List<UserMasterWsComparable> instructionCreatorsList = null;

		SearchUserData filters = new SearchUserData();

		String erpCode = erp.getErpCode();

		FlexwareTokenWs flexwareTokenWs = new FlexwareTokenWs();
		flexwareTokenWs.setToken(getWebToken(erp.getSecurityToken()));

		filters.setFlexwareToken(flexwareTokenWs);

		SolutionCodeWs solutionCodeWs = new SolutionCodeWs();
		solutionCodeWs.setCode(AppConstants.EWI_SOLUTION_CODE);

		filters.setSolutionCode(solutionCodeWs);

		MasterDataElementWs masterDataElementWs = new MasterDataElementWs();
		masterDataElementWs.setCode(AppConstants.ERP_COMPANY_CODE);
		MasterDataObjectWs objectWs = new MasterDataObjectWs();
		objectWs.setCode(erpCode);
		masterDataElementWs.getObjects().add(objectWs);
		filters.getMasterData().add(masterDataElementWs);

		for (String role : roleList) {
			SolutionRoleCodeWs solutionRoleCodeWs = new SolutionRoleCodeWs();
			solutionRoleCodeWs.setCode(role);
			filters.getSolutionRoleCodes().add(solutionRoleCodeWs);
			try {
				instructionCreatorsList = BeanLocator.getSearchUserWsClient().serachUsers(filters);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JSONArray instructionCreatorsArray = new JSONArray();

		JSONObject instructionCreator;

		if (instructionCreatorsList != null && instructionCreatorsList.size() > 0) {

			for (UserMasterWsComparable user : instructionCreatorsList) {

				if (!userNameAganistCreator.contains(user.getUserName())) {
					userNameAganistCreator.add(user.getUserName());

					instructionCreator = new JSONObject();
					instructionCreator.put("userId", user.getUserId());
					instructionCreator.put("username", user.getUserName());
					instructionCreator.put("firstName", user.getFirstName());
					instructionCreator.put("lastName", user.getLastName());
					instructionCreator.put("fullName", user.getFirstName() + " " + user.getLastName());
					String email;
					try {
						email = user.getEmail();
						String[] emailUsersourceArray = email.split("\\|");
						if (emailUsersourceArray.length > 0) {
							instructionCreator.put("email", emailUsersourceArray[0]);
						} else {
							instructionCreator.put("email", email);
						}
					} catch (Exception e1) {
						LOG.info("Error while split mail emailid -  " + e1);
						e1.printStackTrace();
					}

					instructionCreatorsArray.put(instructionCreator);
				}
			}

			JSONObject response = new JSONObject();
			response.put("layoutNotificationList", instructionCreatorsArray);
			response.put("status", "success");

			return response;

		} else {
			JSONObject response = new JSONObject();
			response.put("status", "failure");

			return response;
		}

	}
	
	/************************************************************
	 
	 	@Override
	public JSONObject getUsersMappedErpListUser(UsersErp usersErp) throws JSONException {

		LOG.info("getUsersMappedErpListUser start");
		
	    ObjectMapper objectMapper = new ObjectMapper();

		List<ErpCompany> erpList ;
		List<String> rolesList;

		try {
			
			rolesList=new ArrayList<String>(Arrays.asList(AppConstants.EWI_SOLUTION_ROLE_CODE,AppConstants.EPI_PROCESS_ENGINEER,AppConstants.EPI_ADMINISTRATOR));
			erpList=erpCompanyRepository.getErpCompany(AppConstants.EWI_SOLUTION_CODE, usersErp.getUserId(), rolesList);
		
		   // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		   // String arrayToJson = null;
	       // arrayToJson = objectMapper.writeValueAsString(erpList);
	        
	        JSONObject erp;

			JSONArray erps = new JSONArray();
			
			for (ErpCompany index : erpList) {
				erp = new JSONObject();
				erp.put("erpId", index.getErpId());
				erp.put("erpCode", index.getErpCode());
				erp.put("erpName", index.getErpName());
				erps.put(erp);
			}
			
	        JSONObject response = new JSONObject();
			response.put("erps", erps);
			response.put("status", "success");
			
			LOG.info("getUsersMappedErpListUser End");
			//System.out.println("getUsersMappedErpListUser--"+response.toString());

			return response;

		}catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getUsersMappedErpListUser End");

		return response;
	}

	@Override
	public JSONObject getInstructionCreatorsList(Erp erp) throws JSONException {
		LOG.info("getInstructionCreatorsList start");

		List<String> userNameAganistCreator = new ArrayList<String>();
		List<String> roleList = new ArrayList<String>();
		roleList.add(AppConstants.EWI_SOLUTION_ROLE_CODE);
		roleList.add(AppConstants.EPI_PROCESS_ENGINEER);
		roleList.add(AppConstants.EPI_ADMINISTRATOR);

		List<UserMasterWsComparable> instructionCreatorsList = null;

		SearchUserData filters = new SearchUserData();

		String erpCode = erp.getErpCode();

		FlexwareTokenWs flexwareTokenWs = new FlexwareTokenWs();
		flexwareTokenWs.setToken(getWebToken(erp.getSecurityToken()));

		filters.setFlexwareToken(flexwareTokenWs);

		SolutionCodeWs solutionCodeWs = new SolutionCodeWs();
		solutionCodeWs.setCode(AppConstants.EWI_SOLUTION_CODE);

		filters.setSolutionCode(solutionCodeWs);

		MasterDataElementWs masterDataElementWs = new MasterDataElementWs();
		masterDataElementWs.setCode(AppConstants.ERP_COMPANY_CODE);
		MasterDataObjectWs objectWs = new MasterDataObjectWs();
		objectWs.setCode(erpCode);
		masterDataElementWs.getObjects().add(objectWs);
		filters.getMasterData().add(masterDataElementWs);

		for (String role : roleList) {
			SolutionRoleCodeWs solutionRoleCodeWs = new SolutionRoleCodeWs();
			solutionRoleCodeWs.setCode(role);
			filters.getSolutionRoleCodes().add(solutionRoleCodeWs);
			try {
				// doubt -- need to check with Deepa

				instructionCreatorsList = BeanLocator.getSearchUserWsClient().serachUsers(filters);
				// LOG.info("instructionCreatorsList--"+instructionCreatorsList.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JSONArray instructionCreatorsArray = new JSONArray();

		JSONObject instructionCreator;

		if (instructionCreatorsList.size() > 0) {

			for (UserMasterWsComparable user : instructionCreatorsList) {

				if (!userNameAganistCreator.contains(user.getEmail())) {
					userNameAganistCreator.add(user.getEmail());

					instructionCreator = new JSONObject();
					instructionCreator.put("userId", user.getUserId());
					instructionCreator.put("username", user.getUserName());
					instructionCreator.put("firstName", user.getFirstName());
					instructionCreator.put("lastName", user.getLastName());
					instructionCreator.put("fullName", user.getFirstName() + " " + user.getLastName());
					// instructionCreator.put("email", user.getEmail());
					String email;
					try {
						email = user.getEmail();
						String[] emailUsersourceArray = email.split("\\|");
						if (emailUsersourceArray.length > 0) {
							instructionCreator.put("email", emailUsersourceArray[0]);
						} else {
							instructionCreator.put("email", email);
						}
					} catch (Exception e1) {
						LOG.info("Error while split mail emailid -  " + e1);
						e1.printStackTrace();
					}

					instructionCreatorsArray.put(instructionCreator);
				}
			}

			JSONObject response = new JSONObject();
			response.put("instructionCreatorsList", instructionCreatorsArray);
			response.put("status", "success");

			return response;

		} else {
			JSONObject response = new JSONObject();
			response.put("status", "failure");

			return response;
		}

	}

	
	
	@Override
	public JSONObject getInstructionApproversList(Erp erp) throws JSONException {
		LOG.info("getInstructionApproversList start");

		List<String> userNameAganistCreator = new ArrayList<String>();
		List<String> roleList = new ArrayList<String>();
		roleList.add(AppConstants.EPI_APPROVER);

		List<UserMasterWsComparable> instructionApproversList = null;

		SearchUserData filters = new SearchUserData();

		String erpCode = erp.getErpCode();

		FlexwareTokenWs flexwareTokenWs = new FlexwareTokenWs();
		flexwareTokenWs.setToken(getWebToken(erp.getSecurityToken()));

		System.out.println("getWebToken(erp.getSecurityToken())--"+getWebToken(erp.getSecurityToken()));
		filters.setFlexwareToken(flexwareTokenWs);

		SolutionCodeWs solutionCodeWs = new SolutionCodeWs();
		solutionCodeWs.setCode(AppConstants.EWI_SOLUTION_CODE);

		filters.setSolutionCode(solutionCodeWs);

		MasterDataElementWs masterDataElementWs = new MasterDataElementWs();
		masterDataElementWs.setCode(AppConstants.ERP_COMPANY_CODE);
		MasterDataObjectWs objectWs = new MasterDataObjectWs();
		objectWs.setCode(erpCode);
		masterDataElementWs.getObjects().add(objectWs);
		filters.getMasterData().add(masterDataElementWs);

		for (String role : roleList) {
			SolutionRoleCodeWs solutionRoleCodeWs = new SolutionRoleCodeWs();
			solutionRoleCodeWs.setCode(role);
			filters.getSolutionRoleCodes().add(solutionRoleCodeWs);
			try {
				// doubt -- need to check with Deepa

				instructionApproversList = BeanLocator.getSearchUserWsClient().serachUsers(filters);
				// LOG.info("instructionApproversList--"+instructionApproversList.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JSONArray instructionApproversArray = new JSONArray();

		JSONObject instructionApprover;

		if (instructionApproversList.size() > 0) {

			for (UserMasterWsComparable user : instructionApproversList) {

				if (!userNameAganistCreator.contains(user.getEmail())) {
					userNameAganistCreator.add(user.getEmail());
					if (!erp.getUsername().equals(user.getUserName())) {
						instructionApprover = new JSONObject();
						instructionApprover.put("userId", user.getUserId());
						instructionApprover.put("username", user.getUserName());
						instructionApprover.put("firstName", user.getFirstName());
						instructionApprover.put("lastName", user.getLastName());
						instructionApprover.put("fullName", user.getFirstName() + " " + user.getLastName());
						// instructionApprover.put("email", user.getEmail());

						String email;
						try {
							email = user.getEmail();
							String[] emailUsersourceArray = email.split("\\|");
							if (emailUsersourceArray.length > 0) {
								instructionApprover.put("email", emailUsersourceArray[0]);
							} else {
								instructionApprover.put("email", email);
							}
						} catch (Exception e1) {
							LOG.info("Error while split mail emailid -  " + e1);
							e1.printStackTrace();
						}

						instructionApproversArray.put(instructionApprover);
					}
				}
			}

			JSONObject response = new JSONObject();
			response.put("instructionApproversList", instructionApproversArray);
			response.put("status", "success");

			return response;

		} else {
			JSONObject response = new JSONObject();
			response.put("status", "failure");

			return response;
		}

	}

	************************************************************/
	/****************************************************************************************
	@Override
	public JSONObject getUserMappedCustomersList(UserCustomer userCustomer) throws JSONException {
		LOG.info("getUserMappedCustomersList start");

		MasterdataFilterWs filter = new MasterdataFilterWs();
		filter.setMasterDataElementCode(AppConstants.CUSTOMER_CODE);
		filter.setSolutionCode(AppConstants.EWI_SOLUTION_CODE);
		filter.setUserId(userCustomer.getUserId());
		List<MasterdataWs> customerList = new ArrayList<MasterdataWs>();

		try {
			customerList = BeanLocator.getFlexwareWSBean(AppConstants.FLEXWARE_TARGET).getMasterData(filter);

			if (customerList.size() > 0) {
				JSONArray customerArray = new JSONArray();

				JSONObject customer;
				for (MasterdataWs index : customerList) {
					customer = new JSONObject();
					customer.put("customerId", index.getMasterDataObjectId());
					customer.put("customerCode", index.getMasterDataObjectCode());
					customer.put("customerName", index.getMasterDataObjectName());
					customerArray.put(customer);
				}

				JSONObject response = new JSONObject();
				response.put("customer", customerArray);
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
	****************************************************************************************/

	/*******************************************************************************
	@Override
	public JSONObject getUsersMappedErpListUser(UsersErp usersErp) throws JSONException {

		LOG.info("getUsersMappedErpListUser start");
		LOG.info("AppConstants.ERP_COMPANY_CODE--" + AppConstants.ERP_COMPANY_CODE);
		LOG.info("AppConstants.EWI_SOLUTION_CODE--" + AppConstants.EWI_SOLUTION_CODE);
		LOG.info("Integer.valueOf(usersErp.getUserId())--" + Integer.valueOf(usersErp.getUserId()));

		List<MasterdataWs> erpList = new ArrayList<MasterdataWs>();

		MasterdataFilterWs masterdataFilterWs = new MasterdataFilterWs();
		masterdataFilterWs.setMasterDataElementCode(AppConstants.ERP_COMPANY_CODE);
		masterdataFilterWs.setSolutionCode(AppConstants.EWI_SOLUTION_CODE);
		masterdataFilterWs.setUserId(Integer.valueOf(usersErp.getUserId()));

		try {
			erpList = BeanLocator.getFlexwareWSBean(AppConstants.FLEXWARE_TARGET).getMasterData(masterdataFilterWs);

			if (erpList.size() > 0) {
				JSONArray erps = new JSONArray();

				JSONObject erp;
				for (MasterdataWs index : erpList) {
					erp = new JSONObject();
					erp.put("erpId", index.getMasterDataObjectId());
					erp.put("erpCode", index.getMasterDataObjectCode());
					erp.put("erpName", index.getMasterDataObjectName());
					erps.put(erp);
				}

				JSONObject response = new JSONObject();
				response.put("erps", erps);
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
	public JSONObject getUserMappedCustomersList(UserCustomer userCustomer) throws JSONException {

		LOG.info("getUserMappedCustomersList start");
		
	    ObjectMapper objectMapper = new ObjectMapper();

		List<ErpCustomer> customerList ;
		List<String> rolesList;

		try {
			
			rolesList=new ArrayList<String>(Arrays.asList(AppConstants.EWI_SOLUTION_ROLE_CODE,AppConstants.EPI_PROCESS_ENGINEER,AppConstants.EPI_ADMINISTRATOR));
			customerList=erpCustomerRepository.getErpCustomer(AppConstants.EWI_SOLUTION_CODE, userCustomer.getUserId(), rolesList);
		
			LOG.info("After retreiving from repository...");

			if (customerList.size() > 0) {
				JSONArray customerArray = new JSONArray();

				JSONObject customer;
				for (ErpCustomer index : customerList) {
					customer = new JSONObject();
					customer.put("customerId", index.getCustomerId());
					customer.put("customerCode", index.getCustomerCode());
					customer.put("customerName", index.getCustomerName());
					customerArray.put(customer);
				}

				JSONObject response = new JSONObject();
				response.put("customer", customerArray);
				response.put("status", "success");

				// LOG.info(response.toString());
				LOG.info("getUserMappedCustomersList End");
				return response;

			}
		    //objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    //String arrayToJson = null;
	        //arrayToJson = objectMapper.writeValueAsString(customerList);
	        
	        //JSONObject response = new JSONObject();
			//response.put("customer", arrayToJson);
			//response.put("customer", arrayToJson);

			//response.put("status", "success");

		}catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getUserMappedCustomersList End");

		return response;
	}
	*******************************************************************************/

	/*******************************************************************************
	
	@Override
	public JSONObject getUserMappedCustomersList(UserCustomer userCustomer) throws JSONException {

		LOG.info("getUserMappedCustomersList start");

		List<ErpCustomer> customerList;
		List<String> rolesCodeList;
		JSONArray customerArray = new JSONArray();

		try {

			rolesCodeList = new ArrayList<String>(Arrays.asList(AppConstants.EWI_SOLUTION_ROLE_CODE,
					AppConstants.EPI_PROCESS_ENGINEER, AppConstants.EPI_ADMINISTRATOR));

			customerList=erpCustomerRepository.isAllCustomerMapping(AppConstants.ALL_CUSTOMER_CODE,AppConstants.EWI_SOLUTION_CODE,userCustomer.getUserId(),rolesCodeList);
			System.out.println("isAllCustomerMapping..in try--customerList.size--"+customerList.size());

			customerList=erpCustomerRepository.getErpCustomer(AppConstants.EWI_SOLUTION_CODE,userCustomer.getUserId(),rolesCodeList);		
			System.out.println("getErpCustomer..in try--customerList.size--"+customerList.size());
			 
			IsAllCustomerAsyncImpl isAllCustomerAsyncImpl = new IsAllCustomerAsyncImpl(AppConstants.ALL_CUSTOMER_CODE,
					AppConstants.EWI_SOLUTION_CODE, userCustomer.getUserId(), rolesCodeList);

			CustomerDetailsAsyncImpl customerDetailsAsyncImpl = new CustomerDetailsAsyncImpl(
					AppConstants.EWI_SOLUTION_CODE, userCustomer.getUserId(), rolesCodeList);

			FutureTask<Boolean> futureTask1 = new FutureTask<Boolean>(isAllCustomerAsyncImpl);
			FutureTask<List<ErpCustomer>> futureTask2 = new FutureTask<List<ErpCustomer>>(customerDetailsAsyncImpl);
			ExecutorService executor = Executors.newFixedThreadPool(2);
			executor.execute(futureTask1);
			executor.execute(futureTask2);
			
			System.out.println("After scheduling to execute the two future task");
			
			while (true) {
				try {
					if (futureTask1.isDone()) {

						Boolean futureTask1Returnval;
						futureTask1Returnval = futureTask1.get();
						if (futureTask1Returnval.booleanValue() == true) {
							futureTask2.cancel(true);
							customerArray = allCustomerArray;
							break;
						} else {
							if (futureTask2.isDone()) {
								customerList = futureTask2.get();
								if (customerList.size() > 0) {
									JSONObject customer;
									for (ErpCustomer index : customerList) {
										customer = new JSONObject();
										customer.put("customerId", index.getCustomerId());
										customer.put("customerCode", index.getCustomerCode());
										customer.put("customerName", index.getCustomerName());
										customerArray.put(customer);
										break;
									}
								}
							}
						}
					}

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}

			LOG.info("After retreiving from repository...");

			JSONObject response = new JSONObject();
			response.put("customer", customerArray);
			response.put("status", "success");

			LOG.info("getUserMappedCustomersList End");
			return response;

		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "failure");
		LOG.info("getUserMappedCustomersList End");

		return response;
	}
	*******************************************************************************/


	
}
