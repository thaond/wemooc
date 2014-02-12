package com.liferay.lms;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletURL;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowStateException;

import com.liferay.lms.model.Course;
import com.liferay.lms.model.Module;
import com.liferay.lms.model.impl.ModuleImpl;
import com.liferay.lms.service.CourseLocalServiceUtil;
import com.liferay.lms.service.ModuleLocalServiceUtil;
import com.liferay.portal.kernel.cache.MultiVMPoolUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.announcements.EntryDisplayDateException;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.tls.util.liferay.patch.MethodKeyPatched;

/**
 * Portlet implementation class module
 */
public class modulePortlet extends MVCPortlet {



public static String SEPARATOR = "_";
	public static String IMAGEGALLERY_MAINFOLDER = "icons";
	public static String IMAGEGALLERY_PORTLETFOLDER = "module";
	public static String IMAGEGALLERY_MAINFOLDER_DESCRIPTION = "Module Image Uploads";
	public static String IMAGEGALLERY_PORTLETFOLDER_DESCRIPTION = "";
	
	//private long igFolderId;

	public void init() throws PortletException {

		// Edit Mode Pages
		editJSP = getInitParameter("edit-jsp");

		// Help Mode Pages
		helpJSP = getInitParameter("help-jsp");

		// View Mode Pages
		viewJSP = getInitParameter("view-jsp");

		// View Mode Edit module
		editmoduleJSP = getInitParameter("edit-module-jsp");
	}

	protected void include(String path, RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		PortletRequestDispatcher portletRequestDispatcher = getPortletContext()
				.getRequestDispatcher(path);

		if (portletRequestDispatcher == null) {
			// do nothing
			// _log.error(path + " is not a valid include");
		} else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}

	public void doView(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		
		Course course=null;
		try {
			course = CourseLocalServiceUtil.fetchByGroupCreatedId(themeDisplay.getScopeGroupId());
		} catch (SystemException e) {
			throw new IOException(e);
		}
		
		

		if(renderRequest.getWindowState().equals(LiferayWindowState.POP_UP)){
			String popUpAction = ParamUtil.getString(renderRequest, "popUpAction");
			if("addmodule".equals(popUpAction)){

					try {
						addmodulePopUp(renderRequest,renderResponse);
					} catch (NestableException e) {
					} 

			}
			else if("updatemodule".equals(popUpAction)){

					try {
						updatemodulePopUp(renderRequest,renderResponse);
					} catch (NestableException e) {
					} 		
					
			}
			else if("editmodule".equals(popUpAction)){
				editmodulePopUp(renderRequest,renderResponse);				
		}
		}
		
		
		String jsp = (String) renderRequest.getParameter("view");
		if (jsp == null || jsp.equals("")) {
			showViewDefault(renderRequest, renderResponse);
		} else if (jsp.equalsIgnoreCase("editmodule")) {
			try {
				showViewEditmodule(renderRequest, renderResponse);
			} catch (Exception ex) {
				_log.debug(ex);
				try {
					showViewDefault(renderRequest, renderResponse);
				} catch (Exception ex1) {
					_log.debug(ex1);
				}
			}
		}
	}
	public void doEdit(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		showEditDefault(renderRequest, renderResponse);
	}

	public void doHelp(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		include(helpJSP, renderRequest, renderResponse);
	}

