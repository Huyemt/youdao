package huyemt.json;

import java.util.LinkedList;

/**
 * @author Huyemt
 */

public class TransJson {
    public int errorCode;
    public LinkedList<LinkedList<TransValue>> translateResult;

    public TransJson() {

    }

    public int getErrorCode() {
        return errorCode;
    }

    public LinkedList<LinkedList<TransValue>> getTranslateResult() {
        return translateResult;
    }
}