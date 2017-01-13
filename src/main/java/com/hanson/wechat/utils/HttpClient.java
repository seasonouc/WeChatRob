package com.hanson.wechat.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanson on 2017/1/13.
 */
public class HttpClient {

    private HttpClientContext context = null;
    private CookieStore cookieStore = null;
    private RequestConfig config = null;
    private CloseableHttpClient client = null;
    private static HttpClient instance = null;

    public static HttpClient getInstance(){
        if(instance == null){
            synchronized (HttpClient.class){
                instance = new HttpClient();
            }
        }
        return instance;
    }

    private HttpClient(){
        init();
    }
    public void init(){
         context =  HttpClientContext.create();
         cookieStore = new BasicCookieStore();
         config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000)
                 .setConnectionRequestTimeout(5000).build();
         client = HttpClientBuilder.create().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                 .setRedirectStrategy(new DefaultRedirectStrategy()).setDefaultRequestConfig(config)
                 .setDefaultCookieStore(cookieStore).build();
    }

    public String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = client.execute(httpGet,context);
        cookieStore = context.getCookieStore();
//        List<Cookie> cookieList = cookieStore.getCookies();
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        byte[] body = new byte[inputStream.available()];
        inputStream.read(body);
        return new String(body,"utf8");
    }

    public String post(String url,JSONObject payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if(payload != null){
            StringEntity entity = new StringEntity(payload.toJSONString(),"utf8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
        }

        CloseableHttpResponse response = client.execute(httpPost,context);
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();
        byte[] body = new byte[1024];
        stream.read(body);
        return new String(body,"utf8");

    }
}
