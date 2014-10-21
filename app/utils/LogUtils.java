package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.util.Map;

/**
 * Created by topy on 2014/10/21.
 */
public class LogUtils {

    public  static void error(Class cls,String info){

        Logger log = LoggerFactory.getLogger(cls);
        log.debug("---------CoreApi Debug Start---------");
        log.debug(info);
        log.debug("---------CoreApi Debug End---------");

    }

    public  static void info(Class cls,String info){

        Logger log = LoggerFactory.getLogger(cls);
        log.info("---------CoreApi Debug Start---------");
        log.info(info);
        log.info("---------CoreApi Debug End---------");

    }

    public  static void info(Class cls,Http.Request request){
        String header = getHeader(request);
        String uri = getUrl(request);
        String post = getPost(request);
        Logger log = LoggerFactory.getLogger(cls);
        log.info("---------CoreApi Debug Start---------");
        log.info(header);
        log.info(uri);
        log.info(post);
        log.info("---------CoreApi Debug End---------");

    }

    private static String getHeader(Http.Request request){
        StringBuffer sbHead = new StringBuffer(10);
        sbHead.append("---Header---");
        sbHead.append("\r\n");
        for (Map.Entry<String, String[]> entry : request.headers().entrySet()) {
            String k = entry.getKey();
            String[] v = entry.getValue();
            sbHead.append("------");
            sbHead.append(k);
            sbHead.append(":");
            String value= "";
            for(int i=0;i<v.length;i++){
                value = value +v[i];
            }
            sbHead.append(value);
            sbHead.append("\r\n");
        }
        return sbHead.toString();
    }

    private static String getUrl(Http.Request request){

        return "---URI---"+"\r\n"+request.uri();

    }

    private static String getPost(Http.Request request){

        return "---POST---"+"\r\n"+(request.body().asJson() == null?"":request.body().asJson().toString());

    }
}
