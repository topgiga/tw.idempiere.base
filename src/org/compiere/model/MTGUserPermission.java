package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.Env;

public class MTGUserPermission extends X_TG_User_Permission {

	private static final long serialVersionUID = 1L;

	public MTGUserPermission(Properties ctx, int TG_User_Permission_ID, String trxName, String... virtualColumns) {
		super(ctx, TG_User_Permission_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

	public MTGUserPermission(Properties ctx, int TG_User_Permission_ID, String trxName) {
		super(ctx, TG_User_Permission_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MTGUserPermission(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MTGUserPermission(Properties ctx, String TG_User_Permission_UU, String trxName, String... virtualColumns) {
		super(ctx, TG_User_Permission_UU, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

	public MTGUserPermission(Properties ctx, String TG_User_Permission_UU, String trxName) {
		super(ctx, TG_User_Permission_UU, trxName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean beforeSave(boolean newRecord) {
		// TODO Auto-generated method stub
		System.out.println("MTGUserPermission - beforeSave");

		boolean hasUser = getAD_User_ID() > 0;
		boolean hasRole = getAD_Role_ID() > 0;

		if (hasUser && hasRole) {
			log.saveError("Error", "無法同時設定使用者與角色，請擇一。");
			return false;
		}

		if (!hasUser && !hasRole) {
			log.saveError("Error", "必須指定一個使用者或角色。");
			return false;
		}

		return super.beforeSave(newRecord);
	}

	public static boolean hasPermission(String permissionKey) {
		// 0. 防呆
		if (permissionKey == null || permissionKey.trim().isEmpty()) {
			return false;
		}

		// 1. 取得環境變數
		Properties ctx = Env.getCtx();
		int adUserId = Env.getAD_User_ID(ctx);
		int adRoleId = Env.getAD_Role_ID(ctx);

		// 2. 建立查詢條件 (使用本類別的常數 X_)
		StringBuilder where = new StringBuilder();
		where.append(COLUMNNAME_Name).append("=?");
		where.append(" AND ").append(COLUMNNAME_IsActive).append("='Y'");

		// OR 邏輯
		where.append(" AND (").append(COLUMNNAME_AD_User_ID).append("=?").append(" OR ").append(COLUMNNAME_AD_Role_ID)
				.append("=?").append(")");

		// 3. 執行 Query.match()
		return new Query(ctx, Table_Name, where.toString(), null).setParameters(permissionKey, adUserId, adRoleId)
				.setClient_ID().match();
	}

}
