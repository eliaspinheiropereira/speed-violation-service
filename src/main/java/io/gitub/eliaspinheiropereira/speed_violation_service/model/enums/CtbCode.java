package io.gitub.eliaspinheiropereira.speed_violation_service.model.enums;

public enum CtbCode {
    MEDIUM("218-I"),
    SERIOUS("218-II"),
    VERY_SERIOUS("218-III");

    private final String code;

    CtbCode(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
