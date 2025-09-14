package com.sx.mcp_server_test.controller;

import com.sx.mcp_server_test.tool.MathTools;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private McpSyncServer mcpSyncServer;

    @GetMapping("/updateTool")
    public String test(@RequestParam("type") Integer type){
        if (type == 0){
            addTools();
        } else if (type == 1) {
            delTools();
        }
        return "OK";
    }

    private void delTools() {
        logger.info("Server: {}", mcpSyncServer.getServerInfo());

        ToolCallback[] tools = ToolCallbacks.from(new MathTools());
        for (ToolCallback newTool : tools) {
            mcpSyncServer.removeTool(newTool.getToolDefinition().name());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("Delete tool: {}", newTool);
        }
    }

    private void addTools() {
        logger.info("Server: {}", mcpSyncServer.getServerInfo());

        List<McpServerFeatures.SyncToolSpecification> newTools = McpToolUtils
                .toSyncToolSpecifications(ToolCallbacks.from(new MathTools()));

        for (McpServerFeatures.SyncToolSpecification newTool : newTools) {
            mcpSyncServer.addTool(newTool);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("Add new tool: {}", newTool);
        }
    }
}
