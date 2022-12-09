package classicstun.communication;

import java.util.EventListener;

/**
 * @author JiangZhenli
 */
public interface ResponseListener extends EventListener {

    void onResponse(ResponseEvent event);
}
