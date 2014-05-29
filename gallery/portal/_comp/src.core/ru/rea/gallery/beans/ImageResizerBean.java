package ru.rea.gallery.beans;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sapportals.wcm.repository.IResource;
import com.sapportals.wcm.repository.IResourceContext;
import com.sapportals.wcm.repository.IResourceFactory;
import com.sapportals.wcm.repository.ResourceContext;
import com.sapportals.wcm.repository.ResourceException;
import com.sapportals.wcm.repository.ResourceFactory;
import com.sapportals.wcm.util.content.ContentException;
import com.sapportals.wcm.util.content.IContent;
import com.sapportals.wcm.util.uri.RID;

public class ImageResizerBean {

   public static final String OUTPUT_JPEG_FORMAT = "jpg";
   
   public static final String OUTPUT_PNG_FORMAT = "png";
   
   public static final String OUTPUT_BMP_FORMAT = "bmp";
   
   public static final String IMAGE_LINK_PARAM = "image_link";

   public static final String WIDTH_PARAM = "width";

   public static final String HEIGHT_PARAM = "height";
   
   private static final int DEFAULT_RESIZED_HEIGHT = 90;
   
   private BufferedImage originalImage;
   
   private BufferedImage resizedImage;
   
   public ImageResizerBean(HttpServletRequest request) throws ContentException, IOException {
      getAndSetOriginalImage(request); 
      Sides resizedSides = getResizedImageSides(request);
      resize(resizedSides.getWidth(), resizedSides.getHeight());
   }

   public ImageResizerBean(String imageLink, int resizedHeight) throws ContentException, IOException {
      getAndSetOriginalImage(imageLink);
      int resizedWidth = calculateResizedImageRatio(resizedHeight, true);
      resize(resizedWidth, resizedHeight);
   }
   
   public ImageResizerBean(URL imageURL, int resizedHeight) throws IOException {
      getAndSetOriginalImage(imageURL);
      int resizedWidth = calculateResizedImageRatio(resizedHeight, true);
      resize(resizedWidth, resizedHeight);
   }
   
   public ImageResizerBean(IResource imageResource, int resizedHeight) 
      throws ContentException, IOException {
      getAndSetOriginalImage(imageResource);
      int resizedWidth = calculateResizedImageRatio(resizedHeight, true);
      resize(resizedWidth, resizedHeight);
   }
   
   public BufferedImage getOriginalImage() {
      return originalImage;
   }
   
   public BufferedImage getResizedImage() {
      return resizedImage;
   }
   
   // Get image from HttpServletRequest with request parameter image_link
   // Example: http://ltgpdci01:50000/ibs.ru~test~web_module_image_resizer/ImageResizerServlet?
   // image_link=http://ltgpdci01:50000/ibs.ru~test~test_gallery/images/6_thumb.jpg
   private void getAndSetOriginalImage(HttpServletRequest request) throws ContentException, IOException {
      String imageLink = request.getParameter(IMAGE_LINK_PARAM);
      if (imageLink == null || imageLink.equals("") )
         throw new IllegalArgumentException("No image in HTTP request!");
      getAndSetOriginalImage(imageLink);
   }

   // Get image from link string. Supported http link and KM link only
   // Http example: http://ltgpdci01:50000/ibs.ru~test~test_gallery/images/6_thumb.jpg
   // KM example: /documents/mkhokhlushin/error.jpg
   private void getAndSetOriginalImage(String imageLink) throws IOException, ContentException {
      if (imageLink.toLowerCase().contains(":")) {
         URL url = new URL(imageLink);
         getAndSetOriginalImage(url);
      }
      else {
         IResourceFactory resourceFactory = ResourceFactory.getInstance();
         // Get current user logonID
         IUser user= UMFactory.getAuthenticator().getLoggedInUser();
         // get resource context of current user
         IResourceContext resourceContext = ResourceContext.getInstance(user);
         // IResourceContext resourceContext = ResourceFactory.getInstance().
         // getServiceContextUME("cmadmin_service"); // works well too, but conceptually not right
         // get RID to identify content management
         RID rid = RID.getRID(imageLink);
         // get resource from KM
         IResource imageResource = resourceFactory.getResource(rid, resourceContext);
         getAndSetOriginalImage(imageResource);
      }
   }
   
