package com.maltsev.conference_app.controller;

import com.maltsev.conference_app.model.ReviewStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/review-statuses")
public class ReviewStatusController {

    @GetMapping
    public List<String> list() {
        return Arrays.stream(ReviewStatus.values()).map(Enum::name).toList();
    }
}
