package per.son.taobao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaobaoHttpLogin {
	private static CookieStore sslcookies = new BasicCookieStore();
	private static CookieStore cookies = new BasicCookieStore();
	
	static String TPL_password="xzj930203";
	static String TPL_username="18815608947";

	public static CloseableHttpClient createSSLClientDefault(boolean isSSL){
		if(isSSL){
			SSLContext sslContext = null;
			try {
				sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
					//信任所有
					public boolean isTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						return true;
					}
				}).build();
			} catch (KeyManagementException e) { 
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			}
			if(null != sslContext){
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
				return HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCookieStore(sslcookies).build();
			}else{
				return  HttpClients.custom().setDefaultCookieStore(cookies).build();
			}
		}else{
			return  HttpClients.custom().setDefaultCookieStore(cookies).build();
		}
	}

	public static void headerWrapper(AbstractHttpMessage methord){
		methord.setHeader("user-agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0");
		methord.setHeader("accept", "*/*");
		methord.setHeader("accept-language", "zh-CN");
		methord.setHeader("Accept-Encoding", "gzip, deflate");
		
	}
	
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
	
	public static String getCodeUrl(){
		CloseableHttpClient httpClient = createSSLClientDefault(true);
		HttpPost hp = new HttpPost("https://login.taobao.com/member/request_nick_check.do?_input_charset=utf-8");
		headerWrapper(hp);
		hp.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", TPL_username));
		HttpResponse httpresponse;
		try {
			hp.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
			httpresponse = httpClient.execute(hp);
			HttpEntity entity = httpresponse.getEntity();  
			String body = EntityUtils.toString(entity);  
			System.out.println(body);
			EntityUtils.consume(entity);
			JSONObject J_obj = String2Json(body);
			boolean isNeed = (Boolean)J_obj.get("needcode");
			System.out.println("needcode:" + isNeed);
			if(isNeed){
				String code_url = (String) J_obj.get("url");
				System.out.println("code_url:" + code_url);
				return code_url;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}  
		return null;
	}
	
	public static void handleVilidateCode(String url){
		CloseableHttpClient httpClient = createSSLClientDefault(true);
		HttpGet hg = new HttpGet(url);
		HttpResponse httpresponse;
		try {
			httpresponse = httpClient.execute(hg);
			HttpEntity entity = httpresponse.getEntity();
			InputStream content = entity.getContent();
			byte[] b = IOUtils.toByteArray(content);
			FileUtils.writeByteArrayToFile(new File("d://1.jpeg"), b);
			EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		String codeUrl = getCodeUrl();
		Scanner sc = new Scanner(System.in);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if(codeUrl != null && !codeUrl.equals("")){
			handleVilidateCode(codeUrl);
			System.out.println("请输入验证码：");
			String TPL_checkcode = sc.next();
			params.add(new BasicNameValuePair("TPL_checkcode", TPL_checkcode)); 
		}
		
		
		CloseableHttpClient httpClient = createSSLClientDefault(true);
		HttpPost httpPost = new HttpPost("https://login.taobao.com/member/login.jhtml");
		headerWrapper(httpPost);
		httpPost.setHeader("accept-Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		
		params.add(new BasicNameValuePair("TPL_password", TPL_password));  
		params.add(new BasicNameValuePair("TPL_username", TPL_username));
		params.add(new BasicNameValuePair("newlogin", "1"));   
		params.add(new BasicNameValuePair("callback", "1"));  
		httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));  
		// 发送请求  
		HttpResponse httpresponse = httpClient.execute(httpPost);  
		HttpEntity entity = httpresponse.getEntity();  
		String body = EntityUtils.toString(entity);  
		System.out.println(body);  
		String sessionid = body.substring(body.indexOf("token")+6, body.indexOf("callback=callback") - 1);
		System.out.println(sessionid);
		
		
		HttpGet hg1 = new HttpGet("https://passport.alipay.com/mini_apply_st.js?site=0&token="+sessionid+"&callback=vstCallback65");
		headerWrapper(hg1);
		HttpResponse httpresponse1 = httpClient.execute(hg1);  
		HttpEntity entity1 = httpresponse1.getEntity();  
		String body1 = EntityUtils.toString(entity1);
		
		String st = "";
		String regex = "vstCallback65\\((.*)\\)";
		Pattern compile = Pattern.compile(regex);
		Matcher m = compile.matcher(body1);
		while(m.find()){
			String group = m.group(1);
			JSONObject string2Json = String2Json(group);
			try {
				JSONObject object = (JSONObject) string2Json.get("data");
				st = (String) object.get("st");
				System.out.println(st); 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		//System.out.println(body1);  
		
		
		HttpGet hg2 = new HttpGet("https://login.taobao.com/member/vst.htm?st="+st+"&params=style%3Dminisimple%26sub%3Dtrue%26TPL_username%3D"+TPL_username+"%26loginsite%3D0%26from_encoding%3D%26not_duplite_str%3D%26guf%3D%26full_redirect%3D%26isIgnore%3D%26need_sign%3D%26sign%3D%26from%3Ddatacube%26TPL_redirect_url%3Dhttp%25253A%25252F%25252Fmofang.taobao.com%25252Fs%25252Flogin%26css_style%3D%26allp%3D&_ksTS=1404787873165_78&callback=jsonp79");
		headerWrapper(hg2);
		HttpResponse httpresponse2 = httpClient.execute(hg2);  
		HttpEntity entity2 = httpresponse2.getEntity();  
		String body2 = EntityUtils.toString(entity2);  
		System.out.println(body2);  
		sc.close();
		
		
		
		CloseableHttpClient commonClient = HttpClients.custom().setDefaultCookieStore(sslcookies).build();
		
		
		HttpGet hg4 = new HttpGet("https://rate.taobao.com/feedRateList.htm?auctionNumId=577268444882&userNumId=2923177672&currentPageNum=1&pageSize=20&rateType=&orderType=sort_weight&attribute=&sku=&hasSku=false&folded=0&ua=114%23kYxfepv5TTo8jsbrzWmKmm%2F008q8AS8ZYwp0Brtssm9d7Dj4AoFfGj7v2DN7lIZrmeez9LHaOJB2L7SZQvTZfgj0A9jKHgprb42Wqv%2FwlzQ2Cm3m0CmfBTCT7lotKCcCiOjHwllegT8snIbr26gm8TssTmJDfMonRTT9s6PGPppKbx2hJnB28cZTwJnUfnoTTT8otsZT%2FyQ2sjASRvSwmTjrsww7fbmD8jHsDju7fRmsnTTU26qwjHZyTcmU7njKTi4zGc3pMzWZempZhJvW7kuAe1J1nIrYK2e03h%2BdaRv4JRG7vNGYv5Pw4JKXh2DKbPlZGu%2BXVH44ix2RMVwc8LRmAQLOayni%2FXzZCxcMo5%2FyhnfUGJV3fZ492cyPQu1HeTnCe5uDoeVBaCFy8jF454Es2OmXyHdVN6xkxQ8ZzpEWur6p1RTLc0a2lwp86PcKjL3ISu9kpltRr6d8nhvmteOgPbkKLlAc8CUrWhA9jmV%2BERsQGKF7QORh%2FmgoJAmh61A5onkBz0sDUY55AbnlqUu6QAMOxlV2WO6CpEd%2BkafASptdZPvvN8X%2FCXhKcA6025SrKxxPXNX9IsLP0WHXmmsepKW8rfRO%2Fq9HvCF%2FBwO1Gm9zAlNwM9VGvx498gnlTGHQp0%2FuaN8YDn87p%2FSPcsLOdWJmDRaCKLCoTEEqKD5p6yIiRToG8uOQ1owSfihT9gQ%2B02ZcNhWpsW%3D%3D&_ksTS=1544344531849_1237&callback=jsonp_tbcrate_reviews_list");
		headerWrapper(hg4);
		HttpResponse httpresponse4 = commonClient.execute(hg4);  
		HttpEntity entity4 = httpresponse4.getEntity();  
		String body4 = EntityUtils.toString(entity4);
		System.out.println("=========================>下面测试登陆是够成功");
		System.out.println(body4);  
	}


}