package io.zhijian.tools.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * MCP服务器主类
 * 只负责Spring Boot应用的启动
 * MCP服务器的配置和创建由McpServerConfig负责
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class MyServer {

    /**
     * 启动Web应用
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("Starting MCP Application...");
        SpringApplication.run(MyServer.class, args);
        System.out.println("MCP Application started successfully!");
    }
}