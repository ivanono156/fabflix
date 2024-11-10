import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class FabflixSAXParser extends DefaultHandler {
    public enum DebugMode {
        ON,
        OFF
    }

    public enum Error {
        DUPLICATE("Duplicate"),
        MOVIE_NOT_FOUND("Movie not found"),
        STAR_NOT_FOUND("Star not found"),
        INCONSISTENT("Inconsistent");

        private final String description;

        Error(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return getDescription();
        }
    }

    public static final String XML_FOLDER_PATH = "/home/ubuntu/stanford-movies";
    public static final String ENCODING = "ISO-8859-1";
    public static final String OUTPUT_FILE = "parser_result.txt";

    public static final int invalidIntValue = -1;

    protected String tempValue;

    protected HashMap<String, DataBaseItem> validData = new HashMap<>();
    protected HashMap<String, ArrayList<DataBaseItem>> invalidData = new HashMap<>();

    private DebugMode debugging = DebugMode.ON;

    public void run() {
        Path xmlFilePath = Paths.get(XML_FOLDER_PATH, getXmlFileName());
        parseDocument(String.valueOf(xmlFilePath));
        if (debugging == DebugMode.ON) {
            printData();
        }
    }

    public void setDebugMode(DebugMode debugMode) {
        debugging = debugMode;
    }

    public HashMap<String, DataBaseItem> getValidData() {
        return validData;
    }

    public HashMap<String, ArrayList<DataBaseItem>> getInvalidData() {
        return invalidData;
    }

    public abstract String getItemType();

    protected abstract String getXmlFileName();

    protected void parseDocument(String xmlFilePath) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        InputSource inputSource = new InputSource(xmlFilePath);
        inputSource.setEncoding(ENCODING);

        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(inputSource, this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            System.out.println("Error parsing SAX parser: " + e.getMessage());
        }
    }

    protected void printData() {
        System.out.println("Number of valid " + getItemType() + "s found: " + validData.size());
        for (DataBaseItem data : validData.values()) {
            System.out.println("\t" + data.toString());
        }

        System.out.println("Number of invalid " + getItemType() + "s found: " + invalidData.size());
        for (Map.Entry<String, ArrayList<DataBaseItem>> attr : invalidData.entrySet()) {
            for (DataBaseItem data : attr.getValue()) {
                System.out.println("\t" + attr.getKey() + ": " + data);
            }
        }
    }

    public void writeToFile() {
        try (FileWriter fileWriter = new FileWriter(OUTPUT_FILE);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            writeToFile(printWriter);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    protected void writeToFile(PrintWriter printWriter) {
        printWriter.println("Number of valid " + getItemType() + "s found: " + validData.size());
        for (DataBaseItem data : validData.values()) {
            printWriter.println("\t" + data.toString());
        }

        printWriter.println("Number of invalid " + getItemType() + "s found: " + invalidData.size());
        for (Map.Entry<String, ArrayList<DataBaseItem>> attr : invalidData.entrySet()) {
            for (DataBaseItem data : attr.getValue()) {
                printWriter.println("\t" + attr.getKey() + ": " + data);
            }
        }
    }

    protected final void resetTempValue() {
        tempValue = "";
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        resetTempValue();
        addNewData(qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempValue = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        parseItem(qName);
    }

    protected abstract void addNewData(String qName);

    protected abstract void parseItem(String qName);

    protected int parseIntValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return invalidIntValue;
        }
    }

    protected final void validateData(DataBaseItem data) {
        if (isValidData(data)) {
            validData.put(data.getId(), data);
        } else {
            String invalidDataCause = getCauseOfInvalidData(data);
            addInvalidData(invalidDataCause, data);
        }
    }

    protected final void addInvalidData(String error, DataBaseItem dataBaseItem) {
        if (invalidData.containsKey(error)) {
                invalidData.get(error).add(dataBaseItem);
        } else {
            ArrayList<DataBaseItem> dataBaseItems = new ArrayList<>();
            dataBaseItems.add(dataBaseItem);
            invalidData.put(error, dataBaseItems);
        }
    }

    protected abstract String getCauseOfInvalidData(DataBaseItem data);

    protected boolean isValidData(DataBaseItem data) {
        return data.isValid() && !isDuplicateData(data);
    }

    protected boolean isDuplicateData(DataBaseItem data) {
        return validData.containsKey(data.getId());
    }
}
