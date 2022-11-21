package be.tarsos.dsp.dbLevel;
    
    public class DbLevelDetection
    {
    /**
	 * Calculates the local (linear) energy of an audio buffer.
	 * 
	 * @param buffer
	 *            The audio buffer.
	 * @return The local (linear) energy of an audio buffer.
	 */
	private double localEnergy(final float[] buffer) {
		double power = 0.0D;
		for (float element : buffer) {
			power += element * element;
		}
		return power;
	}

	/**
	 * Returns the dBSPL for a buffer.
	 *  SOD : think this is the db Full scale value rather than the 
	 *  SOund pressure level
	 * @param buffer
	 *            The buffer with audio information.
	 * @return The dBSPL level for the buffer.
	 */
	public double getDbLevel(final float[] buffer) {
		double value = Math.pow(localEnergy(buffer), 0.5);
		value = value / buffer.length;
		return linearToDecibel(value);
	}

	/**
	 * Converts a linear to a dB value.
	 * 
	 * @param value
	 *            The value to convert.
	 * @return The converted value.
	 */
	private double linearToDecibel(final double value) {
		return 20.0 * Math.log10(value);
	}

}
