package tungho.android.example;

import android.util.Log;

/**
 * Created by Hồ Hoàng Tùng on 17/03/2016.
 */
public class Util {
    public static final int LOG_DEBUG = 0;
    public static final int LOG_INFO = 1;
    public static final int LOG_VERBOSE = 2;
    public static final int LOG_ERROR = 3;
    public static final int LOG_WARNING = 4;

    public static final void CompactLog(int action, String tag, String msg){
        if (BuildConfig.DEBUG){
            switch (action){
                case LOG_DEBUG:
                    Log.d(tag, msg);
                    break;
                case LOG_INFO:
                    Log.i(tag, msg);
                    break;
                case LOG_VERBOSE:
                    Log.v(tag, msg);
                    break;
                case LOG_ERROR:
                    Log.e(tag, msg);
                    break;
                case LOG_WARNING:
                    Log.w(tag, msg);
                    break;
            }
        }
    }
}
