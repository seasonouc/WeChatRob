package com.hanson.wechat.com.hanson.wechat.core;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.methods.StringRequestEntity;
import com.hanson.wechat.utils.HttpClient2;
import com.hanson.wechat.utils.QRCode;
import com.hanson.wechat.utils.Utils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.EncodingUtil;
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

    private HttpClient2 client = null;
    private String baseUrl = null;
    private String baseHost = null;
    private String syncHost;
    private String skey;
    private String sid;
    private String uin;
    private String device_id;
    private String syncKeyStr;
    private String passTicket;
    private String msgId;
    private JSONObject user;
    private JSONObject syncKey;

    public Login() {
        client = new HttpClient2();
        Random random = new Random();
        device_id = "e" + (random.nextDouble() + "").substring(2, 17);

    }

    public String getUUID(Map<String, String> params) throws IOException {
        String url = "https://login.weixin.qq.com/jslogin";

        NameValuePair[] pair = new NameValuePair[params.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            pair[i] = new NameValuePair();
            pair[i].setName(entry.getKey());
            pair[i].setValue(entry.getValue());
            i++;
        }
        String paramString = EncodingUtil.formUrlEncode(pair, "utf8");

        String body = client.get(url + "?" + paramString);
        Map<String, String> map = Utils.resolveResult(body);
        return map.get("window.QRLogin.uuid");
    }

    public void generateQRCode(String uuid) {

        String url = "https://login.weixin.qq.com/l/" + uuid;
        String path = Login.class.getClassLoader().getResource("//").getPath() + "qrcode.png";
        QRCode qrcode = new QRCode();
        qrcode.createCode(path, url);
        JFrame frame = new JFrame();
        frame.setSize(400, 400);
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

            String body = null;
            try {
                body = client.get(url);
                Map<String, String> map = Utils.resolveResult(body);

                if ("200".equals(map.get("window.code"))) {
                    String dirUrl = map.get("window.redirect_uri");
                    String tmp = dirUrl.substring(8);
                    baseHost = tmp.substring(0, tmp.indexOf("/"));
                    baseUrl = dirUrl.substring(0, dirUrl.lastIndexOf("/"));
                    return dirUrl + "&fun=new";
                }
            } catch (IOException e) {
                System.out.println("wait4login redo request.....");
                e.printStackTrace();
            }

        }
        return null;
    }

    public boolean login(String directUrl) throws IOException, DocumentException {

        String body = client.get(directUrl);
        System.out.println(body);
        Map<String, String> map = Utils.xml2Map(body);
        skey = map.get("skey");
        sid = map.get("wxsid");
        uin = map.get("wxuin");
        passTicket = map.get("pass_ticket");
        return true;
    }

    public JSONObject snycMessage() throws IOException {
        String url = baseUrl + "/webwxsync?sid=%s&skey=%s&lang=en_US&pass_ticket=%s";

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", uin);
        baseRequest.put("Sid", sid);
        baseRequest.put("Skey", skey);
        baseRequest.put("DeviceID", device_id);

        JSONObject params = new JSONObject();
        params.put("BaseRequest", baseRequest);
        params.put("rr", System.currentTimeMillis() / 1000);

        String body = "";
        JSONObject jsonBody = JSON.parseObject(body);
        return jsonBody;
    }

    public int[] syncCheck() throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        params.put("_", System.currentTimeMillis() - 201000L + "");
        params.put("sid", sid);
        params.put("uin", uin);
        params.put("skey", skey);
        params.put("deviceid", device_id);
        params.put("synckey", syncKeyStr);
        params.put("r", System.currentTimeMillis() + "");


        NameValuePair[] pair = new NameValuePair[params.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            pair[i] = new NameValuePair();
            pair[i].setName(entry.getKey());
            pair[i].setValue(entry.getValue());
            i++;
        }
        String paramString = EncodingUtil.formUrlEncode(pair, "utf8");

        String url = "https://" + syncHost + "/cgi-bin/mmwebwx-bin/synccheck?" + paramString;
        System.out.println("check url:" + url);

        String body = client.get(url);
        Map<String, String> map = Utils.resolveResult(body);

        JSONObject json = JSON.parseObject(map.get("window.synccheck"));
        int retCode = json.getInteger("retcode");
        int selector = json.getInteger("selector");
        int ans[] = {retCode,selector};
        return ans;
    }

    public void init() throws IOException {
        String url = String.format(baseUrl + "/webwxinit?r=%s&lang=en_US&pass_ticket=%s", System.currentTimeMillis() / 1000 + "", passTicket);

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", uin);
        baseRequest.put("Sid", sid);
        baseRequest.put("Skey", skey);
        baseRequest.put("DeviceID", device_id);

        JSONObject params = new JSONObject();
        params.put("BaseRequest", baseRequest);

        String result = client.post(url, params);
        JSONObject json = JSON.parseObject(result);

        syncKey = json.getJSONObject("SyncKey");
        user = json.getJSONObject("User");

        syncKeyStr = generateSyncKeyString(syncKey.getJSONArray("List"));
        System.out.println("syncKeyStr:" + syncKeyStr);
    }


    public void statusNotify() throws IOException {
        String url = String.format(baseUrl + "/webwxstatusnotify?lang=zh_CN&pass_ticket=%s", passTicket);

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", Integer.parseInt(uin));
        baseRequest.put("Sid", sid);
        baseRequest.put("Skey", skey);
        baseRequest.put("DeviceID", device_id);

        JSONObject params = new JSONObject();
        params.put("BaseRequest", baseRequest);
        params.put("Code", 3);
        params.put("FromUserName", user.getString("UserName"));
        params.put("ToUserName", user.getString("UserName"));
        params.put("ClientMsgId", System.currentTimeMillis() / 1000);

        String body = client.post(url, params);
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


    public void procMsg() throws IOException, InterruptedException {
        testSyncCheck();
        while (true) {
            long checkTime = System.currentTimeMillis() / 1000;
            int ret[] = syncCheck();
            System.out.println("retcode:" + ret[0] + " selector:" + ret[1]);
            if(ret[0] == 0){
                switch(ret[1]){
                    case 7:
                    case 2:
                        String msg = sync();
                        System.out.println(msg);
                        break;
                }

            }else if(ret[0] == 1100){

            }

            Thread.sleep(2000);
        }
    }

    public String generateSyncKeyString(JSONArray synKey) {
        String ans = null;

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < synKey.size(); i++) {
            JSONObject j = synKey.getJSONObject(i);
            sb.append(j.getString("Key"));
            sb.append("_");
            sb.append(j.getString("Val"));
            if (i < synKey.size() - 1)
                sb.append("|");
        }
        ans = sb.toString();
        return ans;
    }

    public String sync() throws IOException {
//        String url = String.format(baseUrl + "/webwxsync?sid=%s&skey=%s&lang=zh_CN&pass_ticket=%s", sid, skey, passTicket);

        NameValuePair pair[] = new NameValuePair[3];
        pair[0] = new NameValuePair("sid",sid);
        pair[1]=  new NameValuePair("skey",skey);
        pair[2] =new NameValuePair("pass_ticket",passTicket);
        String url = baseUrl+ "/webwxsync?lang=zh_CN&"+EncodingUtil.formUrlEncode(pair,"utf8");

        JSONObject params = new JSONObject();

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", uin);
        baseRequest.put("Sid", sid);
        baseRequest.put("Skey", skey);
        baseRequest.put("DeviceID", device_id);

        params.put("BaseRequest", baseRequest);
        params.put("SyncKey", syncKey);
        params.put("rr", System.currentTimeMillis() / 1000);

        String msg = client.post(url, params);
        System.out.println(msg);
        JSONObject ret = JSON.parseObject(msg);

        JSONObject syncKey = ret.getJSONObject("SyncKey");
        String syncKeyStr = generateSyncKeyString(syncKey.getJSONArray("List"));
        if(syncKey.getInteger("Count") >0 ){
            this.syncKey = syncKey;
            this.syncKeyStr = syncKeyStr;
        }
        return msg;
    }

    public void getContact(){
        String urlString = "https://" + syncHost + "/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?lang=zh_CN&ype=ex&skey=%s&r=%s";
        String url = String.format(urlString,passTicket,skey,System.currentTimeMillis()/1000+"");

    }

    public void sendMessage(String uid,String content) throws IOException {
        String url = baseUrl + String.format("/webwxsendmsg?pass_ticket=%s",passTicket);
        Random random = new Random();

        String msgId = System.currentTimeMillis()+(""+random.nextDouble()).substring(0,5).replace(".","");
        JSONObject params = new JSONObject();

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", uin);
        baseRequest.put("Sid", sid);
        baseRequest.put("Skey", skey);
        baseRequest.put("DeviceID", device_id);

        params.put("BaseRequest",baseRequest);

        JSONObject msg = new JSONObject();
        msg.put("Type",1);
        msg.put("Content",content);
        msg.put("FromUserName",user.getString(""));
        msg.put("ToUserName",uid);
        msg.put("LocalID",msgId);
        msg.put("ClientMsgId",msgId);

        params.put("Msg",msg);

        client.post(url,params);

    }


    public static void main(String args[]) {
        Login login = new Login();
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("appid", "wx782c26e4c19acffb");
            params.put("fun", "new");
            params.put("lang", "zh_CN");

            Random random = new Random();
            long radomCode = System.currentTimeMillis() + 1 + random.nextInt(999);
            params.put("_", radomCode + "");

            String uuid = login.getUUID(params);
            System.out.println(uuid);

            login.generateQRCode(uuid);
            String dirUrl = login.wait4Login(uuid);
            System.out.println(dirUrl);
            login.login(dirUrl);
            login.init();
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
