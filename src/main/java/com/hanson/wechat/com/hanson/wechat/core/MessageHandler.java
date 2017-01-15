package com.hanson.wechat.com.hanson.wechat.core;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by hanson on 2017/1/14.
 */
public interface MessageHandler {

     public String HandleMaeesage(JSONObject json);
}
