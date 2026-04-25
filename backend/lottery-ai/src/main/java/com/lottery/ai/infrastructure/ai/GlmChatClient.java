package com.lottery.ai.infrastructure.ai;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.ai.application.dto.LotteryUserAnalysisMetrics;
import com.lottery.ai.config.GlmProperties;
import com.lottery.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class GlmChatClient {

    private static final Logger log = LoggerFactory.getLogger(GlmChatClient.class);

    private final ObjectMapper objectMapper;
    private final GlmProperties glmProperties;
    private final SystemPromptProvider systemPromptProvider;

    public GlmChatClient(ObjectMapper objectMapper, GlmProperties glmProperties, SystemPromptProvider systemPromptProvider) {
        this.objectMapper = objectMapper;
        this.glmProperties = glmProperties;
        this.systemPromptProvider = systemPromptProvider;
    }

    public boolean isEnabled() {
        return glmProperties.isEnabled()
                && glmProperties.getApiKey() != null
                && !glmProperties.getApiKey().isBlank();
    }

    public String getModelName() {
        return isEnabled() ? glmProperties.getModel() : "local-fallback";
    }

    public GlmNarrative generateNarrative(LotteryUserAnalysisMetrics metrics, String focus) {
        if (!isEnabled()) {
            throw new BusinessException("GLM API key is not configured");
        }

        try {
            ZhipuAiClient client = ZhipuAiClient.builder()
                    .ofZHIPU()
                    .apiKey(glmProperties.getApiKey())
                    .build();

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(glmProperties.getModel())
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.SYSTEM.value())
                                    .content(systemPromptProvider.loadSystemPrompt())
                                    .build(),
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(buildUserPrompt(metrics, focus))
                                    .build()
                    ))
                    .stream(false)
                    .temperature(glmProperties.getTemperature())
                    .maxTokens(glmProperties.getMaxTokens())
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            JsonNode contentNode = extractNarrativeNode(response);
            return new GlmNarrative(
                    readText(contentNode, "overview"),
                    readStringList(contentNode, "insights"),
                    readStringList(contentNode, "suggestions")
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to call GLM for lottery analysis via SDK: {}", ex.getMessage(), ex);
            throw new BusinessException("GLM analysis is temporarily unavailable");
        }
    }

    public GlmNarrative streamNarrative(LotteryUserAnalysisMetrics metrics, String focus, Consumer<GlmStreamChunk> chunkConsumer) {
        if (!isEnabled()) {
            throw new BusinessException("GLM API key is not configured");
        }

        try {
            ZhipuAiClient client = ZhipuAiClient.builder()
                    .ofZHIPU()
                    .apiKey(glmProperties.getApiKey())
                    .build();

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(glmProperties.getModel())
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.SYSTEM.value())
                                    .content(systemPromptProvider.loadSystemPrompt())
                                    .build(),
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(buildUserPrompt(metrics, focus))
                                    .build()
                    ))
                    .stream(true)
                    .temperature(glmProperties.getTemperature())
                    .maxTokens(glmProperties.getMaxTokens())
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            if (response == null || response.getFlowable() == null) {
                throw new BusinessException("GLM stream response is empty");
            }

            StringBuilder reasoningBuilder = new StringBuilder();
            StringBuilder contentBuilder = new StringBuilder();
            response.getFlowable().blockingForEach(modelData -> {
                if (modelData == null || modelData.getChoices() == null || modelData.getChoices().isEmpty()) {
                    return;
                }
                modelData.getChoices().forEach(choice -> {
                    if (choice == null || choice.getDelta() == null) {
                        return;
                    }
                    if (choice.getDelta().getReasoningContent() != null && !choice.getDelta().getReasoningContent().isBlank()) {
                        reasoningBuilder.append(choice.getDelta().getReasoningContent());
                        if (chunkConsumer != null) {
                            chunkConsumer.accept(new GlmStreamChunk("reasoning", choice.getDelta().getReasoningContent()));
                        }
                    }
                    if (choice.getDelta().getContent() != null && !choice.getDelta().getContent().isBlank()) {
                        contentBuilder.append(choice.getDelta().getContent());
                        if (chunkConsumer != null) {
                            chunkConsumer.accept(new GlmStreamChunk("delta", choice.getDelta().getContent()));
                        }
                    }
                });
            });

            JsonNode contentNode = parseJsonContent(firstNonBlank(contentBuilder.toString(), reasoningBuilder.toString()));
            return new GlmNarrative(
                    readText(contentNode, "overview"),
                    readStringList(contentNode, "insights"),
                    readStringList(contentNode, "suggestions")
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to stream GLM lottery analysis via SDK: {}", ex.getMessage(), ex);
            throw new BusinessException("GLM analysis stream is temporarily unavailable");
        }
    }

    private String buildUserPrompt(LotteryUserAnalysisMetrics metrics, String focus) {
        Map<String, Object> metricPayload = new LinkedHashMap<>();
        metricPayload.put("userId", metrics.userId());
        metricPayload.put("totalDrawCount", metrics.totalDrawCount());
        metricPayload.put("activeDays", metrics.activeDays());
        metricPayload.put("recent30DayDrawCount", metrics.recent30DayDrawCount());
        metricPayload.put("previous30DayDrawCount", metrics.previous30DayDrawCount());
        metricPayload.put("trendSummary", metrics.trendSummary());
        metricPayload.put("highestPrizeLevel", metrics.highestPrizeLevel());
        metricPayload.put("highTierHitCount", metrics.highTierHitCount());
        metricPayload.put("pendingReviewCount", metrics.pendingReviewCount());
        metricPayload.put("favoriteTimeBucket", metrics.favoriteTimeBucket());
        metricPayload.put("mostFrequentPrizeName", metrics.mostFrequentPrizeName());
        metricPayload.put("mostFrequentPrizeCount", metrics.mostFrequentPrizeCount());
        metricPayload.put("averageHitProbability", normalizeProbability(metrics.averageHitProbability()));
        metricPayload.put("firstDrawAt", metrics.firstDrawAt());
        metricPayload.put("latestDrawAt", metrics.latestDrawAt());
        metricPayload.put("prizeLevelDistribution", metrics.prizeLevelDistribution());

        try {
            return String.format(
                    "请分析以下抽奖指标，并只返回中文 JSON 结果。%n"
                            + "如果 focus 不为空，请优先围绕该关注点分析。%n%n"
                            + "focus: %s%n"
                            + "metrics:%n%s",
                    focus == null ? "" : focus,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metricPayload)
            );
        } catch (IOException ex) {
            throw new BusinessException("Failed to build GLM analysis prompt");
        }
    }

    private BigDecimal normalizeProbability(BigDecimal probability) {
        if (probability == null) {
            return BigDecimal.ZERO;
        }
        return probability.setScale(4, RoundingMode.HALF_UP);
    }

    private JsonNode extractNarrativeNode(ChatCompletionResponse response) throws IOException {
        if (response == null || response.getData() == null || response.getData().getChoices() == null || response.getData().getChoices().isEmpty()) {
            throw new BusinessException("GLM response is empty");
        }

        Object message = response.getData().getChoices().get(0).getMessage();
        if (message == null) {
            throw new BusinessException("GLM response message is empty");
        }

        List<String> candidates = extractMessageCandidates(message);
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            try {
                return parseJsonContent(candidate);
            } catch (Exception ignored) {
            }
        }

        throw new BusinessException("GLM response is not valid JSON");
    }

    private List<String> extractMessageCandidates(Object message) {
        ArrayList<String> candidates = new ArrayList<>();
        addCandidate(candidates, invokeStringGetter(message, "getContent"));
        addCandidate(candidates, invokeStringGetter(message, "content"));
        addCandidate(candidates, invokeStringGetter(message, "getReasoningContent"));
        addCandidate(candidates, invokeStringGetter(message, "reasoningContent"));
        addCandidate(candidates, trySerialize(message));
        addCandidate(candidates, String.valueOf(message));
        return candidates;
    }

    private void addCandidate(List<String> candidates, String value) {
        if (value != null && !value.isBlank()) {
            candidates.add(value);
        }
    }

    private String invokeStringGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return stringifyValue(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String trySerialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String stringifyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof List<?> listValue) {
            StringBuilder builder = new StringBuilder();
            for (Object item : listValue) {
                String text = stringifyValue(item);
                if (text != null && !text.isBlank()) {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(text);
                }
            }
            return builder.toString();
        }
        return trySerialize(value);
    }

    private JsonNode parseJsonContent(String content) throws IOException {
        String trimmed = content == null ? "" : content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new BusinessException("GLM response is not valid JSON");
        }

        JsonNode node = objectMapper.readTree(trimmed.substring(start, end + 1));
        validateNarrativeNode(node);
        return node;
    }

    private void validateNarrativeNode(JsonNode node) {
        String overview = node.path("overview").asText("");
        List<String> insights = readOptionalStringList(node, "insights");
        List<String> suggestions = readOptionalStringList(node, "suggestions");

        boolean hasPlaceholderOverview = overview.isBlank() || "80-140字中文摘要".equals(overview.trim());
        boolean hasPlaceholderInsights = insights.stream().anyMatch(item -> item != null && item.matches("洞察\\s*\\d+"));
        boolean hasPlaceholderSuggestions = suggestions.stream().anyMatch(item -> item != null && item.matches("建议\\s*\\d+"));

        if (hasPlaceholderOverview || hasPlaceholderInsights || hasPlaceholderSuggestions) {
            throw new BusinessException("GLM response still contains schema placeholders");
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isTextual() && !fieldNode.asText().isBlank()) {
            return fieldNode.asText();
        }
        throw new BusinessException("GLM response missing field " + fieldName);
    }

    private List<String> readStringList(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (!fieldNode.isArray()) {
            throw new BusinessException("GLM response field is not an array: " + fieldName);
        }
        return java.util.stream.StreamSupport.stream(fieldNode.spliterator(), false)
                .map(JsonNode::asText)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private List<String> readOptionalStringList(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (!fieldNode.isArray()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(fieldNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
    }

    public record GlmNarrative(
            String overview,
            List<String> insights,
            List<String> suggestions
    ) {
    }

    public record GlmStreamChunk(
            String type,
            String text
    ) {
    }
}
