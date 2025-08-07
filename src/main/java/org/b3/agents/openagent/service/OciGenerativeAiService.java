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
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
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
    private static final String CONFIG_LOCATION = "C:/Users/rafa/.oci/config";
    private static final String CONFIG_PROFILE = "DEFAULT";
    private static final String COMPARTMENT_ID = "ocid1.tenancy.oc1..aaaaaaaandjauu6wx5jlsqtydwslzihesfersxtpklp4snz2nubt63j3abra";
    private static final String MODEL_ID = "ocid1.generativeaimodel.oc1.sa-saopaulo-1.amaaaaaask7dceyarsn4m6k3aqvvgatida3omyprlcs3alrwcuusblru4jaa";

    private static final String PROMPT = "Analyze the following code and generate a comprehensive README.md file for a GitHub repository. The README should include: project name and description, installation instructions, configuration details, usage examples, key dependencies, architectural patterns used, and any other relevant information that would help a new developer understand and use this project. Format the output as proper Markdown with appropriate headings, code blocks, and formatting. Focus on making the documentation clear, complete, and user-friendly, and without contributing and license information. The code to analyze is:";

    private static final String CODE_TO_EXPLAIN = "class HelloWorld{\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}";

    /**
     * Generates README content for provided code using the default prompt
     * @param codeToAnalyze The code content to analyze
     * @return Generated README content as a string
     */
    public String generateReadme(String codeToAnalyze) {
        String inputText = PROMPT + "\n" + codeToAnalyze;
        try {
            final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(CONFIG_LOCATION, CONFIG_PROFILE);
            final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
            ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .readTimeoutMillis(240000)
                .retryConfiguration(RetryConfiguration.NO_RETRY_CONFIGURATION)
                .build();
            try (final GenerativeAiInferenceClient generativeAiInferenceClient = new GenerativeAiInferenceClient(provider, clientConfiguration)) {
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
                if (response.getChatResult() != null && 
                    response.getChatResult().getModelVersion() != null) {
                    
                    // Try to extract the text content - Oracle API structure varies
                    String responseText = response.getChatResult().toString();
                    log.info("Raw Oracle AI response: {}", responseText);
                    
                    // Return the structured response for now until we know exact format
                    return responseText;
                } else {
                    log.warn("Empty response from OCI Generative AI: {}", response);
                    return "No content generated";
                }
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
}
