package com.duplicall.screenAnalyse.api;

import com.duplicall.screenAnalyse.Application;
import com.duplicall.screenAnalyse.task.ScreenAnalyseTask;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity analyse() {
        ScreenAnalyseTask task = new ScreenAnalyseTask();
        Application.executorService.submit(task);
        return new ResponseEntity("success", HttpStatus.OK);
    }


}
