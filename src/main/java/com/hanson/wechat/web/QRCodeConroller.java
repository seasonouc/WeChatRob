package com.hanson.wechat.web;

import com.hanson.wechat.core.Login;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hanson on 2017/1/18.
 */
@Controller
public class QRCodeConroller {

    @RequestMapping("{getQRCode}")
    public void getQRCode(HttpServletResponse response) throws IOException {
        Login login = new Login();
        OutputStream outputStream = response.getOutputStream();
        String uuid = login.getUUID();
        login.generateQRCode(uuid,outputStream);
        outputStream.flush();
    }
}
