package com.hanson.wechat.com.hanson.wechat.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hanson.wechat.utils.HttpClient2;
import org.apache.commons.httpclient.HttpClient;

import java.io.IOException;

/**
 * Created by hanson on 2017/1/14.
 */
public class TuLingReply implements MessageHandler{

    private HttpClient2 client = null;
    private final String url = "http://www.tuling123.com/openapi/api";
    private final String key = "b4ac2b1c72c512584db5322dbd6011a4";
    private final String userid = "season_ouc@126.com";


    public TuLingReply(){
        client = new HttpClient2();
    }

    private JSONObject getBaseRequest(){
        JSONObject params = new JSONObject();
        params.put("key",key);
        params.put("userid",userid);
        return params;
    }

    public static void main(String args[]){
        HttpClient2 client = new HttpClient2();
        JSONObject params = new JSONObject();
        params.put( "key","b4ac2b1c72c512584db5322dbd6011a4");
        params.put("info", "今天天气怎么样");
        params.put("loc","北京市中关村");
        params.put("userid","season_ouc@126.com");

        System.out.println(params.toJSONString());
        String url = "http://www.tuling123.com/openapi/api";
        try {
            String result = client.post(url,params);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String HandleMaeesage(JSONObject json) {
        JSONObject params = getBaseRequest();
        String text = json.getString("text");
        params.put("info",text);

        try {
            String result = client.post(url,params);
            JSONObject ret = JSON.parseObject(result);
            if(ret.getInteger("code") == 10000){
                return ret.getString("text");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
