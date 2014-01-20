<%@page import="javax.portlet.PortletRequest"%>
<%@page import="com.liferay.lms.service.ClpSerializer"%>
<%@page import="com.liferay.portal.model.PortletConstants"%>
<%@page import="com.liferay.portlet.PortletURLFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayPortletURL"%>
<%@page import="com.liferay.lms.model.ModuleResult"%>
<%@page import="com.liferay.lms.service.CourseLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.Course"%>
<%@page import="com.liferay.lms.service.ModuleResultLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityResultLocalServiceUtil"%>
<%@page import="com.liferay.portal.service.ImageLocalServiceUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetRenderer"%>
<%@page import="com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetRendererFactory"%>
<%@page import="com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetEntry"%>
<%@page import="com.liferay.lms.service.ModuleLocalServiceUtil"%>
<%@page import="com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.repository.model.FileEntry"%>
<%@page import="com.liferay.lms.model.Module"%>
<%@ include file="/init.jsp"%>
<%
boolean registrado=UserLocalServiceUtil.hasGroupUser(themeDisplay.getScopeGroupId(),themeDisplay.getUserId());
Course course=CourseLocalServiceUtil.fetchByGroupCreatedId(themeDisplay.getScopeGroupId());
int i = 0;
%>
<table class="coursemodule <%="course-status-".concat(String.valueOf(course.getStatus()))%>">
<%
if(course!=null && permissionChecker.hasPermission(course.getGroupCreatedId(),  Course.class.getName(),course.getCourseId(),ActionKeys.VIEW))
{
java.util.List<Module> theModules=ModuleLocalServiceUtil.findAllInGroup(themeDisplay.getScopeGroupId());
Date today=new java.util.Date(System.currentTimeMillis());
for(Module theModule:theModules)
{
	%>

	<tr>
	<td class="icon">
	<% 
	long entryId=theModule.getIcon();
	if(entryId!=0)
	{
		FileEntry image=DLAppLocalServiceUtil.getFileEntry(entryId);
		if(image!=null)
		{
%>
	<img src="<%= themeDisplay.getPathImage()%>/image_gallery?uuid=<%= image.getUuid() %>&groupId=<%=
image.getGroupId() %>" />
<%}
	}
	%>

	</td>
	<td class="title">
	
	<%=theModule.getTitle(themeDisplay.getLocale()) %>
	</td>
	<td class="percent">
	<%
	
	ModuleResult moduleResult=ModuleResultLocalServiceUtil.getByModuleAndUser(theModule.getModuleId(),themeDisplay.getUserId());
	long done=0;
	if(moduleResult!=null)
	{
		done=moduleResult.getResult();
	
	%>
	<%=done %>%
	<%
	}
	%>
	</td>
	<td class="date">
	<%if(theModule.getStartDate()!=null &&today.before(theModule.getStartDate()))
	{
		%>
		<liferay-ui:message key="fecha-inicio"></liferay-ui:message><br />
		<%=	dateFormatDate.format(theModule.getStartDate())%>
		<%
	}
	else
	{
		if(theModule.getEndDate()!=null&&today.before(theModule.getEndDate()))
		{
			%>
			<liferay-ui:message key="fecha-inicio"></liferay-ui:message><br />
			<%=	dateFormatDateTime.format(theModule.getStartDate())%><br /><br />

			<liferay-ui:message key="fecha-fin"></liferay-ui:message><br />
			<%=	dateFormatDateTime.format(theModule.getEndDate())%>
			<%
		}
	}
	%>
	</td>
	<td class="access">
	<%
	java.util.Date now=new java.util.Date(System.currentTimeMillis());
	if(!ModuleLocalServiceUtil.isLocked(theModule.getModuleId(),themeDisplay.getUserId()) ||
		permissionChecker.hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model",themeDisplay.getScopeGroupId(),"ACCESSLOCK"))
	{
	
	Group grupo=themeDisplay.getScopeGroup();
	long retoplid=themeDisplay.getPlid();
	for(Layout theLayout:LayoutLocalServiceUtil.getLayouts(grupo.getGroupId(),false))
	{

		if(theLayout.getFriendlyURL().equals("/reto"))
		{
			retoplid=theLayout.getPlid();
			
		}
	}
	
	//Para el n�mero de m�dulo que se env�a a la vista.
	i++;
	
	LiferayPortletURL  gotoModuleURL = PortletURLFactoryUtil.create(PortalUtil.getHttpServletRequest(renderRequest),
			PortalUtil.getJsSafePortletId("lmsactivitieslist"+PortletConstants.WAR_SEPARATOR+ClpSerializer.getServletContextName()), retoplid, PortletRequest.ACTION_PHASE);		
	gotoModuleURL.setParameter(ActionRequest.ACTION_NAME, "goToModule");
	gotoModuleURL.removePublicRenderParameter("actId");
	gotoModuleURL.setWindowState(WindowState.NORMAL);
	gotoModuleURL.setParameter("moduleId", Long.toString(theModule.getModuleId()));
	gotoModuleURL.setParameter("themeId", Long.toString(i));
	gotoModuleURL.setParameter("actionEditing", Boolean.toString(false));
	gotoModuleURL.setPlid(retoplid);
	gotoModuleURL.setPortletId("lmsactivitieslist_WAR_liferaylmsportlet");
	
	if(ModuleLocalServiceUtil.isUserPassed(theModule.getModuleId(),themeDisplay.getUserId()))
	{
	%>
	<a href="<%=gotoModuleURL.toString() %>"><liferay-ui:message key="module-finissed" /></a>
	<%
	}
	else
	{
	%>
	<a href="<%=gotoModuleURL.toString() %>"><liferay-ui:message key="module-access" /></a>
	<%
		}
	}
	%>
	</td>
	</tr>
	
	<%
}
}
else
{
	renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
}
%>
</table>