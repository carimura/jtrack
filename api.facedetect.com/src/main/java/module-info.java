module api.facedetect.com {

    // core
    requires java.base;
    requires java.desktop;

    // 3rd-party
    requires org.bytedeco.opencv;

    // local
    requires api.gif.com;

    // exports to public space
    exports api.facedetect.com to app.main.entrypoint;
}