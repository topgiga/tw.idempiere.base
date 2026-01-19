package tw.idempiere.base.factories;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import tw.idempiere.base.process.CreateAbstractMessageColumn;
import tw.idempiere.base.process.ImportRealTimeRate;

public class TaiwanProcessFactory implements IProcessFactory {

    @Override
    public ProcessCall newProcessInstance(String className) {
        if (className.equals(CreateAbstractMessageColumn.class.getName()))
            return new CreateAbstractMessageColumn();
        
        if (className.equals(ImportRealTimeRate.class.getName()))
            return new ImportRealTimeRate();

        return null;
    }

}
