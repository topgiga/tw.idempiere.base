package tw.idempiere.base.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.StateEngine;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.wf.MWFActivity;
import org.compiere.wf.MWFProcess;

/**
 * Process to abort selected invalid/timed-out Workflow Activities.
 * Supports multi-selection from InfoWindow (via T_Selection) as well as single record invocation.
 */
public class AbortInvalidActivity extends SvrProcess {

    @Override
    protected void prepare() {
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null)
                ;
            else
                log.log(Level.SEVERE, "Unknown Parameter: " + name);
        }
    }

    @Override
    protected String doIt() throws Exception {
        List<Integer> activityIds = getSelectedActivityIds();

        if (activityIds.isEmpty()) {
            return "@NoSelection@";
        }

        int count = 0;
        int errCount = 0;

        for (int activityId : activityIds) {
            try {
                MWFActivity activity = new MWFActivity(getCtx(), activityId, get_TrxName());
                if (activity.get_ID() <= 0) {
                    continue;
                }

                if (!activity.isProcessed()) {
                    activity.setWFState(StateEngine.STATE_Aborted);
                    activity.setProcessed(true);
                    activity.saveEx(get_TrxName());

                    int processId = activity.getAD_WF_Process_ID();
                    if (processId > 0) {
                        MWFProcess wfProcess = new MWFProcess(getCtx(), processId, get_TrxName());
                        if (wfProcess.get_ID() > 0 && !wfProcess.isProcessed()) {
                            wfProcess.setWFState(StateEngine.STATE_Aborted);
                            wfProcess.setProcessed(true);
                            wfProcess.saveEx(get_TrxName());
                        }
                    }
                    count++;
                }
            } catch (Exception e) {
                errCount++;
                log.log(Level.SEVERE, "Error aborting activity ID: " + activityId, e);
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Aborted ").append(count).append(" Activity(ies).");
        if (errCount > 0) {
            msg.append(" Errors: ").append(errCount).append(".");
        }
        return msg.toString();
    }

    /**
     * Get selected AD_WF_Activity_IDs from T_Selection (InfoWindow multi-selection)
     * or fallback to getRecord_ID() for single record selection.
     */
    private List<Integer> getSelectedActivityIds() {
        List<Integer> list = new ArrayList<>();

        // Check InfoWindow multi-selection table T_Selection
        String sql = "SELECT T_Selection_ID FROM T_Selection WHERE AD_PInstance_ID = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = DB.prepareStatement(sql, get_TrxName());
            pstmt.setInt(1, getAD_PInstance_ID());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                if (id > 0) {
                    list.add(id);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error reading T_Selection", e);
        } finally {
            DB.close(rs, pstmt);
        }

        // Fallback to single record selection if T_Selection is empty
        if (list.isEmpty()) {
            int recordId = getRecord_ID();
            if (recordId > 0) {
                list.add(recordId);
            }
        }

        return list;
    }
}
