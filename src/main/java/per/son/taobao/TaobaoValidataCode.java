package per.son.taobao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TaobaoValidataCode {
	private static CookieStore sslcookies = new BasicCookieStore();
	private static CookieStore cookies = new BasicCookieStore();

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

	public static void main(String[] args) {
		handleVilidateCode("https://pin.aliyun.com/get_img?sessionid=79f17a12babd6a507d1fcd11fc177c86&identity=taobao.login&type=150_40");
	}
}
