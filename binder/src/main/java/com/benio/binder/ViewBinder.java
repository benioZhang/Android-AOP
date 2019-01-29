package com.benio.binder;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ViewBinder {
    private static final String TAG = "ViewBinder";
    static final Map<Class<?>, Method> BINDINGS = new LinkedHashMap<>();
    private static boolean DEBUG = false;

    private ViewBinder() {
        //no instance
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public static void bind(Activity target) {
        Class<?> targetClass = target.getClass();
        if (DEBUG) Log.d(TAG, "Looking up binding for " + targetClass.getName());
        Method method = findBindingMethodForClass(targetClass);
        try {
            method.invoke(null, target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static Method findBindingMethodForClass(Class<?> cls) {
        Method bindingMethod = BINDINGS.get(cls);
        if (bindingMethod != null || BINDINGS.containsKey(cls)) {
            if (DEBUG) Log.d(TAG, "HIT: Cached in binding map.");
            return bindingMethod;
        }
        String clsName = cls.getName();
        try {
            Class<?> bindingClass = cls.getClassLoader().loadClass(clsName + "_ViewBinding");
            bindingMethod = bindingClass.getMethod("bind", cls);
            if (DEBUG) Log.d(TAG, "HIT: Loaded binding class and method.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        BINDINGS.put(cls, bindingMethod);
        return bindingMethod;
    }
}
