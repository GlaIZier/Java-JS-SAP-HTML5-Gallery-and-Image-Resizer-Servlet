
<HTML>

<%-- <%@ taglib uri="htmlb" prefix="hbj" --%>

<jsp:useBean id="GalleryBean" scope="session" class="ru.rea.gallery.beans.GalleryBean" />
<%@ page import = "ru.rea.gallery.beans.ImageResizerBean" %>
<%-- 
<hbj:content id="myContext" >
  <hbj:page title="PageTitle">
   <hbj:form id="myFormId" >
--%>

<!--  <!DOCTYPE html> 
<html>
-->

<head>
   <meta charset="utf-8">
   <meta http-equiv="X-UA-Compatible" content="IE=EDGE">
   <title>SlideShow Gallery</title>
   <link rel="stylesheet" href="/rea.ru~home~gallery~portal/css/style.css" />
   <script type="text/javascript" src="/rea.ru~home~gallery~portal/js/script.js"></script>
   <script type="text/javascript" src="/rea.ru~home~gallery~portal/js/init.js"></script>
</head>
<body>

<%--  @TEST
<h3> <%= GalleryBean.getFolderURL() %> </h3>
<h3> <%= GalleryBean.getFileList() %> </h3>
<h3> <%= GalleryBean.getImageResizerURL() %> </h3>
<h3> <%= GalleryBean.getThumbnailsURL() %> </h3>
<h3> <%= ImageResizerBean.IMAGE_LINK_PARAM %></h3>
<h3> <%= GalleryBean.isFullscreen() %> </h3>
--%>

<div id="wrapper">
   <% if (GalleryBean.isFullscreen() ) { %>
   <div id="fullsize">
      <div id="prev" class="nav" title="Previous Photo/Video"></div>
      <div id="link"></div>
      <div id="next" class="nav" title="Next Photo/Video"></div>
      <div id="photovideo">
      </div>
      <!-- height = 71 for display info -->
      <div id="information">
      </div>
   </div>
   <% } %>
   
   <div id="thumbnails">
      <div id="slideleft" title="Slide Left"></div>
      <div id="slidearea">
         <div id="slider">
         </div>
      </div>
      <div id="slideright" title="Slide Right"></div>
   </div>

   <div id="serviceinfo" title="Service Info">
      <input id="servletlink" type="hidden" value=" <%= GalleryBean.getThumbnailsURL() %>" >
      <input id="folderURL" type="hidden" value="<%= GalleryBean.getFolderURL() %>" >
      <input id="photovideolist" type="hidden" value="<%= GalleryBean.getFileList() %>" >
      <input id="isFullscreen" type="hidden" value="<%= GalleryBean.isFullscreen() %>" >
      <!-- @TEST
         <input id="photovideolist" type="hidden" value="1.jpg:img:Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam ut urna. Mauris nulla. Donec nec mauris. Proin nulla dolor, bibendum et, dapibus in, euismod ut, felis.;2.jpg:img:Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam ut urna. Mauris nulla. Donec nec mauris. Proin nulla dolor, bibendum et, dapibus in, euismod ut, felis.;3.jpg:img;2.mp4:video:Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam ut urna. Mauris nulla. Donec nec mauris. Proin nulla dolor, bibendum et, dapibus in, euismod ut, felis.;4_vertical.jpg:img;4_horizontal.jpg:img;4.jpg:img;5.jpg:img;6.jpg:img;7.jpg:img;8.jpg:img;9.jpg:img;10.jpg:img;2.mp4:video;1.jpg:img;2.jpg:img;3.jpg:img;4.jpg:img;5.jpg:img;6.jpg:img;7.jpg:img;8.jpg:img;9.jpg:img;10.jpg:img;2.mp4:video;1.jpg:img;2.jpg:img;3.jpg:img;4.jpg:img;5.jpg:img;6.jpg:img;7.jpg:img;8.jpg:img;9.jpg:img;10.jpg:img;2.mp4:video">
         <label> <input id="show_modal_window" type="checkbox"> Show modal window instead of switching photo/video</label>
      -->
   </div>
</div>

<div id="overlay">
   <div id="modal_photovideo">
      <!--<p> <div id="close_modal_window" onclick="Gallery.overlayModalWindow()"> Close </div> </p>-->
   </div>
   <img id="close_modal_window" onclick="Gallery.overlayModalWindow()" src="/rea.ru~home~gallery~portal/images/close.gif" alt="Can't load image!">
</div>

</body>

<!-- </html> -->
<%-- 
   </hbj:form>
  </hbj:page>
</hbj:content>
--%>
 	 
</HTML>