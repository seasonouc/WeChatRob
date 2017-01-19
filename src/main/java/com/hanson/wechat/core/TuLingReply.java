package com.hanson.wechat.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanson.wechat.utils.HttpClient2;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * Created by hanson on 2017/1/14.
 */
public class TuLingReply extends Login{

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
        TuLingReply reply = new TuLingReply();
        try {
            String uuid = reply.getUUID();
            System.out.println(uuid);

            reply.generateQRCode(uuid);
            String dirUrl = reply.wait4Login(uuid);
            System.out.println(dirUrl);
            reply.login(dirUrl);
            reply.init();
            reply.statusNotify();
            reply.getContact();
            new Thread(reply).start();
            //        login.procMsg();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void getReply(String msg){
        JSONObject json = JSON.parseObject(msg);
        int msgCount = json.getInteger("AddMsgCount");
        JSONObject ans = new JSONObject();
        if(msgCount >  0){
            JSONArray msgList = json.getJSONArray("AddMsgList");
            for(int i=0;i<msgList.size();i++){
                JSONObject obj = msgList.getJSONObject(i);
                String content = obj.getString("Content");
                int msgType = obj.getInteger("MsgType");
                switch(msgType){
                    case 37:{
                        //frient request
                        String uid = obj.getString("FromUserName");
                        String callBack = getTulingReply(msg,uid);
                        try {
                            sendMessage(uid, callBack);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    case 1:{
                        //group message
                        String uid = obj.getString("FromUserName");
                        String callBack = getTulingReply(content, uid);
                        if (callBack == null) {
                            continue;
                        }
                        ans.put("uid", uid);
                        ans.put("content", callBack);
                        try {
                            sendMessage(uid, callBack);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public String getTulingReply(String msg,String uid){
        JSONObject params = new JSONObject();
        params.put( "key","b4ac2b1c72c512584db5322dbd6011a4");
        params.put("info", msg);
        params.put("userid",uid);

        System.out.println(params.toJSONString());
        String url = "http://www.tuling123.com/openapi/api";
        try {
            String result = client.post(url,params);
            JSONObject reply = JSON.parseObject(result);
            if(reply.getInteger("code") == 100000){
                return reply.getString("text").replace("<br>","").replace("\\xa0", "" );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
