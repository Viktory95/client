import javafx.beans.property.SimpleStringProperty;

public class Repository {

    public SimpleStringProperty path;
    public SimpleStringProperty regexp;
    public SimpleStringProperty dirName;
    public SimpleStringProperty dirPath;


    public Repository(String path, String regexp, String dirPath, String dirName) {
        this.path = new SimpleStringProperty(path);
        this.regexp = new SimpleStringProperty(regexp);
        this.dirName = new SimpleStringProperty(dirName);
        this.dirPath = new SimpleStringProperty(dirPath);
    }

    @Override
    public String toString() {
        return path.getValue() +
                "\n" +
                regexp.getValue() +
                "\n" +
                dirName.getValue() +
                "\n" +
                dirPath.getValue();
    }

}