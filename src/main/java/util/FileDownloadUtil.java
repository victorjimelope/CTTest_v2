package util;

import java.io.ByteArrayInputStream;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public final class FileDownloadUtil {
	
	private FileDownloadUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
	
	public static void downloadFile(String fileName, byte[] fileData) throws Exception {
		
		_nullityCheck(fileName, fileData);

	    FacesContext facesContext = FacesContext.getCurrentInstance();

	    if (facesContext == null || facesContext.getExternalContext() == null) {
	      throw new Exception("FacesContext is not available.");
	    }

	    HttpServletResponse response = _getResponse(facesContext, fileName);
	    
	    try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);

    		ServletOutputStream os = response.getOutputStream()) {
	    	
	    	byte[] buffer = new byte[4096];
	    	int bytesRead;
	    	
	    	while ((bytesRead = bis.read(buffer)) != -1) {
	    		os.write(buffer, 0, bytesRead);
    		}
	    	
	    	os.flush();

	    } catch (Exception e) {
	    	
	    	throw new Exception("Failed to download file '" + fileName + "': " + e.getMessage(), e);
	      
	    } finally {
	    	
	    	facesContext.responseComplete();
	      
	    }
	    
	    
	}
	
	private static void _nullityCheck(String fileName, byte[] fileData) throws Exception {

	  if (fileName == null) {
		  throw new Exception("File name cannot be null.");
	  }

	  if (fileData == null) {
		  throw new Exception("File data cannot be null.");
	  }

	}

	private static HttpServletResponse _getResponse(FacesContext facesContext, String fileName) {
		
		HttpServletResponse response = (HttpServletResponse)
				facesContext.getExternalContext().getResponse();
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setDateHeader("Expires", 0);
		
		return response;

	}
	
}