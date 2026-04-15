package Task02_b;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {


        @Bean(name = "classicEngine")
        public Engine getClassicEngine() {
            return new ClassicEngine();
        }

        @Bean(name = "raceEngine")
        public Engine getRaceEngine() {
            return new RaceEngine();
        }


        @Bean
        public Car sportsCar() {
            return new Car();
        }

    }

