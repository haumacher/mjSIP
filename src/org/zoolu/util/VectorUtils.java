package org.zoolu.util;


import java.util.Vector;


/** Collects some utilities for handling java vectors and arrays.
 * It has been added for compatibility with Java ME.
 */
public class VectorUtils {

	/** Converts an array into a vector.
	 * @param array the array
	 * @return a vector containing all elements of array */
	public static Vector arrayToVector(Object[] array) {
		Vector v=new Vector();
		for (int i=0; i<array.length; i++) v.add(array[i]);
		return v;
	}

	/** Adds a array to a vector.
	 * @param v vector to be filled (destination)
	 * @param a the array to be added (source) */
	public static void addArray(Vector v, Object[] a) {
		for (int i=0; i<a.length; i++) v.add(a[i]);
	}

	/** Adds a vector to a vector.
	 * @param v vector to be filled (destination)
	 * @param a the vector to be added (source) */
	public static void addVector(Vector v, Vector a) {
		for (int i=0; i<a.size(); i++) v.add(a.get(i));
	}

	/** Copies a vector into an array.
	 * @param v the vector
	 * @param array the array to be filled
	 * @return the array */
	public static Object[] vectorToArray(Vector v, Object[] array) {
		if (v.size()!=array.length) throw new RuntimeException("Array length differs from the vector size ("+array.length+"!="+v.size()+")");
		for (int i=0; i<array.length; i++) array[i]=v.get(i);
		return array;
	}

	/** Copy a vector.
	 * @param v the vector to be copied
	 * @return a vector containing all elements of the given vector */
	public static Vector copy(Vector v) {
		Vector v2=new Vector();
		for (int i=0; i<v.size(); i++) v2.add(v.get(i));
		return v2;
	}

}
