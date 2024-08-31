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
        String videoId = "fc3484e3-47a9-43ed-ad9c-21199c5c12b0";
        Object result = videoService.processVideo(videoId);

        // Example assertion, adjust based on what processVideo is supposed to return or do.
        Assert.notNull(result, "The result should not be null");
    }
}
