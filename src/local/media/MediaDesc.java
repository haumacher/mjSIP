package local.media;



import org.zoolu.sdp.*;
import org.zoolu.tools.Parser;

import java.util.Vector;



/** Media description.
  */
public class MediaDesc
{

   /** Media type (e.g. audio, video, message, etc.) */
   String media;

   /** Port */
   int port;

   /** Transport */
   String transport;
   
   /** Vector of media specifications */
   Vector specs;


   
   /** Creates a new MediaDesc.
     * @param media Media type
     * @param port Port
     * @param transport Transport protocol */
   public MediaDesc(String media, int port, String transport)
   {  init(media,port,transport,specs);
   }


   /** Creates a new MediaDesc.
     * @param media Media type
     * @param port Port
     * @param transport Transport protocol
     * @param specs Vector of media specifications (MediaSpec) */
   public MediaDesc(String media, int port, String transport, Vector specs)
   {  init(media,port,transport,specs);
   }


   /** Creates a new MediaDesc.
     * @param md MediaDescriptor (org.zoolu.tools.sdp.MediaDescriptor) used to create this MediaDesc */
   public MediaDesc(MediaDescriptor md)
   {  MediaField mf=md.getMedia();
      String media=mf.getMedia();
      int port=mf.getPort();
      String transport=mf.getTransport();

      Vector attributes=md.getAttributes("rtpmap");
      Vector specs=new Vector(attributes.size());
      for (int i=0; i<attributes.size(); i++)
      {  Parser par=new Parser(((AttributeField)attributes.elementAt(i)).getAttributeValue());
         int avp=par.getInt();
         String codec=null;
         int sample_rate=0;
         if (par.skipChar().hasMore())
         {  char[] delim={'/'};
            codec=par.getWord(delim);
            sample_rate=Integer.parseInt(par.skipChar().getWord(delim));
         }
         specs.addElement(new MediaSpec(media,avp,codec,sample_rate,0));
      }
      init(media,port,transport,specs);
   }


   /** Inits the MediaDesc. */
   private void init(String media, int port, String transport, Vector specs)
   {  this.media=media;
      this.port=port;
      this.transport=transport;
      this.specs=specs;
   }


   /** Gets media type. */
   public String getMedia()
   {  return media;
   }


   /** Gets port. */
   public int getPort()
   {  return port;
   }


   /** Sets port. */
   public void setPort(int port)
   {  this.port=port;
   }


   /** Gets transport protocol. */
   public String getTransport()
   {  return transport;
   }


   /** Gets media specifications. */
   public Vector getMediaSpecs()
   {  return specs;
   }


   /** Sets media specifications. */
   public void setMediaSpecs(Vector media_specs)
   {  this.specs=media_specs;
   }


   /** Adds a new media specification. */
   public void addMediaSpec(MediaSpec media_spec)
   {  if (specs==null) specs=new Vector();
      specs.addElement(media_spec);
   }


   /** Gets the corresponding MediaDescriptor. */
   public MediaDescriptor toMediaDescriptor()
   {  Vector formats=new Vector();
      Vector attributes=new Vector();
      if (specs!=null)
      {  for (int i=0; i<specs.size(); i++)
         {  MediaSpec ms=(MediaSpec)specs.elementAt(i);
            int avp=ms.getAVP();
            String codec=ms.getCodec();
            int rate=ms.getSampleRate(); 
            formats.addElement(String.valueOf(avp));
            attributes.addElement(new AttributeField("rtpmap",String.valueOf(avp)+((codec!=null && rate>0)? " "+codec+"/"+rate : "")));
         }
      }
      return new MediaDescriptor(new MediaField(media,port,0,transport,formats),null,attributes);
   }


   /** Gets a String representation of this object. */
   public String toString()
   {  StringBuffer sb=new StringBuffer();
      sb.append(media).append(" ").append(port).append(" ").append(transport);
      if (specs!=null)
      {  sb.append(" {");
         for (int i=0; i<specs.size(); i++)
         {  if (i>0) sb.append(",");
            sb.append(" ").append(((MediaSpec)specs.elementAt(i)).toString());
         }
         sb.append(" }");
      }
      return sb.toString();
   }


   /** Gets a String compact representation of this object (only media, port, and transport are included). */
   /*public String toStringCompact()
   {  StringBuffer sb=new StringBuffer();
      sb.append(media).append(" ").append(port).append(" ").append(transport);
      return sb.toString();
   }*/


   /** Parses a String and gets a new MediaDesc. */
   public static MediaDesc parseMediaDesc(String str)
   {  //System.out.println("MediaDesc: parsing: "+str);
      Parser par=new Parser(str);
      String media=par.getString();
      int port=par.getInt();
      String transport=par.getString();
      Vector specs=new Vector();
      if (par.goTo("{").hasMore())
      {  par.skipChar();
         int len=par.indexOf("}")-par.getPos();
         if (len>0)
         {  par=new Parser(par.getString(len));
            char[] delim={ ';', ',' };
            while (par.skipWSP().hasMore())
            {  str=par.getWord(delim);
               if (str!=null && str.length()>0) specs.addElement(MediaSpec.parseMediaSpec(str));
            }
         }
      }
      return new MediaDesc(media,port,transport,specs);
   }

}
