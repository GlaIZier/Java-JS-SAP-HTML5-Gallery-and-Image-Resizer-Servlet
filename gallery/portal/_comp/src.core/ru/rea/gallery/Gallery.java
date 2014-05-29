package ru.rea.gallery;

import javax.servlet.http.HttpServletRequest;

import ru.rea.gallery.beans.GalleryBean;

import com.sap.tc.logging.Location;
import com.sapportals.htmlb.page.DynPage;
import com.sapportals.htmlb.page.PageException;
import com.sapportals.portal.htmlb.page.JSPDynPage;
import com.sapportals.portal.htmlb.page.PageProcessorComponent;
import com.sapportals.portal.prt.component.IPortalComponentContext;
import com.sapportals.portal.prt.component.IPortalComponentProfile;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentSession;
import com.sapportals.wcm.repository.ResourceException;

public class Gallery extends PageProcessorComponent {

   static Location loc = Location.getLocation(Gallery.class);

   public DynPage getPage() {
      return new GalleryDynPage();
   }

   public static class GalleryDynPage extends JSPDynPage {
      
      private final IPortalComponentRequest request = (IPortalComponentRequest) getRequest();

      private GalleryBean GalleryBean = null;
      
      private boolean internalError = false;

      public void doInitialization() {
         
         IPortalComponentRequest request = (IPortalComponentRequest) getRequest();
         
         request.getServletResponse(false).setHeader("x-ua-compatible", "IE=EDGE");
         
         IPortalComponentContext componentContext = request.getComponentContext();
         IPortalComponentProfile profile = componentContext.getProfile();     
         String displayMode = profile.getProperty("displayMode");
         String shortFolderURL = profile.getProperty("shortFolderURL");
         String imageResizerURL = profile.getProperty("imageResizerURL");
         String filter = profile.getProperty("filter");
         // TODO TEST
//         com.sap.tc.logging.Location.getLocation(Gallery.class).
//            errorT("server scheme: " + request.getServletRequest().getScheme());
//         com.sap.tc.logging.Location.getLocation(Gallery.class).
//            errorT("server name: " + request.getServletRequest().getServerName());
//         com.sap.tc.logging.Location.getLocation(Gallery.class).
//            errorT("server port: " + request.getServletRequest().getServerPort());
//         com.sap.tc.logging.Location.getLocation(Gallery.class).
//            errorT("servlet path: " + request.getServletRequest().getServletPath());
         
         IPortalComponentSession componentSession = ((IPortalComponentRequest) getRequest())
               .getComponentSession();
         Object o = componentSession.getValue("GalleryBean");
         if (o == null || !(o instanceof GalleryBean)) {
            try {
               GalleryBean = new GalleryBean(displayMode, shortFolderURL, filter, imageResizerURL, request.getServletRequest());
               componentSession.putValue("GalleryBean", GalleryBean);
            } 
            catch (Exception e) {
               internalError = true;
               com.sap.tc.logging.Location.getLocation(Gallery.class).
                  errorT(e.getLocalizedMessage());
            }
            //GalleryBean.setDisplayMode(displayMode);
            //GalleryBean.setFolderURL(folderURL);
         } 
         else {
            GalleryBean = (GalleryBean) o;
         }
         // fill your bean with data here...
      }

      public void doProcessAfterInput() throws PageException {
      }

      public void doProcessBeforeOutput() throws PageException {
         if (!internalError) {
            this.setJspName("index.jsp");
         }
         else { 
            this.setJspName("error.jsp");
         }
      }
   }
}
