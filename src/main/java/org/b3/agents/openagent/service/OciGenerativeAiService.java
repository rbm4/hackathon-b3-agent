/**
 * Copyright (c) 2023, Oracle and/or its affiliates.  All rights reserved.
 * This software is dual-licensed to you under the Universal Permissive License (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose either license.
 */
package org.b3.agents.openagent.service;

import org.springframework.stereotype.Service;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.AssistantMessage;
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponse;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.bmc.retrier.RetryConfiguration;
import lombok.extern.slf4j.Slf4j;


/**
 * This class provides an example of how to use OCI Generative AI Service to generate text.
 * <p>
 * The Generative AI Service queried by this example will be assigned:
 * <ul>
 * <li>an endpoint url defined by constant ENDPOINT</li>
 * <li>
 * The configuration file used by service clients will be sourced from the default
 * location (~/.oci/config) and the CONFIG_PROFILE profile will be used.
 * </li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
public class OciGenerativeAiService {
    private static final String ENDPOINT = "https://inference.generativeai.sa-saopaulo-1.oci.oraclecloud.com";
    private static final String REGION = "sa-saopaulo-1";
    private static final String CONFIG_LOCATION = "C:/Users/ricar/.oci/config";
    private static final String CONFIG_PROFILE = "DEFAULT";
    private static final String COMPARTMENT_ID = "ocid1.tenancy.oc1..aaaaaaaandjauu6wx5jlsqtydwslzihesfersxtpklp4snz2nubt63j3abra";
    private static final String MODEL_ID = "ocid1.generativeaimodel.oc1.sa-saopaulo-1.amaaaaaask7dceyarsn4m6k3aqvvgatida3omyprlcs3alrwcuusblru4jaa";



    /**
     * Generates README content for provided code using the default prompt
     * @param codeToAnalyze The code content to analyze
     * @return Generated README content as a string
     */
    public String generateReadme(String prompt) {
        String inputText = prompt;
        try {
            final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(CONFIG_LOCATION, CONFIG_PROFILE);
            final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
            ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .readTimeoutMillis(240000)
                .retryConfiguration(RetryConfiguration.NO_RETRY_CONFIGURATION)
                .build();
            try (final GenerativeAiInferenceClient generativeAiInferenceClient = GenerativeAiInferenceClient.builder().configuration(clientConfiguration).build(provider)) {
                generativeAiInferenceClient.setEndpoint(ENDPOINT);
                generativeAiInferenceClient.setRegion(REGION);
  
                ChatContent content = TextContent.builder()
                    .text(inputText)
                    .build();
                Message message = UserMessage.builder()
                    .content(java.util.Collections.singletonList(content))
                    .build();
                GenericChatRequest chatRequest = GenericChatRequest.builder()
                    .messages(java.util.Collections.singletonList(message))
                    .maxTokens(600)
                    .temperature((double)1)
                    .frequencyPenalty((double)0)
                    .presencePenalty((double)0)
                    .topP((double)0.75)
                    .isStream(false)
                    .build();

                ChatDetails details = ChatDetails.builder()
                    .servingMode(OnDemandServingMode.builder().modelId(MODEL_ID).build())
                    .compartmentId(COMPARTMENT_ID)
                    .chatRequest(chatRequest)
                    .build();
            
                ChatRequest request = ChatRequest.builder()
                    .chatDetails(details)
                    .build();

                ChatResponse response = generativeAiInferenceClient.chat(request);
                
                // Extract content from response
                if (response.getChatResult() != null) {
                    return extractTextFromChatResponse(response);
                } 
                return null;
            }
        } catch (Exception e) {
            log.error("AI inference failed", e);
            
            // Check for specific Oracle API errors
            if (e.getMessage().contains("Error returned by Chat operation")) {
                return "Oracle AI service error. Please check:\n" +
                       "1. API credentials and permissions\n" +
                       "2. Model availability in your region\n" +
                       "3. Service quotas and limits\n" +
                       "Error: " + e.getMessage();
            }
            
            return "AI inference failed: " + e.getMessage();
        }
    }

    public String extractTextFromChatResponse(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getChatResult() == null || chatResponse.getChatResult().getChatResponse() == null) {
            return null;
        }

        BaseChatResponse baseChatResponse = chatResponse.getChatResult().getChatResponse();

        if (baseChatResponse instanceof CohereChatResponse) {
            return ((CohereChatResponse) baseChatResponse).getText();
        } else if (baseChatResponse instanceof GenericChatResponse) {
            GenericChatResponse genericChatResponse = (GenericChatResponse) baseChatResponse;
            if (genericChatResponse.getChoices() != null && !genericChatResponse.getChoices().isEmpty()) {
                // Get the last choice, assuming it contains the final response text
                ChatChoice lastChoice = genericChatResponse.getChoices().get(genericChatResponse.getChoices().size() - 1);
                if (lastChoice.getMessage() != null && lastChoice.getMessage().getContent() != null && !lastChoice.getMessage().getContent().isEmpty()) {
                    // Get the last content item, assuming it's the primary text content
                    ChatContent lastContent = lastChoice.getMessage().getContent().get(lastChoice.getMessage().getContent().size() - 1);
                    if (lastContent instanceof TextContent) {
                        return ((TextContent) lastContent).getText();
                    }
                }
            }
        }
        return null; // Or throw an exception for unsupported types/missing content
    }
}
