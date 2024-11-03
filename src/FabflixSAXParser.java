import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class FabflixSAXParser extends DefaultHandler {
    public enum DebugMode {
        ON,
        OFF
    }

    public static final String XML_FOLDER_PATH = "C:\\Users\\Ivan Onofre\\University\\CS 122B\\stanford-movies\\";
    public static final String ENCODING = "ISO-8859-1";

    protected String tempValue;

    private final HashSet<DataBaseItem> validData = new HashSet<>();
    private final ArrayList<DataBaseItem> invalidData = new ArrayList<>();
    private final ArrayList<String> brokenAttributes = new ArrayList<>();

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

    public HashSet<DataBaseItem> getValidData() {
        return validData;
    }

    public ArrayList<DataBaseItem>  getInvalidData() {
        return invalidData;
    }

    public ArrayList<String> getBrokenAttributes() {
        return brokenAttributes;
    }

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
        System.out.println("Number of valid items found: " + validData.size());
        for (DataBaseItem data : validData) {
            System.out.println("\t" + data.toString());
        }

        System.out.println("Number of invalid items found: " + invalidData.size());
        for (DataBaseItem data : invalidData) {
            System.out.println("\t" + data.toString());
        }

        System.out.println("Number of broken attributes found: " + brokenAttributes.size());
        for (String attr : brokenAttributes) {
            System.out.println("\t" + attr);
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
            validData.add(data);
        } else {
            String invalidDataCause = getCauseOfInvalidData(data);
            brokenAttributes.add(invalidDataCause + " - " + data);
            invalidData.add(data);
        }
    }

    protected abstract String getCauseOfInvalidData(DataBaseItem data);

    protected boolean isValidData(DataBaseItem data) {
        return data.isValid() && !isDuplicateData(data);
    }

    protected boolean isDuplicateData(DataBaseItem data) {
        return validData.contains(data);
    }
}
