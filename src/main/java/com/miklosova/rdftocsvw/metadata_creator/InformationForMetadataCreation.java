package com.miklosova.rdftocsvw.metadata_creator;

public class InformationForMetadataCreation<T> {
    T informationForMetadataCreation;

    public InformationForMetadataCreation(T informationForMetadataCreation) {
        this.informationForMetadataCreation = informationForMetadataCreation;
    }

    public T getInformationForMetadataCreation() {
        return informationForMetadataCreation;
    }

    public void setInformationForMetadataCreation(T informationForMetadataCreation) {
        this.informationForMetadataCreation = informationForMetadataCreation;
    }

}