	@SuppressWarnings("unchecked")
	public void showViewDefault(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getScopeGroupId();

		PermissionChecker permissionChecker = themeDisplay
				.getPermissionChecker();

		boolean hasAddPermission = permissionChecker.hasPermission(groupId,
				"com.liferay.lms.model", groupId, "ADD_MODULE");

		List<Module> tempResults = Collections.EMPTY_LIST;
		try {
				tempResults = ModuleLocalServiceUtil.findAllInGroup(groupId);
			

		} catch (Exception e) {
			_log.debug(e);
		}
		renderRequest.setAttribute("highlightRowWithKey", renderRequest.getParameter("highlightRowWithKey"));
		renderRequest.setAttribute("containerStart", renderRequest.getParameter("containerStart"));
		renderRequest.setAttribute("containerEnd", renderRequest.getParameter("containerEnd"));
		renderRequest.setAttribute("tempResults", tempResults);
		renderRequest.setAttribute("hasAddPermission", hasAddPermission);

		LiferayPortletResponse liferayPortletResponse=(LiferayPortletResponse)renderResponse;
		PortletURL addmoduleURL = liferayPortletResponse.createRenderURL();
		addmoduleURL.setWindowState(LiferayWindowState.POP_UP);
		addmoduleURL.setParameter("view", "editmodule");
		//addmoduleURL.setParameter("moduleId", "0");
		addmoduleURL.setParameter("editType", "add");
		
		renderRequest.setAttribute("addmoduleURL", addmoduleURL.toString());
		
		PortletURL moduleFilterURL = renderResponse.createRenderURL();
		moduleFilterURL.setParameter("javax.portlet.action", "doView");
		renderRequest.setAttribute("moduleFilterURL", moduleFilterURL.toString());

		include(viewJSP, renderRequest, renderResponse);
	}