   // Get and set image from URL instance
   private void getAndSetOriginalImage(URL url) throws IOException {
      BufferedImage originalImage = ImageIO.read(url);
      if (originalImage.getHeight() <= 0 || originalImage.getWidth() <= 0) 
       throw new IllegalArgumentException("Invalid image height or width parameters!");
      setOriginalImage(originalImage);
   }

   // Get image from IResource instance
   private void getAndSetOriginalImage(IResource resource) throws ContentException, IOException {
      if (resource == null) throw new ResourceException("No image resource to get image!");
      IContent content = resource.getContent();
      InputStream inStream = content.getInputStream();
      BufferedImage originalImage = ImageIO.read(inStream);
      if (originalImage == null)
         throw new IllegalArgumentException("Can't load image from HTTP request!");
      if (originalImage.getHeight() <= 0 || originalImage.getWidth() <= 0) 
         throw new IllegalArgumentException("Invalid image height or width parameters!");
      setOriginalImage(originalImage);
   }
   
   private void setOriginalImage(BufferedImage originalImage) {
      this.originalImage = originalImage;
   }
   
   private static class Sides {
      private final int width;
      private final int height;
      private Sides(int width, int height) {
         this.width = width;
         this.height = height;
      }
      private int getWidth() { return width; }
      private int getHeight() { return height; }
   }
   
   // returns [width, height] from request
   // if there is no both params then returns default values
   // if there is no one param then this param will be calculated to save ratio of new size
   private Sides getResizedImageSides(HttpServletRequest request) {
      int width = calculateResizedImageRatio(DEFAULT_RESIZED_HEIGHT, true);
      int height = DEFAULT_RESIZED_HEIGHT;
      String param = request.getParameter(WIDTH_PARAM);
      if (param !=null && !param.equals(""))
         width = Math.abs(Integer.parseInt(param));
      param = request.getParameter(HEIGHT_PARAM);
      if (param != null && !param.equals(""))
         height = Math.abs(Integer.parseInt(param));
      Sides resizedImageSides = new Sides(width, height);
      return adjustRatio(resizedImageSides);
   }
   
   // calculates another size depends on one size.
   // calculates height of the resizedImage if calculateWidth = false
   private int calculateResizedImageRatio(int oneSize, boolean calculateWidth) {
      if (oneSize == 0 || getOriginalImage().getHeight() == 0 || getOriginalImage().getWidth() == 0) {
         com.sap.tc.logging.Location.getLocation(ImageResizerBean.class).warningT(
            "Image ratio is 0 in calculateDefaultWidth()");
         return 0;
      }
      if (calculateWidth) {
         double ratio = getOriginalImage().getHeight() / (double)oneSize;
         return (int)(getOriginalImage().getWidth() / ratio);
      }
      else {
         double ratio = getOriginalImage().getWidth() / (double)oneSize;
         return (int)(getOriginalImage().getHeight() / ratio);
      }
   }

   // if some param > then original param the default
   // if there was only one param then another should be the same ratio as first
   private Sides adjustRatio(Sides sides) {
      // copy old sides.
      int width = sides.getWidth();
      int height = sides.getHeight();
      // if new width or height > original, then default 
      if (width > getOriginalImage().getWidth() || width > getOriginalImage().getHeight()) {
         width = calculateResizedImageRatio(DEFAULT_RESIZED_HEIGHT, true);
         height = DEFAULT_RESIZED_HEIGHT;
      } 
      else if (height == DEFAULT_RESIZED_HEIGHT && width != calculateResizedImageRatio(DEFAULT_RESIZED_HEIGHT, true))
         height = calculateResizedImageRatio(width, false);
      else if (width == calculateResizedImageRatio(DEFAULT_RESIZED_HEIGHT, true) && height != DEFAULT_RESIZED_HEIGHT)
         width = calculateResizedImageRatio(height, true);
      return new Sides(width, height);
   }
   
   // resizes and sets resized image with defined width and height
   private void resize(int toWidth, int toHeight) {
      BufferedImage resizedImage = new BufferedImage(toWidth, toHeight,
            BufferedImage.TYPE_INT_BGR);
      Graphics2D g = resizedImage.createGraphics();
      try {
         g.drawImage(getOriginalImage(), 0, 0, toWidth, toHeight, null);
      } 
      finally {
         g.dispose();
      }
      setResizedImage(resizedImage);
   }
   
   private void setResizedImage(BufferedImage resizedImage) {
      this.resizedImage = resizedImage;
   }

}
