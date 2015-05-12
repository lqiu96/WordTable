package sample;

import javafx.beans.property.SimpleStringProperty;

public class Example {
    private SimpleStringProperty example;

    /**
     * Sets the SimpleStringProperty to be default value ('No meaning set')
     */
    public Example() {
        example = new SimpleStringProperty("No meaning set");
    }

    /**
     * Sets the example as long as what is passed in is not null
     * @param ex New example to be stored
     */
    public void setExample(String ex) {
        if (ex != null) {
            example.set(ex);
        }
    }

    public String getExample() {
        return example.get();
    }
}