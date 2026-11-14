package za.co.sheeptracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SheepTrackerApplication {
    private static final Logger log =
            LoggerFactory.getLogger(SheepTrackerApplication.class);
    public static void main(String[] args) {
        log.info("Starting application");
        SpringApplication.run(SheepTrackerApplication.class, args);
    }
}
