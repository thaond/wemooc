package com.liferay.lms;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.lms.learningactivity.LearningActivityType;
import com.liferay.lms.learningactivity.LearningActivityTypeRegistry;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.PortletWrapper;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletConfigFactoryUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class ActivityViewer
 */
public class ActivityViewer extends MVCPortlet 
{
	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		long actId=GetterUtil.DEFAULT_LONG;		
		if(ParamUtil.getBoolean(renderRequest, "actionEditingDetails", false)){
			actId=ParamUtil.getLong(renderRequest, "resId", 0);
			renderResponse.setProperty("clear-request-parameters",Boolean.TRUE.toString());
		}
		else{
			actId=ParamUtil.getLong(renderRequest, "actId");
		}
		
		if(Validator.isNull(actId)) {
			renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
		}
		else {
			try {
				LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(actId);
	
				if(Validator.isNull(learningActivity)) {
					renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
				}
				else {
					LearningActivityType learningActivityType=new LearningActivityTypeRegistry().getLearningActivityType(learningActivity.getTypeId());
					
					if(Validator.isNull(learningActivityType)) {
						renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
					}
					else {
						ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
						if(themeDisplay.getLayoutTypePortlet().getPortletIds().contains(learningActivityType.getPortletId())) {
							renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
						}
						else {
							renderRequest.setAttribute("activityHtml", renderPortlet(renderRequest, renderResponse, 
									themeDisplay, themeDisplay.getScopeGroupId(), learningActivityType.getPortletId()));
						}
					}
				}
			}
			catch(Throwable throwable) {
				renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
			}
		}
		
		super.render(renderRequest, renderResponse);
	}

    /**
     * Renders the given portlet as a runtime portlet and returns the portlet's HTML.
     * Based on http://www.devatwork.nl/2011/07/liferay-embedding-portlets-in-your-portlet/
     * @throws PortalException 
     */
    @SuppressWarnings("unchecked")
	public static String renderPortlet(final RenderRequest request, final RenderResponse response,final ThemeDisplay themeDisplay,final long scopeGroup, final String portletId) throws SystemException, IOException, ServletException, ValidatorException, PortalException {
        // Get servlet request / response
        HttpServletRequest servletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request));
        HttpServletResponse servletResponse = PortalUtil.getHttpServletResponse(response);
        
        PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();
        PortletDisplay portletDisplayClone = new PortletDisplay();
        portletDisplay.copyTo(portletDisplayClone);
        final Map<String, Object> requestAttributeBackup = new HashMap<String, Object>();
        for (final String key : Collections.list((Enumeration<String>) servletRequest.getAttributeNames())) {
            requestAttributeBackup.put(key, servletRequest.getAttribute(key));
        }
        // Render the portlet as a runtime portlet
        String result=null;
        long currentScopeGroup = themeDisplay.getScopeGroupId();
        String currentOuterPortlet = (String) servletRequest.getAttribute("OUTER_PORTLET_ID");
        Layout currentLayout = (Layout)servletRequest.getAttribute(WebKeys.LAYOUT);
        try {
			ServletContext servletContext =
					(ServletContext)servletRequest.getAttribute(WebKeys.CTX);
			Thread.currentThread().setContextClassLoader(PortalClassLoaderUtil.getClassLoader());
			
            com.liferay.portal.model.Portlet portlet = PortletLocalServiceUtil.getPortletById(PortalUtil.getCompanyId(request), portletId);
        	servletRequest.setAttribute(WebKeys.RENDER_PORTLET_RESOURCE, Boolean.TRUE);
        	long defaultGroupPlid = LayoutLocalServiceUtil.getDefaultPlid(scopeGroup);
        	if(defaultGroupPlid!=LayoutConstants.DEFAULT_PLID) {
        		servletRequest.setAttribute(WebKeys.LAYOUT, LayoutLocalServiceUtil.getLayout(defaultGroupPlid));
        	}
        	servletRequest.setAttribute(WebKeys.LAYOUT, LayoutLocalServiceUtil.getLayout(defaultGroupPlid));
        	
        	servletRequest.setAttribute("OUTER_PORTLET_ID",PortalUtil.getPortletId(request));
        	
        	StringBundler queryString = new StringBundler();
        	for (Entry<String, String[]> entry : request.getPublicParameterMap().entrySet()) {
        		String[] values = entry.getValue();
				for(int itrValues=values.length-1;itrValues>=0;itrValues--) {
					if(queryString.index()!=0) {
						queryString.append(StringPool.AMPERSAND);
					}
					queryString.append(entry.getKey());
	        		queryString.append(StringPool.EQUAL);
	        		queryString.append(values[itrValues]);
				}
			}

            result = PortalUtil.renderPortlet(servletContext, servletRequest, servletResponse, 
            		new PortletWrapper(portlet){
						private static final long serialVersionUID = 229422682924083706L;
		
						@Override
		        		public boolean isUseDefaultTemplate() {
		        			return false;
		        		}
		        	}, queryString.toString(), false);

            servletRequest.setAttribute(PortletRequest.LIFECYCLE_PHASE,PortletRequest.RENDER_PHASE);
            servletRequest.setAttribute(WebKeys.RENDER_PORTLET, portlet);
            servletRequest.setAttribute(JavaConstants.JAVAX_PORTLET_REQUEST, request);
            servletRequest.setAttribute(JavaConstants.JAVAX_PORTLET_RESPONSE, response);
            servletRequest.setAttribute(JavaConstants.JAVAX_PORTLET_CONFIG, PortletConfigFactoryUtil.create(portlet, servletContext));
            servletRequest.setAttribute("PORTLET_CONTENT", result);
            result=PortalUtil.renderPage(servletContext, servletRequest, servletResponse, "/html/common/themes/portlet.jsp",false);

            /*
			Set<String> runtimePortletIds = (Set<String>)request.getAttribute(
					WebKeys.RUNTIME_PORTLET_IDS);

			if (runtimePortletIds == null) {
				runtimePortletIds = new HashSet<String>();
			}

			runtimePortletIds.add(portletId);

			request.setAttribute(WebKeys.RUNTIME_PORTLET_IDS, runtimePortletIds);
			*/
        }finally {
            // Restore the state
        	themeDisplay.setScopeGroupId(currentScopeGroup);
            servletRequest.setAttribute(WebKeys.LAYOUT, currentLayout);
            servletRequest.setAttribute("OUTER_PORTLET_ID",currentOuterPortlet);
            portletDisplay.copyFrom(portletDisplayClone);
            portletDisplayClone.recycle();
            for (final String key : Collections.list((Enumeration<String>) servletRequest.getAttributeNames())) {
                if (!requestAttributeBackup.containsKey(key)) {
                    servletRequest.removeAttribute(key);
                }
            }
            for (final Map.Entry<String, Object> entry : requestAttributeBackup.entrySet()) {
                servletRequest.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}