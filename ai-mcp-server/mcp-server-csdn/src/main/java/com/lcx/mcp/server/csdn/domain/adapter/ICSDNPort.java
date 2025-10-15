package com.lcx.mcp.server.csdn.domain.adapter;

import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionRequest;
import com.lcx.mcp.server.csdn.domain.model.ArticleFunctionResponse;

import java.io.IOException;

public interface ICSDNPort {

    ArticleFunctionResponse writeArticle(ArticleFunctionRequest request) throws IOException;

}
