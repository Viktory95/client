import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Vi on 18.03.2017.
 */
public class PropertyLoader {
    private static PropertyLoader instance;
    Properties prop;

    private PropertyLoader() {
        InputStream inputStream = null;
        try {
            prop = new Properties();
            String propFileName = "/connectInfo.properties";

            inputStream = PropertyLoader.class.getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }

    public void showPropertys() {
        //prop.entrySet().stream().forEach(System.out::println);
    }

    public static PropertyLoader getInstance() {
        if (instance == null) {
            instance = new PropertyLoader();
        }
        return instance;
    }
}
