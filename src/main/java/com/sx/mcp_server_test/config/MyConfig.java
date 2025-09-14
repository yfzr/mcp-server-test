package com.sx.mcp_server_test.config;

import com.sx.mcp_server_test.service.WeatherService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MyConfig {

    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }

    //    @Bean
//    public List<McpServerFeatures.SyncResourceSpecification> myResources() {
//        var systemInfoResource = new McpSchema.Resource();
//        var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {
//            try {
//                var systemInfo = Map.of(...);
//                String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
//                return new McpSchema.ReadResourceResult(
//                        List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));
//            }
//            catch (Exception e) {
//                throw new RuntimeException("Failed to generate system info", e);
//            }
//        });
//
//        return List.of(resourceSpecification);
//    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
        var prompt = new McpSchema.Prompt("greeting", "A friendly greeting prompt",
                List.of(new McpSchema.PromptArgument("name", "The name to greet", true)));

        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            String nameArgument = (String) getPromptRequest.arguments().get("name");
            if (nameArgument == null) { nameArgument = "friend"; }
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent("Hello " + nameArgument + "! How can I assist you today?"));
            return new McpSchema.GetPromptResult("A personalized greeting message", List.of(userMessage));
        });

        return List.of(promptSpecification);
    }

//    @Bean
//    public List<McpServerFeatures.SyncCompletionSpecification> myCompletions() {
//        var completion = new McpServerFeatures.SyncCompletionSpecification(
//                "code-completion",
//                "Provides code completion suggestions",
//                (exchange, request) -> {
//                    // Implementation that returns completion suggestions
//                    return new McpSchema.CompleteResult((List.of(
//                            new McpSchema.Completion("suggestion1", "First suggestion"),
//                            new McpSchema.Completion("suggestion2", "Second suggestion")
//                    ));
//                }
//        );
//
//        return List.of(completion);
//    }
}
