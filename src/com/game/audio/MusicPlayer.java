package com.game.audio;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Plays a classpath WAV as a continuous background track. */
public final class MusicPlayer implements AutoCloseable {
    private Clip clip;
    private final Set<Clip> effects = ConcurrentHashMap.newKeySet();

    public void playLoop(String resourcePath) {
        close();
        URL resource = MusicPlayer.class.getResource(resourcePath);
        if (resource == null) {
            System.err.println("No se encontró la música: " + resourcePath);
            return;
        }
        try (AudioInputStream audio = AudioSystem.getAudioInputStream(resource)) {
            clip = AudioSystem.getClip();
            clip.open(audio);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("No se pudo reproducir la música: " + e.getMessage());
            close();
        }
    }

    /** Starts a one-shot WAV effect without interrupting the background track. */
    public void playEffect(String resourcePath) {
        Thread playback = new Thread(() -> {
            URL resource = MusicPlayer.class.getResource(resourcePath);
            if (resource == null) {
                System.err.println("No se encontró el efecto: " + resourcePath);
                return;
            }
            try (AudioInputStream audio = AudioSystem.getAudioInputStream(resource)) {
                Clip effect = AudioSystem.getClip();
                effect.open(audio);
                effects.add(effect);
                effect.addLineListener(event -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        effects.remove(effect);
                        effect.close();
                    }
                });
                effect.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("No se pudo reproducir el efecto " + resourcePath + ": " + e.getMessage());
            }
        }, "Mario-Sound-Effect");
        playback.setDaemon(true);
        playback.start();
    }

    @Override public void close() {
        for (Clip effect : effects) {
            effect.stop();
            effect.close();
        }
        effects.clear();
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
