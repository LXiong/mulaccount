package io.virtualapp.entity;

import java.util.List;

import io.virtualapp.util.JsonParser;

/**
 * Created by IVY on 2016/9/14.
 */
public class DefaultJson extends JsonParser.SimpleResult {

    public static final String TYPE_CROSS = "cross";

    public static final String TYPE_FULL = "full";

    public static final String TYPE_CLEAR = "clean";

    public static final String TYPE_NATIVE = "native";

    /**
     * app : Facebook
     * pkg : com.facebook.katana
     */

    public List<DefaultPkgBean> default_pkg;
    public List<AdBean> ad;

    public static class DefaultPkgBean {
        public String app;
        public String pkg;
    }

    public static class AdBean {
        public String type;
        public String img;
        public String title;
        public int interval_time;
        public List<CrossAd> data;
    }

    public static class CrossAd {
        public String type;
        public String img;
        public String title;
        public String pkg;
    }

}
