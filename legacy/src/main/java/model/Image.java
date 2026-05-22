package model;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.MimeType;
import util.Base64Util;

@Data @NoArgsConstructor
public class Image {
	
	private Integer id;
	
	private byte[] imageData;
	
	private MimeType mimeType;
	
	
	public Image(byte[] imageData, MimeType mimeType) {
		this.imageData = imageData;
		this.mimeType = mimeType;
	}
	
	public Image(String imageDataAsBase64, String mimeType) {
		this(Base64Util.decode(imageDataAsBase64), MimeType.findByType(mimeType));
	}
	
	public String getImageDataAsBase64() {
		return (imageData != null && imageData.length > 0 && mimeType != null)
				? "data:" + mimeType.getType() + ";base64," + Base64Util.encode(imageData) : "";
	}
	
	public Image clone() {
		
		Image clone = new Image();
		
		clone.setImageData(imageData);
		clone.setMimeType(mimeType);
		
		return clone;
		
	}
	
}
