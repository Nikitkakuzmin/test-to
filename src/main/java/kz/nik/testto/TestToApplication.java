package kz.nik.testto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "kz.nik.testto")


public class TestToApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestToApplication.class, args);
    }

}
