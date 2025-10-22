/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.lineageos.glyph.Constants.Constants;

public final class FileUtils {

    private static final String TAG = "GlyphFileUtils";
    private static final boolean DEBUG = true;

    public static String readLine(String fileName) {
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName), 512);
            line = reader.readLine();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "No such file " + fileName + " for reading", e);
        } catch (IOException e) {
            Log.e(TAG, "Could not read from file " + fileName, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) { }
        }
        return line;
    }

    public static int readLineInt(String fileName) {
        String line = readLine(fileName);
        if (line == null) {
            return 0;
        }
        try {
            return Integer.parseInt(line.replace("0x", ""));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not convert string to int from file " + fileName, e);
        }
        return 0;
    }

    public static void writeLine(String fileName, String value) {
        String modePath = ResourceUtils.getString("glyph_settings_paths_mode_absolute");
        BufferedWriter writerMode = null;
        BufferedWriter writerValue = null;
        try {
            if (!modePath.isBlank()) {
                writerMode = new BufferedWriter(new FileWriter(modePath));
                writerMode.write("1");
            }
            writerValue = new BufferedWriter(new FileWriter(fileName));
            writerValue.write(value);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "No such file " + fileName + " for writing", e);
        } catch (IOException e) {
            Log.e(TAG, "Could not write to file " + fileName, e);
        } finally {
            try {
                if (writerMode != null) {
                    writerMode.close();
                }
                if (writerValue != null) {
                    writerValue.close();
                }
            } catch (IOException e) {
                // Ignored, not much we can do anyway
            }
        }
    }

    public static void writeLine(String fileName, int value) {
        writeLine(fileName, Integer.toString(value));
    }

    public static void writeLine(String fileName, float value) {
        writeLine(fileName, Float.toString(value));
    }

    public static void writeAllLed(String value) {
        writeLine(ResourceUtils.getString("glyph_settings_paths_all_absolute"), value);
    }

    public static void writeAllLed(int value) {
        writeAllLed(Integer.toString(value));
    }

    public static void writeAllLed(float value) {
        writeAllLed(Integer.toString(Math.round(value)));
    }

    public static void writeFrameLed(String value) {
        if (DEBUG) Log.d(TAG, "String pattern: " + value);
        writeLine(ResourceUtils.getString("glyph_settings_paths_frame_absolute"), value);
    }

    public static void writeFrameLed(int[] value) {
        writeFrameLed(Arrays.toString(value).replaceAll("\\[|\\]", "").replace(", ", " "));
    }

    public static void writeFrameLed(float[] value) {
        int[] intValue = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            intValue[i] = Math.round(value[i]);
        }
        writeFrameLed(intValue);
    }

    public static void writeSingleLed(String led, String value) {
        writeLine(ResourceUtils.getString("glyph_settings_paths_single_absolute"), led + " " + value);
    }

    public static void writeSingleLed(int led, String value) {
        writeSingleLed(Integer.toString(led), value);
    }

    public static void writeSingleLed(String led, int value) {
        writeSingleLed(led, Integer.toString(value));
    }

    public static void writeSingleLed(String led, float value) {
        writeSingleLed(led, Integer.toString(Math.round(value)));
    }

    public static void writeSingleLed(int led, float value) {
        writeSingleLed(Integer.toString(led), Integer.toString(Math.round(value)));
    }
}
