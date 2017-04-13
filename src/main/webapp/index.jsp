<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
    <meta name="description" content=""/>
    <meta name="author" content="Mr.Hanson"/>
    <!--[if IE]>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <![endif]-->
    <title>微信机器人</title>
    <!-- BOOTSTRAP CORE STYLE -->
    <link href="assets/css/bootstrap.css" rel="stylesheet"/>
    <!-- FONT AWESOME ICONS STYLE -->
    <link href="assets/css/font-awesome.css" rel="stylesheet"/>
    <!-- CUSTOM STYLE CSS -->
    <link href="assets/css/custom.css" rel="stylesheet"/>

    <link rel="stylesheet" href="assets/css/style.css" type="text/css">

</head>
<body>
<a id="menu-button" class="btn btn-success" href="#"> MENU</a>
<div id="sidebar-wrapper">
    <ul class="sidebar-nav ">
        <a id="menu-close" href="#" class="pull-right toggle">CLOSE</a>
        <span class="move-me">
    <li>
        <div class="main-name">
      <a href="#"><strong>DESIGN</strong></a>
            </div>
    </li>
    <li>
      <a href="#home-sec">主页</a>
    </li>
    <li>
      <a href="#donate-list">打赏清单</a>
    </li>
      <li>
      <a href="#guide">使用指南</a>
    </li>
    <li>
      <a href="#contact-us">联系我们</a>
    </li>
          </span>
    </ul>
</div>
<!-- END MENU SECTION-->
<div id="home-sec">
    <div class="overlay">
        <div class="container">
            <div class="row text-center">
                <div class="col-md-8 col-md-offset-2">
                    <h2>用摄像头扫描下面的二维码登录微信机器人</h2>
                    <p>
                        <img src="getQRCode.do" class="imageApt" alt="扫描"/>
                    </p>
                    <hr/>
                    <hr/>
                    <div class="just-pad"></div>
                </div>
            </div>
            <div class="row text-center">
                <table>
                    <caption><h2>打赏</h2></caption>
                    <tr>
                        <td>
                            <div>
                                <h4>微信</h4>
                                <p>
                                    <img src="/assets/img/wexinpay.png" class="imageApt"/>
                                </p>
                            </div>
                        </td>
                        <td>
                            <div>
                                <h4>支付宝</h4>
                                <p>
                                    <img src="/assets/img/alipay.jpg" class="imageApt"/>
                                </p>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>
<!-- END HOME SECTION-->

<div id="donate-list">
    <table class="zebra">
        <caption>打赏名单</caption>
        <thead>
        <tr>
            <td>壕名</td>
            <td>RMB</td>
            <td>时间</td>
        </tr>
        </thead>
    </table>
</div>
<div id="guide">
    <table class="zebra">
        <caption>使用指导</caption>
        <thead>
        <tr>
            <td>指令</td>
            <td>作用</td>
        </tr>
        <tr>
            <td>开启</td>
            <td>开启微信机器人</td>
        </tr>
        </thead>
    </table>
</div>
<div id="contact-us">
    <h4>扫描下方微信联系作者留言你想要的功能，我们会在第一时间做出来</h4>
    <p>
        <img src="/assets/img/myweixin.jpg" style="display:block;width:50%;"/>
    </p>
</div>

<!-- JQUERY SCRIPTS-->
<script src="assets/js/jquery-1.11.1.js"></script>
<!-- BOOTSTRAP SCRIPTS-->
<script src="assets/js/bootstrap.js"></script>
<!-- SCROLL SCRIPTS-->
<script src="assets/js/jquery.easing.min.js"></script>
<!-- CUSTOM SCRIPTS-->
<script src="assets/js/custom.js"></script>

</body>
</html>
