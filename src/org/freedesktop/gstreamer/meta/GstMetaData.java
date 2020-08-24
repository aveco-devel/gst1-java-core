package org.freedesktop.gstreamer.meta;

import java.util.function.Supplier;
import org.freedesktop.gstreamer.lowlevel.GType;
import static org.freedesktop.gstreamer.lowlevel.GstMetaApi.GST_META_API;

/**
 * Gst meta data GTypes
 * @author Jokertwo
 */
public enum GstMetaData {

    VIDEO_CROP_META(GST_META_API::gst_video_crop_meta_api_get_type),
    VIDEO_GL_TEXTURE_META(GST_META_API::gst_video_gl_texture_upload_meta_api_get_type),
    VIDEO_META(GST_META_API::gst_video_meta_api_get_type),
    VIDEO_REGION_OF_INTEREST(GST_META_API::gst_video_region_of_interest_meta_api_get_type),
    VIDEO_TIME_CODE_META(GST_META_API::gst_video_time_code_meta_api_get_type);

    private final Supplier<GType> gTypeMetaSup;

    GstMetaData(Supplier<GType> gTypeMetaSup) {
        this.gTypeMetaSup = gTypeMetaSup;
    }

    public GType getType() {
        return gTypeMetaSup.get();
    }
}