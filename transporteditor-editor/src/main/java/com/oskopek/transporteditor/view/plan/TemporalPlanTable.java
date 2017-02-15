package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javaslang.collection.Stream;
import org.controlsfx.control.table.TableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class TemporalPlanTable {

    private static final transient Logger logger = LoggerFactory.getLogger(TemporalPlanTable.class);

    private TemporalPlanTable() {
        // intentionally empty
    }

    public static TableFilter<TemporalPlanAction> build(Collection<TemporalPlanAction> actions,
            Consumer<Collection<TemporalPlanAction>> updatePlan) {
        List<TemporalPlanAction> actionList = Stream.ofAll(actions).sortBy(TemporalPlanAction::getEndTimestamp)
                .sortBy(TemporalPlanAction::getStartTimestamp).toJavaList();
        TableView<TemporalPlanAction> tableView = new TableView<>(FXCollections.observableList(actionList));

        TableColumn<TemporalPlanAction, Number> startColumn = new TableColumn<>("Start");
        startColumn.cellValueFactoryProperty().setValue(param -> {
                    return new ReadOnlyIntegerWrapper(param.getValue().getStartTimestamp());
                });
        TableColumn<TemporalPlanAction, Number> endColumn = new TableColumn<>("End");
        endColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyIntegerWrapper(param.getValue().getEndTimestamp()));
        TableColumn<TemporalPlanAction, String> actionColumn = new TableColumn<>("Action");
        actionColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getAction().getName()));
        TableColumn<TemporalPlanAction, String> whoColumn = new TableColumn<>("Who");
        whoColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getAction().getWho().getName()));
        TableColumn<TemporalPlanAction, String> whereColumn = new TableColumn<>("Where");
        whereColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getAction().getWhere().getName()));
        TableColumn<TemporalPlanAction, String> whatColumn = new TableColumn<>("What");
        whatColumn.cellValueFactoryProperty().setValue(param -> new ReadOnlyStringWrapper(
                param.getValue().getAction().getWhat() == null ? ""
                        : param.getValue().getAction().getWhat().getName()));

        tableView.getColumns().setAll(startColumn, endColumn, actionColumn, whoColumn, whereColumn, whatColumn);
        tableView.getColumns().forEach(c -> c.setSortable(false));
        return TableFilter.forTableView(tableView).lazy(true).apply();
    }

}
