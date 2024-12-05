package org.mjsip.server;


import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoolu.util.DateFormat;


/** CallLoggerImpl implements a simple CallLogger.
  * <p> A CallLogger keeps trace of all processed calls.
  */
public class CallLoggerImpl implements CallLogger {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CallLoggerImpl.class);

	/** Maximum number of concurrent calls. */
	static final int MAX_SIZE=10000;

	/** Table : (String)call_id --> (Long)invite date. */
	Hashtable<String, Date> invite_dates;
	/** Table : (String)call_id --> (Long)2xx date. */
	Hashtable<String, Date> accepted_dates;
	/** Table : (String)call_id --> (Long)4xx date. */
	Hashtable<String, Date> refused_dates;
	/** Table : (String)call_id --> (Long)bye date. */
	Hashtable<String, Date> bye_dates;
	
	/** Table : (String)call_id --> (String)caller. */
	Hashtable<String, String> callers;
	/** Table : (String)call_id --> (String)callee. */
	Hashtable<String, String> callees;

	/** Set : (String)call_id. */
	Vector<String> calls;

	/** 
	 * Costructs a new CallLoggerImpl.
	 */
	public CallLoggerImpl(String filename) {
		invite_dates=new Hashtable<>();
		accepted_dates=new Hashtable<>();
		refused_dates=new Hashtable<>();
		bye_dates=new Hashtable<>();
		calls=new Vector<>();
		callers=new Hashtable<>();
		callees=new Hashtable<>();
		
		LOG.info("Date \tCall-Id \tStatus \tCaller \tCallee \tSetup Time \tCall Time");
	}
 
	
	/** Updates log with the present message.
	  */
	@Override
	public void update(SipMessage msg) {
		
		String method=msg.getCSeqHeader().getMethod();
		String call_id=msg.getCallIdHeader().getCallId();

		if (method.equalsIgnoreCase(SipMethods.INVITE)) {
			
			if (msg.isRequest()) {
				if (!invite_dates.containsKey(call_id)) {
					Date time=new Date();
					String caller=msg.getFromHeader().getNameAddress().getAddress().toString();
					String callee=msg.getToHeader().getNameAddress().getAddress().toString();
					insert(invite_dates,call_id,time);
					callers.put(call_id,caller);
					callees.put(call_id,callee);
					eventlog(time,call_id,SipMethods.INVITE,caller,callee);
				}
			}
			else {
				StatusLine status_line=msg.getStatusLine();
				int code=status_line.getCode();
				if (code>=200 && code<300 && !accepted_dates.containsKey(call_id)) {
					Date time=new Date();
					insert(accepted_dates,call_id,time);
					String reason=status_line.getReason();     
					eventlog(time,call_id,String.valueOf(code)+" "+reason,"","");
				}
				else
				if (code>=300 && !refused_dates.containsKey(call_id)) {
					Date time=new Date();
					insert(refused_dates,call_id,time);
					String reason=status_line.getReason();     
					eventlog(time,call_id,String.valueOf(code)+" "+reason,"","");
				}
			}
		}
		else
		if (method.equalsIgnoreCase(SipMethods.BYE)) {
			
			if (msg.isRequest()) {
				if (!bye_dates.containsKey(call_id)) {
					Date time=new Date();
					insert(bye_dates,call_id,time);
					eventlog(time,call_id,SipMethods.BYE,"","");
					calllog(call_id);
				}
			}      
		}
	}


	/** Insters/updates a call-state table.
	  */
	private void insert(Hashtable<String, Date> table, String call_id, Date time) {
		if (!invite_dates.containsKey(call_id) && !accepted_dates.containsKey(call_id) && !refused_dates.containsKey(call_id) && !bye_dates.containsKey(call_id)); {
			if (calls.size()>=MAX_SIZE)  {
				String call_0=calls.elementAt(0);
				invite_dates.remove(call_0);
				accepted_dates.remove(call_0);
				refused_dates.remove(call_0);
				bye_dates.remove(call_0);
				callers.remove(call_0);
				callees.remove(call_0);
				calls.removeElementAt(0);
			}
			calls.addElement(call_id);
		}
		table.put(call_id,time);
	}


	/** 
	 * Prints a generic event log.
	 * 
	 * FIXME mybe this function partially replicates things {@link Logger} does
	 */
	private void eventlog(Date time, String call_id, String event, String caller, String callee) {
		//call_logger.log(DateFormat.formatHHMMSS(time)+"\t"+call_id+"\t"+event+"\t"+caller+"\t"+callee);
		LOG.info("{}\t{}\t{}\t{}\t{]", DateFormat.formatYyyyMMddHHmmssSSS(time), call_id, event, caller, callee);
	}


	/** 
	 * Prints a call report.
	 */
	private void calllog(String call_id) {
		Date invite_time=invite_dates.get(call_id);
		Date accepted_time=accepted_dates.get(call_id);
		Date bye_time=bye_dates.get(call_id);
		if (invite_time!=null && accepted_time!=null && bye_time!=null) 
			//call_logger.log(DateFormat.formatHHMMSS(invite_time)+"\t"+call_id+"\tCALL \t"+callers.get(call_id)+"\t"+callees.get(call_id)+"\t"+(accepted_time.getTime()-invite_time.getTime())+"\t"+(bye_time.getTime()-accepted_time.getTime()));
			LOG.info("{}\t{}\tCALL \t{}\t{}\t{}\t{}",
					DateFormat.formatYyyyMMddHHmmssSSS(invite_time), call_id, callers.get(call_id), callees.get(call_id),
					(accepted_time.getTime() - invite_time.getTime()),
					(bye_time.getTime() - accepted_time.getTime())
				);
	}

}
