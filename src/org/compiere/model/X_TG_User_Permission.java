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

/** Generated Model for TG_User_Permission
 *  @author iDempiere (generated)
 *  @version Release 11 - $Id$ */
@org.adempiere.base.Model(table="TG_User_Permission")
public class X_TG_User_Permission extends PO implements I_TG_User_Permission, I_Persistent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20260121L;

    /** Standard Constructor */
    public X_TG_User_Permission (Properties ctx, int TG_User_Permission_ID, String trxName)
    {
      super (ctx, TG_User_Permission_ID, trxName);
      /** if (TG_User_Permission_ID == 0)
        {
			setName (null);
			setTG_User_Permission_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_TG_User_Permission (Properties ctx, int TG_User_Permission_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, TG_User_Permission_ID, trxName, virtualColumns);
      /** if (TG_User_Permission_ID == 0)
        {
			setName (null);
			setTG_User_Permission_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_TG_User_Permission (Properties ctx, String TG_User_Permission_UU, String trxName)
    {
      super (ctx, TG_User_Permission_UU, trxName);
      /** if (TG_User_Permission_UU == null)
        {
			setName (null);
			setTG_User_Permission_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_TG_User_Permission (Properties ctx, String TG_User_Permission_UU, String trxName, String ... virtualColumns)
    {
      super (ctx, TG_User_Permission_UU, trxName, virtualColumns);
      /** if (TG_User_Permission_UU == null)
        {
			setName (null);
			setTG_User_Permission_ID (0);
        } */
    }

    /** Load Constructor */
    public X_TG_User_Permission (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_TG_User_Permission[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_Role getAD_Role() throws RuntimeException
	{
		return (org.compiere.model.I_AD_Role)MTable.get(getCtx(), org.compiere.model.I_AD_Role.Table_ID)
			.getPO(getAD_Role_ID(), get_TrxName());
	}

	/** Set Role.
		@param AD_Role_ID Responsibility Role
	*/
	public void setAD_Role_ID (int AD_Role_ID)
	{
		if (AD_Role_ID < 0)
			set_Value (COLUMNNAME_AD_Role_ID, null);
		else
			set_Value (COLUMNNAME_AD_Role_ID, Integer.valueOf(AD_Role_ID));
	}

	/** Get Role.
		@return Responsibility Role
	  */
	public int getAD_Role_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Role_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_User getAD_User() throws RuntimeException
	{
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_ID)
			.getPO(getAD_User_ID(), get_TrxName());
	}

	/** Set User/Contact.
		@param AD_User_ID User within the system - Internal or Business Partner Contact
	*/
	public void setAD_User_ID (int AD_User_ID)
	{
		if (AD_User_ID < 1)
			set_Value (COLUMNNAME_AD_User_ID, null);
		else
			set_Value (COLUMNNAME_AD_User_ID, Integer.valueOf(AD_User_ID));
	}

	/** Get User/Contact.
		@return User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description Optional short description of the record
	*/
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** ASI_EDIT = ASI_EDIT */
	public static final String NAME_ASI_EDIT = "ASI_EDIT";
	/** Set Name.
		@param Name Alphanumeric identifier of the entity
	*/
	public void setName (String Name)
	{

		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName()
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	/** Set TG_User_Permission.
		@param TG_User_Permission_ID TG_User_Permission
	*/
	public void setTG_User_Permission_ID (int TG_User_Permission_ID)
	{
		if (TG_User_Permission_ID < 1)
			set_ValueNoCheck (COLUMNNAME_TG_User_Permission_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_TG_User_Permission_ID, Integer.valueOf(TG_User_Permission_ID));
	}

	/** Get TG_User_Permission.
		@return TG_User_Permission	  */
	public int getTG_User_Permission_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_TG_User_Permission_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set TG_User_Permission_UU.
		@param TG_User_Permission_UU TG_User_Permission_UU
	*/
	public void setTG_User_Permission_UU (String TG_User_Permission_UU)
	{
		set_Value (COLUMNNAME_TG_User_Permission_UU, TG_User_Permission_UU);
	}

	/** Get TG_User_Permission_UU.
		@return TG_User_Permission_UU	  */
	public String getTG_User_Permission_UU()
	{
		return (String)get_Value(COLUMNNAME_TG_User_Permission_UU);
	}
}