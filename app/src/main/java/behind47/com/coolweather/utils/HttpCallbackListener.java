package behind47.com.coolweather.utils;

/**
 * Created by behind47 on 2018/5/22.
 *
 * methods for response and error
 */
public interface HttpCallbackListener {
    // deal with HttpResponse
    void onFinish(String response);
    // deal with HttpException
    void onError(Exception e);
}
