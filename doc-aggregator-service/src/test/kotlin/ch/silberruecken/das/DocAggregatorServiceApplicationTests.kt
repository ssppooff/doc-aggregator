package ch.silberruecken.das

import ch.silberruecken.das.sh.OAuth2TestConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class, OAuth2TestConfiguration::class)
@SpringBootTest
class DocAggregatorServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}

