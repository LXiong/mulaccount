package io.virtualapp.util;

import com.google.gson.JsonSyntaxException;
import com.lody.virtual.helper.utils.VLog;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Agg (2014) All Rights Reserved.
 * Created by Herbert Dai on 7/29/15.
 */
public class JsonParser {
    public static JsonParser jsonParser;

    public static JsonParser getInstance() {
        if (jsonParser == null) {
            jsonParser = new JsonParser();
        }
        return jsonParser;
    }

    public <T extends SimpleResult> T fromJson(byte[] response, Class<T> tClass) {
        return fromJson(Utility.getStrFromByte(response), tClass);
    }

    public <T extends SimpleResult> T fromJson(String response, Class<T> tClass) {
        String jsonStr = response.replace("\"data\":[]", "\"data\":null");

        T obj = null;
        try {
            obj = Utility.getGson().fromJson(jsonStr, tClass);
        } catch (NumberFormatException e) {
            VLog.d("JsonParser", e.toString() + " json = " + jsonStr);
        } catch (JsonSyntaxException e) {
            VLog.d("JsonParser", e.toString() + " json = " + jsonStr);
        } catch (IllegalStateException e) {
            VLog.d("JsonParser", e.toString() + " json = " + jsonStr);
        }

        // try to parse as SimpleResult
        if (obj == null) {

            SimpleResult result1 = null;
            try {
                result1 = Utility.getGson().fromJson(jsonStr, SimpleResult.class);
            } catch (NumberFormatException e) {
                VLog.d("JsonParser", e.toString() + " json = " + jsonStr);
            } catch (JsonSyntaxException e) {
                VLog.d("JsonParser", e.toString() + " json = " + jsonStr);
            } catch (IllegalStateException e) {
                VLog.d("JsonParser", e.toString() + " json = " + jsonStr);
            }
            VLog.d("JsonParser", "simpleresult = " + result1);
            if (result1 == null) {
                return null;
            }

            try {
                Constructor<T> constructor = tClass.getConstructor(new Class[0]);
                obj = constructor.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public static class SimpleResult implements Serializable {

    }
}
