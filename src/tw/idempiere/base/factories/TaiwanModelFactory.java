package tw.idempiere.base.factories;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.MWorkflowAbstractMessage;
import org.compiere.model.PO;
import org.compiere.util.Env;

import tw.idempiere.base.model.MTwWFActivity;

public class TaiwanModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		if (tableName.equals(MWorkflowAbstractMessage.Table_Name))
			return MWorkflowAbstractMessage.class;
		if (tableName.equals("AD_WF_Activity"))
			return tw.idempiere.base.model.MTwWFActivity.class;
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		if (tableName.equals(MWorkflowAbstractMessage.Table_Name))
			return new MWorkflowAbstractMessage(Env.getCtx(), Record_ID, trxName);
		if (tableName.equals(MTwWFActivity.Table_Name))
			return new tw.idempiere.base.model.MTwWFActivity(Env.getCtx(), Record_ID, trxName);
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		if (tableName.equals(MWorkflowAbstractMessage.Table_Name))
			return new MWorkflowAbstractMessage(Env.getCtx(), rs, trxName);
		if (tableName.equals(MTwWFActivity.Table_Name))
			return new tw.idempiere.base.model.MTwWFActivity(Env.getCtx(), rs, trxName);
		return null;
	}

}
