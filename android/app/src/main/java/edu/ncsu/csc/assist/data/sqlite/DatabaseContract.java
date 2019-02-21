package edu.ncsu.csc.assist.data.sqlite;

public final class DatabaseContract {

    //Prevent accidental instantiations
    private DatabaseContract() {

    }

    public static class RawData {
        public static final String TABLE_NAME = "raw_data";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_VALUE = "value";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_TYPE + " NOT NULL TEXT," +
                        COLUMN_NAME_TIMESTAMP + " NOT NULL TIMESTAMP," +
                        COLUMN_NAME_VALUE + " NOT NULL INTEGER," +
                        "PRIMARY KEY(" + COLUMN_NAME_TYPE + "," + COLUMN_NAME_TIMESTAMP + ")" +
                        ")";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