	public void showViewEditmodule(RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {

		boolean isPopUp = renderRequest.getWindowState().equals(LiferayWindowState.POP_UP);
		
		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		
		SimpleDateFormat formatDia    = new SimpleDateFormat("dd");
		formatDia.setTimeZone(themeDisplay.getTimeZone());
		SimpleDateFormat formatMes    = new SimpleDateFormat("MM");
		formatMes.setTimeZone(themeDisplay.getTimeZone());		
		SimpleDateFormat formatAno    = new SimpleDateFormat("yyyy");
		formatAno.setTimeZone(themeDisplay.getTimeZone());		
		SimpleDateFormat formatHora   = new SimpleDateFormat("HH");
		formatHora.setTimeZone(themeDisplay.getTimeZone());		
		SimpleDateFormat formatMinuto = new SimpleDateFormat("mm");
		formatMinuto.setTimeZone(themeDisplay.getTimeZone());		

		LiferayPortletResponse liferayPortletResponse=(LiferayPortletResponse)renderResponse;
		PortletURL editmoduleURL = null;
		
		if(isPopUp){
			editmoduleURL = liferayPortletResponse.createRenderURL();
			editmoduleURL.setParameter("view", "editmodule");
		}
		else
		{
			editmoduleURL = liferayPortletResponse.createActionURL();
		}
		editmoduleURL.setWindowState(LiferayWindowState.POP_UP);
		
		String editType = ParamUtil.getString(renderRequest, "editType",(String)renderRequest.getAttribute("editType"));
		if ("edit".equalsIgnoreCase(editType)) {
			if(isPopUp){
				editmoduleURL.setParameter("popUpAction", "updatemodule");
			}
			else{
				editmoduleURL.setParameter("javax.portlet.action", "updatemodule");
			}
			
			long moduleId = 0;
			if(renderRequest.getAttribute("moduleId")!=null){
				moduleId = (Long)renderRequest.getAttribute("moduleId");
			}
			else{
				moduleId = ParamUtil.getLong(renderRequest, "resourcePrimKey");
			}
			
			Module module = ModuleLocalServiceUtil.getModule(moduleId);
			String title = module.getTitle();
			renderRequest.setAttribute("title", title);
			String description = module.getDescription(themeDisplay.getLocale())+"";
			renderRequest.setAttribute("description", description);
			String order = module.getOrdern()+"";
			renderRequest.setAttribute("ordern", order);
			String icon = module.getIcon()+"";
			renderRequest.setAttribute("icon", icon);
			if(module.getStartDate()==null)
			{
				module.setStartDate(new java.util.Date(System.currentTimeMillis()));
			}
			if(module.getEndDate()==null)
			{
				module.setEndDate(new java.util.Date(System.currentTimeMillis()+1000*84000*365));
			}
			renderRequest.setAttribute("startDateDia", formatDia.format(module.getStartDate()));
			renderRequest.setAttribute("startDateMes", formatMes.format(module.getStartDate()));
			renderRequest.setAttribute("startDateAno", formatAno.format(module.getStartDate()));
			String startDate = dateToJsp(renderRequest, module.getStartDate());
			renderRequest.setAttribute("startDate", startDate);
			renderRequest.setAttribute("endDateDia", formatDia.format(module.getEndDate()));
			renderRequest.setAttribute("endDateMes", formatMes.format(module.getEndDate()));
			renderRequest.setAttribute("endDateAno", formatAno.format(module.getEndDate()));
			String endDate = dateToJsp(renderRequest, module.getEndDate());
			renderRequest.setAttribute("endDate", endDate);
            renderRequest.setAttribute("module", module);
		} else {
			if(isPopUp){
				editmoduleURL.setParameter("popUpAction", "addmodule");
			}
			else{
				editmoduleURL.setParameter("javax.portlet.action", "addmodule");
			}
			Module errormodule = (Module) renderRequest.getAttribute("errormodule");
			if (errormodule != null) {
				if (editType.equalsIgnoreCase("update")) {
					editmoduleURL.setParameter("javax.portlet.action", "updatemodule");
                }
				renderRequest.setAttribute("module", errormodule);
				 renderRequest.setAttribute("startDateDia", formatDia.format(errormodule.getStartDate()));
	                renderRequest.setAttribute("startDateMes", formatMes.format(errormodule.getStartDate()));
	                renderRequest.setAttribute("startDateAno", formatAno.format(errormodule.getStartDate()));
					String startDate = dateToJsp(renderRequest,errormodule.getStartDate());
					renderRequest.setAttribute("startDate", startDate);
	                renderRequest.setAttribute("endDateDia", formatDia.format(errormodule.getEndDate()));
	                renderRequest.setAttribute("endDateMes", formatMes.format(errormodule.getEndDate()));
	                renderRequest.setAttribute("endDateAno", formatAno.format(errormodule.getEndDate()));
					String endDate = dateToJsp(renderRequest,errormodule.getEndDate());
					renderRequest.setAttribute("endDate", endDate);
			} else {
				ModuleImpl blankmodule = new ModuleImpl();
				blankmodule.setModuleId(0);
				blankmodule.setTitle("");
				blankmodule.setDescription("");
				blankmodule.setOrdern(0);
				blankmodule.setIcon(0);
				blankmodule.setStartDate(new Date());
				renderRequest.setAttribute("startDateDia", formatDia.format(blankmodule.getStartDate()));
				renderRequest.setAttribute("startDateMes", formatMes.format(blankmodule.getStartDate()));
				renderRequest.setAttribute("startDateAno", formatAno.format(blankmodule.getStartDate()));
				String startDate = dateToJsp(renderRequest, blankmodule.getStartDate());
				renderRequest.setAttribute("startDate", startDate);
				blankmodule.setEndDate(new Date());
				renderRequest.setAttribute("endDateDia", formatDia.format(blankmodule.getEndDate()));
				renderRequest.setAttribute("endDateMes", formatMes.format(blankmodule.getEndDate()));
				renderRequest.setAttribute("endDateAno", formatAno.format(blankmodule.getEndDate()));
				String endDate = dateToJsp(renderRequest, blankmodule.getEndDate());
				renderRequest.setAttribute("endDate", endDate);
				renderRequest.setAttribute("module", blankmodule);
			}

		}

	
		renderRequest.setAttribute("editmoduleURL", editmoduleURL.toString());
		
			include(editmoduleJSP, renderRequest, renderResponse);
		
	}

	private String dateToJsp(PortletRequest request, Date date) {
		PortletPreferences prefs = request.getPreferences();
		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
				WebKeys.THEME_DISPLAY);
		SimpleDateFormat format = new SimpleDateFormat(prefs.getValue("module-date-format", "yyyy/MM/dd"));
		format.setTimeZone(themeDisplay.getTimeZone());
		return format.format(date);
	}

