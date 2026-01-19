package tw.idempiere.base.factories;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import tw.idempiere.base.process.ImportCustomsRate;
import tw.idempiere.base.process.ImportRealTimeRate;

public class TaiwanProcessFactory implements IProcessFactory {

    @Override
    public ProcessCall newProcessInstance(String className) {
        
        if (className.equals(ImportRealTimeRate.class.getName()))
            return new ImportRealTimeRate();
        if (className.equals(ImportCustomsRate.class.getName()))
            return new ImportCustomsRate();

        return null;
    }

}
