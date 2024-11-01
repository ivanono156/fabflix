public class StarSAXParser extends FabflixSAXParser {
    private static final String XML_FILE_NAME = "actors63.xml";

    private static final String STAR_TAG = "actor";
    private static final String ID_TAG = "stagename"; //FIXME: another way to make an id?
    private static final String NAME_TAG = "stagename";
    private static final String BIRTHYEAR_TAG = "dob";

    private Star tempStar;
    
    public StarSAXParser() {
        super();
    }

    @Override
    protected String getXmlFileName() {
        return XML_FILE_NAME;
    }

    @Override
    protected void addNewData(String qName) {
        if (qName.equalsIgnoreCase(STAR_TAG)) {
            tempStar = new Star();
        }
    }

    @Override
    protected void parseItem(String qName) {
        if (qName.equalsIgnoreCase(STAR_TAG)) {
            addDataToList(tempStar);
        } else if (qName.equalsIgnoreCase(ID_TAG)) {
            tempStar.setId(tempValue);
            tempStar.setName(tempValue);
        } else if (qName.equalsIgnoreCase(NAME_TAG)) {
            tempStar.setName(tempValue);
        } else if (qName.equalsIgnoreCase(BIRTHYEAR_TAG)) {
            tempStar.setBirthYear(parseIntValue(tempValue));
        }
    }

    @Override
    protected boolean isValidItem(DataBaseItem item) {
        Star star = (Star) item;
        return star.getId() != null && !star.getId().isEmpty()
                && star.getName() != null && !star.getName().isEmpty();
    }

    public static void main(String[] args) {
        StarSAXParser parser = new StarSAXParser();
        parser.run();
    }
}
