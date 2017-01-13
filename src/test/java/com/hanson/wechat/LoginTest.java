package com.hanson.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by hanson on 2017/1/11.
 */
public class LoginTest {
    public static void main(String args[]){
        JSONObject json = new JSONObject();
        json.put("hanson","hanson");
        JSONObject json1 = new JSONObject();
        json1.put("forest","forest");
        json.put("child",json1);
//        System.out.println(json.toJSONString());
//        System.out.println(json.toString());

        JSONObject json2 = (JSONObject) JSON.parse(json.toJSONString());
        System.out.println(json2.toString());
    }

}
