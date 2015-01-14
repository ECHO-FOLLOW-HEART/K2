package ch.qos.logback.core.rolling;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by Heaven on 2015/1/14.
 */
public class AizouFileAppender<E> extends ch.qos.logback.core.rolling.RollingFileAppender<E> {
    @Override
    public void openFile(String file_name) throws IOException {
        super.openFile(file_name);
        File logFile = new File(file_name);
        String version = "#Version: 1.0\n";
        String fields = "#Fields: date time c-ip cs-method cs-uri sc-status bytes cached\n";
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = "#Date: " + dateFormat.format(now);
        if (logFile.exists() && logFile.isFile() && logFile.length() == 0) {
            Collection<String> header = new ArrayList<String>();
            header.add(version);
            header.add(date);
            header.add(fields);
            FileUtils.writeLines(logFile, header);
        }
    }
}
