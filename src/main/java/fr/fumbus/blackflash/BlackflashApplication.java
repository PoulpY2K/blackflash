package fr.fumbus.blackflash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

import java.util.TimeZone;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class BlackflashApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
        SpringApplication.run(BlackflashApplication.class, args);
    }

}
