package com.axonivy.utils.gdpr.converter;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * Converter that can handle POJOs. Can handle each type of object.
 * 
 * Attention: If a no selection item is provided its item value must be ''. An
 * empty string ('') is not allowed as choosable item value (it will always be
 * converted to {@code null}).
 * 
 * Attention: If the POJOs must match each other (e.g. in a dropdown) over
 * multiple views it is required that the POJOs implement the hasCode method
 * properly.
 * 
 * Attention: This converter could fail in rare cases because it is based on the
 * identityHashcode which is not unique for an object.
 */
@FacesConverter("pojoConverter")
public class PojoConverter implements Converter {
	private static final String CONVERTER_ID = PojoConverter.class.getName();
	private static final String EMPTY = "";
	private static final String KEY_DELIMITER = ":::";
	private static final String PARAM = "%s";
	private static final String MAP_KEY_TEMPLATE = CONVERTER_ID + KEY_DELIMITER + PARAM + KEY_DELIMITER + PARAM;

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object item) throws ConverterException {
		if (item != null && !isEmptyString(item)) {
			Map<String, Object> viewMap = getViewMap(context);
			String identityHashCode = String.valueOf(System.identityHashCode(item));
			String mapKey = String.format(MAP_KEY_TEMPLATE, component.getId(), identityHashCode);
			viewMap.put(mapKey, item);

			return identityHashCode;
		}
		return EMPTY;
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String selectedvalue) {
		if (selectedvalue != null && selectedvalue.length() > 0) {
			String mapKey = String.format(MAP_KEY_TEMPLATE, component.getId(), selectedvalue);
			Map<String, Object> viewMap = getViewMap(context);
			return viewMap.get(mapKey);
		}
		return null;
	}

	private boolean isEmptyString(Object item) {
		return String.class.isAssignableFrom(item.getClass()) && EMPTY.equals(item);
	}

	private Map<String, Object> getViewMap(FacesContext context) {
		UIViewRoot viewRoot = context.getViewRoot();
		return viewRoot.getViewMap();
	}
}
