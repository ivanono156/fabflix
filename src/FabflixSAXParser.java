import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;

public abstract class FabflixSAXParser extends DefaultHandler {
    public enum DebugMode {
        ON,
        OFF
    }

    public static final String XML_FOLDER_PATH = "C:\\Users\\Ivan Onofre\\University\\CS 122B\\stanford-movies\\";
    public static final String ENCODING = "ISO-8859-1";

    protected String tempValue;

    private final ArrayList<DataBaseItem> validData;
    private final ArrayList<DataBaseItem> invalidData;
    private final ArrayList<String> brokenAttributes;

    private DebugMode debugging = DebugMode.ON;

    public FabflixSAXParser() {
        validData = new ArrayList<>();
        invalidData = new ArrayList<>();
        brokenAttributes = new ArrayList<>();
    }

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
        if (!validData.contains(data) && isValidData(data)) {
            validData.add(data);
        } else {
            String invalidDataCause = getCauseOfInvalidData(data);
            brokenAttributes.add(invalidDataCause + " - " + data.toString());
            invalidData.add(data);
        }
    }

    protected abstract String getCauseOfInvalidData(DataBaseItem data);

    protected abstract boolean isValidData(DataBaseItem data);
}
