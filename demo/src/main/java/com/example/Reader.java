package com.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class Reader {
    /* 数据处理 */
    // 读取json对象
    public static JSONObject readJson(String path) {
        String content;
        if (!Files.exists(Paths.get(path))) {
            System.err.println("文件不存在");
            return null;
        }
        content = read(path);
        if (content == null) {
            System.err.println("文件内容为空");
            return null;
        }
        JSONObject json = new JSONObject(content);
        return json;
    }

    // 读取jsonarry对象
    public static JSONArray readJsoa(String path) {
        String content;
        if (!Files.exists(Paths.get(path))) {
            System.err.println("文件不存在");
            return null;
        }
        content = read(path);
        if (content == null) {
            System.err.println("文件内容为空");
            return null;
        }
        JSONArray json = new JSONArray(content);
        return json;
    }

    // 读取文件数据
    public static String read(String path) {
        try {
            byte[] bytes;
            bytes = Files.readAllBytes(Paths.get(path));
            String content = new String(bytes, StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                System.err.println("文件内容为空或文件不可读取");
                return null;
            }
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}