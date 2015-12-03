package com.colobu.douban.recommender;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DoubanFileDataModel extends FileDataModel {
    public static Map<String,Long> userNameAndIDMapping = new HashMap<>();
    public static Map<Long,String> userIDAndNameMapping = new HashMap<>();
    private static long userID = 0;

    public DoubanFileDataModel(File dataFile) throws IOException {
        super(dataFile);
    }

    public DoubanFileDataModel(File dataFile, String delimiterRegex) throws IOException {
        super(dataFile, delimiterRegex);
    }

    public DoubanFileDataModel(File dataFile, boolean transpose, long minReloadIntervalMS) throws IOException {
        super(dataFile, transpose, minReloadIntervalMS);
    }

    public DoubanFileDataModel(File dataFile, boolean transpose, long minReloadIntervalMS, String delimiterRegex) throws IOException {
        super(dataFile, transpose, minReloadIntervalMS, delimiterRegex);
    }

    @Override
    protected long readUserIDFromString(String value) {
        value = value.trim();
        if (userNameAndIDMapping.containsKey(value)) {
            return userNameAndIDMapping.get(value);
        }

        userNameAndIDMapping.put(value, userID);
        userIDAndNameMapping.put(userID, value);
        userID++;
        return (userID -1);
    }

    @Override
    protected long readItemIDFromString(String value) {
        value = value.trim();
        return super.readItemIDFromString(value);
    }

    @Override
    protected void processLine(String line, FastByIDMap<?> data, FastByIDMap<FastByIDMap<Long>> timestamps, boolean fromPriorData) {
        String[] fields = line.split(",");
        if (fields[2].equals("-1")) {
            fields[2] = "3";
        }
        line = fields[0] + "," + fields[1] + "," + fields[2];

        super.processLine(line, data, timestamps, fromPriorData);
    }

    @Override
    protected void processLineWithoutID(String line, FastByIDMap<FastIDSet> data, FastByIDMap<FastByIDMap<Long>> timestamps) {
        String[] fields = line.split(",");
        if (fields[2].equals("-1")) {
            fields[2] = "3";
        }
        line = fields[0] + "," + fields[1] + "," + fields[2];
        super.processLineWithoutID(line, data, timestamps);
    }
}
