public class StarSAXParser extends FabflixSAXParser {
    private static final String XML_FILE_NAME = "actors63.xml";

    private static final String STAR_TAG = "actor";
    private static final String ID_AND_NAME_TAG = "stagename";
    private static final String BIRTHYEAR_TAG = "dob";

    private Star tempStar;

    @Override
    public String getItemType() {
        return "Star";
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
        switch (qName.toLowerCase()) {
            case STAR_TAG:
                validateData(tempStar);
                break;
            case ID_AND_NAME_TAG:
                tempStar.setId(tempValue);
                tempStar.setName(tempValue);
            case BIRTHYEAR_TAG:
                addStarBirthYear(tempStar, tempValue);
                break;
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
        if (isDuplicateData(star)) {
            return Error.DUPLICATE.getDescription();
        } else if (star.getId() == null || star.getId().isEmpty()) {
            return Error.INCONSISTENT.getDescription();
        } else if (star.getName() == null || star.getName().isEmpty()) {
            return Error.INCONSISTENT.getDescription();
        }
        return "Unknown error while parsing data";
    }

    public static void main(String[] args) {
        StarSAXParser parser = new StarSAXParser();
        parser.run();
    }
}
