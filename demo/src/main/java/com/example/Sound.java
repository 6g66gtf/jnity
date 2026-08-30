package com.example;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class Sound {
    //音效清单
    private Map<String,Clip> Sound_Map = new HashMap<>();
    //音乐清单
    private Map<String,String> BGM_Map = new HashMap<>();

    public String setting = "setting.txt";
    public String path = Reader.readJson(setting).getString("content");

    public static Sound s;

    static {
        try {
            s = new Sound();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static Sound getSoundSystem() {return s;}

    //初始化映射表
    private Sound() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        JSONArray sounds = Reader.readJsoa(path);
        for (int i = 0;i<sounds.length();i++) {
            //获取音频键值与文件名映射
            JSONObject sound = sounds.getJSONObject(i);

            String key = sound.getString("key");

            //根据映射读取Clip对象并存储到MAP中
            switch (sound.getString("type")) {
                case "sound":
                    AudioInputStream ais = AudioSystem.getAudioInputStream(new File(sound.getString("file")));
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    Sound_Map.put(key, clip);
                    break;
                case "BGM":
                    // 只存储路径，播放时使用 SourceDataLine 流式读取
                    BGM_Map.put(key, sound.getString("file"));
                    break;
            
                default:
                    break;
            }
        }
    }

    //重新加载音效
    public void loadSound() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        JSONArray sounds = Reader.readJsoa(path);
        for (int i = 0;i<sounds.length();i++) {
            //获取音频键值与文件名映射
            JSONObject sound = sounds.getJSONObject(i);
            //根据映射读取Clip对象并存储到MAP中
            switch (sound.getString("type")) {
                case "sound":
                    AudioInputStream ais = AudioSystem.getAudioInputStream(new File(sound.getString("file")));
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    Sound_Map.put( sound.getString("key"), clip);
                    break;
            
                default:
                    break;
            }
        }
    }

    //获取Clip对象
    public Clip getClip(String key) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        return Sound_Map.get(key);
    }
    //播放音效
    public void PlaySound(String key) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Clip clip = getClip(key);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }
    //循环播放音效
    public void PlaySoundLoop(String key) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Clip clip = getClip(key);
        stopBGM();
        if (clip != null) {
            if (!clip.isRunning()) clip.stop();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    //------------------------流式音频播放----------------------------

    private Thread bgmThread;
    private SourceDataLine bgmLine;
    private volatile boolean isBgmPlaying = false;
    private String currentBgmKey = null;

    /**
     * 循环播放背景音乐（使用 SourceDataLine，内存友好）
     * @param key 在 JSON 中定义的 BGM 键名
     */
    public void playBGM(String key) {
        String filePath = BGM_Map.get(key);
        if (filePath == null) {
            System.err.println("未找到 BGM 键: " + key);
            return;
        }

        // 如果正在播放同一首曲子，不重复启动
        if (isBgmPlaying && key.equals(currentBgmKey)) {
            return;
        }
        stopBGM(); // 停止当前播放

        currentBgmKey = key;
        isBgmPlaying = true;

        bgmThread = new Thread(() -> {
            while (isBgmPlaying) {
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filePath))) {

                    AudioFormat baseFormat = ais.getFormat();
                    // 将音频格式统一转换为 PCM_SIGNED（兼容性更好）
                    AudioFormat pcmFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,  // 16位采样
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false
                    );

                    AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, ais);

                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcmFormat);
                    if (!AudioSystem.isLineSupported(info)) {
                        System.err.println("当前系统不支持该音频格式: " + filePath);
                        break;
                    }

                    try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                        bgmLine = line;
                        line.open(pcmFormat);
                        line.start();

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while (isBgmPlaying && (bytesRead = pcmStream.read(buffer, 0, buffer.length)) != -1) {
                            line.write(buffer, 0, bytesRead);
                        }
                        line.drain();
                    }

                } catch (UnsupportedAudioFileException e) {
                    System.err.println("不支持的音频格式: " + filePath);
                    break;
                } catch (IOException | LineUnavailableException e) {
                    e.printStackTrace();
                    break;
                }
                // 若循环标志仍为 true，则自动重新打开文件流（循环播放）
            }
        });

        bgmThread.setDaemon(true);
        bgmThread.start();
    }

    /**
     * 停止当前背景音乐
     */
    public void stopBGM() {
        isBgmPlaying = false;
        currentBgmKey = null;

        if (bgmLine != null) {
            bgmLine.stop();
            bgmLine.close();
            bgmLine = null;
        }

        if (bgmThread != null) {
            bgmThread.interrupt();
            bgmThread = null;
        }
    }

    /**
     * 停止所有音频（包括短音效和背景音乐）
     */
    public void stopAll() {
        stopBGM();
        for (Clip clip : Sound_Map.values()) {
            if (clip.isRunning()) clip.stop();
            clip.close();
        }
        Sound_Map.clear();
        try {
            loadSound();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}
