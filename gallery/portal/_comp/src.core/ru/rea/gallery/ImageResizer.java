package ru.rea.gallery;


import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.rea.gallery.beans.ImageResizerBean;

import com.sapportals.portal.prt.component.*;

public class ImageResizer extends AbstractPortalComponent {

   public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
      HttpServletResponse resp = null;
      HttpServletRequest req = null;
      ImageResizerBean irb = null;
      try { 
         resp = request.getServletResponse(true);
         req = request.getServletRequest();
         resp.setContentType("image/jpeg");
         irb = new ImageResizerBean(req);
         ImageIO.write(irb.getResizedImage(),ImageResizerBean.OUTPUT_JPEG_FORMAT,
               resp.getOutputStream());
         //resp.getWriter().write("Hi!");
      } 
      // try to return original image
      catch (Exception outside) {
         com.sap.tc.logging.Location.getLocation(ImageResizer.class).
            errorT(outside.getLocalizedMessage());
         try {
            ImageIO.write(irb.getOriginalImage(),ImageResizerBean.OUTPUT_JPEG_FORMAT,
                  resp.getOutputStream());
         }
         catch (Exception inside) {
            com.sap.tc.logging.Location.getLocation(ImageResizer.class).
               errorT(inside.getLocalizedMessage());
            showErrorImage(irb, resp);
         }
      }
   }

   private void showErrorImage(ImageResizerBean irb, HttpServletResponse resp) {
      try {
         irb = new ImageResizerBean("/documents/Temp/error/error.png", 90);
         ImageIO.write(irb.getResizedImage(),ImageResizerBean.OUTPUT_PNG_FORMAT,
               resp.getOutputStream());
      } 
      catch (Exception e) {
         // nothing can do
         com.sap.tc.logging.Location.getLocation(ImageResizer.class).
            errorT(e.getLocalizedMessage());
      }
   }
   
   
}

/*
 * @Test Hello world app 
 * HttpServletResponse resp =
 * request.getServletResponse(true); resp.setContentType("text/html"); 
 * try {
 * resp.getWriter().write("Hi!"); } catch (IOException e) {
 * com.sap.tc.logging.Location.getLocation(GalleryBean.class).
 * errorT(e.getLocalizedMessage()); }
 */