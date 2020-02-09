package com.flex.adapter.controller;

import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flex.adapter.constants.AppConstants;
import com.flex.adapter.model.AssemblyFilter;
import com.flex.adapter.model.AttachmentWrapper;
import com.flex.adapter.model.EprBusinessUnit;
import com.flex.adapter.model.Erp;
import com.flex.adapter.model.LayoutFilter;
import com.flex.adapter.model.LoginUser;
import com.flex.adapter.model.LogoutUser;
import com.flex.adapter.model.PIDocumentFilter;
import com.flex.adapter.model.UploadAssembly;
import com.flex.adapter.model.UserCustomer;
import com.flex.adapter.model.UsersErp;
import com.flex.adapter.service.FlexstationService;
import com.flex.adapter.service.FlexwareService;
 import static java.lang.System.out;

@CrossOrigin(origins = "*")
@RestController
public class RestApiController {

	private Logger LOG = LoggerFactory.getLogger(RestApiController.class);

	@Autowired
	FlexwareService flexwareService;

	@Autowired
	FlexstationService flexstationService;

	@RequestMapping(value = AppConstants.USER_VALIATION, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> validateUser(@RequestBody LoginUser loginUser) throws JSONException {

		LOG.info("validateUser controller");
		JSONObject documentResults = flexwareService.validateUser(loginUser);

		return ResponseEntity.ok(documentResults.toString());
	}

	@RequestMapping(value = AppConstants.TOKEN_VALIATION, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> validateToken(@RequestBody LoginUser token) throws JSONException {

		LOG.info("validateUser controller");
		JSONObject documentResults = flexwareService.validateToken(token);

		return ResponseEntity.ok(documentResults.toString());
	}

	@RequestMapping(value = AppConstants.GET_ERPS, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getUserMappedErpsList(@RequestBody UsersErp usersErp)
			throws JSONException {

		LOG.info("getUserMappedErpsList controller");

		//if(flexstationService.isAuthorised(usersErp.getUserId())){
			JSONObject erpResults = flexwareService.getUsersMappedErpListUser(usersErp);

			return ResponseEntity.ok(erpResults.toString());
		//}
		//return ResponseEntity.ok("not authorised");
	}

/*	@RequestMapping(value = AppConstants.GET_BUSINESSUNITS, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getErpBusinessUnitList(@RequestBody EprBusinessUnit eprBusinessUnit)
			throws JSONException {

		LOG.info("getErpBusinessUnitList controller");
		JSONObject erpResults = flexstationService.getErpBusinessUnitList(eprBusinessUnit);

		return ResponseEntity.ok(erpResults.toString());
	}
*/
	@RequestMapping(value = AppConstants.POST_GET_BU, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getBusinessUnit(@RequestBody EprBusinessUnit eprBusinessUnit)
			throws JSONException {

		LOG.info("getErpBusinessUnitList controller");
		//if(flexstationService.isAuthorised(eprBusinessUnit.getUserId())){
			JSONObject erpResults = flexstationService.getBusinessUnit(eprBusinessUnit.getErpId());
			return ResponseEntity.ok(erpResults.toString());
		//}
		//return ResponseEntity.ok("not authorised");
	}
	
	
	@RequestMapping(value = AppConstants.GET_CUSTOMERS, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getUserMappedCustomersList(@RequestBody UserCustomer userCustomer)
			throws JSONException {

		LOG.info("getUserMappedCustomersList controller");

		//if (flexstationService.isAuthorised(userCustomer.getUserId())) {
			JSONObject erpResults = flexwareService.getUserMappedCustomersList(userCustomer);

			return ResponseEntity.ok(erpResults.toString());
		//}
		//return ResponseEntity.ok("not authorised");
	}

	/*@RequestMapping(value = AppConstants.GET_DOCUMENT_CNT, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getAssemblySummaryList(@RequestBody AssemblyFilter assemblyFilter)
			throws JSONException {

		LOG.info("getAssemblySummaryList controller");

		JSONObject erpResults = flexstationService.getAssemblySummaryList(assemblyFilter);

		return ResponseEntity.ok(erpResults.toString());
	}*/

	@RequestMapping(value = AppConstants.GET_DOCUMENT_CNT_FETURE_TASK, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getAssemblySummaryListWithFutureTask(
			@RequestBody AssemblyFilter assemblyFilter) throws JSONException {

		LOG.info("getAssemblySummaryList controller");

		//if (flexstationService.isAuthorised(assemblyFilter.getUserId())) {

			JSONObject erpResults = flexstationService.getAssemblySummaryListWithFutureTask(assemblyFilter);

			return ResponseEntity.ok(erpResults.toString());
		//}
		//return ResponseEntity.ok("not authorised");
	}

	// @RequestMapping(value ="/", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<String> welcome() {
		return ResponseEntity.ok("AEPI");
	}

	@RequestMapping(value = AppConstants.GET_LINE_LIST, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getLineList(@RequestBody AssemblyFilter assemblyFilter)
			throws JSONException {

		LOG.info("getAssemblySummaryList controller");

		//if (flexstationService.isAuthorised(assemblyFilter.getUserId())) {

			JSONObject lineList = flexstationService.getLineList(assemblyFilter);

			return ResponseEntity.ok(lineList.toString());
		//}

		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.GET_CREATOR_LIST, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getInstructionCretorList(@RequestBody Erp erp) throws JSONException {

		LOG.info("getInstructionCretorList controller");

		//if (flexstationService.isAuthorised(erp.getUsername())) {

		JSONObject instructionCreatorsList = flexwareService.getInstructionCreatorsList(erp);

		return ResponseEntity.ok(instructionCreatorsList.toString());
		//}		
		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.GET_APPROVER_LIST, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getInstructionApproverList(@RequestBody Erp erp) throws JSONException {

		LOG.info("getInstructionApproverList controller");
		
		JSONObject instructionApproversList = flexwareService.getInstructionApproversList(erp);

		return ResponseEntity.ok(instructionApproversList.toString());
		
		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.POST_ASSEMBLY_UPLOAD, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> UploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("erpCode") String erpCode, @RequestParam("token") String securityToken) throws JSONException {

		LOG.info("UploadFile controller");

		Path fileStorageLocation;

		// prepare result to return it in client's browser
		String fileName = file.getOriginalFilename();
		JSONObject response = new JSONObject();
		String error_msg;
		Path targetLocation = null;
		if (fileName.contains(".csv")) {
			fileStorageLocation = Paths.get("/tmp/upload/").toAbsolutePath().normalize();

			try {
				if (!Files.exists(Paths.get("/tmp/upload/").toAbsolutePath().normalize()))
					Files.createDirectories(fileStorageLocation);
			} catch (Exception ex) {
				error_msg = "Could not create the directory where the uploaded files will be stored.";
				response.put("status", "failure");
				response.put("message", error_msg);
				ex.printStackTrace();
			}
			try {
				fileName = StringUtils.cleanPath(file.getOriginalFilename());
				if(fileName.contains("/")) {
					fileName = fileName.substring(fileName.lastIndexOf("/")+1);
				}
			} catch (Exception e) {
				error_msg = "Filename contains invalid path sequence";
				response.put("status", "failure");
				response.put("message", error_msg);
				e.printStackTrace();
			}
			try {
				// Copy file to the target location (Replacing existing file
				// with the same name)
				targetLocation = fileStorageLocation.resolve(fileName);
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			} catch (AccessDeniedException ae) {
				error_msg = fileName + " is already exist. Please upload new file";
				response.put("status", "failure");
				response.put("message", error_msg);
				ae.printStackTrace();
			} catch (Exception e) {
				error_msg = fileName + "-- File can't be saved on server. Please contact administrators";
				response.put("status", "failure");
				response.put("message", error_msg);
				e.printStackTrace();
			}
			try {
				UploadAssembly uploadAssembly = new UploadAssembly();
				uploadAssembly.setErpCode(erpCode);
				uploadAssembly.setSecurityToken(securityToken);
				response = flexstationService.UploadFile(targetLocation.toString(),
						Files.newBufferedReader(Paths.get(targetLocation.toUri())), uploadAssembly);
			} catch (Exception e) {
				error_msg = fileName + "-- File can't be validate file. Please contact administrators";
				e.printStackTrace();
			}
		} else {
			LOG.info("Inside not CSV file...");
			error_msg = "Invalid file.Please upload only CSV file. ";
			response.put("status", "failure");
			response.put("messgae", error_msg);

		}
		return ResponseEntity.ok(response.toString());

		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.POST_INSTRUCTION_LAYOUT_CHANGE, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> changeEwiInstructionStatusChange(@RequestBody LayoutFilter layoutFilter)
			throws JSONException {

		LOG.info("changeEwiInstructionStatusChange controller");

		//if(flexstationService.isAuthorised(layoutFilter.getSecurityToken())) {
		JSONObject lineList = flexstationService.checkAndRemoveEPIDocumentsByConfig(layoutFilter);
		return ResponseEntity.ok(lineList.toString());
		
		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.POST_APPROVE_EWI_DOCUMENT, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> generateEwiDocument(@RequestBody PIDocumentFilter processInstruction)
			throws JSONException {

		LOG.info("generateEwiDocument controller");

		JSONObject processInstructionSelected = flexstationService.approveProcessInstruction(processInstruction);
		return ResponseEntity.ok(processInstructionSelected.toString());

		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.POST_APPROVE_DISABLE_EWI_DOCUMENT, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> disableEwiDocument(@RequestBody PIDocumentFilter processInstruction)
			throws JSONException {

		LOG.info("generateEwiDocument controller");

		JSONObject processInstructionSelected = flexstationService.approveDisableProcessInstruction(processInstruction);
		return ResponseEntity.ok(processInstructionSelected.toString());

		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.POST_COPY_INSTRUCTION_ATTACHMENT, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> copyAttachments(@RequestBody List<AttachmentWrapper> attachmentWrapper)
			throws JSONException {

		LOG.info("copyAttachments controller");

		JSONObject documentResults = flexstationService.copyAttachments(attachmentWrapper);

		return ResponseEntity.ok(documentResults.toString());
		
		//return ResponseEntity.ok("not authorised");
	}

	@RequestMapping(value = AppConstants.GET_ASSEMBLY_LIST, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> getAssemblyList(@RequestBody AssemblyFilter assemblyFilter)
			throws JSONException {

		LOG.info("getAssemblyList controller");
		JSONObject assemblyResults = flexstationService.getAssemblyList(assemblyFilter);

		return ResponseEntity.ok(assemblyResults.toString());
		
		//return ResponseEntity.ok("not authorised");
	}
	
	@RequestMapping(value = AppConstants.POST_USER_LOG_OUT, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> userLogout(@RequestBody LogoutUser logoutUser) throws JSONException {

		LOG.info("validateUser controller");
		JSONObject userLoggoutStatus = flexwareService.userLogoutAction(logoutUser);

		return ResponseEntity.ok(userLoggoutStatus.toString());
		//return ResponseEntity.ok("not authorised");
	}
	
	@RequestMapping(value = AppConstants.POST_LAYOUT_NOTIFICATION, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> layoutNotfication(@RequestBody Erp erp) throws JSONException {

		LOG.info("getInstructionCretorList controller");

		JSONObject instructionCreatorsList = flexwareService.layoutNotification(erp);

		out.println("instructionCreatorsList--"+instructionCreatorsList.toString());
		return ResponseEntity.ok(instructionCreatorsList.toString());
		
		//return ResponseEntity.ok("not authorised");

	}
}
