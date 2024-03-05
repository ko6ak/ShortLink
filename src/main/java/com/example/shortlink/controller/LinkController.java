package com.example.shortlink.controller;

import com.example.shortlink.dto.LinkRequestDTO;
import com.example.shortlink.entity.Link;
import com.example.shortlink.exception.DataRequestException;
import com.example.shortlink.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@AllArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("link", new LinkRequestDTO());
        return "index";
    }

    @PostMapping("/create")
    public ModelAndView create(@ModelAttribute("link") LinkRequestDTO link, HttpServletRequest request){
        Link dbLink;
        ModelAndView model;

        try {
            dbLink = linkService.create(link);
        }
        catch (DataRequestException e) {
            model = new ModelAndView("error");
            model.addObject("message", e.getMessage());
            model.setStatus(HttpStatus.NOT_FOUND);
            return model;
        }

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        model = new ModelAndView("result");
        model.addObject("shortlink", baseUrl + "/" + dbLink.getEndOfShortLink());
        model.setStatus(HttpStatus.CREATED);

        return model;
    }

    @GetMapping("/{endOfShortLink}")
    public ModelAndView get(@PathVariable String endOfShortLink){
        Link dbLink;
        ModelAndView model;

        try {
            dbLink = linkService.get(endOfShortLink);
        }
        catch (DataRequestException e) {
            model = new ModelAndView("error");
            model.addObject("message", e.getMessage());
            model.setStatus(HttpStatus.NOT_FOUND);
            return model;
        }

        model = new ModelAndView("redirect:" + dbLink.getLongLink());
        model.setStatus(HttpStatus.FOUND);

        return model;
    }
}
