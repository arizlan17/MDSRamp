package Task02_b;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Car {

    @Autowired
    @Qualifier("classicEngine")
    private Engine engine;


    public void drive() {
        engine.start();

        System.out.println("The car is driving down the road.");
    }
}