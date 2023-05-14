/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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

package org.zoolu.sdp;



import java.util.Enumeration;
import java.util.Vector;



/** Class OfferAnswerModel collects some static methods for managing SDP materials
  * in accord to RFC3264 ("An Offer/Answer Model with the Session Description Protocol (SDP)").
  */
public class OfferAnswerModel
{
   /** Costructs a new SessionDescriptor from a given SessionDescriptor
     * with olny media types and attribute values specified by a MediaDescriptor Vector.
     * <p> If no attribute is specified for a particular media, all present attributes are kept.
     * <br>If no attribute is present for a selected media, the media is kept (regardless any sepcified attributes).
     * @param sdp the given SessionDescriptor
     * @param m_descs Vector of MediaDescriptor with the selecting media types and attributes
     * @return this SessionDescriptor */
   /*public static SessionDescriptor sdpMediaProduct(SessionDescriptor sdp, Vector m_descs)
   {  Vector new_media=new Vector();
      if (m_descs!=null)
      {  for (Enumeration e=m_descs.elements(); e.hasMoreElements(); )
         {  MediaDescriptor spec_md=(MediaDescriptor)e.nextElement();
            MediaDescriptor prev_md=sdp.getMediaDescriptor(spec_md.getMedia().getMedia());
            if (prev_md!=null)
            {  Vector spec_attributes=spec_md.getAttributes();
               Vector prev_attributes=prev_md.getAttributes();
               if (spec_attributes.size()==0 || prev_attributes.size()==0)
               {  new_media.addElement(prev_md);
               }
               else
               {  Vector new_attributes=new Vector();
                  for (Enumeration i=spec_attributes.elements(); i.hasMoreElements(); )
                  {  AttributeField spec_attr=(AttributeField)i.nextElement();
                     String spec_name=spec_attr.getAttributeName();
                     String spec_value=spec_attr.getAttributeValue();
                     for (Enumeration k=prev_attributes.elements(); k.hasMoreElements(); )
                     {  AttributeField prev_attr=(AttributeField)k.nextElement();
                        String prev_name=prev_attr.getAttributeName();
                        String prev_value=prev_attr.getAttributeValue();
                        if (prev_name.equals(spec_name) && prev_value.equalsIgnoreCase(spec_value))
                        {  new_attributes.addElement(prev_attr);
                           break;
                        }
                     }
                  }
                  if (new_attributes.size()>0) new_media.addElement(new MediaDescriptor(prev_md.getMedia(),prev_md.getConnection(),new_attributes));
               }
            }
         }
      }
      SessionDescriptor new_sdp=new SessionDescriptor(sdp);
      new_sdp.removeMediaDescriptors();
      new_sdp.addMediaDescriptors(new_media);
      return new_sdp;
   }*/
   
   /** Costructs a new SessionDescriptor from a given SessionDescriptor
     * with olny the first specified media attribute.
     * <p> If no attribute is present for a media, the media is dropped.
     * @param sdp the given SessionDescriptor
     * @param a_name the attribute name
     * @return this SessionDescriptor */
   /*public static SessionDescriptor sdpAttributeSelection(SessionDescriptor sdp, String a_name)
   {  Vector new_media=new Vector();
      for (Enumeration e=sdp.getMediaDescriptors().elements(); e.hasMoreElements(); )
      {  MediaDescriptor md=(MediaDescriptor)e.nextElement();
         AttributeField attr=md.getAttribute(a_name);
         if (attr!=null)
         { new_media.addElement(new MediaDescriptor(md.getMedia(),md.getConnection(),attr));
         }
      }
      SessionDescriptor new_sdp=new SessionDescriptor(sdp);
      new_sdp.removeMediaDescriptors();
      new_sdp.addMediaDescriptors(new_media);
      return new_sdp;
   }*/
   

   /** Calculates a SDP product of a starting SDP and an offered SDP.
     * <p/>
     * The product is calculated as answer of a SDP offer, according to RFC3264.
     * @param start_sdp the starting SDP (SessionDescriptor)
     * @param offer_sdp the offered SDP (SessionDescriptor)
     * @return the answered SDP (SessionDescriptor) */
   public static SessionDescriptor makeSessionDescriptorProduct(SessionDescriptor start_sdp, SessionDescriptor offer_sdp)
   {  SessionDescriptor answer_sdp=new SessionDescriptor(start_sdp);
      answer_sdp.removeMediaDescriptors();
      Vector answer_md_list=makeMediaDescriptorProduct(start_sdp.getMediaDescriptors(),offer_sdp.getMediaDescriptors());
      answer_sdp.addMediaDescriptors(answer_md_list);
      return answer_sdp;
   }


