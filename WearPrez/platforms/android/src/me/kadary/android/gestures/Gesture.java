/*
 * Gesture.java
 *
 * Created: 18.08.2011
 *
 * Copyright (C) 2011 Robert Nesselrath
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package me.kadary.android.gestures;

import java.io.Serializable;
import java.util.List;

public class Gesture implements Serializable {

	private static final long serialVersionUID = 7148492971634218981L;
	private String label;
	private List<float[]> values;

	public Gesture(List<float[]> values, String label) {
		setValues(values);
		setLabel(label);
	}

	public void setValues(List<float[]> values) {
		this.values = values;
	}

	public String getLabel() {
		return label;
	}

	public List<float[]> getValues() {
		return values;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public float getValue(int index, int dim) {
		return values.get(index)[dim];
	}

	public void setValue(int index, int dim, float f) {
		values.get(index)[dim] = f;
	}

	public int length() {
		return values.size();
	}
}