	public void showEditDefault(RenderRequest renderRequest,
			RenderResponse renderResponse) throws PortletException, IOException {

		include(editJSP, renderRequest, renderResponse);
	}

	/* Portlet Actions */

	@ProcessAction(name = "newmodule")
	public void newmodule(ActionRequest request, ActionResponse response) {
		response.setRenderParameter("view", "editmodule");
		try {
			response.setWindowState(LiferayWindowState.POP_UP);
		} catch (WindowStateException e) {
			// TODO Auto-generated catch block
		}
		response.setRenderParameter("moduleId", "0");
		response.setRenderParameter("editType", "add");
	}
	

	@ProcessAction(name = "addmodule")
	public void addmodule(ActionRequest request, ActionResponse response) throws Exception {
            Module module = moduleFromRequest(request);
            ArrayList<String> errors = moduleValidator.validatemodule(module, request);
            ThemeDisplay themeDisplay = (ThemeDisplay) request
			.getAttribute(WebKeys.THEME_DISPLAY);

            if (errors.isEmpty()) {
				try {
					ModuleLocalServiceUtil.addmodule(module);
                	
                	response.setRenderParameter("view", "");
                	SessionMessages.add(request, "module-added-successfully");
            	} catch (Exception cvex) {
            		SessionErrors.add(request, "please-enter-a-unique-code");
                    response.setRenderParameter("view", "editmodule");
                    response.setRenderParameter("editType", "add");
                    response.setRenderParameter("moduleId",Long.toString(module.getModuleId()));
                    response.setRenderParameter("title", module.getTitle()+"");
                    response.setRenderParameter("description", module.getDescription(themeDisplay.getLocale())+"");
                    response.setRenderParameter("icon", module.getIcon()+"");
                    response.setRenderParameter("startDate", module.getStartDate()+"");
                    response.setRenderParameter("endDate", module.getEndDate()+"");
            	}
            } else {
                for (String error : errors) {
                        SessionErrors.add(request, error);
                }
                response.setRenderParameter("view", "editmodule");
                response.setRenderParameter("editType", "add");
                    response.setRenderParameter("moduleId",Long.toString(module.getModuleId()));
                    response.setRenderParameter("title", module.getTitle()+"");
                    response.setRenderParameter("description", module.getDescription(themeDisplay.getLocale())+"");
                    response.setRenderParameter("ordern", module.getOrdern()+"");
                    response.setRenderParameter("icon", module.getIcon()+"");
                    response.setRenderParameter("startDate", module.getStartDate()+"");
                    response.setRenderParameter("endDate", module.getEndDate()+"");
            }
	}
	
	private void addmodulePopUp(RenderRequest request, RenderResponse response) throws IOException, PortalException, SystemException  {
        Module module = moduleFromRequest(request);
        ArrayList<String> errors = moduleValidator.validatemodule(module, request);
        ThemeDisplay themeDisplay = (ThemeDisplay) request
		.getAttribute(WebKeys.THEME_DISPLAY);

        if (errors.isEmpty()) {
			try {
				module=ModuleLocalServiceUtil.addmodule(module);
   				request.setAttribute("moduleId",module.getModuleId());
				request.setAttribute("view", "editmodule");
				request.setAttribute("editType", "edit");
							
            	SessionMessages.add(request, "module-added-successfully");
        	} catch (Exception cvex) {
        		SessionErrors.add(request, "please-enter-a-unique-code");
        		request.setAttribute("view", "editmodule");
        		request.setAttribute("editType", "add");
        		request.setAttribute("errormodule", module);
        	}
        } else {
            for (String error : errors) {
                    SessionErrors.add(request, error);
            }
            	request.setAttribute("view", "editmodule");
            	request.setAttribute("editType", "add");
            	request.setAttribute("errormodule", module);
        }
	}

