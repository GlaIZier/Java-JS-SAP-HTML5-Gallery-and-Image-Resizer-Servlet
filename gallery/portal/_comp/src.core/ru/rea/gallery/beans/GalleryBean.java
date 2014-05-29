package ru.rea.gallery.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sapportals.wcm.repository.ICollection;
import com.sapportals.wcm.repository.IResource;
import com.sapportals.wcm.repository.IResourceContext;
import com.sapportals.wcm.repository.IResourceList;
import com.sapportals.wcm.repository.IResourceListIterator;
import com.sapportals.wcm.repository.ResourceContext;
import com.sapportals.wcm.repository.ResourceException;
import com.sapportals.wcm.repository.ResourceFactory;
import com.sapportals.wcm.util.uri.RID;

public class GalleryBean implements Serializable {
   
   private static final long serialVersionUID = 42L;
   
   private static final String KM_ROOT_FOLDER_NAME = "docs";
   
   private static final String KM_PATH = "/irj/go/km/docs";
   
   private static final Set<String> IMAGE_EXTENSIONS = new HashSet<String>
      (Arrays.asList("jpg", "jpeg", "png", "bmp", "gif"));
   
   private static final Set<String> VIDEO_EXTENSIONS = new HashSet<String>
      (Arrays.asList("mp4"));
   
   private static final String FILTER_SEPARATOR = ",";

   private boolean fullscreen;
   
   private String folderURL;
   
   private String imageResizerURL; 
   
   private String thumbnailsURL;
   
   // for KM API without "http://host:port/.../KM_ROOT_FOLDER_NAME/" for RID
   private String shortFolderURL;
   
   private String[] filter;
   
   private String fileList;
   
   public GalleryBean(String folderURL, String imageResizerURL) throws ResourceException, NoSuchFieldException {
      this("Fullscreen", folderURL, "", imageResizerURL);
   }
   
   public GalleryBean(String displayMode, String shortFolderURL, String filter, 
         String shortImageResizerURL, HttpServletRequest request) 
      throws ResourceException, NoSuchFieldException {
      this(displayMode, createFolderURL(shortFolderURL, request), shortFolderURL, filter,  createImageResizerURL(shortImageResizerURL, request) );
   }

   public GalleryBean(String displayMode, String folderURL, String filter, String imageResizerURL) throws ResourceException, NoSuchFieldException {
      this(displayMode, folderURL, createShortFolderURL(folderURL), filter, imageResizerURL);
   }
   
   public GalleryBean(String displayMode, String folderURL, String shortFolderURL, String filter, String imageResizerURL) 
      throws ResourceException, NoSuchFieldException {
      setDisplayMode(displayMode);
      setFolderURL(folderURL);
      setShortFolderURL(shortFolderURL);
      setFilter(filter);
      setFileList(createFileList(getShortFolderURL(), getFilter() ) );
      setImageResizerURL(imageResizerURL);
   }

   private static String createFolderURL(String shortFolderURL, HttpServletRequest request) {
      String preamble = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + KM_PATH;
      if (shortFolderURL.charAt(0) != '/')
         return preamble + "/" + shortFolderURL;
      else
         return preamble + shortFolderURL;
   }
   
   private static String createImageResizerURL(String shortImageResizerURL,
         HttpServletRequest request) {
      String imageResizerURL = shortImageResizerURL;
      if (!shortImageResizerURL.contains(":") && !shortImageResizerURL.equals("") ) { 
         if (shortImageResizerURL.charAt(0) != '/')
            shortImageResizerURL = "/" + shortImageResizerURL;
         imageResizerURL = request.getScheme() + "://" + request.getServerName() + ":" + 
            request.getServerPort() + shortImageResizerURL;
      }
      return imageResizerURL;
   }
   
   private static String createShortFolderURL(String folderURL) {
      if (folderURL == null || folderURL.equals("")) 
         throw new IllegalArgumentException("FolderURL is not set or empty!");
      // get short link to folder without "http://host:port/.../KM_ROOT_FOLDER_NAME/" for RID
      int firstIndex = folderURL.lastIndexOf(KM_ROOT_FOLDER_NAME);
      if (firstIndex == -1)  
         throw new java.lang.IllegalArgumentException("KM root folder name wasn't found!");
      String shortFolderURL = folderURL.substring(firstIndex + 
            KM_ROOT_FOLDER_NAME.length());
      return shortFolderURL;
   }
   
   private static String createFileList(String shortFolderURL, String[] filter) throws NoSuchFieldException, ResourceException {
      if (shortFolderURL == null || shortFolderURL.equals("")) 
         throw new UnsatisfiedLinkError("Short FolderURL is not set or empty!");
      // works well too, but conceptually not right
      // IResourceContext resourceContext = ResourceFactory.getInstance().
      //    getServiceContextUME("cmadmin_service"); 
      // Get current user logonID
      IUser user = UMFactory.getAuthenticator().getLoggedInUser();
      IResourceContext resourceContext = ResourceContext.getInstance(user);
      // get RID to identify content management
      RID rid = RID.getRID(shortFolderURL);
      IResource resource = ResourceFactory.getInstance().getResource(rid, resourceContext);
      if (resource == null)
         throw new IllegalArgumentException("KM folder with photo and video is empty!");
      String fileList = "";
      if (resource.isCollection()) {
         ICollection collection = (ICollection) resource;
         IResourceList resourceList = collection.getChildren();
         IResourceListIterator iterator = resourceList.listIterator();
         while (iterator.hasNext()) {
            IResource curResource = iterator.next();
            fileList += formResource(curResource, filter);
         }
      }
      else {
         fileList += formResource(resource, filter);
      }
      fileList = removeLastSemicolon(fileList);
      return fileList;
   }

