package com.example.shortlink;

import com.example.shortlink.dto.LinkRequestDTO;
import com.example.shortlink.entity.Link;
import com.example.shortlink.exception.DataRequestException;
import com.example.shortlink.service.LinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.example.shortlink.LinkIntegrationTests.START_URL;
import static com.example.shortlink.service.LinkService.SHORT_LINK_EXIST;
import static com.example.shortlink.service.LinkService.SHORT_LINK_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class LinkControllerTests {

    @Autowired
    MockMvc mvc;

    @MockBean
    LinkService linkService;

    @Test
    public void index() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void createOk() throws Exception {
        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder().build();

        Link link = Link.builder()
                .endOfShortLink("habr")
                .longLink("https://habr.com/ru/articles/")
                .ttl(Timestamp.from(Instant.now()))
                .build();

        when(linkService.create(any(LinkRequestDTO.class))).thenReturn(link);

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isCreated())
                .andExpect(view().name("result"))
                .andReturn().getModelAndView().getModel().get("shortlink");

        assertThat(result).startsWith(START_URL);
        assertThat(result.split("/")[3]).isEqualTo("habr");
    }

    @Test
    public void createExistShortLink() throws Exception {
        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder().build();

        when(linkService.create(any(LinkRequestDTO.class))).thenThrow(new DataRequestException(SHORT_LINK_EXIST));

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andReturn().getModelAndView().getModel().get("message");

        assertThat(result).isEqualTo(SHORT_LINK_EXIST);
    }

    @Test
    public void getOk() throws Exception {
        Link link = Link.builder()
                .endOfShortLink("habr")
                .longLink("https://habr.com/ru/articles/")
                .ttl(Timestamp.from(Instant.now()))
                .build();

        when(linkService.get("habr")).thenReturn(link);

        mvc.perform(get("/habr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:https://habr.com/ru/articles/"));
    }

    @Test
    public void getShortLinkNotFound() throws Exception {
        when(linkService.get("habr")).thenThrow(new DataRequestException(SHORT_LINK_NOT_FOUND));

        String result = (String) mvc.perform(get("/habr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andReturn().getModelAndView().getModel().get("message");

        assertThat(result).isEqualTo(SHORT_LINK_NOT_FOUND);
    }
}