	@ProcessAction(name = "eventmodule")
	public void eventmodule(ActionRequest request, ActionResponse response)
			throws Exception {
		long key = ParamUtil.getLong(request, "resourcePrimKey");
		int containerStart = ParamUtil.getInteger(request, "containerStart");
		int containerEnd = ParamUtil.getInteger(request, "containerEnd");
		if (Validator.isNotNull(key)) {
            response.setRenderParameter("highlightRowWithKey", Long.toString(key));
            response.setRenderParameter("containerStart", Integer.toString(containerStart));
            response.setRenderParameter("containerEnd", Integer.toString(containerEnd));
		}
	}
	public void upmodule(ActionRequest actionRequest, ActionResponse actionResponse)
	throws Exception {

	ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
	String portletId = PortalUtil.getPortletId(actionRequest);
	long moduleId = ParamUtil.getLong(actionRequest, "resourcePrimKey",0);
	
	if(moduleId>0)
	{
		com.liferay.lms.service.ModuleLocalServiceUtil.goUpModule(moduleId);
	}
	
}
	public void downmodule(ActionRequest actionRequest, ActionResponse actionResponse)
	throws Exception {

	ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
	String portletId = PortalUtil.getPortletId(actionRequest);
	long moduleId = ParamUtil.getLong(actionRequest, "resourcePrimKey",0);
	
	if(moduleId>0)
	{
		com.liferay.lms.service.ModuleLocalServiceUtil.goDownModule(moduleId);
	}
	
}
	@ProcessAction(name = "editmodule")
	public void editmodule(ActionRequest request, ActionResponse response)
			throws Exception {
		long key = ParamUtil.getLong(request, "resourcePrimKey");
		if (Validator.isNotNull(key)) {
			response.setRenderParameter("moduleId", Long.toString(key));
			try {
				response.setWindowState(LiferayWindowState.POP_UP);
			} catch (WindowStateException e) {
				// TODO Auto-generated catch block
			}
			response.setRenderParameter("view", "editmodule");
			response.setRenderParameter("editType", "edit");
		}
	}
	
	private void editmodulePopUp(RenderRequest request, RenderResponse renderResponse) {
		long key = ParamUtil.getLong(request, "resourcePrimKey");
		if (Validator.isNotNull(key)) {
			request.setAttribute("moduleId",key);
			request.setAttribute("view", "editmodule");
			request.setAttribute("editType", "edit");
		}
	}

	@ProcessAction(name = "deletemodule")
	public void deletemodule(ActionRequest request, ActionResponse response)throws Exception {
		long id = ParamUtil.getLong(request, "resourcePrimKey");
		if (Validator.isNotNull(id)) {
			Module module = ModuleLocalServiceUtil.getModule(id);
			ModuleLocalServiceUtil.deleteModule(module);
            MultiVMPoolUtil.clear();
			SessionMessages.add(request, "module-deleted-successfully");
		} else {
			SessionErrors.add(request, "module-error-deleting");
		}
	}

	@ProcessAction(name = "updatemodule")
	public void updatemodule(ActionRequest request, ActionResponse response) throws Exception {
            Module module = moduleFromRequest(request);
            ArrayList<String> errors = moduleValidator.validatemodule(module, request);
            ThemeDisplay themeDisplay = (ThemeDisplay) request
			.getAttribute(WebKeys.THEME_DISPLAY);
            
            if (errors.isEmpty()) {
            	try {
                	ModuleLocalServiceUtil.updateModule(module);
                	MultiVMPoolUtil.clear();
                	response.setRenderParameter("view", "");
                	SessionMessages.add(request, "module-updated-successfully");
            	} catch (Exception cvex) {
            		SessionErrors.add(request, "please-enter-a-unique-code");
                    response.setRenderParameter("view", "editmodule");
                    response.setRenderParameter("editType", "edit");
                    response.setRenderParameter("moduleId",Long.toString(module.getModuleId()));
                    response.setRenderParameter("title", module.getTitle()+"");
                    response.setRenderParameter("description", module.getDescription(themeDisplay.getLocale())+"");
                    response.setRenderParameter("ordern", module.getOrdern()+"");
                    response.setRenderParameter("icon", module.getIcon()+"");
                    response.setRenderParameter("startDate", module.getStartDate()+"");
                    response.setRenderParameter("endDate", module.getEndDate()+"");
            	}
            } else {
                for (String error : errors) {
                        SessionErrors.add(request, error);
                }
				response.setRenderParameter("moduleId)",Long.toString(module.getPrimaryKey()));
				response.setRenderParameter("view", "editmodule");
				response.setRenderParameter("editType", "edit");
                    response.setRenderParameter("moduleId",Long.toString(module.getModuleId()));
                    response.setRenderParameter("title", module.getTitle()+"");
                    response.setRenderParameter("description", module.getDescription(themeDisplay.getLocale())+"");
                    response.setRenderParameter("ordern", module.getOrdern()+"");
                    response.setRenderParameter("icon", module.getIcon()+"");
                    response.setRenderParameter("startDate", module.getStartDate()+"");
                    response.setRenderParameter("endDate", module.getEndDate()+"");
            }
        }
	
