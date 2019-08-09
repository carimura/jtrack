module api.oci.oracle {
    requires java.base;

    requires oci.java.sdk.objectstorage.generated;
    requires oci.java.sdk.common;

    exports api.oci.oracle;
}
