/*
 * Copyright (c) 2019 Christophe Lafolet
 * Copyright (c) 2019 Neil C Smith
 * Copyright (C) 2014 Tom Greenwood <tgreenwood@cafex.com>
 * Copyright (C) 2007 Wayne Meissner
 * Copyright (C) 1999,2000 Erik Walthinsen <omega@cse.ogi.edu>
 *                    2000 Wim Taymans <wtay@chello.be>
 *
 * This file is part of gstreamer-java.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freedesktop.gstreamer;

import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import org.freedesktop.gstreamer.glib.NativeFlags;
import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GstBufferAPI;
import org.freedesktop.gstreamer.lowlevel.GstBufferAPI.BufferStruct;
import org.freedesktop.gstreamer.lowlevel.GstBufferAPI.MapInfoStruct;
import org.freedesktop.gstreamer.meta.Meta;
import org.freedesktop.gstreamer.meta.MetaDataFactory;
import static org.freedesktop.gstreamer.lowlevel.GstBufferAPI.GSTBUFFER_API;

/**
 * Buffers are the basic unit of data transfer in GStreamer. They contain the
 * timing and offset along with other arbitrary metadata that is associated with
 * the GstMemory blocks that the buffer contains.
 * <p>
 * See upstream documentation at
 * <a href="https://gstreamer.freedesktop.org/data/doc/gstreamer/stable/gstreamer/html/GstBuffer.html"
 * >https://gstreamer.freedesktop.org/data/doc/gstreamer/stable/gstreamer/html/GstBuffer.html</a>
 */
public class Buffer extends MiniObject {

    public static final String GTYPE_NAME = "GstBuffer";

    private final MapInfoStruct mapInfo;
    private final BufferStruct struct;

    /**
     * Creates a newly allocated buffer without any data.
     */
    public Buffer() {
        this(Natives.initializer(GSTBUFFER_API.ptr_gst_buffer_new()));
    }

    /**
     * Creates a newly allocated buffer with data of the given size. The buffer
     * memory is not cleared. If the requested amount of memory cannot be
     * allocated, an exception will be thrown.
     * <p>
     * Note that when size == 0, the buffer data pointer will be NULL.
     *
     * @param size
     */
    public Buffer(int size) {
        this(Natives.initializer(allocBuffer(size)));
    }

    Buffer(Initializer init) {
        super(init);
        mapInfo = new MapInfoStruct();
        struct = new BufferStruct(getRawPointer());
    }

    private static Pointer allocBuffer(int size) {
        Pointer ptr = GSTBUFFER_API.ptr_gst_buffer_new_allocate(null, size, null);
        if (ptr == null) {
            throw new OutOfMemoryError("Could not allocate Buffer of size " + size);
        }
        return ptr;
    }

    /**
     * Gets a {@link java.nio.ByteBuffer} that can access the native memory
     * associated with this Buffer, with the option of ensuring the memory is
     * writable.
     * <p>
     * When requesting a writable buffer, if the buffer is writable but the
     * underlying memory isn't, a writable copy will automatically be created
     * and returned. The readonly copy of the buffer memory will then also be
     * replaced with this writable copy.
     * <p>
     * <b>The Buffer should be unmapped with {@link #unmap()} after usage.</b>
     *
     * @param writable
     * @return A {@link java.nio.ByteBuffer} that can access this Buffer's data.
     */
    public ByteBuffer map(boolean writable) {
        final boolean ok = GSTBUFFER_API.gst_buffer_map(this, mapInfo,
                writable ? GstBufferAPI.GST_MAP_WRITE : GstBufferAPI.GST_MAP_READ);
        if (ok && mapInfo.data != null) {
            return mapInfo.data.getByteBuffer(0, mapInfo.size.intValue());
        }
        return null;
    }

    /**
     * Release the memory previously mapped with {@link #map(boolean)}
     */
    public void unmap() {
        GSTBUFFER_API.gst_buffer_unmap(this, mapInfo);
    }

    /**
     * Get the amount of memory blocks that this buffer has. This amount is never
     * larger than what {@code gst_buffer_get_max_memory()} returns.
     *
     * @return the number of memory blocks this buffer is made of.
     */
    public int getMemoryCount() {
        return GSTBUFFER_API.gst_buffer_n_memory(this);
    }

    /**
     * Gets the timestamps of this buffer. The buffer DTS refers to the
     * timestamp when the buffer should be decoded and is usually monotonically
     * increasing.
     *
     * @return a long representing the timestamp or {@link ClockTime#NONE}
     * when the timestamp is not known or relevant.
     */
    public long getDecodeTimestamp() {
        return (long) this.struct.readField("dts");
    }

    /**
     * Set the decode timestamp of the Buffer
     *
     * @param val a long representing the timestamp or
     *            {@link ClockTime#NONE} when the timestamp is not known or relevant.
     */
    public void setDecodeTimestamp(long val) {
        this.struct.writeField("dts", val);
    }

    /**
     * Gets the timestamps of this buffer. The buffer PTS refers to the
     * timestamp when the buffer content should be presented to the user and is
     * not always monotonically increasing.
     *
     * @return a long representing the timestamp or {@link ClockTime#NONE}
     * when the timestamp is not known or relevant.
     */
    public long getPresentationTimestamp() {
        return (long) this.struct.readField("pts");
    }

    /**
     * Set the presentation timestamp of the Buffer
     *
     * @param val a long representing the timestamp or
     *            {@link ClockTime#NONE} when the timestamp is not known or relevant.
     */
    public void setPresentationTimestamp(long val) {
        this.struct.writeField("pts", val);
    }

