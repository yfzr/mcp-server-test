package com.sx.mcp_server_test.controller;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequestMapping("/aaa")
@RestController
public class A2AExample {

//    private final ChatModel chatModel;
    private final AgentCardProvider agentCardProvider;
//    private final ReactAgent localDataAnalysisAgent;

    @Autowired
    public A2AExample(
                      AgentCardProvider agentCardProvider) {
//        this.chatModel = chatModel;
        this.agentCardProvider = agentCardProvider;
//        this.localDataAnalysisAgent = localDataAnalysisAgent;
    }

    @GetMapping("/test")
    public String runDemo(@RequestParam("msg") String msg) throws GraphRunnerException {
        // 1. 本地直连：验证本地注册的 ReactAgent 可用
//        Optional<OverAllState> localResult = localDataAnalysisAgent.invoke(
//                "请对上月销售数据进行趋势分析，并给出关键结论。"
//        );
//        localResult.ifPresent(state -> {
//            System.out.println("本地调用成功");
//        });

        // 2. 发现：通过 AgentCardProvider 从注册中心获取该 Agent 的 AgentCard
        A2aRemoteAgent remote = A2aRemoteAgent.builder()
                .name("data_analysis_agent")
                .agentCardProvider(agentCardProvider)  // 从 Nacos 自动获取 AgentCard
                .description("数据分析远程代理")
                .build();

        // 3. 远程调用：通过 A2aRemoteAgent 调用
        Optional<OverAllState> remoteResult = remote.invoke(msg);
        remoteResult.ifPresent(state -> {
            System.out.println("远程调用成功：" + state);
        });

        return remoteResult.get().toString();
    }
}
