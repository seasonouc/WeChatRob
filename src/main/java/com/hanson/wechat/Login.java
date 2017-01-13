package com.hanson.wechat;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.EncodingUtil;
import com.hanson.wechat.utils.HttpClient;
import org.dom4j.DocumentException;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by hanson on 2017/1/11.
 */
public class Login {

    private HttpClient client = null;
    private String baseUrl = null;
    private String baseHost = null;
    private String syncHost ;
    private String skey;
    private String sid;
    private String uin;
    private String device_id;
    private String syncKeyStr;
    private String passTicket;
    private String retCode;
    private String selector;
    private String msgId;
    private JSONObject user;
    private JSONObject syncKey;

    public Login(){
        client =  HttpClient.getInstance();
        Random random = new Random();
        device_id = "e"+(random.nextDouble()+"").substring(2,17);

    }

    public String getUUID(Map<String,String> params) throws IOException {
        String url = "https://login.weixin.qq.com/jslogin";

        NameValuePair[]pair = new NameValuePair[params.size()];
        int i = 0;
        for(Map.Entry<String,String> entry:params.entrySet()){
            pair[i] = new NameValuePair();
            pair[i].setName(entry.getKey());
            pair[i].setValue(entry.getValue());
            i++;
        }
//        HttpMethod method = new GetMethod();
        String paramString = EncodingUtil.formUrlEncode(pair,"utf8");
//        HttpClient client = new HttpClient();
//        method.setURI(new URI(url+"?"+paramString));
//        int code = client.executeMethod(method);
        String body = client.get(url+"?"+paramString);
        Map<String,String> map = Utils.resolveResult(body);
        return map.get("window.QRLogin.uuid");
    }

    public void generateQRCode(String uuid){

        String url = "https://login.weixin.qq.com/l/" + uuid;
        String path = Login.class.getClassLoader().getResource("//").getPath()+"qrcode.png";
        QRCode qrcode = new QRCode();
        qrcode.createCode(path,url);
        JFrame frame = new JFrame();
        frame.setSize(400,400);
        ImageIcon imageIcon = new ImageIcon(path);
        JLabel label = new JLabel();
        label.setIcon(imageIcon);
        frame.add(label);
        frame.setVisible(true);

    }

    public String wait4Login(String uuid) throws InterruptedException {
        String loginTemp = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?tip=%s&uuid=%s&_=%s";
        int tip = 1;
        int retryTime = 10;
        String url = String.format(loginTemp, tip, uuid, System.currentTimeMillis() / 1000);
        while (retryTime > 0) {
            Thread.sleep(3000);
            retryTime--;
//            HttpClient client = new HttpClient();
//            HttpMethod method = new GetMethod(url);
//            client.executeMethod(method);
//            byte responseBody[] = method.getResponseBody();
            String body = null;
            try {
                body = client.get(url);
                Map<String, String> map = Utils.resolveResult(body);
//            String reponse = new String(responseBody,"utf-8");

//            System.out.println(reponse);
                if ("200".equals(map.get("window.code"))) {
                    String dirUrl = map.get("window.redirect_uri");
                    String tmp = dirUrl.substring(8);
                    baseHost = tmp.substring(0, tmp.indexOf("/"));
                    baseUrl = dirUrl.substring(0, dirUrl.lastIndexOf("/"));
                    return dirUrl + "&fun=new";
                }
            } catch (IOException e) {
                System.out.println("wait4login redo request.....");
            }

        }
        return null;
    }

    public boolean login(String directUrl) throws IOException, DocumentException {
//        HttpClient client = new HttpClient();
//        HttpMethod method = new GetMethod(directUrl);
//        Header header = new Header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
//        method.setRequestHeader(header);
//        client.executeMethod(method);
//        byte []body = method.getResponseBody();
        String body = client.get(directUrl);
        System.out.println(body);
        Map<String,String> map = Utils.xml2Map(body);
        skey = map.get("skey");
        sid = map.get("wxsid");
        uin = map.get("wxuin");
        passTicket = map.get("pass_ticket");
        return true;
    }

