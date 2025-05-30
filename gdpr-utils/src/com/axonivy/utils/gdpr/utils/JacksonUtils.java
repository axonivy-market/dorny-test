package com.axonivy.utils.gdpr.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JacksonUtils {
	public static ObjectMapper objectMapper;

	public JacksonUtils() { }

	public static <T> List<T> jsonValueToEntities(String jsonValue, Class<T> classType) {
		if (StringUtils.isBlank(jsonValue)) {
			return new ArrayList<>();
		}
		try {
			return getObjectMapper().readValue(jsonValue, getListOfJavaType(classType));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> JavaType getListOfJavaType(Class<T> type) {
		return getObjectMapper().getTypeFactory().constructCollectionType(List.class, type);
	}

	public static ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = JsonMapper.builder()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
					.enable(Feature.ALLOW_COMMENTS)
					.build();
		}
		return objectMapper;
	}
}
