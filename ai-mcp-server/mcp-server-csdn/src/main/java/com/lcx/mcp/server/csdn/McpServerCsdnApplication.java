package com.lcx.mcp.server.csdn;

import com.lcx.mcp.server.csdn.domain.service.CSDNArticleService;
import com.lcx.mcp.server.csdn.infrastructure.gateway.ICSDNService;
import com.lcx.mcp.server.csdn.types.properties.CSDNApiProperties;
import jakarta.annotation.Resource;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class McpServerCsdnApplication implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(McpServerCsdnApplication.class);

    @Resource
    private CSDNApiProperties csdnApiProperties;

    public static void main(String[] args) {
        SpringApplication.run(McpServerCsdnApplication.class, args);
    }

    @Bean
    public ICSDNService csdnService() {
        // 配置 OkHttpClient，确保请求和响应都使用 UTF-8 编码
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request.Builder requestBuilder = original.newBuilder();

                    // 确保请求体使用 UTF-8 编码
                    if (original.body() != null) {
                        requestBuilder.removeHeader("Content-Type");
                        requestBuilder.addHeader("Content-Type", "application/json; charset=UTF-8");
                    }

                    // 添加 Accept-Charset 头，要求服务器返回 UTF-8 编码
                    requestBuilder.addHeader("Accept-Charset", "UTF-8");

                    okhttp3.Request request = requestBuilder.build();
                    okhttp3.Response response = chain.proceed(request);

                    // 如果响应体存在且没有正确的字符集，手动处理编码
                    if (response.body() != null) {
                        MediaType contentType = response.body().contentType();
                        if (contentType != null) {
                            // 如果没有charset或charset不是UTF-8，强制使用UTF-8
                            if (contentType.charset() == null || !StandardCharsets.UTF_8.equals(contentType.charset())) {
                                MediaType newContentType = MediaType.parse("application/json; charset=UTF-8");
                                byte[] bodyBytes = response.body().bytes();
                                String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
                                okhttp3.ResponseBody newBody = okhttp3.ResponseBody.create(newContentType, bodyString);

                                return response.newBuilder()
                                        .body(newBody)
                                        .build();
                            }
                        }
                    }

                    return response;
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://bizapi.csdn.net/")
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(ICSDNService.class);
    }

    @Bean
    public ToolCallbackProvider csdnTools(CSDNArticleService csdnArticleService) {
        return MethodToolCallbackProvider.builder().toolObjects(csdnArticleService).build();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("check csdn cookie ...");
        if (csdnApiProperties.getCookie() == null) {
            log.warn("csdn cookie key is null, please set it in application.yml");
        } else {
            log.info("csdn cookie  key is {}", csdnApiProperties.getCookie());
        }
    }

}