    public JSONObject snycMessage() throws IOException {
        String url = baseUrl + "/webwxsync?sid=%s&skey=%s&lang=en_US&pass_ticket=%s";
//        PostMethod postMethod = new PostMethod();

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin",uin);
        baseRequest.put("Sid",sid);
        baseRequest.put("Skey",skey);
        baseRequest.put("DeviceID",device_id);

        JSONObject params = new JSONObject();
        params.put("BaseRequest",baseRequest);
        params.put("rr",System.currentTimeMillis()/1000);



//        StringRequestEntity entity = new StringRequestEntity(baseRequest.toJSONString(),"application/json","utf8");
//        postMethod.setRequestEntity(entity);
//        client.executeMethod(postMethod);
//        String body = postMethod.getResponseBodyAsString();
        String body = "";
        JSONObject jsonBody = JSON.parseObject(body);
        return jsonBody;
    }

    public void syncCheck() throws IOException {
        Map<String,String>params = new HashMap<String, String>();

        params.put("_",System.currentTimeMillis()-201000L+"");
        params.put("sid",sid);
        params.put("uin",uin);
        params.put("skey",skey);
        params.put("deviceid",device_id);
        params.put("synckey",syncKeyStr);
        params.put("r",System.currentTimeMillis()+"");


        NameValuePair []pair = new NameValuePair[params.size()];
        int i = 0;
        for(Map.Entry<String,String> entry:params.entrySet()){
            pair[i] = new NameValuePair();
            pair[i].setName(entry.getKey());
            pair[i].setValue(entry.getValue());
            i++;
        }
        String paramString = EncodingUtil.formUrlEncode(pair,"utf8");

        String url = "https://"+ syncHost + "/cgi-bin/mmwebwx-bin/synccheck?"+paramString;
        System.out.println("check url:"+url);

//        Header header = new Header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
//
//        GetMethod getMethod = new GetMethod();
//        getMethod.setURI(new URI(url));
//        getMethod.setRequestHeader(header);
//        client.executeMethod(getMethod);
//        byte[] body = getMethod.getResponseBody();
        String body = client.get(url);
        Map<String,String> map = Utils.resolveResult(body);

        JSONObject json = JSON.parseObject(map.get("window.synccheck"));
        retCode = json.getString("retcode");
        selector = json.getString("selector");
    }

    public void webSyncCheck() throws IOException{
        String url = "https://"+ syncHost + "/cgi-bin/mmwebwx-bin/webwxsync?lang=zh_CN&";

        JSONObject params = new JSONObject();

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin",uin);
        baseRequest.put("Sid",sid);
        baseRequest.put("Skey",skey);
        baseRequest.put("DeviceID",device_id);
        params.put("BaseRequest",baseRequest);
        params.put("SyncKey",syncKey);


    }
    public void init() throws IOException {
        String url = String.format(baseUrl + "/webwxinit?r=%s&lang=en_US&pass_ticket=%s",System.currentTimeMillis()/1000 + "",passTicket);
//        PostMethod postMethod = new PostMethod();

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin",uin);
        baseRequest.put("Sid",sid);
        baseRequest.put("Skey",skey);
        baseRequest.put("DeviceID",device_id);

        JSONObject params = new JSONObject();
        params.put("BaseRequest",baseRequest);

//        StringRequestEntity entity = new StringRequestEntity(params.toJSONString(),"application/json","utf8");
//        postMethod.setRequestEntity(entity);
//        postMethod.setURI(new URI(url));
//        client.executeMethod(postMethod);
//        byte[] body = postMethod.getResponseBody();
        String result = client.post(url,params);
//        String result = new String(body,"utf8");
        JSONObject json =  JSON.parseObject(result);

         syncKey  = json.getJSONObject("SyncKey");
        user = json.getJSONObject("User");

        syncKeyStr = generateSyncKeyString(syncKey.getJSONArray("List"));
        System.out.println("syncKeyStr:"+syncKeyStr);
    }


