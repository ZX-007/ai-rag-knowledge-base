package com.lcx.mcp.server.csdn;

import com.alibaba.fastjson.JSON;
import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionRequest;
import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionResponse;
import com.lcx.mcp.server.csdn.domain.service.CSDNArticleService;
import com.lcx.mcp.server.csdn.infrastructure.gateway.ICSDNService;
import com.lcx.mcp.server.csdn.infrastructure.gateway.dto.ArticleRequestDTO;
import com.lcx.mcp.server.csdn.infrastructure.gateway.dto.ArticleResponseDTO;
import com.lcx.mcp.server.csdn.types.utils.MarkdownConverter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Collections;

@ActiveProfiles("local")
@SpringBootTest
class McpServerCsdnApplicationTests {

    private final Logger log = LoggerFactory.getLogger(McpServerCsdnApplicationTests.class);

    @Autowired
    private ICSDNService csdnService;
    @Autowired
    private CSDNArticleService csdnArticleService;

    @Test
    public void test_md2html() {
        String content = """
                **关于DDD是什么，在维基百科有一个明确的定义。"Domain-driven design (DDD) is a major software design approach." 也就是说DDD是一种主要的软件设计方法。而软件设计涵盖了；范式、模型、框架、方法论。**
                
                - 范式（paradigm）指的是一种编程思想。
                - 模型（model）指的是对现实世界或者问题的抽象描述。
                - 框架（framework）指的是提供了一系列通用功能和结构的软件工具。
                - 方法论（methodology）指的是一种系统的、有组织的解决问题的方法。
                
                所以，DDD不只是只有指导思想，伴随的DDD的还包括框架结构分层。但说到底，这些仍然是理论讨论。在没有一个DDD落地项目物参考下，其实大部分码农是没法完成DDD开发的。所以小傅哥今年花费了5个月假期/周末的时间，完成的《DDD简明开发教程》，帮助大家落地DDD编码。
                """;
        System.out.println(content);
        System.out.println(MarkdownConverter.convertToHtml(content));
    }

