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
            validateData(tempStar);
        } else if (qName.equalsIgnoreCase(ID_TAG)) {
            tempStar.setId(tempValue);
            tempStar.setName(tempValue);
        } else if (qName.equalsIgnoreCase(NAME_TAG)) {
            tempStar.setName(tempValue);
        } else if (qName.equalsIgnoreCase(BIRTHYEAR_TAG)) {
            addStarBirthYear(tempStar, tempValue);
        }
    }

    private void addStarBirthYear(Star star, String value) {
        int dob = parseIntValue(value);
        if (dob != -1) {
            star.setBirthYear(dob);
        }
    }

    @Override
    protected String getCauseOfInvalidData(DataBaseItem data) {
        Star star = (Star) data;
        if (star.getId() == null || star.getId().isEmpty()) {
            return "Missing star Id";
        } else if (star.getName() == null || star.getName().isEmpty()) {
            return "Missing star name";
        }
        return "Unknown error while parsing data";
    }

    @Override
    protected boolean isValidData(DataBaseItem item) {
        Star star = (Star) item;
        return star.getId() != null && !star.getId().isEmpty()
                && star.getName() != null && !star.getName().isEmpty();
    }

    public static void main(String[] args) {
        StarSAXParser parser = new StarSAXParser();
        parser.run();
    }
}
