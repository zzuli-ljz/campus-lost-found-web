package com.campus.lostfound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简化的启动类 - 用于测试
 */
@SpringBootApplication
@RestController
public class SimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class, args);
    }

    @GetMapping("/demo")
    public String demo() {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>校园失物招领系统</title>
                <style>
                    body {
                        font-family: 'Microsoft YaHei', sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 0;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .container {
                        text-align: center;
                        background: white;
                        padding: 3rem;
                        border-radius: 1rem;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                        max-width: 500px;
                        width: 90%;
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 1rem;
                        font-size: 2rem;
                    }
                    p {
                        color: #666;
                        line-height: 1.6;
                        margin-bottom: 2rem;
                    }
                    .btn {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 1rem 2rem;
                        text-decoration: none;
                        border-radius: 0.5rem;
                        font-weight: bold;
                        transition: transform 0.3s ease;
                    }
                    .btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                    }
                    .success {
                        color: #28a745;
                        font-size: 1.2rem;
                        margin-bottom: 1rem;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success">✅ 系统启动成功！</div>
                    <h1>🏫 校园失物招领系统</h1>
                    <p>
                        欢迎使用校园失物招领系统！<br>
                        系统已成功启动，您可以开始使用各项功能。
                    </p>
                    <a href="/campus-lost-found/auth/login" class="btn">🚀 开始使用</a>
                </div>
            </body>
            </html>
            """;
    }

    @GetMapping("/demo/login")
    public String demoLogin() {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>登录 - 校园失物招领系统</title>
                <style>
                    body {
                        font-family: 'Microsoft YaHei', sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 0;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .login-container {
                        background: white;
                        padding: 3rem;
                        border-radius: 1rem;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                        max-width: 400px;
                        width: 90%;
                    }
                    h2 {
                        text-align: center;
                        color: #333;
                        margin-bottom: 2rem;
                    }
                    .form-group {
                        margin-bottom: 1.5rem;
                    }
                    label {
                        display: block;
                        margin-bottom: 0.5rem;
                        color: #555;
                        font-weight: bold;
                    }
                    input {
                        width: 100%;
                        padding: 0.75rem;
                        border: 2px solid #ddd;
                        border-radius: 0.5rem;
                        font-size: 1rem;
                        box-sizing: border-box;
                    }
                    input:focus {
                        outline: none;
                        border-color: #667eea;
                    }
                    .btn {
                        width: 100%;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 1rem;
                        border: none;
                        border-radius: 0.5rem;
                        font-size: 1rem;
                        font-weight: bold;
                        cursor: pointer;
                        transition: transform 0.3s ease;
                    }
                    .btn:hover {
                        transform: translateY(-2px);
                    }
                    .demo-accounts {
                        margin-top: 2rem;
                        padding: 1rem;
                        background: #f8f9fa;
                        border-radius: 0.5rem;
                        font-size: 0.9rem;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="login-container">
                    <h2>🔐 登录系统</h2>
                        <form action="/campus-lost-found/auth/login" method="post">
                        <div class="form-group">
                            <label for="username">用户名：</label>
                            <input type="text" id="username" name="username" placeholder="请输入用户名" required>
                        </div>
                        <div class="form-group">
                            <label for="password">密码：</label>
                            <input type="password" id="password" name="password" placeholder="请输入密码" required>
                        </div>
                        <button type="submit" class="btn">登录</button>
                    </form>
                    <div class="demo-accounts">
                        <strong>测试账户：</strong><br>
                        管理员：admin / admin123<br>
                        普通用户：user / user123
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    @GetMapping("/test-login")
    public String testLogin() {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>登录测试</title>
                <style>
                    body {
                        font-family: 'Microsoft YaHei', sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 20px;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .container {
                        background: white;
                        padding: 2rem;
                        border-radius: 1rem;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                        max-width: 600px;
                        width: 90%;
                    }
                    .btn {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 0.75rem 1.5rem;
                        text-decoration: none;
                        border-radius: 0.5rem;
                        margin: 0.5rem;
                        font-weight: bold;
                        transition: transform 0.3s ease;
                    }
                    .btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                    }
                    .test-accounts {
                        background: #f8f9fa;
                        padding: 1rem;
                        border-radius: 0.5rem;
                        margin: 1rem 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>🔐 登录测试页面</h2>
                    <p>请使用以下测试账户进行登录测试：</p>
                    
                    <div class="test-accounts">
                        <h4>测试账户：</h4>
                        <p><strong>管理员：</strong> admin / admin123</p>
                        <p><strong>普通用户：</strong> user / user123</p>
                        <p><strong>其他用户：</strong> lisi / user123, wangwu / user123, zhaoliu / user123</p>
                    </div>
                    
                    <div>
                        <a href="/campus-lost-found/auth/login" class="btn">🔑 去登录</a>
                        <a href="/campus-lost-found/" class="btn">🏠 去主页</a>
                        <a href="/campus-lost-found/demo" class="btn">📱 去演示页</a>
                    </div>
                    
                    <div style="margin-top: 2rem; padding: 1rem; background: #e3f2fd; border-radius: 0.5rem;">
                        <h4>💡 提示：</h4>
                        <ul>
                            <li>如果登录后页面刷新，说明登录失败</li>
                            <li>如果登录成功，应该跳转到系统主页</li>
                            <li>可以在浏览器开发者工具中查看网络请求</li>
                        </ul>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
