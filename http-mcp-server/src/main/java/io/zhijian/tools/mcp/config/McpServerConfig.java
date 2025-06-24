package io.zhijian.tools.mcp.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;

/**
 * MCP 服务器配置类
 * 负责配置 HTTP SSE 传输和 Servlet 注册
 */
@Configuration
@EnableWebMvc
public class McpServerConfig implements WebMvcConfigurer {

    @Bean
    public HttpServletSseServerTransportProvider servletSseServerTransportProvider() {
        return new HttpServletSseServerTransportProvider(new ObjectMapper(), "/sse");
    }

    @Bean
    public ServletRegistrationBean<HttpServletSseServerTransportProvider> sseServlet(
            HttpServletSseServerTransportProvider transportProvider) {
        ServletRegistrationBean<HttpServletSseServerTransportProvider> bean = 
            new ServletRegistrationBean<>(transportProvider, "/sse/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
} 