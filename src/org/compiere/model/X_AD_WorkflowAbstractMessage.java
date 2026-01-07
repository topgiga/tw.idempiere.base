/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for AD_WorkflowAbstractMessage
 *  @author iDempiere (generated)
 *  @version Release 11 - $Id$ */
@org.adempiere.base.Model(table="AD_WorkflowAbstractMessage")
public class X_AD_WorkflowAbstractMessage extends PO implements I_AD_WorkflowAbstractMessage, I_Persistent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20260106L;

    /** Standard Constructor */
    public X_AD_WorkflowAbstractMessage (Properties ctx, int AD_WorkflowAbstractMessage_ID, String trxName)
    {
      super (ctx, AD_WorkflowAbstractMessage_ID, trxName);
      /** if (AD_WorkflowAbstractMessage_ID == 0)
        {
			setAD_Table_ID (0);
			setAD_WorkflowAbstractMessage_ID (0);
			setRecord_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_AD_WorkflowAbstractMessage (Properties ctx, int AD_WorkflowAbstractMessage_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, AD_WorkflowAbstractMessage_ID, trxName, virtualColumns);
      /** if (AD_WorkflowAbstractMessage_ID == 0)
        {
			setAD_Table_ID (0);
			setAD_WorkflowAbstractMessage_ID (0);
			setRecord_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_AD_WorkflowAbstractMessage (Properties ctx, String AD_WorkflowAbstractMessage_UU, String trxName)
    {
      super (ctx, AD_WorkflowAbstractMessage_UU, trxName);
      /** if (AD_WorkflowAbstractMessage_UU == null)
        {
			setAD_Table_ID (0);
			setAD_WorkflowAbstractMessage_ID (0);
			setRecord_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_AD_WorkflowAbstractMessage (Properties ctx, String AD_WorkflowAbstractMessage_UU, String trxName, String ... virtualColumns)
    {
      super (ctx, AD_WorkflowAbstractMessage_UU, trxName, virtualColumns);
      /** if (AD_WorkflowAbstractMessage_UU == null)
        {
			setAD_Table_ID (0);
			setAD_WorkflowAbstractMessage_ID (0);
			setRecord_ID (0);
        } */
    }

    /** Load Constructor */
    public X_AD_WorkflowAbstractMessage (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder ("X_AD_WorkflowAbstractMessage[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException
	{
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_ID)
			.getPO(getAD_Table_ID(), get_TrxName());
	}

	/** Set Table.
		@param AD_Table_ID Database Table information
	*/
	public void setAD_Table_ID (int AD_Table_ID)
	{
		if (AD_Table_ID < 1)
			set_Value (COLUMNNAME_AD_Table_ID, null);
		else
			set_Value (COLUMNNAME_AD_Table_ID, Integer.valueOf(AD_Table_ID));
	}

	/** Get Table.
		@return Database Table information
	  */
	public int getAD_Table_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set AD_WorkflowAbstractMessage.
		@param AD_WorkflowAbstractMessage_ID AD_WorkflowAbstractMessage
	*/
	public void setAD_WorkflowAbstractMessage_ID (int AD_WorkflowAbstractMessage_ID)
	{
		if (AD_WorkflowAbstractMessage_ID < 1)
			set_ValueNoCheck (COLUMNNAME_AD_WorkflowAbstractMessage_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_AD_WorkflowAbstractMessage_ID, Integer.valueOf(AD_WorkflowAbstractMessage_ID));
	}

	/** Get AD_WorkflowAbstractMessage.
		@return AD_WorkflowAbstractMessage	  */
	public int getAD_WorkflowAbstractMessage_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_WorkflowAbstractMessage_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set AD_WorkflowAbstractMessage_UU.
		@param AD_WorkflowAbstractMessage_UU AD_WorkflowAbstractMessage_UU
	*/
	public void setAD_WorkflowAbstractMessage_UU (String AD_WorkflowAbstractMessage_UU)
	{
		set_ValueNoCheck (COLUMNNAME_AD_WorkflowAbstractMessage_UU, AD_WorkflowAbstractMessage_UU);
	}

	/** Get AD_WorkflowAbstractMessage_UU.
		@return AD_WorkflowAbstractMessage_UU	  */
	public String getAD_WorkflowAbstractMessage_UU()
	{
		return (String)get_Value(COLUMNNAME_AD_WorkflowAbstractMessage_UU);
	}

	/** Set Abstract Message.
		@param AbstractMessage Abstract Message
	*/
	public void setAbstractMessage (String AbstractMessage)
	{
		set_Value (COLUMNNAME_AbstractMessage, AbstractMessage);
	}

	/** Get Abstract Message.
		@return Abstract Message	  */
	public String getAbstractMessage()
	{
		return (String)get_Value(COLUMNNAME_AbstractMessage);
	}

	/** Set Record ID.
		@param Record_ID Direct internal record ID
	*/
	public void setRecord_ID (int Record_ID)
	{
		if (Record_ID < 0)
			set_ValueNoCheck (COLUMNNAME_Record_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_Record_ID, Integer.valueOf(Record_ID));
	}

	/** Get Record ID.
		@return Direct internal record ID
	  */
	public int getRecord_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Record_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}