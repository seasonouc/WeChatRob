package com.hanson.wechat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by hanson on 2017/1/12.
 */
public class Utils {
    public static Map resolveResult(String input) throws UnsupportedEncodingException {
//        String input = new String(data,"utf8");
        Map<String,String> ans = new HashMap();
        String[] step1 = input.replace(" ","").replace("\"","").replace("\n","").split(";");
        for(String str:step1){
            int index = str.indexOf('=');
            String key = str.substring(0,index);
            String value = str.substring(index+1);
            ans.put(key,value);
        }
        return ans;
    }
    public static Map resolveUrl(String url){
        int start = url.indexOf("?") + 1;
        Map<String,String> map = new HashMap<String, String>();
        String[] paramArray = url.substring(start).split("&");
        for(String str:paramArray){
            int s = str.indexOf("=");
            String key = str.substring(0,s).trim();
            String value = str.substring(s+1).trim();
            map.put(key,value);
        }
        return map;
    }
    public static Map<String, String> xml2Map(String xml) throws DocumentException, UnsupportedEncodingException {
//        String xml = new String(body,"utf8");
        Document doc = DocumentHelper.parseText(xml);
        Element element = doc.getRootElement();
        Map<String, String> map = new HashMap<String, String>();

        Stack<Element> stack = new Stack<Element>();
        stack.add(element);

        while(stack.size()>0){
            Element e = stack.pop();
            if(e.attributes().isEmpty()&&e.elements().isEmpty()){
                map.put(e.getName(),e.getTextTrim());
            }else{
                for(Object e1:e.elements()){
                    stack.add((Element)e1);
                }
            }
        }
        return map;
    }

    public static String join(String i ,String j){
        StringBuffer sb  =new StringBuffer();
        sb.append(j.charAt(0));
        for(int k=1;k<j.length();k++){
            sb.append(i);
            sb.append(j.charAt(k));
        }
        return sb.toString();
    }
    public static void main(String args[]) throws IOException {
        Map<String,String>params = new HashMap<String, String>();

        params.put("_",1484304023254L+"");
        params.put("sid","cjK504v8KPiboS6c");
        params.put("uin","1648525640");
        params.put("skey","@crypt_562da188_d1a32289799ee314e9cacbb8003d97d9");
        params.put("deviceid","e032527603215145");
        params.put("synckey","1_649660180|2_649660798|3_649660568|1000_1484302141");
        params.put("r",1484304242664L+"");

        NameValuePair[]pair = new NameValuePair[params.size()];
        int i = 0;
        for(Map.Entry<String,String> entry:params.entrySet()){
            pair[i] = new NameValuePair();
            pair[i].setName(entry.getKey());
            pair[i].setValue(entry.getValue());
            i++;
        }
        String paramString = EncodingUtil.formUrlEncode(pair,"utf8");

        String url = "https://webpush.wx.qq.com/cgi-bin/mmwebwx-bin/synccheck?"+paramString;
        System.out.println(url);
        GetMethod getMethod = new GetMethod();
        HttpClient client = new HttpClient();

        Header header = new Header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
        Header headerco = new Header("Cookie","RK=9W1C56mzFx; pgv_pvi=5852839936; tvfe_boss_uuid=2dc0a677e9cb904f; mobileUV=1_157dbd45387_218e; sdi_stat_uid=c7bfca47-cf9e-415f-99d8-d6a4df949739; sdi_from_2016100920123201194=26; sdi_from_2016101418224701169=0; pac_uid=1_542463303; gaduid=5854fce3258af; luin=o0542463303; lskey=0001000088b8d6f6324a365eb9539b62f44f38f5233cf1d5fae23ca45da9d4a410417e57b0002102793de953; _ga=GA1.2.182106652.1479435492; pt2gguin=o0542463303; ptcz=b64e4bc789c3e8646e4d873e4d8a34d54fe8f2d85d9ba0af4eb9cdc2eaf5257a; pgv_pvid=8412819983; o_cookie=542463303; pgv_si=s4910415872; webwxuvid=5f19fc989c7cac1c3e6c38786d5ddac9ee7748f803e39f74a299bf39181dcc047819a70e5271713ff28082d7bf35cf0b; webwx_auth_ticket=CIsBEPKHl9IEGoAB8YhwAzmk/DhMMPbmW4catVnF4dmh2W709LKYFmEnxs0FKrSE1749/Sy0sgdIARKYuCwuXPe81o5isv0m5qISjKlN4PDwRjeOxWhO7zJoRcIugaXR369kvgVH/uVHqE5ZzF7OSKdRruPbW+OdAU3ggRqwe0H4RlHypSYMPwDaAlA=; wxloadtime=1484304241_expired; wxpluginkey=1484302141; wxuin=1648525640; wxsid=cjK504v8KPiboS6c; webwx_data_ticket=gSeraEM6e9NNXiIYIoV4YrVA; mm_lang=zh_CN; MM_WX_NOTIFY_STATE=1; MM_WX_SOUND_STATE=1");
        getMethod.addRequestHeader(header);
        getMethod.setURI(new URI(url));
        client.executeMethod(getMethod);


        byte[] body = getMethod.getResponseBody();
        System.out.println(new String(body,"utf-8"));
    }
}
