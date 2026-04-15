package Task02_b;

import org.springframework.context.ApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new org.springframework.context.annotation.AnnotationConfigApplicationContext(AppConfig.class);
        Car myCar = context.getBean(Car.class);
        myCar.drive();
    }
}
