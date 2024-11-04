import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class FabflixSAXParser extends DefaultHandler {
    public enum DebugMode {
        ON,
        OFF
    }

    public static final String XML_FOLDER_PATH = "C:\\Users\\Ivan Onofre\\University\\CS 122B\\stanford-movies\\";
    public static final String ENCODING = "ISO-8859-1";
    public static final String OUTPUT_FILE = "parser_result.txt";

    protected String tempValue;

    protected HashMap<String, DataBaseItem> validData = new HashMap<>();
    protected ArrayList<String> invalidData = new ArrayList<>();

    private DebugMode debugging = DebugMode.ON;

    public void run() {
        String xmlFilePath = XML_FOLDER_PATH + getXmlFileName();
        parseDocument(xmlFilePath);
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

    public ArrayList<String> getInvalidData() {
        return invalidData;
    }

    public abstract String getItemType();

    protected abstract String getXmlFileName();

    protected void parseDocument(String xmlFilePath) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        InputSource inputSource = new InputSource(xmlFilePath);
        inputSource.setEncoding(ENCODING);

        try {
            javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
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
        for (String attr : invalidData) {
            System.out.println("\t" + attr);
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
        for (String attr : invalidData) {
            printWriter.println("\t" + attr);
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
            return -1;
        }
    }

    protected final void validateData(DataBaseItem data) {
        if (isValidData(data)) {
            validData.put(data.getId(), data);
        } else {
            String invalidDataCause = getCauseOfInvalidData(data);
            invalidData.add(invalidDataCause + " - " + data);
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
