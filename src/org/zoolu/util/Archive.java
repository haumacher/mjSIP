/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.util;



import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.ImageIcon;



/** Collection of static methods for handling files and jar archives.
*/
public class Archive {
	
	/** The base path */
	public static String BASE_PATH=(new File("")).getAbsolutePath();



	/** Gets a URL from a String. */
	/*public static URL getURL(String url) {
		try  {
			return new URL(url);
		}
		catch (java.net.MalformedURLException e) {
			System.err.println("ERROR: malformed url "+url);
			return null;
		} 
	}*/


	/** Gets the complete URL of a file included within a jar archive. */
	public static URL getJarURL(String jar_archive, String file_name) throws java.net.MalformedURLException {
		//if (jar_archive==null || file_name==null) return null;
		//else
		String url="jar:file:"+BASE_PATH+"/"+jar_archive+"!/"+file_name;
		try {
			return new URL(url);
		}
		catch (java.net.MalformedURLException e) {
			throw new java.net.MalformedURLException("Malformed url "+url);
		}
	}


	/** Gets the complete URL of a file. */
	public static URL getFileURL(String file_name) throws java.net.MalformedURLException {
		//if (file_name==null) return null;
		//else
		String url="file:"+BASE_PATH+"/"+file_name;
		try {
			return new URL("file:"+BASE_PATH+"/"+file_name);
		}
		catch (java.net.MalformedURLException e) {
			throw new java.net.MalformedURLException("Malformed url "+url);
		}
	}


	/** Gets an Image from file */
	public static Image getImage(String file_name) throws java.io.FileNotFoundException {
		//if (file_name==null) return null;
		//else      
		Toolkit toolkit=Toolkit.getDefaultToolkit();
		file_name=BASE_PATH+"/"+file_name;
		if (!new File(file_name).canRead()) throw new java.io.FileNotFoundException("Can't read the file "+file_name);
		Image image=toolkit.getImage(file_name);
		// wait for image loading (4 attempts with 80ms of delay)
		for (int i=0; i<4 && image.getWidth(null)<0; i++) try { Thread.sleep(80); } catch (Exception e) {}
		return image;
	}
	
	
	/** Gets an Image from a URL. */
	public static Image getImage(URL url) throws java.io.IOException {
		//if (url==null) return null;
		//else        
		Toolkit toolkit=Toolkit.getDefaultToolkit();
		try {
			url.openConnection().connect();
			Image image=toolkit.getImage(url);
			// wait for image loading (4 attempts with 80ms of delay)
			for (int i=0; i<4 && image.getWidth(null)<0; i++) try { Thread.sleep(80); } catch (Exception e) {}
			return image;
		}
		catch (java.io.IOException e) {
			throw new java.io.IOException("Can't read the file "+url.toString());
		} 
	}


	/** Gets an ImageIcon from file */
	public static ImageIcon getImageIcon(String file_name) throws java.io.FileNotFoundException {
		file_name=BASE_PATH+"/"+file_name;
		if (!new File(file_name).canRead()) throw new java.io.FileNotFoundException("Can't read the file "+file_name);
		return new ImageIcon(file_name);
	}


	/** Gets an ImageIcon from an URL */
	public static ImageIcon getImageIcon(URL url) throws java.io.IOException {
		//if (url==null) return null;
		//else        
		try {
			url.openConnection().connect();
			return new ImageIcon(url);
		}
		catch (java.io.IOException e) {
			throw new java.io.IOException("Can't read the file "+url.toString());
		} 
	}


	/** Gets an InputStream from an URL */
	public static InputStream getInputStream(URL url) throws java.io.IOException {
		//if (url==null) return null;
		//else         
		try {
			return (url).openStream();
		}
		catch (java.io.IOException e) {
			throw new java.io.IOException("Can't read the file "+url.toString());
		} 
	}
	
	
	/** Gets an AudioInputStream from an URL */
	public static AudioInputStream getAudioInputStream(URL url) throws java.io.IOException, javax.sound.sampled.UnsupportedAudioFileException {
		//if (url==null) return null;
		//else         
		try {
			return AudioSystem.getAudioInputStream(url);
		}
		catch (java.io.IOException e) {
			throw new java.io.IOException("Can't read the file "+url.toString());
		} 
	}

}
