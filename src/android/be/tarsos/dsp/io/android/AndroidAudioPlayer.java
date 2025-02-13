package be.tarsos.dsp.io.android;
import java.lang.*;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioAttributes;
import android.util.Log;


import be.tarsos.dsp.AudioDispatcherControllable;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessorControllable;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

/**
 * Plays audio from an {@link be.tarsos.dsp.AudioDispatcher} or {@link be.tarsos.dsp.AudioGenerator}
 * on an Android {@link AudioTrack}. This class only supports mono, 16 bit PCM. Depending on your device,
 * some sample rates could not be supported. This class uses the method that writes floats
 * to {@link android.media.AudioTrack} which is only introduced in Android API Level 21.
 *
 * @author Alex Mikhalev
 * @author Joren Six
 * @see AudioTrack
 */
public class AndroidAudioPlayer implements AudioProcessorControllable {
    /**
     * The default stream type to use.
     */
    public static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final String TAG = "AndroidAudioPlayer";

    private final AudioTrack audioTrack;
    private int m_sampleRate;
    private int m_intLastPlayerHeadPoistionWhenFileWasRead;
    private AudioDispatcherControllable dispatcher;
    private float m_flSecondsProcessed = 0;
    private boolean paused = false; 
    /**
     * Constructs a new AndroidAudioPlayer from an audio format, default buffer size and stream type.
     *
     * @param audioFormat The audio format of the stream that this AndroidAudioPlayer will process.
     *                    This can only be 1 channel, PCM 16 bit.
     * @param bufferSizeInSamples  The requested buffer size in samples.
     * @param streamType  The type of audio stream that the internal AudioTrack should use. For
     *                    example, {@link AudioManager#STREAM_MUSIC}.
     * @throws IllegalArgumentException if audioFormat is not valid or if the requested buffer size is invalid.
     * @see AudioTrack
     */
    public AndroidAudioPlayer(TarsosDSPAudioFormat audioFormat, int bufferSizeInSamples, int streamType) {
        if (audioFormat.getChannels() != 1) {
            throw new IllegalArgumentException("TarsosDSP only supports mono audio channel count: " + audioFormat.getChannels());
        }

        // The requested sample rate
        m_sampleRate = (int) audioFormat.getSampleRate();


        //The buffer size in bytes is twice the buffer size expressed in samples if 16bit samples are used:
        int bufferSizeInBytes = bufferSizeInSamples * audioFormat.getSampleSizeInBits()/8;

        // From the Android API about getMinBufferSize():
        // The total size (in bytes) of the internal buffer where audio data is read from for playback.
        // If track's creation mode is MODE_STREAM, you can write data into this buffer in chunks less than or equal to this size,
        // and it is typical to use chunks of 1/2 of the total size to permit double-buffering. If the track's creation mode is MODE_STATIC,
        // this is the maximum length sample, or audio clip, that can be played by this instance. See getMinBufferSize(int, int, int) to determine
        // the minimum required buffer size for the successful creation of an AudioTrack instance in streaming mode. Using values smaller
        // than getMinBufferSize() will result in an initialization failure.
        int minBufferSizeInBytes = AudioTrack.getMinBufferSize(m_sampleRate, AudioFormat.CHANNEL_OUT_MONO,  AudioFormat.ENCODING_PCM_16BIT);
        if(minBufferSizeInBytes > bufferSizeInBytes){
            throw new IllegalArgumentException("The buffer size should be at least " + (minBufferSizeInBytes/(audioFormat.getSampleSizeInBits()/8)) + " (samples) according to  AudioTrack.getMinBufferSize().");
        }

        //http://developer.android.com/reference/android/media/AudioTrack.html#AudioTrack(int, int, int, int, int, int)
//        audioTrack = new AudioTrack(streamType, m_sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes,AudioTrack.MODE_STREAM);

        Log.e("TUNE_TRACKER", "setting up audiotrack ");

        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(m_sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(bufferSizeInBytes)
                .build();


        Log.e("TUNE_TRACKER", "after setting up audiotrack ");

        audioTrack.play();
    }

    /**
     * Constructs a new AndroidAudioPlayer from an audio format.
     *
     * @param audioFormat The audio format that this AndroidAudioPlayer will process.
     * @see AndroidAudioPlayer#AndroidAudioPlayer(TarsosDSPAudioFormat, int, int)
     */
    public AndroidAudioPlayer(TarsosDSPAudioFormat audioFormat) {
        this(audioFormat, 4096, DEFAULT_STREAM_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(AudioEvent audioEvent) {
        int overlapInSamples = audioEvent.getOverlap();
        int stepSizeInSamples = audioEvent.getBufferSize() - overlapInSamples;
        byte[] byteBuffer = audioEvent.getByteBuffer();
        float secProc = this.round(this.dispatcher.secondsProcessed(), 3);        
    
        if (!paused) {

            if (secProc != m_flSecondsProcessed) 
            {
                m_intLastPlayerHeadPoistionWhenFileWasRead = audioTrack.getPlaybackHeadPosition();
                m_flSecondsProcessed = secProc;
            }

        
        //int ret = audioTrack.write(audioEvent.getFloatBuffer(),overlapInSamples,stepSizeInSamples,AudioTrack.WRITE_BLOCKING);
            
            int ret = audioTrack.write(byteBuffer,overlapInSamples*2,stepSizeInSamples*2);
            if (ret < 0) {
                Log.e("TUNE_TRACKER", "AudioTrack.write returned error code " + ret);
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingFinished() {
        audioTrack.flush();
        audioTrack.stop();
        audioTrack.release();
    }

    @Override
    public void play()
    {
        paused = false;
        audioTrack.play(); 
    }

    @Override
    public void pause()
    {        
        paused = true;       
        audioTrack.pause();    
      //  audioTrack.flush();    
    }


    private  float round(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
    }


    public float getSecondsProcessed() {
        return m_flSecondsProcessed;
    }

    public void setSecondsProcessed(float secondsProcessed) {
        m_flSecondsProcessed = secondsProcessed;
        audioTrack.pause();
        audioTrack.setPlaybackHeadPosition(0);
        audioTrack.flush();
        m_intLastPlayerHeadPoistionWhenFileWasRead = 0;
    }


    public int getPositionMs() {
        return convertFramesToTime(audioTrack.getPlaybackHeadPosition() - m_intLastPlayerHeadPoistionWhenFileWasRead);
    }

    public int getPlaybackPostion() {
        return audioTrack.getPlaybackHeadPosition();
    }

    public int getLastPlaybackPostionBeforeWriteToTrack() {
        return m_intLastPlayerHeadPoistionWhenFileWasRead;
    }

    public float getDispatcherElapsedTime() {
        return this.dispatcher.secondsProcessed();
    }


    private int convertFramesToTime(int intFrames)
    {
        return (int)Math.floor(((double)intFrames / m_sampleRate) * 1000.0);
    }

    public void setDispatcher(AudioDispatcherControllable newDispatcher){
		this.dispatcher = newDispatcher;
	}
}
