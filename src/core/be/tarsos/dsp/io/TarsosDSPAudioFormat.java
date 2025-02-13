/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package be.tarsos.dsp.io;

/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>AudioFormat</code> is the class that specifies a particular arrangement of data in a sound stream.
 * By examing the information stored in the audio format, you can discover how to interpret the bits in the
 * binary sound data.
 * <p>
 * Every data LineWavelet has an audio format associated with its data stream. The audio format of a source (playback) data LineWavelet indicates
 * what kind of data the data LineWavelet expects to receive for output.  For a target (capture) data LineWavelet, the audio format specifies the kind
 * of the data that can be read from the LineWavelet.
 * Sound files also have audio formats, of course.
 * <p>
 * The <code>AudioFormat</code> class accommodates a number of common sound-file encoding techniques, including
 * pulse-code modulation (PCM), mu-law encoding, and a-law encoding.  These encoding techniques are predefined,
 * but service providers can create new encoding types.
 * The encoding that a specific format uses is named by its <code>encoding</code> field.
 *<p>
 * In addition to the encoding, the audio format includes other properties that further specify the exact
 * arrangement of the data.
 * These include the number of channels, sample rate, sample size, byte order, frame rate, and frame size.
 * Sounds may have different numbers of audio channels: one for mono, two for stereo.
 * The sample rate measures how many "snapshots" (samples) of the sound pressure are taken per second, per channel.
 * (If the sound is stereo rather than mono, two samples are actually measured at each instant of time: one for the left channel,
 * and another for the right channel; however, the sample rate still measures the number per channel, so the rate is the same
 * regardless of the number of channels.   This is the standard use of the term.)
 * The sample size indicates how many bits are used to store each snapshot; 8 and 16 are typical values.
 * For 16-bit samples (or any other sample size larger than a byte),
 * byte order is important; the bytes in each sample are arranged in
 * either the "little-endian" or "big-endian" style.
 * For encodings like PCM, a frame consists of the set of samples for all channels at a given
 * point in time, and so the size of a frame (in bytes) is always equal to the size of a sample (in bytes) times
 * the number of channels.  However, with some other sorts of encodings a frame can contain
 * a bundle of compressed data for a whole series of samples, as well as additional, non-sample
 * data.  For such encodings, the sample rate and sample size refer to the data after it is decoded into PCM,
 * and so they are completely different from the frame rate and frame size.
 *
 * <p>An <code>AudioFormat</code> object can include a set of
 * properties. A property is a pair of key and value: the key
 * is of type <code>String</code>, the associated property
 * value is an arbitrary object. Properties specify
 * additional format specifications, like the bit rate for
 * compressed formats. Properties are mainly used as a means
 * to transport additional information of the audio format
 * to and from the service providers. Therefore, properties
 * are ignored in the AudioFormat method.
 *
 * <p>The following table lists some common properties which
 * service providers should use, if applicable:
 *
 * <table border=0>
 *  <tr>
 *   <th>Property key</th>
 *   <th>Value type</th>
 *   <th>Description</th>
 *  </tr>
 *  <tr>
 *   <td>&quot;bitrate&quot;</td>
 *   <td>{@link java.lang.Integer Integer}</td>
 *   <td>average bit rate in bits per second</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;vbr&quot;</td>
 *   <td>{@link java.lang.Boolean Boolean}</td>
 *   <td><code>true</code>, if the file is encoded in variable bit
 *       rate (VBR)</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;quality&quot;</td>
 *   <td>{@link java.lang.Integer Integer}</td>
 *   <td>encoding/conversion quality, 1..100</td>
 *  </tr>
 * </table>
 *
 * <p>Vendors of service providers (plugins) are encouraged
 * to seek information about other already established
 * properties in third party plugins, and follow the same
 * conventions.
 *
 * @author Kara Kytle
 * @author Florian Bomers
 * @since 1.3
 */
public class TarsosDSPAudioFormat {

    // INSTANCE VARIABLES
	

    /**
     * The audio encoding technique used by this format.
     */
    protected Encoding encoding;

    /**
     * The number of samples played or recorded per second, for sounds that have this format.
     */
    protected float sampleRate;

    /**
     * The number of bits in each sample of a sound that has this format.
     */
    protected int sampleSizeInBits;

    /**
     * The number of audio channels in this format (1 for mono, 2 for stereo).
     */
    protected int channels;

    /**
     * The number of bytes in each frame of a sound that has this format.
     */
    protected int frameSize;

    /**
     * The number of frames played or recorded per second, for sounds that have this format.
     */
    protected float frameRate;