	private void updatemodulePopUp(RenderRequest request, RenderResponse response) throws PortalException, SystemException, IOException {
        Module module = moduleFromRequest(request);
		request.setAttribute("moduleId",module.getModuleId());
		request.setAttribute("view", "editmodule");
		request.setAttribute("editType", "edit");
		
        ArrayList<String> errors = moduleValidator.validatemodule(module, request);
        ThemeDisplay themeDisplay = (ThemeDisplay) request
		.getAttribute(WebKeys.THEME_DISPLAY);
        
        if (errors.isEmpty()) {
        	try {
            	ModuleLocalServiceUtil.updateModule(module);
            	MultiVMPoolUtil.clear();
            	SessionMessages.add(request, "module-updated-successfully");
        	} catch (Exception cvex) {
        		SessionErrors.add(request, "please-enter-a-unique-code");
        		request.setAttribute("title", module.getTitle());
        		request.setAttribute("description", module.getDescription(themeDisplay.getLocale()));
        		request.setAttribute("ordern",Long.toString(module.getOrdern()));
        		request.setAttribute("icon", Long.toString(module.getIcon()));
        		request.setAttribute("startDate", module.getStartDate().toString());
        		request.setAttribute("endDate", module.getEndDate().toString());
        	}
        } else {
            for (String error : errors) {
                    SessionErrors.add(request, error);
            }
            	request.setAttribute("title", module.getTitle()+"");
            	request.setAttribute("description", module.getDescription(themeDisplay.getLocale()));
        		request.setAttribute("ordern",Long.toString(module.getOrdern()));
        		request.setAttribute("icon", Long.toString(module.getIcon()));
        		request.setAttribute("startDate", module.getStartDate().toString());
        		request.setAttribute("endDate", module.getEndDate().toString());
        }
    }

	@ProcessAction(name = "setmodulePref")
	public void setmodulePref(ActionRequest request, ActionResponse response) throws Exception {

		String rowsPerPage = ParamUtil.getString(request, "module-rows-per-page");
		String dateFormat = ParamUtil.getString(request, "module-date-format");
		String datetimeFormat = ParamUtil.getString(request, "module-datetime-format");

		ArrayList<String> errors = new ArrayList<String>();
		if (moduleValidator.validateEditmodule(rowsPerPage, dateFormat, datetimeFormat, errors)) {
			response.setRenderParameter("module-rows-per-page", "");
			response.setRenderParameter("module-date-format", "");
			response.setRenderParameter("module-datetime-format", "");

			PortletPreferences prefs = request.getPreferences();
			prefs.setValue("module-rows-per-page", rowsPerPage);
			prefs.setValue("module-date-format", dateFormat);
			prefs.setValue("module-datetime-format", datetimeFormat);
			prefs.store();

			SessionMessages.add(request, "module-prefs-success");
		}
	}