    public void statusNotify() throws IOException {
        String url = String.format(baseUrl + "/webwxstatusnotify?lang=zh_CN&pass_ticket=%s",passTicket);

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin",Integer.parseInt(uin));
        baseRequest.put("Sid",sid);
        baseRequest.put("Skey",skey);
        baseRequest.put("DeviceID",device_id);

        JSONObject params = new JSONObject();
        params.put("BaseRequest",baseRequest);
        params.put("Code",3);
        params.put("FromUserName",user.getString("UserName"));
        params.put("ToUserName",user.getString("UserName"));
        params.put("ClientMsgId",System.currentTimeMillis()/1000);

//        PostMethod postMethod = new PostMethod();
//        StringRequestEntity entity = new StringRequestEntity(params.toJSONString(),"application/json","utf8");
//        postMethod.setRequestEntity(entity);
//        postMethod.setURI(new URI(url));
//        client.executeMethod(postMethod);
//
//        byte[] body = postMethod.getResponseBody();
        String body = client.post(url,params);
//        String reresult = new String(body,"utf8");
        System.out.println("statusNotify result:" + body);
    }

    public void testSyncCheck() {
//        String []hosts = {"webpush.","webpush2."};
//        for(String host:hosts){
            syncHost = "webpush." + baseHost;
//            try {
//                syncCheck();
//                if("0".equals(retCode)){
//                    return;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void status_notify(){

    }

    public void get_contact(){

    }


    public void procMsg() throws IOException, InterruptedException {
        testSyncCheck();
        while(true){
            long checkTime = System.currentTimeMillis()/1000;
            syncCheck();
            System.out.println("retcode:"+retCode+" selector:"+selector);
            if("0".equals(retCode)){
                if("2".equals(selector)){
                    String msg = sync();
                    System.out.println("receive message:"+msg);
                }
            }
            Thread.sleep(2000);
        }
    }

    public String generateSyncKeyString(JSONArray synKey){
        String ans=  null;

        StringBuffer sb = new StringBuffer();
        for(int i=0;i<synKey.size();i++){
            JSONObject j = synKey.getJSONObject(i);
            sb.append(j.getString("Key"));
            sb.append("_");
            sb.append(j.getString("Val"));
            sb.append("|");
        }
        ans = sb.toString();
        int index = ans.lastIndexOf("|");
        ans = ans.substring(0,index);
        return ans;
    }

    public String sync() throws IOException {
        String url = String.format(baseUrl + "/webwxsync?sid=%s&skey=%s&lang=en_US&pass_ticket=%s",sid,skey,passTicket);
        JSONObject params = new JSONObject();

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin",uin);
        baseRequest.put("Sid",sid);
        baseRequest.put("Skey",skey);
        baseRequest.put("DeviceID",device_id);

        params.put("BaseRequest",baseRequest);
        params.put("Synckey",syncKey);
        params.put("rr",System.currentTimeMillis()/1000);

//        PostMethod postMethod = new PostMethod();
//        postMethod.setURI(new URI(url));
//
//        StringRequestEntity entity = new StringRequestEntity(params.toJSONString(),"application/json","utf8");
//        postMethod.setRequestEntity(entity);
//        client.executeMethod(postMethod);
//
//        byte[] body = postMethod.getResponseBody();
//        String msg = new String(body,"utf8");
        String msg = client.post(url,params);
        System.out.println(msg);
        JSONObject ret = JSON.parseObject(msg);

        syncKey = ret.getJSONObject("SyncKey");
        syncKeyStr = generateSyncKeyString(syncKey.getJSONArray("List"));
        return msg;
    }

    public static void main(String args[]){
        Login login = new Login();
        try {
            Map<String,String> params = new HashMap<String,String>();
            params.put("appid","wx782c26e4c19acffb");
            params.put("fun","new");
            params.put("lang","zh_CN");

            Random random = new Random();
            long  radomCode = System.currentTimeMillis() + 1 + random.nextInt(999);
            params.put("_", radomCode+"");

            String uuid = login.getUUID(params);
            System.out.println(uuid);
//            String regex = "window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"";
//            String uuid = result.replace(regex,"").replace("\"","");


            login.generateQRCode(uuid);
            String dirUrl = login.wait4Login(uuid);
            System.out.println(dirUrl);
            login.login(dirUrl);
            login.init();
//            Map<String,String> rewardParams = Utils.resolveUrl(dirUrl);
            login.statusNotify();
            login.procMsg();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

}
