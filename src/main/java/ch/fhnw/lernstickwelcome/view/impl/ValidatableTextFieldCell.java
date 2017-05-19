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

/**
 * This cell is an implementation of the {@link TextFieldTableCell}. It provides
 * the table cell with the functionality of providing a validation action. <br>
 * <br>
 * The validation action is a {@link BiFunction BiFunction&lt;Object, Integer, Throwable&gt;} 
 * that takes the newly provided value and the row id and returns a throwable on
 * invalid input.<br>
 * Invalid rows will have the css type "error" added.<br>
 * Additionally, if the Throwable is a {@link TableCellValidationException} the 
 * tooltip text will be set for this table cell.
 * @author sschw
 * @param <S> The type of the TableView generic type (i.e. S == TableView&lt;S&gt;). This should also match with the first generic type in TableColumn.
 * @param <T> The type of the item contained within the Cell.
 */
public class ValidatableTextFieldCell<S, T> extends TextFieldTableCell<S, T> {

    private final BiFunction<Object, Integer, Throwable> validationAction;
    private final ResourceBundle rb;

    /**
     * Constructs a new ValidationTextFieldCell.
     * @param converter The converter provides the cell with the functionality converting between the display text and the value it shows.
     * @param validationAction The validation action defines how a table cell should be validated.
     * @param rb 
     */
    public ValidatableTextFieldCell(StringConverter<T> converter, BiFunction<Object, Integer, Throwable> validationAction, ResourceBundle rb) {
        super(converter);
        this.validationAction = validationAction;
        this.rb = rb;
    }

    /**
     * Creates a new {@link Callback} function which creates a table cell for 
     * String values.<br>
     * Use this function to initialize a new ValidatableTextFieldCell.
     * @param <U> The type of the TableView generic type (i.e. S == TableView&lt;U&gt;). This should also match with the first generic type in TableColumn.
     * @param validationAction The validation action that should be run on 
     * {@link #commitEdit(java.lang.Object) }. 
     * @param rb The resource bundle that should be used to set the correct text
     * if a {@link TableCellValidationException} is thrown by the function.
     * @return a new instance of a Callback function is provided for 
     * {@link TableColumn#setCellFactory(javafx.util.Callback) }.
     */
    public static <U> Callback<TableColumn<U, String>, TableCell<U, String>> forTableColumn(BiFunction<Object, Integer, Throwable> validationAction, ResourceBundle rb) {
        return c -> new ValidatableTextFieldCell<>(new DefaultStringConverter(), validationAction, rb);
    }

    /**
     * super.commitEdit is only called if the validationAction is runned 
     * without returning a Throwable.
     * @see ValidatableTextFieldCell
     * @see TextFieldTableCell#commitEdit(java.lang.Object) 
     * @param newValue 
     */
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

    /**
     * Runs super.cancelEdit() and removes the css class "error".
     * @see TextFieldTableCell#cancelEdit() 
     */
    @Override
    public void cancelEdit() {
        getStyleClass().remove("error");
        super.cancelEdit();
    }
}
