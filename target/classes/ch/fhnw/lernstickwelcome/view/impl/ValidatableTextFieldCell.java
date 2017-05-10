package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.controller.exception.TableCellValidationException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class ValidatableTextFieldCell<S, T> extends TextFieldTableCell<S, T> {

    private final BiFunction<Object, Integer, Throwable> validationAction;
    private final ResourceBundle rb;

    public ValidatableTextFieldCell(StringConverter<T> converter, BiFunction<Object, Integer, Throwable> validationAction, ResourceBundle rb) {
        super(converter);
        this.validationAction = validationAction;
        this.rb = rb;
    }

    public static <U> Callback<TableColumn<U, String>, TableCell<U, String>> forTableColumn(BiFunction<Object, Integer, Throwable> validationAction, ResourceBundle rb) {
        return c -> new ValidatableTextFieldCell<>(new DefaultStringConverter(), validationAction, rb);
    }

    @Override
    public void commitEdit(T newValue) {
        Throwable t = validationAction.apply(newValue, getTableRow().getIndex());
        if (t == null) {
            getStyleClass().remove("error");
            setTooltip(null);
            super.commitEdit(newValue);
        } else {
            if (t instanceof TableCellValidationException) {
                TableCellValidationException ex = (TableCellValidationException) t;
                setTooltip(new Tooltip(MessageFormat.format(rb.getString(ex.getMessage()), ex.getMessageDetails())));
            }
            getStyleClass().add("error");
        }
    }

    @Override
    public void cancelEdit() {
        getStyleClass().remove("error");
        super.cancelEdit();
    }
}
