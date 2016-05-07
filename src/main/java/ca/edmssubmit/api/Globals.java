package ca.edmssubmit.api;

import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Globals {
	public static final String API_BASE = "http://INSERTURLHERE:80/api/";
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	
	public static String implodeString(List<String> strings) {
		return implodeString(strings, ", ");
	}
	
	public static String implodeString(List<String> strings, String separator) {
		StringBuilder builder = new StringBuilder();
		int stringsSize = strings.size();
		int iterator = 0;
		for (String s: strings) {
			builder.append(s);
			if (iterator != stringsSize - 1) {
				builder.append(separator);
			}
			iterator++;
		}
		return builder.toString();
	}
}
