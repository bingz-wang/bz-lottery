package com.lottery.ai.infrastructure.ai;

import com.lottery.ai.config.GlmProperties;
import com.lottery.common.exception.BusinessException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PromptProvider {

    private static final String DEFAULT_PROMPT_LOCATION = "classpath:prompt/SYSTEM_PROMPT.md";

    private final GlmProperties glmProperties;
    private final ResourceLoader resourceLoader;

    public PromptProvider(GlmProperties glmProperties, ResourceLoader resourceLoader) {
        this.glmProperties = glmProperties;
        this.resourceLoader = resourceLoader;
    }

    public String loadSystemPrompt() {
        String location = resolvePromptLocation();
        String prompt = readPrompt(location).trim();
        if (prompt.isBlank()) {
            throw new BusinessException("AI system prompt file is empty");
        }
        return prompt;
    }

    private String resolvePromptLocation() {
        String configuredLocation = glmProperties.getSystemPromptLocation();
        return configuredLocation == null || configuredLocation.isBlank()
                ? DEFAULT_PROMPT_LOCATION
                : configuredLocation.trim();
    }

    private String readPrompt(String location) {
        if (location.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX) || location.startsWith("file:")) {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new BusinessException("AI system prompt file is not configured");
            }
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new BusinessException("Failed to read AI system prompt file");
            }
        }

        Path path = Path.of(location);
        if (!Files.isRegularFile(path)) {
            throw new BusinessException("AI system prompt file is not configured");
        }

        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BusinessException("Failed to read AI system prompt file");
        }
    }
}

