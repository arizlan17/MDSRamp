package Task02_a;

public class Car {
    private Engine engine;
    private MusicPlayer musicPlayer;

    // CONSTRUCTOR INJECTION (Mandatory)
    public Car(Engine engine) {
        System.out.println("Spring injected ClassicEngine via Constructor!");
        this.engine = engine;
    }

    // SETTER INJECTION (Optional)
    public void setMusicSystem(MusicPlayer musicPlayer) {
        System.out.println("Spring injected MusicSystem via Setter!");
        this.musicPlayer = musicPlayer;
    }

    public void drive() {
        engine.start();
        if (musicPlayer != null) {
            musicPlayer.play();
        }
        System.out.println("The car is driving down the road.");
    }
}