package com.example.shortlink;

import com.example.shortlink.dto.LinkRequestDTO;
import com.example.shortlink.entity.Link;
import com.example.shortlink.exception.DataRequestException;
import com.example.shortlink.repository.LinkRepository;
import com.example.shortlink.service.LinkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.shortlink.service.LinkService.*;
import static com.example.shortlink.util.LinkUtil.LONG_LINK_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTests {

    @Mock
    LinkRepository linkRepository;

    @InjectMocks
    LinkService linkService;

    @Test
    void createOk(){
        String habr = "https://habr.com/ru/articles/";
        String endOfShortLink = "habr";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink(habr)
                .endOfShortLink(endOfShortLink)
                .ttl("")
                .build();

        Link link = Link.builder()
                .endOfShortLink(endOfShortLink)
                .longLink(habr)
                .ttl(null)
                .build();

        when(linkRepository.get(anyString())).thenReturn(null);
        when(linkRepository.create(any(Link.class))).thenReturn(link);

        assertThat(linkService.create(linkRequestDTO)).isEqualTo(link);
    }

    @Test
    void createExistShortLink(){
        String habr = "https://habr.com/ru/articles/";
        String endOfShortLink = "habr";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink(habr)
                .endOfShortLink(endOfShortLink)
                .ttl("")
                .build();

        Link link = Link.builder().build();

        when(linkRepository.get(anyString())).thenReturn(link);

        assertThatThrownBy(() -> linkService.create(linkRequestDTO), SHORT_LINK_EXIST).isInstanceOf(DataRequestException.class);
    }

    @Test
    void createEmptyLongLink(){
        String endOfShortLink = "habr";

        LinkRequestDTO linkRequestDTO = LinkRequestDTO.builder()
                .longLink("")
                .endOfShortLink(endOfShortLink)
                .ttl("")
                .build();

        Link link = Link.builder().build();

        when(linkRepository.get(anyString())).thenReturn(link);

        assertThatThrownBy(() -> linkService.create(linkRequestDTO), LONG_LINK_REQUIRED).isInstanceOf(DataRequestException.class);
    }

    @Test
    void getOk(){
        String habr = "https://habr.com/ru/articles/";
        String endOfShortLink = "habr";

        Link link = Link.builder()
                .endOfShortLink(habr)
                .longLink(endOfShortLink)
                .ttl(Timestamp.from(Instant.now().plus(100, ChronoUnit.DAYS)))
                .build();

        when(linkRepository.get(endOfShortLink)).thenReturn(link);

        assertThat(linkService.get(endOfShortLink)).isEqualTo(link);
    }

    @Test
    void getShortLinkNotFound(){
        String endOfShortLink = "habr";

        when(linkRepository.get(endOfShortLink)).thenReturn(null);

        assertThatThrownBy(() -> linkService.get(endOfShortLink), SHORT_LINK_NOT_FOUND).isInstanceOf(DataRequestException.class);
    }

    @Test
    void getShortLinkExpired(){
        String habr = "https://habr.com/ru/articles/";
        String endOfShortLink = "habr";

        Link link = Link.builder()
                .endOfShortLink(habr)
                .longLink(endOfShortLink)
                .ttl(Timestamp.from(Instant.now().minus(100, ChronoUnit.DAYS)))
                .build();

        when(linkRepository.get(endOfShortLink)).thenReturn(link);

        assertThatThrownBy(() -> linkService.get(endOfShortLink), SHORT_LINK_EXPIRED).isInstanceOf(DataRequestException.class);
    }
}
