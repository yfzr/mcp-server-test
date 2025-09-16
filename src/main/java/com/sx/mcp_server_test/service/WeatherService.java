/*
* Copyright 2025 - 2025 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.sx.mcp_server_test.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

	private final RestClient restClient;

	public WeatherService() {
		this.restClient = RestClient.builder()
				.defaultHeader("Accept", "application/geo+json")
				.defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
				.build();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record WeatherResponse(@JsonProperty("current") Current current) { // @formatter:off
		public record Current(
			@JsonProperty("time") LocalDateTime time, 
			@JsonProperty("interval") int interval,
			@JsonProperty("temperature_2m") double temperature_2m) {
		}
	} // @formatter:on

//	@Tool(description = "Get the temperature (in celsius) for a specific location") // @formatter:off
	public WeatherResponse weatherForecast( 
		@ToolParam(description = "The location latitude") double latitude,
		@ToolParam(description = "The location longitude") double longitude,
		ToolContext toolContext) { // @formatter:on

		WeatherResponse weatherResponse = restClient
				.get()
				.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
						latitude, longitude)
				.retrieve()
				.body(WeatherResponse.class);

		return weatherResponse;
	}

    @Tool(description = "Get the temperature (in celsius) for a specific location")
    public String getTemperature(McpSyncServerExchange exchange,
                                  @ToolParam(description = "The location latitude") double latitude,
                                  @ToolParam(description = "The location longitude") double longitude) {

        WeatherResponse weatherResponse = restClient
                .get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                        latitude, longitude)
                .retrieve()
                .body(WeatherResponse.class);

        StringBuilder deepseekWeatherPoem = new StringBuilder();
        StringBuilder zhipuWeatherPoem = new StringBuilder();

        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
                .level(McpSchema.LoggingLevel.INFO)
                .data("Start sampling")
                .build());

        if (exchange.getClientCapabilities().sampling() != null) {
            var messageRequestBuilder = McpSchema.CreateMessageRequest.builder()
                    .systemPrompt("You are a poet!")
                    .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
                            new McpSchema.TextContent(
                                    "Please write a poem about this weather forecast (temperature is in Celsius). Use markdown format :\n "
                                            + ModelOptionsUtils
                                            .toJsonStringPrettyPrinter(weatherResponse)))));

            var deepseekLlmMessageRequest = messageRequestBuilder
                    .modelPreferences(McpSchema.ModelPreferences.builder().addHint("deepseek").build())
                    .build();
            McpSchema.CreateMessageResult openAiLlmResponse = exchange.createMessage(deepseekLlmMessageRequest);

            deepseekWeatherPoem.append(((McpSchema.TextContent) openAiLlmResponse.content()).text());

            var zhipuLlmMessageRequest = messageRequestBuilder
                    .modelPreferences(McpSchema.ModelPreferences.builder().addHint("zhipu").build())
                    .build();
            McpSchema.CreateMessageResult anthropicAiLlmResponse = exchange.createMessage(zhipuLlmMessageRequest);

            zhipuWeatherPoem.append(((McpSchema.TextContent) anthropicAiLlmResponse.content()).text());

        }

        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
                .level(McpSchema.LoggingLevel.INFO)
                .data("Finish Sampling")
                .build());

        String responseWithPoems = "Deepseek poem about the weather: " + deepseekWeatherPoem + "\n\n" +
                "Zhipu poem about the weather: " + zhipuWeatherPoem + "\n"
                + ModelOptionsUtils.toJsonStringPrettyPrinter(weatherResponse);

        logger.info(zhipuWeatherPoem.toString(), responseWithPoems);

        return responseWithPoems;

    }
}