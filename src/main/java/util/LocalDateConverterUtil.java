package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

@FacesConverter("localDateConverter")
public class LocalDateConverterUtil implements Converter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return !StringUtils.isBlank(value) ? LocalDate.parse(value, FORMATTER) : null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return (value instanceof LocalDate) ? ((LocalDate) value).format(FORMATTER) : "";
    }
}

