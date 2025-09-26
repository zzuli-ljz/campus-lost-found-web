package com.campus.lostfound;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 简单的HTTP服务器 - 用于测试
 */
public class TestServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        
        server.createContext("/", new RootHandler());
        server.createContext("/test", new TestHandler());
        server.createContext("/campus-lost-found", new MainHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Test server started on http://localhost:8082");
        System.out.println("Visit http://localhost:8082/test to see test page");
        System.out.println("Visit http://localhost:8082/campus-lost-found to see main page");
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Test Server</title>
                </head>
                <body>
                    <h1>Test Server Running</h1>
                    <p><a href="/test">Go to Test Page</a></p>
                    <p><a href="/campus-lost-found">Go to Main App</a></p>
                </body>
                </html>
                """;
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>测试页面</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; }
                        .container { max-width: 600px; margin: 0 auto; }
                        .success { color: green; font-size: 18px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success">✅ 服务器运行正常！</div>
                        <h1>校园失物招领系统 - 测试页面</h1>
                        <p>这是一个简单的测试服务器，用于验证基本功能。</p>
                        <p>如果您看到这个页面，说明服务器已经成功启动。</p>
                        <p><a href="/campus-lost-found">访问主应用</a></p>
                    </div>
                </body>
                </html>
                """;
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    static class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
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
                        .warning {
                            background: #fff3cd;
                            border: 1px solid #ffeaa7;
                            color: #856404;
                            padding: 1rem;
                            border-radius: 0.5rem;
                            margin: 1rem 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🏫 校园失物招领系统</h1>
                        <p>系统正在运行中...</p>
                        <div class="warning">
                            <strong>⚠️ 注意：</strong><br>
                            这是一个简化的测试服务器。<br>
                            完整功能需要Spring Boot环境支持。<br>
                            当前无法进行数据库操作和用户认证。
                        </div>
                        <p>如果您需要测试完整功能，请确保：</p>
                        <ul style="text-align: left; color: #666;">
                            <li>Java环境配置正确</li>
                            <li>Maven依赖已下载</li>
                            <li>数据库连接正常</li>
                        </ul>
                    </div>
                </body>
                </html>
                """;
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}