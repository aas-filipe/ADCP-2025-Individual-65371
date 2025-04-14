package pt.unl.fct.di.apdc.firstwebapp.util.data;

public abstract class Data {

    protected boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

    public abstract boolean validInput();
}
