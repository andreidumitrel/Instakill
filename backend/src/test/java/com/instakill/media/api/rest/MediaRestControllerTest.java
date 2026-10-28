package com.instakill.media.api.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie obtainAuthCookie(String username) throws Exception {
        String payload = "{\"username\":\"" + username + "\",\"email\":\"" + username + "@example.com\",\"password\":\"password123\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("ACCESS_TOKEN");
    }

    @Test
    void rejectsUnauthenticatedUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/media").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadsMediaSuccessfullyAndServesStatically() throws Exception {
        Cookie authCookie = obtainAuthCookie("mediauser1");
        byte[] content = "fake mp4 video bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", content
        );

        MvcResult result = mockMvc.perform(multipart("/api/media")
                        .file(file)
                        .cookie(authCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaUrl").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String mediaUrl = json.get("mediaUrl").asText();
        assertThat(mediaUrl).startsWith("/media/");
        assertThat(mediaUrl).endsWith(".mp4");

        // Verify the uploaded media can be fetched via GET /media/** statically
        mockMvc.perform(get(mediaUrl))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        Cookie authCookie = obtainAuthCookie("mediauser2");
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        mockMvc.perform(multipart("/api/media")
                        .file(emptyFile)
                        .cookie(authCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidExtension() throws Exception {
        Cookie authCookie = obtainAuthCookie("mediauser3");
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "echo hello".getBytes()
        );

        mockMvc.perform(multipart("/api/media")
                        .file(maliciousFile)
                        .cookie(authCookie))
                .andExpect(status().isBadRequest());
    }
}

