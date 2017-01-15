package com.hanson.wechat.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;

/**
 * Created by hanson on 2017/1/13.
 */
public class HttpClient2 {

    private HttpClient client = null;

    private HttpState state = null;
    public HttpClient2(){
        client = new HttpClient();
        state = new HttpState();
        client.setState(state);
    }

    public String post(String url, JSONObject params) throws IOException {
        PostMethod method = new PostMethod(url);
//        Header header1 = new Header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
//        Header header2 = new Header("Connectiont","Keep-Alive");
        Header header3 = new Header("Cookie",getCookieStr());

        if(params !=null){
            StringRequestEntity entity = new StringRequestEntity(params.toJSONString(),"application/json","utf8");
            method.setRequestEntity(entity);
        }
//        method.addRequestHeader(header1);
//        method.addRequestHeader(header2);
        method.addRequestHeader(header3);
        client.executeMethod(method);
        byte[] body = method.getResponseBody();

        String ans = new String(body,"utf8");
        return ans;
    }
    public String get(String url) throws IOException {
//        Header header1 = new Header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
//        Header header2 = new Header("Connectiont","Keep-Alive");
        Header header3 = new Header("Cookie",getCookieStr());
        GetMethod method = new GetMethod(url);
//        method.addRequestHeader(header1);
//        method.addRequestHeader(header2);
        method.addRequestHeader(header3);
        client.executeMethod(method);
        byte[] body = method.getResponseBody();
        String ans = new String(body,"utf8");
        return ans;
    }
    private String getCookieStr(){
        Cookie[] cookies = state.getCookies();
        StringBuffer sb = new StringBuffer();
        if(cookies != null){
            for(int i =0;i<cookies.length;i++){
                sb.append(cookies[i].toString());
                if(i<cookies.length-1){
                    sb.append(";");
                }
            }
        }
        return sb.toString();
    }
}
