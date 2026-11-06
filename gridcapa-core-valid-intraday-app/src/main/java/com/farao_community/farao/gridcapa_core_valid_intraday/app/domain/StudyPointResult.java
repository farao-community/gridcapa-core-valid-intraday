package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;

public class StudyPointResult {
    private final Vertex vertex;
    private Status status;
    private String shiftedCgmUrl;
    private String networkWithPraUrl;
    private String raoResultFileUrl;

    public StudyPointResult(final Vertex vertex) {
        this.vertex = vertex;
        this.status = Status.NOT_STARTED;
        this.shiftedCgmUrl = "";
        this.networkWithPraUrl = "";
        this.raoResultFileUrl = "";
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getShiftedCgmUrl() {
        return shiftedCgmUrl;
    }

    public void setShiftedCgmUrl(final String shiftedCgmUrl) {
        this.shiftedCgmUrl = shiftedCgmUrl;
    }

    public String getNetworkWithPraUrl() {
        return networkWithPraUrl;
    }

    public void setNetworkWithPraUrl(final String networkWithPraUrl) {
        this.networkWithPraUrl = networkWithPraUrl;
    }

    public String getRaoResultFileUrl() {
        return raoResultFileUrl;
    }

    public void setRaoResultFileUrl(final String raoResultFileUrl) {
        this.raoResultFileUrl = raoResultFileUrl;
    }

    public enum Status {
        NOT_STARTED,
        SUCCESS,
        ERROR
    }
}
