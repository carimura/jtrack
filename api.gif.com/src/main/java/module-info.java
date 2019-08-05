module api.gif.com {

    // system
    requires java.base;
    requires java.desktop;

    // exports to public space
    exports api.gif.com to api.facedetect.com, app.main.entrypoint;
}