package com.hanson.wechat.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



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

    private Map<String,WXBot> jobMap = null;

    private ThreadPoolExecutor executor = null;
    private BlockingQueue<Runnable>  queue = null;
    private WXBotSet(){
        queue = new ArrayBlockingQueue<Runnable>(20);
        executor = new ThreadPoolExecutor(15,20,1000L, TimeUnit.MILLISECONDS,queue);
        jobMap = new HashMap<String, WXBot>();
    }
    public void addJob(WXBot bot){
        executor.execute(bot);
    }

}
