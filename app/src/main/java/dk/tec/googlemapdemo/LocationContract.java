package dk.tec.googlemapdemo;

public class LocationContract {
    private LocationContract() {
    }
    public static class LocationEntry {
        public static final String TABLE_NAME = "location";
        public static final String _ID = "_id";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
    }
}