	private Module moduleFromRequest(PortletRequest actRequest) throws PortalException, SystemException {
		ThemeDisplay themeDisplay = (ThemeDisplay) actRequest.getAttribute(WebKeys.THEME_DISPLAY);
		UploadPortletRequest request = PortalUtil.getUploadPortletRequest(actRequest);

		Module module = null;
        long moduleId=ParamUtil.getLong(request, "resourcePrimKey",0);
        
        if(moduleId>0)
        {
        	 module=ModuleLocalServiceUtil.getModule(ParamUtil.getLong(request, "resourcePrimKey",0));
        }
        else
        {
        	module=new ModuleImpl();
        }
        
		Enumeration<String> parNames= request.getParameterNames();
		while(parNames.hasMoreElements())
		{
		  String paramName=parNames.nextElement();
		  if(paramName.startsWith("title_")&&paramName.length()>6)
		  {
			  String language=paramName.substring(6);
			  Locale locale=LocaleUtil.fromLanguageId(language);
			  module.setTitle(request.getParameter(paramName),locale);
		  }
		}
		module.setDescription(ParamUtil.getString(request, "description"),themeDisplay.getLocale());
      
        try {
            module.setIcon(ParamUtil.getLong(request, "icon"));
        } catch (Exception nfe) {
		    //Controled en Validator
        }
	    PortletPreferences prefs = actRequest.getPreferences();
        int startDateAno = ParamUtil.getInteger(request, "startDateAno");
        int startDateMes = ParamUtil.getInteger(request, "startDateMes");
        int startDateDia = ParamUtil.getInteger(request, "startDateDia");
        long precedence=ParamUtil.getLong(request, "precedence");
        module.setStartDate(PortalUtil.getDate(startDateMes, startDateDia, startDateAno, 0, 0, themeDisplay.getTimeZone(), new EntryDisplayDateException()));

        int endDateAno = ParamUtil.getInteger(request, "endDateAno");
        int endDateMes = ParamUtil.getInteger(request, "endDateMes");
        int endDateDia = ParamUtil.getInteger(request, "endDateDia");
        module.setEndDate(PortalUtil.getDate(endDateMes, endDateDia, endDateAno, 23, 59, themeDisplay.getTimeZone(), new EntryDisplayDateException()));
        
		try {
		    module.setPrimaryKey(ParamUtil.getLong(request,"resourcePrimKey"));
		} catch (NumberFormatException nfe) {
			//Controled en Validator
        }
		module.setCompanyId(themeDisplay.getCompanyId());
		module.setGroupId(themeDisplay.getScopeGroupId());
		module.setUserId(themeDisplay.getUserId());
		module.setPrecedence(precedence);
		//module.setExpandoBridgeAttributes(serviceContext);	
		//Saving image
		String fileName = request.getFileName("fileName");
		String title = fileName;// + " uploaded by " + user.getFullName();
		File file = request.getFile("fileName");
		
		long fileMaxSize = 1024 * 1024;
		try {
			fileMaxSize = Long.parseLong(PrefsPropsUtil.getString(PropsKeys.DL_FILE_MAX_SIZE));
			//System.out.println("---\n fileMaxSize 0 : " + fileMaxSize+", "+ file.length());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(fileName!=null && !fileName.equals("") && file.length() <= fileMaxSize){
			
			try {
				String contentType = request.getContentType("fileName");
				long repositoryId = DLFolderConstants.getDataRepositoryId(themeDisplay.getScopeGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
				long igFolderId=createIGFolders(actRequest, themeDisplay.getUserId(),repositoryId);
							
				//Subimos el Archivo en la Document Library
				ServiceContext serviceContextImg= ServiceContextFactory.getInstance( DLFileEntry.class.getName(), request);
				//Damos permisos al archivo para usuarios de comunidad.
				serviceContextImg.setAddGroupPermissions(true);
				serviceContextImg.setAddGuestPermissions(true);
				FileEntry image = DLAppLocalServiceUtil.addFileEntry(
					                      themeDisplay.getUserId(), repositoryId , igFolderId , fileName, contentType, fileName, StringPool.BLANK, StringPool.BLANK, file , serviceContextImg ) ;
						
				module.setIcon(image.getFileEntryId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
		} else if(file.length() > fileMaxSize){
			module.setIcon(0);
		}

		return module;
	}

	public static Object invoke(
	            boolean newInstance, String className, String methodName,
	            String[] parameterTypeNames, Object... arguments)
	    throws Exception {
	
	    Thread currentThread = Thread.currentThread();
	
	    ClassLoader contextClassLoader = currentThread.getContextClassLoader();
	
	    try {
	            currentThread.setContextClassLoader(
	                    PortalClassLoaderUtil.getClassLoader());
	
	            MethodKey methodKey = new MethodKeyPatched(
	                    className, methodName, parameterTypeNames,
	                    PortalClassLoaderUtil.getClassLoader());
	
	            MethodHandler methodHandler = new MethodHandler(
	                    methodKey, arguments);
	
	            return methodHandler.invoke(newInstance);
	    }
	    catch (InvocationTargetException ite) {
	            throw (Exception)ite.getCause();
	    }
	    finally {
	            currentThread.setContextClassLoader(contextClassLoader);
	    }
	}


	private long createIGFolders(PortletRequest request,long userId,long repositoryId) throws PortalException, SystemException{
		//Variables for folder ids
		Long igMainFolderId = 0L;
		Long igPortletFolderId = 0L;
		Long igRecordFolderId = 0L;
	    //Search for folders
	    boolean igMainFolderFound = false;
	    boolean igPortletFolderFound = false;
	    try {
	    	//Get the main folder
	    	Folder igMainFolder = DLAppLocalServiceUtil.getFolder(repositoryId,DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,IMAGEGALLERY_MAINFOLDER);
	    	igMainFolderId = igMainFolder.getFolderId();
	    	igMainFolderFound = true;
	    	//Get the portlet folder
	    	DLFolder igPortletFolder = DLFolderLocalServiceUtil.getFolder(repositoryId,igMainFolderId,IMAGEGALLERY_PORTLETFOLDER);
	    	igPortletFolderId = igPortletFolder.getFolderId();
	    	igPortletFolderFound = true;
	    } catch (Exception ex) {
	    }
	    
		ServiceContext serviceContext= ServiceContextFactory.getInstance( DLFolder.class.getName(), request);
		//Damos permisos al archivo para usuarios de comunidad.
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
	    
	    //Create main folder if not exist
	    if(!igMainFolderFound) {
	    	Folder newImageMainFolder=DLAppLocalServiceUtil.addFolder(userId, repositoryId, 0, IMAGEGALLERY_MAINFOLDER, IMAGEGALLERY_MAINFOLDER_DESCRIPTION, serviceContext);
	    	igMainFolderId = newImageMainFolder.getFolderId();
	    	igMainFolderFound = true;
	    }
	    //Create portlet folder if not exist
	    if(igMainFolderFound && !igPortletFolderFound){
	    	Folder newImagePortletFolder = DLAppLocalServiceUtil.addFolder(userId, repositoryId, igMainFolderId, IMAGEGALLERY_PORTLETFOLDER, IMAGEGALLERY_PORTLETFOLDER_DESCRIPTION, serviceContext);	    	
	    	igPortletFolderFound = true;
	    	igPortletFolderId = newImagePortletFolder.getFolderId();
	    }
	    //Create this record folder
	    if(igPortletFolderFound){
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	    	Date date = new Date();
	    	String igRecordFolderName=dateFormat.format(date)+SEPARATOR+userId;
	    	Folder newImageRecordFolder = DLAppLocalServiceUtil.addFolder(userId,repositoryId, igPortletFolderId,igRecordFolderName, igRecordFolderName, serviceContext);
	    	igRecordFolderId = newImageRecordFolder.getFolderId();
	    }
	    return igRecordFolderId;
	  }

	protected String editmoduleJSP;
	protected String editJSP;
	protected String helpJSP;
	protected String viewJSP;

	private static Log _log = LogFactoryUtil.getLog(modulePortlet.class);

}