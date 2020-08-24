package org.freedesktop.gstreamer.lowlevel;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.freedesktop.gstreamer.meta.GstMetaInfo;
import org.freedesktop.gstreamer.timecode.GstVideoTimeCodeFlags;

/**
 * @author Jokertwo
 */
public interface GstMetaApi extends Library {
    GstMetaApi GST_META_API = GstNative.load("gstvideo", GstMetaApi.class);

    GType gst_video_time_code_meta_api_get_type();

    GType gst_video_crop_meta_api_get_type();

    GType gst_video_gl_texture_upload_meta_api_get_type();

    GType gst_video_meta_api_get_type();

    GType gst_video_region_of_interest_meta_api_get_type();

    GstMetaInfo gst_video_time_code_meta_get_info();


    @Structure.FieldOrder({"meta", "tc"})
    class GstVideoTimeCodeMetaStruct extends Structure {
        public GstMetaStruct.ByValue meta;
        public GstVideoTimeCodeStruct.ByValue tc;

        public GstVideoTimeCodeMetaStruct(Pointer p) {
            super(p);
            read();
        }
    }

    @Structure.FieldOrder({"config", "hours", "minutes", "seconds", "frames", "field_count"})
    class GstVideoTimeCodeStruct extends Structure {
        public static class ByValue extends GstVideoTimeCodeStruct implements Structure.ByValue {
        }

        public GstVideoTimeCodeConfigStruct.ByValue config;
        public int hours;
        public int minutes;
        public int seconds;
        public int frames;
        public int field_count;

        public GstVideoTimeCodeStruct() {
        }

        public GstVideoTimeCodeStruct(Pointer p) {
            super(p);
            read();
        }
    }

    @Structure.FieldOrder({"flags", "info"})
    class GstMetaStruct extends Structure {
        public static final class ByValue extends GstMetaStruct implements Structure.ByValue {
        }

        public long flags;
        public GstMetaInfoStruct.ByReference info;
    }

    @Structure.FieldOrder({"api", "type", "size"})
    class GstMetaInfoStruct extends Structure {
        public static class ByReference extends GstMetaInfoStruct implements Structure.ByReference {
        }

        public GstMetaInfoStruct() {
        }

        public GstMetaInfoStruct(Pointer p) {
            super(p);
            read();
        }

        public GType api;
        public GType type;
        public long size;
    }

    @Structure.FieldOrder({"fps_n", "fps_d", "flags", "latest_daily_jam"})
    class GstVideoTimeCodeConfigStruct extends Structure {
        public static class ByValue extends GstVideoTimeCodeConfigStruct implements Structure.ByValue {
        }

        public int fps_n;
        public int fps_d;
        public GstVideoTimeCodeFlags flags;
        public Pointer latest_daily_jam;

        public GstVideoTimeCodeConfigStruct() {
        }

        public GstVideoTimeCodeConfigStruct(Pointer p) {
            super(p);
            read();
        }
    }

}