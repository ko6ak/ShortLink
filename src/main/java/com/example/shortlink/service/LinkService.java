package com.example.shortlink.service;

import com.example.shortlink.dto.LinkRequestDTO;
import com.example.shortlink.entity.Link;
import com.example.shortlink.exception.DataRequestException;
import com.example.shortlink.repository.LinkRepository;
import com.example.shortlink.util.LinkUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@AllArgsConstructor
public class LinkService {
    public static final String SHORT_LINK_EXIST = "Короткая ссылка с таким именем уже есть.";
    public static final String SHORT_LINK_NOT_FOUND = "Короткой ссылки, сохраненной с таким именнем, нет.";
    public static final String SHORT_LINK_EXPIRED = "Срок действия этой короткой ссылки истек.";

    private final LinkRepository linkRepository;

    public Link create(LinkRequestDTO linkRequestDTO){
        if (linkRepository.get(linkRequestDTO.getEndOfShortLink()) != null) throw new DataRequestException(SHORT_LINK_EXIST);
        Link link = LinkUtil.toLink(linkRequestDTO);
        return linkRepository.create(link);
    }

    public Link get(String endOfShortLink){
        Link link = linkRepository.get(endOfShortLink);
        if (link == null) throw new DataRequestException(SHORT_LINK_NOT_FOUND);

        Timestamp timestamp = link.getTtl();
        if (timestamp != null && timestamp.before(Timestamp.from(Instant.now()))) {
            linkRepository.delete(link);
            throw new DataRequestException(SHORT_LINK_EXPIRED);
        }
        return link;
    }
}
