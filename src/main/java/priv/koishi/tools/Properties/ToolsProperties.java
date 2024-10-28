package priv.koishi.tools.Properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Properties;

/**
 * @author KOISHI
 * Date:2024-10-28
 * Time:下午5:01
 */
public class ToolsProperties extends Properties {

    /**
     * 重写Properties的load方法，更换配置文件中的‘\’为‘/’
     */
    @Override
    public synchronized void load(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        while (true) {
            //缓冲流以行读取数据
            String line = bufferedReader.readLine();
            if (Objects.isNull(line)) {
                break;
            }
            //注意: properties属性类文件存在第一个隐藏字符,需要删除掉，否则第一个数据以key查找不存在
            if (line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            //如果是#注释内容，则不做操作
            if (!line.startsWith("#") && !line.isEmpty()) {
                //将读取的数据格式为’=‘分割,以key,Value方式存储properties属性类文件数据
                String[] split = line.split("=");
                //由于‘\’在Java中表示转义字符，需要将读取的路径进行转换为‘/’符号,这里“\\\\”代表一个‘\’
                put(split[0], split[1].replaceAll("\\\\", "/"));
            }
        }
    }

}
