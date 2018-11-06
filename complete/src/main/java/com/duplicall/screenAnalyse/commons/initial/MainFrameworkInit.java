package com.duplicall.screenAnalyse.commons.initial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class MainFrameworkInit implements ApplicationListener<ContextRefreshedEvent> {
    protected static final Logger logger = LoggerFactory.getLogger(MainFrameworkInit.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            logger.info("________初始化___________");
        }
    }
}
