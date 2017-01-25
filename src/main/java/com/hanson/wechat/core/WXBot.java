package com.hanson.wechat.core;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanson.wechat.utils.HttpClient2;
import com.hanson.wechat.utils.QRCode;
import com.hanson.wechat.utils.Utils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by hanson on 2017/1/11.
 */
public class WXBot implements  Runnable{

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
    protected JSONObject user;
    private JSONObject syncKey;
    private Map<String,String> friendMap;
    private String uuid = null;

    private final static Logger LOG = LoggerFactory.getLogger(WXBot.class);

    public WXBot() {
        client = new HttpClient2();
//        client = HttpClient2.getInstance();
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        sb.append("e");
        for(int i = 0;i<15;i++){
            sb.append(random.nextInt(10));
        }
        device_id = sb.toString();
    }

    public String getUUID() throws IOException {
        String url = "https://login.weixin.qq.com/jslogin";

        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", "wx782c26e4c19acffb");
        params.put("fun", "new");
        params.put("lang", "zh_CN");

        Random random = new Random();
        long radomCode = System.currentTimeMillis() + 1 + random.nextInt(999);
        params.put("_", radomCode + "");

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
        uuid = map.get("window.QRLogin.uuid");

        LOG.debug("get uuid:" + uuid);
        return uuid;
    }

    public void generateQRCode(OutputStream outputStream) {

        String url = "https://login.weixin.qq.com/l/" + uuid;
        String path = WXBot.class.getClassLoader().getResource("//").getPath() + "qrcode.png";
        QRCode qrcode = new QRCode();
        if(outputStream == null){
            qrcode.createCode(path, url);
            JFrame frame = new JFrame();
            frame.setSize(400, 400);
            ImageIcon imageIcon = new ImageIcon(path);
            JLabel label = new JLabel();
            label.setIcon(imageIcon);
            frame.add(label);
            frame.setVisible(true);
        }else{
            qrcode.createCode(outputStream,url);
        }
        LOG.debug("generate qrcode");

    }

    public String wait4Login(String uuid) throws InterruptedException {
        String loginTemp = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?tip=%s&uuid=%s&_=%s";
        int tip = 1;
        int retryTime = 10;
        String url = String.format(loginTemp, tip, uuid, System.currentTimeMillis() / 1000);
        while (retryTime > 0) {
            Thread.sleep(3000);
            retryTime--;

            try {
                String body = client.get(url);
                Map<String, String> map = Utils.resolveResult(body);

                if ("200".equals(map.get("window.code"))) {
                    String dirUrl = map.get("window.redirect_uri");
                    String tmp = dirUrl.substring(8);
                    baseHost = tmp.substring(0, tmp.indexOf("/"));
                    syncHost = "webpush." + baseHost;
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
        LOG.info("user onLine:{}",user);
        syncKeyStr = generateSyncKeyString(syncKey.getJSONArray("List"));
        System.out.println("syncKeyStr:" + syncKeyStr);
    }


    public void statusNotify() throws IOException {
        String url = String.format(baseUrl + "/webwxstatusnotify?lang=zh_CN&pass_ticket=%s", passTicket);

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", uin);
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

    public String generateSyncKeyString(JSONArray synKey) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < synKey.size(); i++) {
            JSONObject j = synKey.getJSONObject(i);
            sb.append(j.getString("Key"));
            sb.append("_");
            sb.append(j.getString("Val"));
            if (i < synKey.size() - 1)
                sb.append("|");
        }
        return sb.toString();
    }

    public String sync() throws IOException {
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
        JSONObject ret = JSON.parseObject(msg);

        JSONObject syncKey = ret.getJSONObject("SyncKey");
        String syncKeyStr = generateSyncKeyString(syncKey.getJSONArray("List"));
        if(syncKey.getInteger("Count") >0 ){
            this.syncKey = syncKey;
            this.syncKeyStr = syncKeyStr;
        }
        return msg;
    }

    public boolean getContact(){
        String url = baseUrl + "/webwxgetcontact?pass_ticket=%s&skey=%s&r=%s";
        url = String.format(url,passTicket,skey,System.currentTimeMillis()/1000+"");
        try {
            String  ret = client.post(url,null);
            JSONObject contact = JSON.parseObject(ret);
            int amount = contact.getInteger("MemberCount");
            JSONArray array = contact.getJSONArray("MemberList");
            friendMap = new HashMap<String, String>();
            for (int i=0;i<array.size();i++) {
                JSONObject member = array.getJSONObject(i);
                String userName = member.getString("UserName");
                String remarkName = member.getString("RemarkName");
                if(remarkName==null||"".equals(remarkName)){
                    continue;
                }
                friendMap.put(userName,remarkName);
            }
            System.out.println(ret);
        } catch (IOException e) {
            return false;
        }
        return true;
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

        params.put("Scene",0);

        JSONObject msg = new JSONObject();
        msg.put("Type",1);
        msg.put("Content",content);
        msg.put("FromUserName",user.getString("UserName"));
        msg.put("ToUserName",uid);
        msg.put("LocalID",msgId);
        msg.put("ClientMsgId",msgId);

        params.put("Msg",msg);

        client.post(url,params);

    }



    public  void getReply(String msg){
    }

    public void run() {

        try {
//            String uuid = getUUID();
//            System.out.println(uuid);
//
//            generateQRCode(uuid);
            String dirUrl = wait4Login(uuid);
            System.out.println(dirUrl);
            if(dirUrl == null){
                return;
            }
            login(dirUrl);
            init();
            statusNotify();
            getContact();
        } catch (IOException e) {
            LOG.error("error:{}",e.getCause());
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            LOG.error("error:{}",e.getCause());
            e.printStackTrace();
            return;
        } catch (DocumentException e) {
            LOG.error("error:{}",e.getCause());
            e.printStackTrace();
            return;
        }
        while (true) {
            try {
                int ret[] = syncCheck();
//                System.out.println("retcode:" + ret[0] + " selector:" + ret[1]);
                if (ret[0] == 0) {
                    switch (ret[1]) {
                        case 7:
                        case 2:
                        case 6:
                            String msg = sync();
                            getReply(msg);
                            break;
                    }

                } else if (ret[0] == 1100) {
                    LOG.info("user offline,user info:{}",user);
                    break;
                }
            } catch (IOException e) {
                LOG.error("error:{}",e.getCause());
                e.printStackTrace();
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("error:{}",e.getCause());
                e.printStackTrace();
                break;
            }
        }
    }
//
//    public static void main(String args[]) {
//        WXBot login = new WXBot();
//        try {
//            String uuid = login.getUUID();
//            System.out.println(uuid);
//
//            login.generateQRCode(uuid);
//            String dirUrl = login.wait4Login(uuid);
//            System.out.println(dirUrl);
//            login.login(dirUrl);
//            login.init();
//            login.statusNotify();
//            login.getContact();
//            new Thread(login).start();
//            //        login.procMsg();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
//    }
}
