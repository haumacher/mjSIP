/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * Options for SIP a command-line tool or user interface.
 */
public class UIConfig extends Configure {

	/** Whether unregistering the contact address */
	public boolean doUnregister=false;
	/** Whether unregistering all contacts beafore registering the contact address */
	public boolean doUnregisterAll=false;

	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static UIConfig init(String file, Flags flags) {
		UIConfig result=new UIConfig();
		result.loadFile(file);
		result.updateWith(flags);
		return result;
	}

	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("do_unregister"))  {  doUnregister=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("do_unregister_all")) {  doUnregisterAll=(par.getString().toLowerCase().startsWith("y"));  return;  }
	}

	/**
	 * Adds settings read from command line arguments.
	 */
	protected void updateWith(Flags flags) {
		Boolean unregist=flags.getBoolean("-u",null,"unregisters the contact address with the registrar server (the same as -g 0)");
		if (unregist!=null) this.doUnregister=unregist.booleanValue();
		
		Boolean unregist_all=flags.getBoolean("-z",null,"unregisters ALL contact addresses");
		if (unregist_all!=null) this.doUnregisterAll=unregist_all.booleanValue();
	}		

}
