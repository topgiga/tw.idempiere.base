package tw.idempiere.base.factories;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.MWorkflowAbstractMessage;
import org.compiere.model.PO;
import org.compiere.util.Env;

public class TaiwanModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		if(tableName.equals(MWorkflowAbstractMessage.Table_Name)) 
			return MWorkflowAbstractMessage.class;
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		if(tableName.equals(MWorkflowAbstractMessage.Table_Name)) 
			return new MWorkflowAbstractMessage(Env.getCtx(),Record_ID,trxName);
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		if(tableName.equals(MWorkflowAbstractMessage.Table_Name)) 
			return new MWorkflowAbstractMessage(Env.getCtx(),rs,trxName);
		return null;
	}

}
