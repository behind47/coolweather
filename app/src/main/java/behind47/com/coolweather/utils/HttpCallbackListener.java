package behind47.com.coolweather.utils;

/**
 * Created by behind47 on 2018/5/22.
 *
 * methods for response or error
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
