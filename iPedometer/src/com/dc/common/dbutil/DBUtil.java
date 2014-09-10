package com.dc.common.dbutil;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DBUtil extends SQLiteOpenHelper {
	
	public static String DB_NAME = "ipedometer";
	public static String TABLE_NAME = "accountment";
	public static String CREATE_TABLE_SQL = "create table if not exists accountment(id integer primary key autoincrement,x,y,z,timetamp,cal)";
	
	public DBUtil(Context context, String name, CursorFactory factory,
			int version) {
//		super(context, name, factory, version);
		super(new CustomPathDatabaseContext(context, getDirPath()), name, factory, version);
	}
	/**
     * 获取db文件在sd卡的路径
     * @return
     */
    private static String getDirPath(){
        // 这里返回存放db的文件夹的绝对路径
        return Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "db";
    }
	@Override
	public void onCreate(SQLiteDatabase arg0) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}
	
	/**
	 * 创建数据库和数据库表
	 */
	public void createDBAndTable() {
        // 打开数据库
        getWritableDatabase().execSQL(CREATE_TABLE_SQL);
	}
}

/**
 * 自定义数据库路径上下文
 * @author chensga
 *
 */
class CustomPathDatabaseContext extends ContextWrapper{

    private String mDirPath;
    
    public CustomPathDatabaseContext(Context base, String dirPath) {
            super(base);
            this.mDirPath = dirPath;
    }
    
    @Override
    public File getDatabasePath(String name) 
    {
        File result = new File(mDirPath + File.separator + name);

        if (!result.getParentFile().exists()) {
            result.getParentFile().mkdirs();
        }

        return result;
    }
    
    @Override 
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory)
    {
            return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
    }
    @Override 
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler){
           return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), factory, errorHandler);
    }
}