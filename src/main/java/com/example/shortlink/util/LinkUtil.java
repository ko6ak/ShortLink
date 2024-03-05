package com.example.shortlink.util;

import com.example.shortlink.dto.LinkRequestDTO;
import com.example.shortlink.entity.Link;
import com.example.shortlink.exception.DataRequestException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class LinkUtil {
    public static final int END_OF_SHORT_LINK_SIZE = 10;
    public static final String LONG_LINK_REQUIRED = "Нужен длинный линк.";
    public static final String TTL_IS_NOT_A_NUMBER = "TTL должен быть числом.";
    public static final String TTL_MUST_BE_POSITIVE = "TTL должен быть больше нуля.";

    public static Link toLink(LinkRequestDTO linkRequestDTO) {
        String longLink = linkRequestDTO.getLongLink().trim();
        String endOfShortLink = linkRequestDTO.getEndOfShortLink().trim();
        String ttl = linkRequestDTO.getTtl().trim();

        Link link = new Link();

        if (longLink.isEmpty()) throw new DataRequestException(LONG_LINK_REQUIRED);
        link.setLongLink(longLink);

        link.setEndOfShortLink(endOfShortLink.isEmpty() ? generateEndOfShortLink() : endOfShortLink);

        if (!ttl.isEmpty()){
            try{
                int expire = Integer.parseInt(ttl);
                if (expire > 0) link.setTtl(Timestamp.from(Instant.now().plus(expire, ChronoUnit.DAYS)));
                else throw new DataRequestException(TTL_MUST_BE_POSITIVE);
            }
            catch (NumberFormatException e) {
                throw new DataRequestException(TTL_IS_NOT_A_NUMBER);
            }
        }
        return link;
    }

    private static String generateEndOfShortLink(){
        return new Random().ints(65, 123)
                .filter(num -> (num > 64 && num < 91 ) || (num > 96 && num < 123))
                .limit(END_OF_SHORT_LINK_SIZE)
                .collect(StringBuffer::new, StringBuffer::appendCodePoint, StringBuffer::append)
                .toString();
    }
}