    @Test
    public void test_saveArticle() throws IOException {
        // 构建请求对象
        ArticleRequestDTO request = new ArticleRequestDTO();
        request.setTitle("测试文章标题");
        request.setMarkdowncontent("# 测试文章内容\n这是一篇测试文章");
        request.setContent("<h1>测试文章内容</h1><p>这是一篇测试文章</p>");
        request.setReadType("public");
        request.setLevel("0");
        request.setTags("测试,文章");
        request.setStatus(0);
        request.setCategories("后端");
        request.setType("original");
        request.setOriginal_link("");
        request.setAuthorized_status(true);
        request.setDescription("这是一篇测试文章的描述");
        request.setResource_url("");
        request.setNot_auto_saved("0");
        request.setSource("pc_mdeditor");
        request.setCover_images(Collections.emptyList());
        request.setCover_type(0);
        request.setIs_new(1);
        request.setVote_id(0);
        request.setResource_id("");
        request.setPubStatus("draft");
        request.setSync_git_code(0);

        // 调用接口
        String cookie = "uuid_tt_dd=10_21028300260-1753456099227-316694; fid=20_85341549170-1753456101247-517882; UserName=2301_80465378; UserInfo=dfbc53bf50cf49b59e8567b3c6361a87; UserToken=dfbc53bf50cf49b59e8567b3c6361a87; UserNick=ZX-07; AU=811; UN=2301_80465378; BT=1753962628382; p_uid=U010000; csdn_newcert_2301_80465378=1; c_adb=1; c_ab_test=1; vip_auto_popup=1; c_segment=6; dc_sid=38ea92bd88633815056f815f535443bb; dc_session_id=10_1760429088541.825268; SESSION=df4fba43-eaac-4e47-8f2d-d7dd24cb72c7; c_first_ref=default; c_first_page=https%3A//blog.csdn.net/2301_80465378/article/details/153266504; creative_btn_mp=3; c_dsid=11_1760430755603.549748; c_page_id=default; c_pref=https%3A//editor.csdn.net/; c_ref=https%3A//mp.csdn.net/; log_Id_view=246; log_Id_click=16; dc_tos=t446wh; log_Id_pv=15";
        Call<ArticleResponseDTO> call = csdnService.saveArticle(request, cookie);
        Response<ArticleResponseDTO> response = call.execute();

        System.out.println("\r\n=== 测试结果 ===");
        System.out.println("HTTP 状态码: " + response.code());
        System.out.println("是否成功: " + response.isSuccessful());
        System.out.println("响应消息: " + response.message());
        
        // 验证结果
        if (response.isSuccessful()) {
            ArticleResponseDTO articleResponseDTO = response.body();
            System.out.println("响应体: " + JSON.toJSONString(articleResponseDTO));
            log.info("发布文章成功 {}", articleResponseDTO);
        } else {
            System.out.println("请求失败！");
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                System.out.println("错误响应体: " + errorBody);
            }
        }
    }

    // {"code":401,"message":"请登录后操作！","traceId":"","data":null}
    // {"code":400,"msg":"今天发表文章数量已达到限制的5篇。","traceId":"","data":null}
    @Test
    public void test_domain_saveArticle() throws IOException {
        String json = """
                {
                    "title": "互联网大厂Java面试：严肃面试官与搞笑程序员的对决",
                    "description": "在互联网大厂的面试中，严肃的面试官与搞笑的程序员上演了一场精彩的对话。面试官提出Java核心知识、HashMap、Spring等问题，程序员则用幽默的方式作答。本文不仅展现了轻松的面试氛围，还附上了详细的技术问题答案解析，帮助读者更好地理解相关知识。",
                    "tags": "Java,面试,互联网,程序员,Spring,SpringBoot,HashMap,JVM",
                
                    "markdowncontent": "## 场景：\\n\\n在某互联网大厂的面试室，一位严肃的面试官正准备提问，而对面坐着一位看似紧张却又想显得轻松的程序员小张。\\n\\n**面试官**：我们先来聊聊Java核心知识。第一个问题，Java中的JVM是如何管理内存的？\\n\\n**程序员小张**：哦，这个简单！JVM就像一个巨大的购物车，负责把所有的变量都放进去，呃……然后就……管理起来？\\n\\n**面试官**：嗯，第二个问题，请说说HashMap的工作原理。\\n\\n**程序员小张**：HashMap嘛，就是……呃，一个很大的箱子，大家都往里面扔东西，有时候会打架……\\n\\n**面试官**：那么第三个问题，能不能讲讲Spring和SpringBoot的区别？\\n\\n**程序员小张**：Spring是……呃，春天？SpringBoot就是穿靴子的春天嘛！哈哈……\\n\\n**面试官**：好，今天的问题就问到这里。回去等通知吧。\\n\\n## 答案解析：\\n\\n1. **JVM内存管理**：JVM内存管理包括堆内存和栈内存，堆内存用于存储对象实例，栈内存用于执行线程时的栈帧。\\n\\n2. **HashMap原理**：HashMap通过哈希函数将键映射到对应的值，并通过链表解决哈希冲突。\\n\\n3. **Spring与SpringBoot区别**：Spring是一个大型应用框架，而SpringBoot是基于Spring的快速开发套件，简化了Spring应用的配置。",
                    "content": "<h2>场景：</h2>\\n<p>在某互联网大厂的面试室，一位严肃的面试官正准备提问，而对面坐着一位看似紧张却又想显得轻松的程序员小张。</p>\\n<p><strong>面试官</strong>：我们先来聊聊Java核心知识。第一个问题，Java中的JVM是如何管理内存的？</p>\\n<p><strong>程序员小张</strong>：哦，这个简单！JVM就像一个巨大的购物车，负责把所有的变量都放进去，呃……然后就……管理起来？</p>\\n<p><strong>面试官</strong>：嗯，第二个问题，请说说HashMap的工作原理。</p>\\n<p><strong>程序员小张</strong>：HashMap嘛，就是……呃，一个很大的箱子，大家都往里面扔东西，有时候会打架……</p>\\n<p><strong>面试官</strong>：那么第三个问题，能不能讲讲Spring和SpringBoot的区别？</p>\\n<p><strong>程序员小张</strong>：Spring是……呃，春天？SpringBoot就是穿靴子的春天嘛！哈哈……</p>\\n<p><strong>面试官</strong>：好，今天的问题就问到这里。回去等通知吧。</p>\\n<h2>答案解析：</h2>\\n<ol>\\n<li>\\n<p><strong>JVM内存管理</strong>：JVM内存管理包括堆内存和栈内存，堆内存用于存储对象实例，栈内存用于执行线程时的栈帧。</p>\\n</li>\\n<li>\\n<p><strong>HashMap原理</strong>：HashMap通过哈希函数将键映射到对应的值，并通过链表解决哈希冲突。</p>\\n</li>\\n<li>\\n<p><strong>Spring与SpringBoot区别</strong>：Spring是一个大型应用框架，而SpringBoot是基于Spring的快速开发套件，简化了Spring应用的配置。</p>\\n</li>\\n</ol>\\n",
                
                    "status": 0,
                    "pubStatus": "draft",
                    "readType": "public",
                    "level": "0",
                
                    "cover_type": 0,
                    "cover_images": [],
                
                    "source": "pc_mdeditor",
                    "not_auto_saved": "0",
                    "is_new": 1,
                
                    "resource_id": "",
                    "resource_url": "",
                    "sync_git_code": 0,
                    "vote_id": 0
                }
                """;
        ArticleFunctionRequest request = JSON.parseObject(json, ArticleFunctionRequest.class);
        ArticleFunctionResponse response = csdnArticleService.saveArticle(request);
        log.info("测试结果:{}", JSON.toJSONString(response));
    }

}
