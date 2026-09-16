package org.example.designPattern.Structural.Facade;


 class TV {
    public void on() {
        System.out.println("TV is ON");
    }

    public void setInput() {
        System.out.println("TV input set to HDMI");
    }
}

// Subsystem 2
 class SoundSystem {
    public void on() {
        System.out.println("Sound System is ON");
    }

    public void setVolume(int volume) {
        System.out.println("Volume set to " + volume);
    }
}

// Subsystem 3
 class StreamingDevice {
    public void on() {
        System.out.println("Streaming Device is ON");
    }

    public void playMovie(String movie) {
        System.out.println("Playing movie: " + movie);
    }
}

// Facade
 class HomeTheaterFacade {

    private TV tv;
    private SoundSystem soundSystem;
    private StreamingDevice streamingDevice;

    public HomeTheaterFacade() {
        tv = new TV();
        soundSystem = new SoundSystem();
        streamingDevice = new StreamingDevice();
    }

    public void watchMovie(String movie) {
        System.out.println("Preparing Home Theater...\n");

        tv.on();
        tv.setInput();

        soundSystem.on();
        soundSystem.setVolume(20);

        streamingDevice.on();
        streamingDevice.playMovie(movie);

        System.out.println("\nEnjoy your movie!");
    }
}

public class FacadeDesignPattern {
    public static void main(String[] args) {
        HomeTheaterFacade homeTheater = new HomeTheaterFacade();

        homeTheater.watchMovie("Interstellar");
    }
}
