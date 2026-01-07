package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;
public class MWorkflowAbstractMessage extends X_AD_WorkflowAbstractMessage {

	@Override
	protected boolean beforeSave(boolean newRecord) {
		log.log(Level.SEVERE, "before save works");
		return super.beforeSave(newRecord);
	}

	private static final long serialVersionUID = 1L;

	public MWorkflowAbstractMessage(Properties ctx, int AD_WorkflowAbstractMessage_ID, String trxName,
			String... virtualColumns) {
		super(ctx, AD_WorkflowAbstractMessage_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

	public MWorkflowAbstractMessage(Properties ctx, int AD_WorkflowAbstractMessage_ID, String trxName) {
		super(ctx, AD_WorkflowAbstractMessage_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MWorkflowAbstractMessage(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MWorkflowAbstractMessage(Properties ctx, String AD_WorkflowAbstractMessage_UU, String trxName,
			String... virtualColumns) {
		super(ctx, AD_WorkflowAbstractMessage_UU, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

	public MWorkflowAbstractMessage(Properties ctx, String AD_WorkflowAbstractMessage_UU, String trxName) {
		super(ctx, AD_WorkflowAbstractMessage_UU, trxName);
		// TODO Auto-generated constructor stub
	}

	public static MWorkflowAbstractMessage get(Properties ctx, int AD_Table_ID, int Record_ID, String trxName) {
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(COLUMNNAME_AD_Table_ID).append("=? AND ").append(COLUMNNAME_Record_ID).append("=?");

		MWorkflowAbstractMessage retValue = new Query(ctx, Table_Name, whereClause.toString(), trxName)
				.setParameters(AD_Table_ID, Record_ID).first();

		if (retValue == null) {
			retValue = new MWorkflowAbstractMessage(ctx, 0, trxName);
			retValue.setAD_Table_ID(AD_Table_ID);
			retValue.setRecord_ID(Record_ID);
		}

		return retValue;
	}

}
