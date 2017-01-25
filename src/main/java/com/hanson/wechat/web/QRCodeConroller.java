package com.hanson.wechat.web;

import com.hanson.wechat.core.TuLingReply;
import com.hanson.wechat.core.WXBot;
import com.hanson.wechat.core.WXBotSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hanson on 2017/1/18.
 */
@Controller
public class QRCodeConroller {

    private final static Logger LOG = LoggerFactory.getLogger(TuLingReply.class);

    @RequestMapping("{getQRCode}")
    public void getQRCode(HttpServletResponse response) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        WXBot bot = new TuLingReply();
        bot.getUUID();
        bot.generateQRCode(outputStream);
        outputStream.flush();
        WXBotSet.getInstance().addJob(bot);
    }
}
