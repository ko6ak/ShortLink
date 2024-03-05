package com.example.shortlink.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkRequestDTO {
    private String endOfShortLink;
    private String longLink;
    private String ttl;
}
