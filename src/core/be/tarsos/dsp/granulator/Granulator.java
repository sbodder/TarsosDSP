package be.tarsos.dsp.granulator;


import java.util.ArrayList;
import java.util.Arrays;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

/**
 * Granulator plays back samples using granular synthesis. 
 * Methods can be used to control playback rate, pitch, grain size,
 *  grain interval and grain randomness and position (this last case assumes that the playback rate is zero). 
 * 
 * 
 * 
 * @author ollie
 * @author Joren
 */
public class Granulator implements AudioProcessor  {

	public static final float ADAPTIVE_INTERP_LOW_THRESH = 0.5f;
	public static final float ADAPTIVE_INTERP_HIGH_THRESH = 2.5f;
	
	/** The position in milliseconds. */
	protected double position;
	
	/**
	 * The millisecond position increment per sample. Calculated from the ratio
	 * of the {@link AudioContext}'s sample rate and the {@link Sample}'s sample
	 * rate.
	 */
	private double positionIncrement;
	
	
	private float grainInterval;
	private float grainSize;
	private float grainRandomness;
	
	/** The time in milliseconds since the last grain was activated. */
	private float timeSinceLastGrain;

	/** The length of one sample in milliseconds. */
	private double msPerSample;

	/** The pitch, bound to the pitch envelope. */
	private float pitchFactor;
	
	/** The pitch, bound to the pitch envelope. */
	private float timeStretchFactor;

	/** The list of current grains. */
	private ArrayList<Grain> grains;

	/** A list of free grains. */
	private ArrayList<Grain> freeGrains;

	/** A list of dead grains. */
	private ArrayList<Grain> deadGrains;
	
	/** The interpolation type. */
	//protected InterpolationType interpolationType;

	/** The window used by grains. */
	private final float[] window;
	
		
	private final float[] audioBuffer;
	private int audioBufferWatermark;
	
	private final float[] outputBuffer;
	

	/**
	 * Instantiates a new GranularSamplePlayer.
	 * 
	 * @param sampleRate the sample rate.
	 * @param bufferSize the size of an output buffer.
	 */
	public Granulator(float sampleRate,int bufferSize) {
		grains = new ArrayList<Grain>();
		freeGrains = new ArrayList<Grain>();
		deadGrains = new ArrayList<Grain>();
		
		audioBuffer = new float[(int) (12*60*sampleRate)];//max 12 minutes of audio
		audioBufferWatermark = 0;
		
		pitchFactor = 1.0f;
		
		grainInterval = 40.0f;
		grainSize = 100.0f;
		grainRandomness = 0.1f;
		
		window = new be.tarsos.dsp.util.fft.CosineWindow().generateCurve(bufferSize);
		outputBuffer = new float[bufferSize];
		
		msPerSample = 1000.0f/sampleRate;
		
		positionIncrement = msPerSample;
	}

		
	public void start() {
		timeSinceLastGrain = 0;
	}
	
	
	/** Flag to indicate special case for the first grain. */
	private boolean firstGrain = true;

	/** Special case method for playing first grain. */
	private void firstGrain() {
		if(firstGrain) {
			Grain g = new Grain();
			g.position = position;
			g.age = grainSize / 4f;
			g.grainSize = grainSize;
			
			grains.add(g);
			firstGrain = false;
			timeSinceLastGrain = grainInterval / 2f;
		}
	}

	@Override
	public boolean process(AudioEvent audioEvent) {
		System.arraycopy(audioEvent.getFloatBuffer(), 0, audioBuffer,
				audioBufferWatermark, audioEvent.getBufferSize());
		audioBufferWatermark += audioEvent.getBufferSize();

		// grains.clear();
		// position = audioEvent.getTimeStamp()*1000 - 5000;

		// reset output
		Arrays.fill(outputBuffer, 0);

		firstGrain();

		int bufferSize = audioEvent.getBufferSize();

		// now loop through the buffer
		for (int i = 0; i < bufferSize; i++) {
			// determine if we need a new grain
			if (timeSinceLastGrain > grainInterval) {
				Grain g = null;
				if (freeGrains.size() > 0) {
					g = freeGrains.get(0);
					freeGrains.remove(0);
				} else {
					g = new Grain();
				}
				g.reset(grainSize, grainRandomness, position,timeStretchFactor,pitchFactor);
				grains.add(g);
				timeSinceLastGrain = 0f;
				//System.out.println(grains.size());
			}

			// gather the output from each grain
			for (int gi = 0; gi < grains.size(); gi++) {
				Grain g = grains.get(gi);
				// calculate value of grain window
				float windowScale = getValueFraction((float) (g.age / g.grainSize));
				// get position in sample for this grain
				// get the frame for this grain

				double sampleValue;
				getFrameLinear(g.position);
				if (pitchFactor > ADAPTIVE_INTERP_HIGH_THRESH) {
					sampleValue = getFrameNoInterp(g.position);
				} else if (pitchFactor > ADAPTIVE_INTERP_LOW_THRESH) {
					sampleValue = getFrameLinear(g.position);
				} else {
					sampleValue = getFrameCubic(g.position);
				}
				sampleValue = sampleValue * windowScale;
				outputBuffer[i] += (float) sampleValue;
			}
			// increment time
			position += positionIncrement * timeStretchFactor;

			for (int gi = 0; gi < grains.size(); gi++) {
				Grain g = grains.get(gi);
				calculateNextGrainPosition(g);
			}
			// increment timeSinceLastGrain
			timeSinceLastGrain += msPerSample;
			// finally, see if any grains are dead
			for (int gi = 0; gi < grains.size(); gi++) {
				Grain g = grains.get(gi);
				if (g.age > g.grainSize) {
					freeGrains.add(g);
					deadGrains.add(g);
				}
			}
			for (int gi = 0; gi < deadGrains.size(); gi++) {
				Grain g = deadGrains.get(gi);
				grains.remove(g);
			}
			deadGrains.clear();
		}
		audioEvent.setFloatBuffer(outputBuffer);

		return true;
	}
	

