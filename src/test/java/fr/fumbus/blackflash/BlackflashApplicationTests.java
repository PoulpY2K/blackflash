package fr.fumbus.blackflash;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class BlackflashApplicationTests {

    @Mock
    ConfigurableApplicationContext mockContext;

    private TimeZone originalTimeZone;

    @BeforeEach
    void setUp() {
        originalTimeZone = TimeZone.getDefault();
    }

    @AfterEach
    void tearDown() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void main_setsDefaultTimezoneToEuropeParis() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        try (MockedStatic<SpringApplication> mockedSpring = mockStatic(SpringApplication.class)) {
            mockedSpring
                    .when(() -> SpringApplication.run(eq(BlackflashApplication.class), any(String[].class)))
                    .thenReturn(mockContext);

            BlackflashApplication.main(new String[]{});
        }

        assertThat(TimeZone.getDefault().getID()).isEqualTo("Europe/Paris");
    }
}
