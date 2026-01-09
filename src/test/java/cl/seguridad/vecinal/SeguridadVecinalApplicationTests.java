package cl.seguridad.vecinal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:3000")
class SeguridadVecinalApplicationTests {

    @Test
    @Disabled
    void contextLoads() {
    }

}
