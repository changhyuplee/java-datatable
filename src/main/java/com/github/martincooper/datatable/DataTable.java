package com.github.martincooper.datatable;

import com.github.martincooper.datatable.sorting.DataSort;
import com.github.martincooper.datatable.sorting.SortItem;
import com.github.martincooper.datatable.sorting.SortOrder;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.control.Try;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.vavr.API.*;
import static io.vavr.Patterns.*;

/**
 * DataTable class.
 * Created by Martin Cooper on 08/07/2017.
 */
public class DataTable implements IBaseTable {

    private final String name;
    private final DataRowCollectionModifiable rows;
    private final DataColumnCollection columns;

    /**
     * Private DataTable constructor. Empty Table with no columns.
     * Use 'build' to create instance.
     *
     * @param tableName The name of the table.
     */
    private DataTable(String tableName) {
        this.name = tableName;
        this.columns = new DataColumnCollection(this);
        this.rows = DataRowCollectionModifiable.build(this);
    }

    /**
     * Private DataTable Constructor.
     * Use 'build' to create instance.
     *
     * @param tableName The name of the table.
     * @param columns The collection of columns in the table.
     */
    private DataTable(String tableName, Iterable<IDataColumn> columns) {
        this.name = tableName;
        this.columns = new DataColumnCollection(this, columns);
        this.rows = DataRowCollectionModifiable.build(this);
    }

    /**
     * Returns an iterator over elements of type DataRow.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<DataRow> iterator() {
        return this.rows.iterator();
    }

    /**
     * The name of the table.
     *
     * @return Returns the table name.
     */
    @Override
    public String name() { return this.name; }

    /**
     * The column collection.
     *
     * @return Returns the columns collection.
     */
    @Override
    public DataColumnCollection columns() { return this.columns; }

    /**
     * The row collection.
     *
     * @return Returns the row collection.
     */
    @Override
    public DataRowCollectionModifiable rows() { return this.rows; }

    /**
     * Returns the rowCount / row count of the table.
     *
     * @return The row count of the table.
     */
    @Override
    public Integer rowCount() {
        return this.columns.count() > 0
                ? this.columns.get(0).data().length()
                : 0;
    }

    /**
     * Return a new DataTable based on this table (clone).
     *
     * @return Returns a clone of this DataTable.
     */
    @Override
    public DataTable toDataTable() {
        return DataTable.build(this.name, this.columns).get();
    }

    /**
     * Return a new Data View based on this table.
     *
     * @return A new Data View based on this table.
     */
    @Override
    public DataView toDataView() {
        return DataView.build(this, this.rows).get();
    }

    /**
     * Filters the row data using the specified predicate,
     * returning the results as a DataView over the original table.
     *
     * @param predicate The filter criteria.
     * @return Returns a DataView with the filter results.
     */
    public DataView filter(Predicate<DataRow> predicate) {
        return this.rows.filter(predicate);
    }

    /**
     * Map operation across the Data Rows in the table.
     *
     * @param mapper The mapper function.
     * @param <U> The return type.
     * @return Returns the mapped results.
     */
    public <U> Seq<U> map(Function<? super DataRow, ? extends U> mapper) {
        return this.rows.map(mapper);
    }

    /**
     * FlatMap implementation for the DataRowCollection class.
     *
     * @param <U> Mapped return type.
     * @param mapper The map function.
     * @return Returns a sequence of the applied flatMap.
     */
    public <U> Seq<U> flatMap(Function<? super DataRow, ? extends Iterable <? extends U>> mapper) {
        return this.rows.flatMap(mapper);
    }

    /**
     * Reduce implementation for the DataRowCollection class.
     *
     * @param reducer The reduce function.
     * @return Returns a single, reduced DataRow.
     */
    public DataRow reduce(BiFunction<? super DataRow, ? super DataRow, ? extends DataRow> reducer) {
        return this.rows.reduce(reducer);
    }

    /**
     * GroupBy implementation for the DataRowCollection class.
     *
     * @param grouper The group by function.
     * @return Returns a map containing the grouped data.
     */
    public <C> Map<C, Vector<DataRow>> groupBy(Function<? super DataRow, ? extends C> grouper) {
        return this.rows.groupBy(grouper);
    }

    /**
     * Fold Left implementation for the DataRowCollection class.
     *
     * @param <U> Fold return type.
     * @param folder The fold function.
     * @return Returns a single value of U.
     */
    public <U> U foldLeft(U zero, BiFunction<? super U, ? super DataRow, ? extends U> folder) {
        return this.rows.foldLeft(zero, folder);
    }

    /**
     * Fold Right implementation for the DataRowCollection class.
     *
     * @param <U> Fold return type.
     * @param folder The fold function.
     * @return Returns a single value of U.
     */
    public <U> U foldRight(U zero, BiFunction<? super DataRow, ? super U, ? extends U> folder) {
        return this.rows.foldRight(zero, folder);
    }

    /**
     * Accessor to a specific row by index.
     *
     * @return Returns a single row.
     */
    public DataRow row(Integer rowIdx) {
        return this.rows.get(rowIdx);
    }

