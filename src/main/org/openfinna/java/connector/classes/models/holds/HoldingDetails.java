package org.openfinna.java.connector.classes.models.holds;

import java.io.Serializable;
import java.util.List;

public class HoldingDetails implements Serializable {
    private List<HoldingType> holdingTypes;
    private String info;
    private boolean commentsEnabled, partTextEnabled;
    private String dateSelectionValue;

    public HoldingDetails(List<HoldingType> holdingTypes, String info, boolean commentsEnabled, boolean partTextEnabled, String dateSelectionValue) {
        this.holdingTypes = holdingTypes;
        this.info = info;
        this.commentsEnabled = commentsEnabled;
        this.partTextEnabled = partTextEnabled;
        this.dateSelectionValue = dateSelectionValue;
    }

    public boolean isCommentsEnabled() {
        return commentsEnabled;
    }

    public void setCommentsEnabled(boolean commentsEnabled) {
        this.commentsEnabled = commentsEnabled;
    }

    public boolean isPartTextEnabled() {
        return partTextEnabled;
    }

    public void setPartTextEnabled(boolean partTextEnabled) {
        this.partTextEnabled = partTextEnabled;
    }

    public String getDateSelectionValue() {
        return dateSelectionValue;
    }

    public void setDateSelectionValue(String dateSelectionValue) {
        this.dateSelectionValue = dateSelectionValue;
    }

    public List<HoldingType> getHoldingTypes() {
        return holdingTypes;
    }

    public void setHoldingTypes(List<HoldingType> holdingTypes) {
        this.holdingTypes = holdingTypes;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public static class HoldingType implements Serializable {
        private String id;
        private String name;

        public HoldingType(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