    /**
     * Indicates whether the audio data is stored in big-endian or little-endian order.
     */
    protected boolean bigEndian;


    /** The set of properties */
    private HashMap<String, Object> properties;
    
    public static final int NOT_SPECIFIED = -1;


    /**
     * Constructs an <code>AudioFormat</code> with the given parameters.
     * The encoding specifies the convention used to represent the data.
     * The other parameters are further explained in the 
     * @param encoding                  the audio encoding technique
     * @param sampleRate                the number of samples per second
     * @param sampleSizeInBits  the number of bits in each sample
     * @param channels                  the number of channels (1 for mono, 2 for stereo, and so on)
     * @param frameSize                 the number of bytes in each frame
     * @param frameRate                 the number of frames per second
     * @param bigEndian                 indicates whether the data for a single sample
     *                                                  is stored in big-endian byte order (<code>false</code>
     *                                                  means little-endian)
     */
    public TarsosDSPAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits,
                       int channels, int frameSize, float frameRate, boolean bigEndian) {

        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;
        this.properties = null;
    }


    /**
     * Constructs an <code>AudioFormat</code> with the given parameters.
     * The encoding specifies the convention used to represent the data.
     * The other parameters are further explained in the 
     * @param encoding         the audio encoding technique
     * @param sampleRate       the number of samples per second
     * @param sampleSizeInBits the number of bits in each sample
     * @param channels         the number of channels (1 for mono, 2 for
     *                         stereo, and so on)
     * @param frameSize        the number of bytes in each frame
     * @param frameRate        the number of frames per second
     * @param bigEndian        indicates whether the data for a single sample
     *                         is stored in big-endian byte order
     *                         (<code>false</code> means little-endian)
     * @param properties       a <code>Map&lt;String,Object&gt;</code> object
     *                         containing format properties
     *
     * @since 1.5
     */
    public TarsosDSPAudioFormat(Encoding encoding, float sampleRate,
                       int sampleSizeInBits, int channels,
                       int frameSize, float frameRate,
                       boolean bigEndian, Map<String, Object> properties) {
        this(encoding, sampleRate, sampleSizeInBits, channels,
             frameSize, frameRate, bigEndian);
        this.properties = new HashMap<String, Object>(properties);
    }


    /**
     * Constructs an <code>AudioFormat</code> with a linear PCM encoding and
     * the given parameters.  The frame size is set to the number of bytes
     * required to contain one sample from each channel, and the frame rate
     * is set to the sample rate.
     *
     * @param sampleRate                the number of samples per second
     * @param sampleSizeInBits  the number of bits in each sample
     * @param channels                  the number of channels (1 for mono, 2 for stereo, and so on)
     * @param signed                    indicates whether the data is signed or unsigned
     * @param bigEndian                 indicates whether the data for a single sample
     *                                                  is stored in big-endian byte order (<code>false</code>
     *                                                  means little-endian)
     */
    public TarsosDSPAudioFormat(float sampleRate, int sampleSizeInBits,
                       int channels, boolean signed, boolean bigEndian) {

        this((signed == true ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED),
             sampleRate,
             sampleSizeInBits,
             channels,
             (channels == NOT_SPECIFIED || sampleSizeInBits == NOT_SPECIFIED)?
             NOT_SPECIFIED:
             ((sampleSizeInBits + 7) / 8) * channels,
             sampleRate,
             bigEndian);
    }

    /**
     * Obtains the type of encoding for sounds in this format.
     *
     * @return the encoding type
     * @see Encoding#PCM_SIGNED
     * @see Encoding#PCM_UNSIGNED
     * @see Encoding#ULAW
     * @see Encoding#ALAW
     */
    public Encoding getEncoding() {

        return encoding;
    }

    /**
     * Obtains the sample rate.
     * For compressed formats, the return value is the sample rate of the uncompressed
     * audio data.
     * When this AudioFormat is used for queries capabilities , a sample rate of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any sample rate is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the sample rate is not defined for this audio format.
     * @return the number of samples per second,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getFrameRate()
     */
    public float getSampleRate() {

        return sampleRate;
    }

    /**
     * Obtains the size of a sample.
     * For compressed formats, the return value is the sample size of the
     * uncompressed audio data.
     * When this AudioFormat is used for queries or capabilities , a sample size of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any sample size is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the sample size is not defined for this audio format.
     * @return the number of bits in each sample,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getFrameSize()
     */
    public int getSampleSizeInBits() {

        return sampleSizeInBits;
    }

    /**
     * Obtains the number of channels.
     * When this AudioFormat is used for queries  or capabilities , a return value of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any (positive) number of channels is
     * acceptable.
     * @return The number of channels (1 for mono, 2 for stereo, etc.),
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     */
    public int getChannels() {

        return channels;
    }

    /**
     * Obtains the frame size in bytes.
     * When this AudioFormat is used for queries or capabilities, a frame size of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any frame size is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the frame size is not defined for this audio format.
     * @return the number of bytes per frame,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getSampleSizeInBits()
     */
    public int getFrameSize() {

        return frameSize;
    }

    /**
     * Obtains the frame rate in frames per second.
     * When this AudioFormat is used for queries or capabilities , a frame rate of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any frame rate is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the frame rate is not defined for this audio format.
     * @return the number of frames per second,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getSampleRate()
     */
    public float getFrameRate() {

        return frameRate;
    }


    /**
     * Indicates whether the audio data is stored in big-endian or little-endian
     * byte order.  If the sample size is not more than one byte, the return value is
     * irrelevant.
     * @return <code>true</code> if the data is stored in big-endian byte order,
     * <code>false</code> if little-endian
     */
    public boolean isBigEndian() {

        return bigEndian;
    }


    /**
     * Obtain an unmodifiable map of properties.
     * The concept of properties is further explained in
     * the.
     *
     * @return a <code>Map&lt;String,Object&gt;</code> object containing
     *         all properties. If no properties are recognized, an empty map is
     *         returned.
     *
     * @see #getProperty(String)
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
	public Map<String,Object> properties() {
        Map<String,Object> ret;
        if (properties == null) {
            ret = new HashMap<String,Object>(0);
        } else {
            ret = (Map<String,Object>) (properties.clone());
        }
        return (Map<String,Object>) Collections.unmodifiableMap(ret);
    }


    /**
     * Obtain the property value specified by the key.
     * The concept of properties is further explained in
     * the.
     *
     * <p>If the specified property is not defined for a
     * particular file format, this method returns
     * <code>null</code>.
     *
     * @param key the key of the desired property
     * @return the value of the property with the specified key,
     *         or <code>null</code> if the property does not exist.
     *
     * @see #properties()
     * @since 1.5
     */
    public Object getProperty(String key) {
        if (properties == null) {
            return null;
        }
        return properties.get(key);
    }


    /**
     * Indicates whether this format matches the one specified.  To match,
     * two formats must have the same encoding, the same number of channels,
     * and the same number of bits per sample and bytes per frame.
     * The two formats must also have the same sample rate,
     * unless the specified format has the sample rate value <code>AudioSystem.NOT_SPECIFIED</code>,
     * which any sample rate will match.  The frame rates must
     * similarly be equal, unless the specified format has the frame rate
     * value <code>AudioSystem.NOT_SPECIFIED</code>.  The byte order (big-endian or little-endian)
     * must match if the sample size is greater than one byte.
     *
     * @param format format to test for match
     * @return <code>true</code> if this format matches the one specified,
     * <code>false</code> otherwise.
     */
    /*
     * $$kk: 04.20.99: i changed the semantics of this.
     */
    public boolean matches(TarsosDSPAudioFormat format) {

        if (format.getEncoding().equals(getEncoding()) &&
            ( (format.getSampleRate() == (float)NOT_SPECIFIED) || (format.getSampleRate() == getSampleRate()) ) &&
            (format.getSampleSizeInBits() == getSampleSizeInBits()) &&
            (format.getChannels() == getChannels() &&
             (format.getFrameSize() == getFrameSize()) &&
             ( (format.getFrameRate() == (float)NOT_SPECIFIED) || (format.getFrameRate() == getFrameRate()) ) &&
             ( (format.getSampleSizeInBits() <= 8)  || (format.isBigEndian() == isBigEndian()) ) ) )
            return true;

        return false;
    }


    /**
     * Returns a string that describes the format, such as:
     * "PCM SIGNED 22050 Hz 16 bit mono big-endian".  The contents of the string
     * may vary between implementations of Java Sound.
     *
     * @return a string that describes the format parameters
     */
    public String toString() {
        String sEncoding = "";
        if (getEncoding() != null) {
            sEncoding = getEncoding().toString() + " ";
        }

        String sSampleRate;
        if (getSampleRate() == (float) NOT_SPECIFIED) {
            sSampleRate = "unknown sample rate, ";
        } else {
            sSampleRate = "" + getSampleRate() + " Hz, ";
        }

        String sSampleSizeInBits;
        if (getSampleSizeInBits() == (float) NOT_SPECIFIED) {
            sSampleSizeInBits = "unknown bits per sample, ";
        } else {
            sSampleSizeInBits = "" + getSampleSizeInBits() + " bit, ";
        }

        String sChannels;
        if (getChannels() == 1) {
            sChannels = "mono, ";
        } else
            if (getChannels() == 2) {
                sChannels = "stereo, ";
            } else {
                if (getChannels() == NOT_SPECIFIED) {
                    sChannels = " unknown number of channels, ";
                } else {
                    sChannels = ""+getChannels()+" channels, ";
                }
            }

        String sFrameSize;
        if (getFrameSize() == (float) NOT_SPECIFIED) {
            sFrameSize = "unknown frame size, ";
        } else {
            sFrameSize = "" + getFrameSize()+ " bytes/frame, ";
        }

        String sFrameRate = "";
        if (Math.abs(getSampleRate() - getFrameRate()) > 0.00001) {
            if (getFrameRate() == (float) NOT_SPECIFIED) {
                sFrameRate = "unknown frame rate, ";
            } else {
                sFrameRate = getFrameRate() + " frames/second, ";
            }
        }

        String sEndian = "";
        if ((getEncoding().equals(Encoding.PCM_SIGNED)
             || getEncoding().equals(Encoding.PCM_UNSIGNED))
            && ((getSampleSizeInBits() > 8)
                || (getSampleSizeInBits() == NOT_SPECIFIED))) {
            if (isBigEndian()) {
                sEndian = "big-endian";
            } else {
                sEndian = "little-endian";
            }
        }

        return sEncoding
            + sSampleRate
            + sSampleSizeInBits
            + sChannels
            + sFrameSize
            + sFrameRate
            + sEndian;

    }

    /**
     * The <code>Encoding</code> class  names the  specific type of data representation
     * used for an audio stream.   The encoding includes aspects of the
     * sound format other than the number of channels, sample rate, sample size,
     * frame rate, frame size, and byte order.
     * <p>
     * One ubiquitous type of audio encoding is pulse-code modulation (PCM),
     * which is simply a linear (proportional) representation of the sound
     * waveform.  With PCM, the number stored in each sample is proportional
     * to the instantaneous amplitude of the sound pressure at that point in
     * time.  The numbers are frequently signed or unsigned integers.
     * Besides PCM, other encodings include mu-law and a-law, which are nonlinear
     * mappings of the sound amplitude that are often used for recording speech.
     * <p>
     * You can use a predefined encoding by referring to one of the static
     * objects created by this class, such as PCM_SIGNED or
     * PCM_UNSIGNED.  Service providers can create new encodings, such as
     * compressed audio formats or floating-point PCM samples, and make
     * these available through the <code>AudioSystem</code> class.
     * <p>
     * The <code>Encoding</code> class is static, so that all
     * <code>AudioFormat</code> objects that have the same encoding will refer
     * to the same object (rather than different instances of the same class).
     * This allows matches to be made by checking that two format's encodings
     * are equal.
     *
     * @author Kara Kytle
     * @since 1.3
     */
    public static class Encoding {


        // ENCODING DEFINES

        /**
         * Specifies signed, linear PCM data.
         */
        public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");

        /**
         * Specifies unsigned, linear PCM data.
         */
        public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");

        /**
         * Specifies u-law encoded data.
         */
        public static final Encoding ULAW = new Encoding("ULAW");

        /**
         * Specifies a-law encoded data.
         */
        public static final Encoding ALAW = new Encoding("ALAW");


        // INSTANCE VARIABLES

        /**
         * Encoding name.
         */
        private String name;


        // CONSTRUCTOR

        /**
         * Constructs a new encoding.
         * @param name  the name of the new type of encoding
         */
        public Encoding(String name) {
            this.name = name;
        }


        // METHODS

        /**
         * Finalizes the equals method
         */
        public final boolean equals(Object obj) {
            if (toString() == null) {
                return (obj != null) && (obj.toString() == null);
            }
            if (obj instanceof Encoding) {
                return toString().equals(obj.toString());
            }
            return false;
        }

        /**
         * Finalizes the hashCode method
         */
        public final int hashCode() {
            if (toString() == null) {
                return 0;
            }
            return toString().hashCode();
        }

        /**
         * Provides the <code>String</code> representation of the encoding.  This <code>String</code> is
         * the same name that was passed to the constructor.  For the predefined encodings, the name
         * is similar to the encoding's variable (field) name.  For example, <code>PCM_SIGNED.toString()</code> returns
         * the name "pcm_signed".
         *
         * @return the encoding name
         */
        public final String toString() {
            return name;
        }

    } // class Encoding
}
