/**
*
*  TarsosDSP is developed by Joren Six at 
*  The Royal Academy of Fine Arts & Royal Conservatory,
*  University College Ghent,
*  Hoogpoort 64, 9000 Ghent - Belgium
*  
*  http://tarsos.0110.be/tag/TarsosDSP
*
**/
/*
 *  Copyright (c) 2007 - 2008 by Damien Di Fede <ddf@compartmental.net>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package be.hogent.tarsos.dsp.filters;

/**
 * Four stage low pass filter.
 *
 */
public class LowPassFS extends IIRFilter{
	
	public LowPassFS(float freq, float sampleRate) {
		//minimum frequency is 60Hz!
		super(freq>60?freq:60, sampleRate);
	}

	@Override
	protected void calcCoeff() {
		float freqFrac = getFrequency() / getSampleRate();
		float x = (float) Math.exp(-14.445 * freqFrac);
		a = new float[] { (float) Math.pow(1 - x, 4) };
		b = new float[] { 4 * x, -6 * x * x, 4 * x * x * x, -x * x * x * x };
	}


}
