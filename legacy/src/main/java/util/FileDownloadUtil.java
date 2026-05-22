package util;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exception.DownloadFileException;
import exception.ValidationException;

public final class FileDownloadUtil {
	
	private static final Logger logger = LogManager.getLogger(FileDownloadUtil.class);
	
	
	public static void downloadFile(String fileName, byte[] fileData) throws Exception {
		
		_checkNullability(fileName, fileData);

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
	    	
	    	StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			throw new DownloadFileException(e);
			
	    } finally {
	    	
	    	facesContext.responseComplete();
	      
	    }
	    
	    
	}
	
	private static void _checkNullability(String fileName,
			byte[] fileData) throws Exception {

		if (StringUtils.isBlank(fileName)) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					ResUtil.get("label_name")));
		}
		
		if (fileData == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					ResUtil.get("label_data")));
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