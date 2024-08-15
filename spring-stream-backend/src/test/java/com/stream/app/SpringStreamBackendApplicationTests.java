package com.stream.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import com.stream.app.services.VideoService;

@SpringBootTest
class SpringStreamBackendApplicationTests {

    @Autowired
    private VideoService videoService;

    @Test
    void testProcessVideo() {
        String videoId = "c5358bf9-6df4-431f-8799-e7037b390f7f";
        Object result = videoService.processVideo(videoId);

        // Example assertion, adjust based on what processVideo is supposed to return or do.
        Assert.notNull(result, "The result should not be null");
    }
}
