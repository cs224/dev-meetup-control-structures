package control.structures.utils;

import org.apache.commons.lang3.StringUtils;

public class InfrastructureUtils {

	public static final int truncateTo = 50;
	
	public static String truncate(Object input) {
		StringBuilder sb = new StringBuilder();
		if(null == input) return "null";
		String sinput = input.toString();
		sb.append("[size=").append(sinput.length()).append(", `");
		sb.append(StringUtils.abbreviate(sinput, truncateTo));
		sb.append("´]");
		return sb.toString();
	}
	
}
