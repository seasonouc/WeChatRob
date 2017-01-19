package com.hanson.wechat.core;

/**
 * Created by hanson on 2017/1/19.
 */
public class WXBotSet {

    public static WXBotSet instance = null;
    public static WXBotSet getInstance(){
        if(instance == null){
            synchronized (WXBotSet.class){
                if(instance == null){
                    instance = new WXBotSet();
                }
            }
        }
        return instance;
    }
    private WXBotSet(){

    }
}
