package com.duplicall.screenAnalyse.api;

import com.duplicall.screenAnalyse.Application;
import com.duplicall.screenAnalyse.commons.pojo.ScreenVideo;
import com.duplicall.screenAnalyse.task.ScreenAnalyseTask;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/rest/screen")
public class ScreenController {

//    private static final String template = "Hello, %s!";
//    private final AtomicLong counter = new AtomicLong();

//    @RequestMapping("/greeting")
//    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
//        return new Greeting(counter.incrementAndGet(),
//                            String.format(template, name));
//    }

    @RequestMapping(value = "/analyse", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity analyse(@RequestBody ScreenVideo screenVideo) {
//        screenVideo.setVideo_file_ftp("/11f137137ea06bc3434fc53bdd3f2111.mp4");
//        screenVideo.setVideo_file_xml("/ASCHN000000000005000020180423042502026.trace");
        ScreenAnalyseTask task = new ScreenAnalyseTask(screenVideo);
        Application.executorService.submit(task);
        return new ResponseEntity("success", HttpStatus.OK);
    }


}
