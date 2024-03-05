package com.example.shortlink;

import com.example.shortlink.dto.LinkRequestDTO;
import com.example.shortlink.entity.Link;
import com.example.shortlink.repository.LinkRepository;
import com.example.shortlink.service.LinkService;
import com.example.shortlink.util.LinkUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LinkIntegrationTests {
    static final String START_URL = "http://localhost";

    @Autowired
    MockMvc mvc;

    @Autowired
    LinkRepository linkRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.0");
    static {
        postgreSQLContainer.start();
    }

    @Autowired
    Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void createOk() throws Exception {
        String habr = "https://habr.com/ru/articles/";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink(habr)
                .endOfShortLink("")
                .ttl("")
                .build();

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isCreated())
                .andExpect(view().name("result"))
                .andReturn().getModelAndView().getModel().get("shortlink");

        assertThat(result).startsWith(START_URL);

        Link link = linkRepository.get(result.split("/")[3]);
        assertThat(link.getLongLink()).isEqualTo(habr);
        assertThat(link.getTtl()).isNull();
    }

    @Test
    public void createOkWithShortLink() throws Exception {
        String habr = "https://habr.com/ru/articles/";
        String endOfShortLink = "habr";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink(habr)
                .endOfShortLink(endOfShortLink)
                .ttl("")
                .build();

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isCreated())
                .andExpect(view().name("result"))
                .andReturn().getModelAndView().getModel().get("shortlink");

        assertThat(result).startsWith(START_URL);

        Link link = linkRepository.get(endOfShortLink);
        assertThat(link.getLongLink()).isEqualTo(habr);
        assertThat(link.getTtl()).isNull();
    }

    @Test
    public void createOkWithShortLinkAndTTL() throws Exception {
        String habr = "https://habr.com/ru/articles/";
        String endOfShortLink = "habr";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink(habr)
                .endOfShortLink(endOfShortLink)
                .ttl("3")
                .build();

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isCreated())
                .andExpect(view().name("result"))
                .andReturn().getModelAndView().getModel().get("shortlink");

        assertThat(result).startsWith(START_URL);

        Link link = linkRepository.get(endOfShortLink);
        assertThat(link.getLongLink()).isEqualTo(habr);
        assertThat(link.getTtl()).isBetween(Instant.now().plus(3, ChronoUnit.DAYS).minus(1, ChronoUnit.MINUTES),
                                            Instant.now().plus(3, ChronoUnit.DAYS));
    }

    @Test
    public void createOkWithShortLinkAndWrongTTL() throws Exception {
        String habr = "https://habr.com/ru/articles/";
        String endOfShortLink = "habr";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink(habr)
                .endOfShortLink(endOfShortLink)
                .ttl("aaa")
                .build();

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andReturn().getModelAndView().getModel().get("message");

        assertThat(result).isEqualTo(LinkUtil.TTL_IS_NOT_A_NUMBER);
    }

    @Test
    public void createOkWithoutLongLink() throws Exception {
        String endOfShortLink = "habr";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink("")
                .endOfShortLink(endOfShortLink)
                .ttl("3")
                .build();

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andReturn().getModelAndView().getModel().get("message");

        assertThat(result).isEqualTo(LinkUtil.LONG_LINK_REQUIRED);
    }

    @Test
    public void createExistShortLink() throws Exception {
        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink("https://habr.com/ru/articles/")
                .endOfShortLink("google")
                .ttl("")
                .build();

        String result = (String) mvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("link", linkRequestDTO))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andReturn().getModelAndView().getModel().get("message");

        assertThat(result).isEqualTo(LinkService.SHORT_LINK_EXIST);
    }

    @Test
    public void getOk() throws Exception {
        mvc.perform(get("/google")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:https://www.google.ru/"));
    }

    @Test
    public void getShortLinkNotFound() throws Exception {
        String result = (String) mvc.perform(get("/abc")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andReturn().getModelAndView().getModel().get("message");

        assertThat(result).isEqualTo(LinkService.SHORT_LINK_NOT_FOUND);
    }

    @Test
    public void getShortLinkExpired() throws Exception {
        Link link = Link.builder()
                .endOfShortLink("habr")
                .longLink("https://habr.com/ru/articles/")
                .ttl(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .build();

        linkRepository.create(link);

        String result = (String) mvc.perform(get("/habr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andReturn().getModelAndView().getModel().get("message");

        assertThat(result).isEqualTo(LinkService.SHORT_LINK_EXPIRED);
    }
}