   /** Calculates a MediaDescriptor list product of a starting MediaDescriptor list
     * and an offered MediaDescriptor list.
     * <p/>
     * The product is calculated as answer of a media offer, according to RFC3264.
     * @param start_md_list the starting MediaDescriptor list (as Vector of MediaDescriptors)
     * @param offer_md_list the offered MediaDescriptor list (as Vector of MediaDescriptors)
     * @return the answered MediaDescriptor list (as Vector of MediaDescriptors) */
   public static Vector makeMediaDescriptorProduct(Vector start_md_list, Vector offer_md_list)
   {  Vector answer_md_list=new Vector();
      Vector aux_md_list=new Vector(start_md_list);
      for (int i=0; i<offer_md_list.size(); i++)
      {  MediaDescriptor offer_md=(MediaDescriptor)offer_md_list.elementAt(i);
         String media_type=offer_md.getMedia().getMedia();
         for (int j=0; j<aux_md_list.size(); j++)
         {  MediaDescriptor aux_md=(MediaDescriptor)aux_md_list.elementAt(j);
            MediaField aux_mf=aux_md.getMedia();
            if (aux_mf.getMedia().equals(media_type))
            {  /*
               // select the proper formats 
               Vector aux_ft_list=aux_mf.getFormatList();
               Vector offer_ft_list=offer_md.getMedia().getFormatList();
               Vector answer_ft_list=new Vector();
               for (int h=0; h<offer_ft_list.size(); h++)
               {  String offer_ft=(String)offer_ft_list.elementAt(h);
                  for (int k=0; k<aux_ft_list.size(); k++)
                  {  if (offer_ft.equals((String)aux_ft_list.elementAt(k)))
                     {  answer_ft_list.addElement(offer_ft);
                        break;
                     }
                  }
               }
               // select the proper attributes
               Vector aux_attr_list=aux_md.getAttributes("rtpmap");
               Vector answer_attr_list=new Vector();
               for (int h=0; h<answer_ft_list.size(); h++)
               {  String answer_ft=(String)answer_ft_list.elementAt(h);
                  for (int k=0; k<aux_attr_list.size(); k++)
                  {  AttributeField attr=(AttributeField)aux_attr_list.elementAt(k);
                     if (attr.getAttributeValue().startsWith(answer_ft))
                     {  answer_attr_list.addElement(attr);
                     }
                  }
               }
               MediaField answer_mf=new MediaField(aux_mf.getMedia(),aux_mf.getPort(),0,aux_mf.getTransport(),answer_ft_list);
               MediaDescriptor answer_md=new MediaDescriptor(answer_mf,null,answer_attr_list);
               */
               MediaDescriptor answer_md=makeMediaDescriptorProduct(aux_md,offer_md);
               answer_md_list.addElement(answer_md);
               // remove this media from the base list (actually from the aux copy), and break
               aux_md_list.removeElementAt(j);
               break;
            }
         }
      }
      return answer_md_list;
   }   


   /** Calculates a MediaDescriptor product of a given MediaDescriptor and an offered
     * MediaDescriptor.
     * <p/>
     * The result is calculated as answer of a media offer, according to RFC3264. */
   public static MediaDescriptor makeMediaDescriptorProduct(MediaDescriptor start_md, MediaDescriptor offer_md)
   {  // select the proper formats 
      MediaField start_mf=start_md.getMedia();
      Vector start_ft_list=start_mf.getFormatList();
      Vector offer_ft_list=offer_md.getMedia().getFormatList();
      Vector answer_ft_list=new Vector();
      for (int h=0; h<offer_ft_list.size(); h++)
      {  String offer_ft=(String)offer_ft_list.elementAt(h);
         for (int k=0; k<start_ft_list.size(); k++)
         {  if (offer_ft.equals((String)start_ft_list.elementAt(k)))
            {  answer_ft_list.addElement(offer_ft);
               break;
            }
         }
      }
      // select the 'rtpmap' attributes
      Vector start_attr_list=start_md.getAttributes("rtpmap");
      Vector answer_attr_list=new Vector();
      for (int h=0; h<answer_ft_list.size(); h++)
      {  String answer_ft=(String)answer_ft_list.elementAt(h);
         for (int k=0; k<start_attr_list.size(); k++)
         {  AttributeField attr=(AttributeField)start_attr_list.elementAt(k);
            if (attr.getAttributeValue().startsWith(answer_ft))
            {  answer_attr_list.addElement(attr);
            }
         }
      }
      MediaField answer_mf=new MediaField(start_mf.getMedia(),start_mf.getPort(),0,start_mf.getTransport(),answer_ft_list);
      MediaDescriptor answer_md=new MediaDescriptor(answer_mf,null,answer_attr_list);
      
      // select other attributes
      //answer_md.addAttributes(SdesOfferAnswerModel.makeCryptoAttributeProduct(start_md,offer_md));     
      
      return answer_md;
   }

}