    /**
     * Gets the duration of this buffer.
     *
     * @return a ClockTime representing the timestamp or {@link ClockTime#NONE}
     * when the timestamp is not known or relevant.
     */
    public long getDuration() {
        return (long) this.struct.readField("duration");
    }

    /**
     * Set the duration of this buffer.
     *
     * @param val a long representing the duration or
     *            {@link ClockTime#NONE} when the timestamp is not known or relevant.
     */
    public void setDuration(long val) {
        this.struct.writeField("duration", val);
    }

    /**
     * Get the offset (media-specific) of this buffer
     *
     * @return a media specific offset for the buffer data. For video frames,
     * this is the frame number of this buffer. For audio samples, this is the
     * offset of the first sample in this buffer. For file data or compressed
     * data this is the byte offset of the first byte in this buffer.
     */
    public long getOffset() {
        return (Long) this.struct.readField("offset");
    }

    /**
     * Set the offset (media-specific) of this buffer
     *
     * @param val a media specific offset for the buffer data. For video frames,
     *            this is the frame number of this buffer. For audio samples, this is the
     *            offset of the first sample in this buffer. For file data or compressed
     *            data this is the byte offset of the first byte in this buffer.
     */
    public void setOffset(long val) {
        this.struct.writeField("offset", val);
    }

    /**
     * Get the offset (media-specific) of this buffer
     *
     * @return a media specific offset for the buffer data. For video frames,
     * this is the frame number of this buffer. For audio samples, this is the
     * offset of the first sample in this buffer. For file data or compressed
     * data this is the byte offset of the first byte in this buffer.
     */
    public long getOffsetEnd() {
        return (Long) this.struct.readField("offset_end");
    }

    /**
     * Set the offset (media-specific) of this buffer
     *
     * @param val a media specific offset for the buffer data. For video frames,
     *            this is the frame number of this buffer. For audio samples, this is the
     *            offset of the first sample in this buffer. For file data or compressed
     *            data this is the byte offset of the first byte in this buffer.
     */
    public void setOffsetEnd(long val) {
        this.struct.writeField("offset_end", val);
    }

    /**
     * Get the GstBufferFlags describing this buffer.
     * <p>
     * Since GStreamer 1.10
     *
     * @return an EnumSet of {@link BufferFlags}
     */
    @Gst.Since(minor = 10)
    public EnumSet<BufferFlags> getFlags() {
        Gst.checkVersion(1, 10);
        int nativeInt = GstBufferAPI.GSTBUFFER_API.gst_buffer_get_flags(this);
        return NativeFlags.fromInt(BufferFlags.class, nativeInt);
    }

    /**
     * Get metadata from buffer. In case that in buffer is not selected type of metadata return null
     *
     * @param clazz requested type of metadata
     * @return return metadata from buffer. Return null if in buffer is not selected metadata type
     */
    public <M extends Meta> M getMetadata(Class<M> clazz) {
        MetaDataFactory metaDataFactory = new MetaDataFactory();
        Pointer pointer = GSTBUFFER_API.gst_buffer_get_meta(this, metaDataFactory.getGType(clazz));
        // can not create metadata class from null pointer
        if (pointer == null) {
            return null;
        }
        return metaDataFactory.getInstance(clazz, pointer);
    }


    /**
     * Check if buffer contains selected type of metadata
     * <p>
     * Since GStreamer 1.14
     *
     * @param clazz type of metadata
     * @return return true only if buffer contains selected type of metadata
     */
    @Gst.Since(minor = 14)
    public boolean containsMetadata(Class<? extends Meta> clazz) {
        Gst.checkVersion(1, 14);
        return getNumberOfMeta(clazz) > 0;
    }

    /**
     * Check number of metadata for selected type. There can be more metadata in case multiple video/audio layer
     * <p>
     * Since GStreamer 1.14
     *
     * @param clazz type of metadata
     * @return return number of metadata
     */
    @Gst.Since(minor = 14)
    public int getNumberOfMeta(Class<? extends Meta> clazz) {
        Gst.checkVersion(1, 14);
        MetaDataFactory metaDataFactory = new MetaDataFactory();
        return GSTBUFFER_API.gst_buffer_get_n_meta(this, metaDataFactory.getGType(clazz));
    }


    /**
     * Set some of the GstBufferFlags describing this buffer. This is a union
     * operation and does not clear flags that are not mentioned.
     * <p>
     * Since GStreamer 1.10
     *
     * @param flags an EnumSet of {@link BufferFlags} to be set on the buffer.
     * @return true if flags were successfully set on this buffer
     */
    @Gst.Since(minor = 10)
    public boolean setFlags(EnumSet<BufferFlags> flags) {
        Gst.checkVersion(1, 10);
        return GstBufferAPI.GSTBUFFER_API.gst_buffer_set_flags(this, NativeFlags.toInt(flags));
    }

    /**
     * unset the GstBufferFlags describing this buffer. This is a difference
     * operation and does not clear flags that are not mentioned.
     * <p>
     * Since GStreamer 1.10
     *
     * @param flags an EnumSet of {@link BufferFlags} to be cleared on the buffer.
     * @return true if flags were successfully cleared on this buffer
     */
    @Gst.Since(minor = 10)
    public boolean unsetFlags(EnumSet<BufferFlags> flags) {
        Gst.checkVersion(1, 10);
        return GstBufferAPI.GSTBUFFER_API.gst_buffer_unset_flags(this, NativeFlags.toInt(flags));
    }

}