	/**
	 * Retrieves a frame of audio using linear interpolation. If the frame is
	 * not in the sample range then zeros are returned.
	 * 
	 * @param posInMS
	 *            The frame to read -- can be fractional (e.g., 4.4).
	 * @param result
	 *            The framedata to fill.
	 */
	public double getFrameLinear(double posInMS) {
		double result = 0.0;
		double sampleNumber = msToSamples(posInMS);
		int sampleNumberFloor = (int) Math.floor(sampleNumber);
		if (sampleNumberFloor > 0 && sampleNumberFloor < audioBufferWatermark) {
			double sampleNumberFraction = sampleNumber - sampleNumberFloor;
			if (sampleNumberFloor == audioBufferWatermark - 1) {
				result = audioBuffer[sampleNumberFloor];
			} else {
				// linear interpolation
				double current = audioBuffer[sampleNumberFloor];
				double next = audioBuffer[sampleNumberFloor];
				result = (float) ((1 - sampleNumberFraction) * current + sampleNumberFraction * next);
			} 
		}
		return result;
	}
	
	/**
	 * Retrieves a frame of audio using no interpolation. If the frame is not in
	 * the sample range then zeros are returned.
	 * 
	 * @param posInMS
	 *            The frame to read -- will take the last frame before this one.
	 *
	 */
	public float getFrameNoInterp(double posInMS) {
		double frame = msToSamples(posInMS);
		int frame_floor = (int) Math.floor(frame);
		return audioBuffer[frame_floor];
	}
	
	/**
	 * Retrieves a frame of audio using cubic interpolation. If the frame is not
	 * in the sample range then zeros are returned.
	 * 
	 * @param posInMS
	 *            The frame to read -- can be fractional (e.g., 4.4).
	 */
	public float getFrameCubic(double posInMS) {
		float frame = (float) msToSamples(posInMS);
		float result = 0.0f;
		float a0, a1, a2, a3, mu2;
		float ym1, y0, y1, y2;
	
		int realCurrentSample = (int) Math.floor(frame);
		float fractionOffset = (float) (frame - realCurrentSample);

		if (realCurrentSample >= 0 && realCurrentSample < (audioBufferWatermark - 1)) {
			realCurrentSample--;
			if (realCurrentSample < 0) {
				ym1 = audioBuffer[0];
				realCurrentSample = 0;
			} else {
				ym1 = audioBuffer[realCurrentSample++];
			}
			y0 = audioBuffer[realCurrentSample++];
			if (realCurrentSample >= audioBufferWatermark) {
				y1 = audioBuffer[audioBufferWatermark-1]; // ??
			} else {
				y1 = audioBuffer[realCurrentSample++];
			}
			if (realCurrentSample >= audioBufferWatermark) {
				y2 = audioBuffer[audioBufferWatermark-1];
			} else {
				y2 = audioBuffer[realCurrentSample++];
			}
			mu2 = fractionOffset * fractionOffset;
			a0 = y2 - y1 - ym1 + y0;
			a1 = ym1 - y0 - a0;
			a2 = y1 - ym1;
			a3 = y0;
			result = a0 * fractionOffset * mu2 + a1 * mu2 + a2 * fractionOffset + a3;
		}
		return result;
	}
	
	
	private double msToSamples(double posInMs){
		return (posInMs) / msPerSample;
	}

	@Override
	public void processingFinished() {
		
	}
	
	/**
	 * Returns the value of the buffer at the given fraction along its length (0 = start, 1 = end). Uses linear interpolation.
	 * 
	 * @param fraction the point along the buffer to inspect. 
	 * 
	 * @return the value at that point.
	 */
	public float getValueFraction(float fraction) {
		float posInBuf = fraction * window.length;
		int lowerIndex = (int)posInBuf;
		float offset = posInBuf - lowerIndex;
		int upperIndex = (lowerIndex + 1) % window.length;
		return (1 - offset) * window[lowerIndex] + offset * window[upperIndex];
	}

	/**
	 * Calculate next position for the given Grain.
	 * 
	 * @param g the Grain.
	 */
	private void calculateNextGrainPosition(Grain g) {
		int direction = timeStretchFactor >= 0 ? 1 : -1;	//this is a bit odd in the case when controlling grain from positionEnvelope
		g.age += msPerSample;
		g.position += direction * positionIncrement * pitchFactor;	
	}

	public void setTimestretchFactor(float currentFactor) {
		timeStretchFactor = currentFactor;
	}

	public void setPitchShiftFactor(float currentFactor) {
		pitchFactor = currentFactor;
	}



	public void setGrainInterval(int grainInterval) {
		this.grainInterval = grainInterval;
	}



	public void setGrainSize(int grainSize) {
		this.grainSize = grainSize;
		
	}

	public void setGrainRandomness(float grainRandomness) {
		this.grainRandomness = grainRandomness;
	}



	/**
	 * @param position in seconds
	 */
	public void setPosition(float position) {
		this.position = position * 1000;
	}
}

