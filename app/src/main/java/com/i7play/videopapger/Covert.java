package com.i7play.videopapger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/7.
 */

public class Covert {
    public static void main(String args[]){
        String path = "C:\\Users\\Administrator\\Desktop\\22";
        File root = new File(path);
        for (File f: root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("mp4");
            }
        })){
            ArrayList<String> cmd = new ArrayList<>();
            cmd.add("E:\\编程软件\\ffmpeg\\bin\\ffmpeg.exe");
            cmd.add("-ss");
            cmd.add("00:00:01");
            cmd.add("-i");
            cmd.add(f.getPath());
            cmd.add("-f");
            cmd.add("image2");
            cmd.add("-y");
            cmd.add("-s");
            cmd.add("270x480");
            cmd.add(f.getPath().replace("mp4", "jpg"));

            try {
                new ProcessBuilder().command(cmd).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
