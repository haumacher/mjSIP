package org.mjsip.phone.gui;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Vector;

import org.slf4j.LoggerFactory;
import org.zoolu.util.Configure;
import org.zoolu.util.Parser;


/** Classs StringList handles a vector of Strings.
  * It can be used to load, manage, and save string values. 
  */
final class StringList extends Configure {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(StringList.class);

	/** The list */
	Vector<String> list;

	/** File name */
	File file=null;

	/** File URL */
	URL file_url=null;


	/** Costructs a new StringList from the specified <i>file</i> */
	public StringList(File file) {
		list=new Vector<>();
		this.file=file;
		load();
	}
	

	/** Costructs a new StringList from the specified URL <i>url</i> */
	public StringList(URL url) {
		list=new Vector<>();
		file_url=url;
		load();
	}
	

	/** Loads list */
	public void load() {
		if (file!=null) loadFile(file);
		else
		if (file_url!=null) loadFile(file_url);
	}

	
	/** Saves list */
	public void save() {
		if (file!=null) saveFile(file);
	}

	/** Saves Configure attributes on the specified <i>file</i> */
	protected void saveFile(File file) {
		if (file==null) return;
		//else
		try {
			writeTo(new FileWriter(file));
		}
		catch (IOException e) {
			LOG.error("Failed writing file ({})", file, e);
		}         
	}

	/** Writes Configure attributes to the specified Writer <i>wr</i> */
	protected void writeTo(Writer wr) throws java.io.IOException {
		BufferedWriter out=new BufferedWriter(wr);
		out.write(toLines());
		out.close();
	}

	/** Gets elements */
	public Vector<String> getElements() {
		return list;
	}


	/** Gets the element at positon i */
	public String elementAt(int i) {
		return list.elementAt(i);
	}


	/** Inserts element at positon i */
	public void insertElementAt(String elem, int i) {
		list.insertElementAt(elem,i);
	}


	/** Removes element at positon i */
	public void removeElementAt(int i) {
		list.removeElementAt(i);
	}


	/** Adds element */
	public void addElement(String elem) {
		list.addElement(elem);
	}


	/** Whether the element is present */
	public boolean contains(String elem) {
		return (indexOf(elem)>=0);
	}


	/** Index of the element (if present) */
	public int indexOf(String elem) {
		return list.indexOf(elem);
	}


	/** Whether an element that containg <i>subelem</i>*/
	/*public boolean containsSubElement(String subelem) {
		return indexOfSubElement(subelem)>=0;
	}*/


	/** Whether an element that containg <i>subelem</i>*/
	/*public int indexOfSubElement(String subelem) {
		for (int i=0; i<list.size(); i++)  {
			String elem=(String)list.elementAt(i);
			int index=elem.indexOf(subelem);
			if (index>=0 && index<elem.length()) return i;
		}
		return -1;
	}*/
	
		 
	/** Parses a single line (loaded from the config file) */
	@Override
	protected void parseLine(String line) {
		list.addElement(line);
	}

	@Override
	public void setOption(String attribute, Parser par) {
		throw new UnsupportedOperationException();
	}

	/** Converts the entire object into lines (to be saved into the config file) */
	protected String toLines() {
		StringBuilder str= new StringBuilder();
		for (int i=0; i<list.size(); i++)      {
			String elem=list.elementAt(i);
			str.append(elem).append("\n");
		}
		return str.toString();
	}   
}