    /**
     * Accessor to a specific column by index.
     *
     * @param colIdx The index of the column to return.
     * @return Returns a single column.
     */
    public IDataColumn column(Integer colIdx) {
        return this.columns.get(colIdx);
    }

    /**
     * Accessor to a specific column by name.
     *
     * @param colName The name of the column to return.
     * @return Returns a single column.
     */
    public IDataColumn column(String colName) {
        return this.columns.get(colName);
    }

    /**
     * Table QuickSort by single column name.
     *
     * @param columnName The column name to sort.
     * @return Returns the results as a sorted Data View.
     */
    @Override
    public Try<DataView> quickSort(String columnName) {
        return this.quickSort(columnName, SortOrder.Ascending);
    }

    /**
     * Table QuickSort by single column name and a sort order.
     *
     * @param columnName The column name to sort.
     * @param sortOrder  The sort order.
     * @return Returns the results as a sorted Data View.
     */
    @Override
    public Try<DataView> quickSort(String columnName, SortOrder sortOrder) {
        SortItem sortItem = new SortItem(columnName, sortOrder);
        return DataSort.quickSort(this, this.rows.asSeq(), Stream.of(sortItem));
    }

    /**
     * Table QuickSort by single column index.
     *
     * @param columnIndex The column index to sort.
     * @return Returns the results as a sorted Data View.
     */
    @Override
    public Try<DataView> quickSort(Integer columnIndex) {
        return quickSort(columnIndex, SortOrder.Ascending);
    }

    /**
     * Table QuickSort by single column index and a sort order.
     *
     * @param columnIndex The column index to sort.
     * @param sortOrder   The sort order.
     * @return Returns the results as a sorted Data View.
     */
    @Override
    public Try<DataView> quickSort(Integer columnIndex, SortOrder sortOrder) {
        SortItem sortItem = new SortItem(columnIndex, sortOrder);
        return DataSort.quickSort(this, this.rows.asSeq(), Stream.of(sortItem));
    }

    /**
     * Table QuickSort by single sort item.
     *
     * @param sortItem The sort item.
     * @return Returns the results as a sorted Data View.
     */
    @Override
    public Try<DataView> quickSort(SortItem sortItem) {
        return this.quickSort(Stream.of(sortItem));
    }

    /**
     * Table QuickSort by multiple sort items.
     *
     * @param sortItems The sort items.
     * @return Returns the results as a sorted Data View.
     */
    @Override
    public Try<DataView> quickSort(Iterable<SortItem> sortItems) {
        return DataSort.quickSort(this, this.rows.asSeq(), Stream.ofAll(sortItems));
    }

    /**
     * Builds an instance of a DataTable.
     *
     * @param tableName The name of the table.
     * @return Returns an instance of a DataTable.
     */
    public static DataTable build(String tableName) {
        return new DataTable(tableName);
    }

    /**
     * Builds an instance of a DataTable.
     * Columns are validated before creation, returning a Failure on error.
     *
     * @param tableName The name of the table.
     * @param columns The column collection.
     * @return Returns a DataTable wrapped in a Try.
     */
    public static Try<DataTable> build(String tableName, IDataColumn[] columns) {
        return build(tableName, Stream.of(columns));
    }

    /**
     * Builds an instance of a DataTable.
     * Columns are validated before creation, returning a Failure on error.
     *
     * @param tableName The name of the table.
     * @param columns The column collection.
     * @return Returns a DataTable wrapped in a Try.
     */
    public static Try<DataTable> build(String tableName, Iterable<IDataColumn> columns) {
        return build(tableName, Stream.ofAll(columns));
    }

    /**
     * Builds an instance of a DataTable.
     * Columns are validated before creation, returning a Failure on error.
     *
     * @param tableName The name of the table.
     * @param columns The column collection.
     * @return Returns a DataTable wrapped in a Try.
     */
    public static Try<DataTable> build(String tableName, Stream<IDataColumn> columns) {
        return Match(validateColumns(columns)).of(
          Case($Success($()), cols -> Try.success(new DataTable(tableName, cols))),
          Case($Failure($()), Try::failure)
        );
    }

    /**
     * Validates the column data.
     *
     * @param columns The columns to validate.
     * @return Returns a Success or Failure.
     */
    private static Try<Seq<IDataColumn>> validateColumns(Iterable<IDataColumn> columns) {
        return validateColumnNames(Stream.ofAll(columns))
                .flatMap(DataTable::validateColumnDataLength);
    }

    /**
     * Validates there are no duplicate columns names.
     *
     * @param columns The columns to check.
     * @return Returns a Success or Failure.
     */
    private static Try<Seq<IDataColumn>> validateColumnNames(Seq<IDataColumn> columns) {
        return columns.groupBy(IDataColumn::name).length() != columns.length()
                ? DataTableException.tryError("Columns contain duplicate names.")
                : Try.success(columns);
    }

    /**
     * Validates the number of items in each column is the same.
     *
     * @param columns The columns to check.
     * @return Returns a Success or Failure.
     */
    private static Try<Seq<IDataColumn>> validateColumnDataLength(Seq<IDataColumn> columns) {
        return columns.groupBy(col -> col.data().length()).length() > 1
                ? DataTableException.tryError("Columns have different lengths.")
                : Try.success(columns);
    }
}