   // form string for one photo/video
   private static String formResource(IResource resource, String[] filter) throws ResourceException {
      String extension = resource.getName().substring(
            resource.getName().lastIndexOf(".") + 1).toLowerCase();
      String name = resource.getName();
      for (String iFilter : filter) {
         if ( name.toLowerCase().contains(iFilter.toLowerCase() ) ) {
            if (IMAGE_EXTENSIONS.contains(extension)) {
               return resource.getName() + ":" + "img" + ":" + resource.getDescription() + ";";
            } 
            else if (VIDEO_EXTENSIONS.contains(extension)) {
               return resource.getName() + ":" + "video" + ":" + resource.getDescription()
                     + ";";
            } 
            else {
               com.sap.tc.logging.Location.getLocation(GalleryBean.class).warningT(
                     "Unknown format inside KM gallery folder: '" + extension + "'!");
               return "";
            }
         }
      }
      com.sap.tc.logging.Location.getLocation(GalleryBean.class).infoT(
            name + " isn't contained in filter.");
      return "";
   }
   
   private static String removeLastSemicolon(String fileList) {
      if (fileList.equals(null) || fileList.equals("")) 
         throw new java.lang.IllegalArgumentException("File list of the KM folder with " +
         "photo and video is empty! Check correctness of 'KM gallery folder' property in iView settings.");
      if (fileList.charAt(fileList.length() - 1) == ';') 
         return fileList.substring(0, fileList.length() - 1);
      else 
         return fileList;
   }

   public boolean isFullscreen() {
      return fullscreen;
   }
   
   public void setFullscreen(boolean toFullscreen) {
      fullscreen = toFullscreen;
   }

   public String getFolderURL() {
      return folderURL;
   }

   public void setFolderURL(String folderURL) {
      if (folderURL == null || folderURL.equals("")) 
         throw new IllegalArgumentException("FolderURL is not set or empty!");
      // watch for last '/'. Add it if folder doesn't contain it
      if (folderURL.charAt(folderURL.length() - 1) != '/') 
         folderURL += "/";
      this.folderURL = folderURL;
   }
   
   public String getFileList() {
      return fileList;
   }
   
   public void setFileList(String fileList) {
      if (fileList == null) 
         throw new IllegalArgumentException("Can't set fileList to null!");
      this.fileList = fileList;
   }

   public String getShortFolderURL() {
      return shortFolderURL;
   }

   public void setShortFolderURL(String shortFolderURL) {
      if (shortFolderURL == null || shortFolderURL.equals("")) 
         throw new IllegalArgumentException("ShortFolderURL is not set or empty!");
      if (shortFolderURL.charAt(shortFolderURL.length() - 1) != '/') 
         shortFolderURL += "/";
      if (shortFolderURL.charAt(0) != '/') 
         shortFolderURL = "/" + shortFolderURL;
      this.shortFolderURL = shortFolderURL;
   }
   
   /**
    * @param filter - String separated by ','
    */
   public void setFilter(String filter) {
      if (filter == null) filter = "";
      // delete all space symbols
      filter = filter.replaceAll("\\s+","");
      this.filter = filter.split(FILTER_SEPARATOR);
   }
   
   public String[] getFilter() {
      return filter;
   }
   
   public String getImageResizerURL() {
      return imageResizerURL;
   }

   public void setImageResizerURL(String imageResizerURL) {
      if (imageResizerURL == null || imageResizerURL.equals("")) {
         imageResizerURL = folderURL;
         setThumbnailsURL(folderURL);
         com.sap.tc.logging.Location.getLocation(GalleryBean.class).warningT(
            "No servlet link to image resize. Fullsize photos will be used fo thumbnails!");
      }
      else {
         this.imageResizerURL = imageResizerURL;
         setThumbnailsURL(imageResizerURL + "?" + ImageResizerBean.IMAGE_LINK_PARAM + "=" + 
            getShortFolderURL());
      }
   }
   
   public String getThumbnailsURL() {
      return thumbnailsURL;
   }

   public void setThumbnailsURL(String thumbnailsURL) {
      if (thumbnailsURL == null) 
         throw new IllegalArgumentException("ThumbnailsURL can't be set to null!");
      this.thumbnailsURL = thumbnailsURL;
   }
   
   private void setDisplayMode(String displayMode) {
      if (displayMode == null) 
         throw new IllegalArgumentException("DisplayMode is null!");
      if (displayMode.equalsIgnoreCase("Fullscreen")) 
         fullscreen = true;
      else if (displayMode.equalsIgnoreCase("Preview"))
         fullscreen = false;
      else 
         throw new IllegalArgumentException("Unknown value in display mode property!");
   }

   /**
    * Returns gallery file list inside folder (addressed by folderURL) with format for
    * gallery script: <filename>:img/video:<description>;... 
    * @throws NoSuchFieldException 
    * @throws ResourceException 
    */
//   private String createFileList() throws ResourceException, NoSuchFieldException {
//      String fileList = formFileList();
//      }
//      catch (Exception e) {
//         fileList = "error.jpg:img"; 
//         com.sap.tc.logging.Location.getLocation(GalleryBean.class).
//            errorT(e.getLocalizedMessage());
//      } 
//      return fileList;
//   }
   
   
}
