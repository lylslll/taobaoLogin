package per.son.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class StringUtil {

	/**
	 * 字符串转换JSONObject
	 * @param string
	 * @return
	 */
	public static JSONObject String2Json(String string){
		if(string == null){
			return null;
		}
		String jsonString = string.trim();
		if(jsonString.startsWith("{") && jsonString.endsWith("}")){
			JSONObject jb = null;
			try {
				jb = new JSONObject(jsonString);
				return jb;
			}catch (Exception e) {
				return null;
			}
		}else if(jsonString.startsWith("[") && jsonString.endsWith("]")){
			JSONArray ja = null;
			try {
				ja = new JSONArray(jsonString);
				return (JSONObject)ja.get(0);
			}catch(Exception e){
				return null;
			} 
		}
		return null;
	}
	
	/**
	 * 正则匹配出想要的字符串
	 * @param regex
	 * @param Content
	 * @param groupNum
	 * @return
	 */
	public static String getRegexStr(String regex, String Content,int groupNum){
		Pattern compile = Pattern.compile(regex);
		Matcher m = compile.matcher(Content);
		while(m.find()){
			String group = m.group(groupNum);
			if(null != group){
				return group;
			} 
		}
		return null;
	}
}
