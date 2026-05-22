package com.cttest.model.entity;

import com.cttest.model.enums.MimeType;
import com.cttest.util.Base64Util;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_data", columnDefinition = "MEDIUMBLOB", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private byte[] imageData;

    @Column(name = "mime_type", nullable = false)
    private MimeType mimeType;

    @com.fasterxml.jackson.annotation.JsonGetter("imageData")
    public String getSerializableImageData() {
        return getImageDataAsBase64();
    }

    @com.fasterxml.jackson.annotation.JsonSetter("imageData")
    public void setSerializableImageData(String dataUri) {
        if (dataUri != null && dataUri.contains(";base64,")) {
            String base64 = dataUri.substring(dataUri.indexOf(";base64,") + 8);
            String mime = dataUri.substring(dataUri.indexOf(":") + 1, dataUri.indexOf(";"));
            this.imageData = Base64Util.decode(base64);
            this.mimeType = MimeType.findByType(mime);
        }
    }

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
