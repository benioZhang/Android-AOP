package com.benio.ast.track;

import com.benio.ast.TrackInvoker;

import java.util.Map;

public class Tracker {

    @TrackInvoker
    public static void onEvent(String id) {
        // 模拟埋点
        System.out.println("Event " + id);
    }

    // 暂未支持
    public static void onEvent(String id, Map<String, String> params) {
        // 模拟埋点
        System.out.println("Event " + id);
    }
}
