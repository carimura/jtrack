FROM openjdk:11

# todo: move to a separate docker image
ENV OPENCV_VERSION="4.1.0"

ENV LIB="libswscale-dev \
         libtbb2 \
         libtbb-dev \
         libjpeg-dev \
         libpng-dev \
         libtiff-dev \
         libavformat-dev \
         libpq-dev \
         libavcodec-dev\
         libdc1394-22-dev"

ENV PKG="build-essential \
         cmake \
         git \
         wget \
         unzip \
         yasm \
         pkg-config"

RUN apt-get update && \
    apt-get install --no-install-recommends -qy $PKG && \
    apt-get install --no-install-recommends -qy $LIB && \
    apt-get install --no-install-recommends -qy gcc

RUN mkdir /tmp/opencv && \
    cd /tmp/opencv && \
    wget -O opencv.zip https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.zip && \
    unzip opencv.zip && \
    wget -O opencv_contrib.zip https://github.com/opencv/opencv_contrib/archive/${OPENCV_VERSION}.zip && \
    unzip opencv_contrib.zip && \
    mkdir /tmp/opencv/opencv-${OPENCV_VERSION}/build && cd /tmp/opencv/opencv-${OPENCV_VERSION}/build && \
    cmake \
      -D OPENCV_EXTRA_MODULES_PATH=/tmp/opencv/opencv_contrib-${OPENCV_VERSION}/modules \
      -D CMAKE_INSTALL_PREFIX=/usr/local \
      -D BUILD_TIFF=ON \
      -D BUILD_opencv_java=ON \
      -D WITH_CUDA=OFF \
      -D ENABLE_AVX=ON \
      -D WITH_OPENGL=ON \
      -D WITH_OPENCL=ON \
      -D WITH_IPP=ON \
      -D WITH_TBB=ON \
      -D WITH_EIGEN=ON \
      -D WITH_V4L=ON \
      -D BUILD_TESTS=OFF \
      -D BUILD_PERF_TESTS=OFF \
      -D CMAKE_BUILD_TYPE=RELEASE \
      -D BUILD_opencv_python=NO \
      -D BUILD_opencv_python2=NO \
      -D BUILD_opencv_python3=NO \
      -D INSTALL_C_EXAMPLES=NO \
      -D INSTALL_PYTHON_EXAMPLES=NO \
      -D BUILD_ANDROID_EXAMPLES=NO \
      -D BUILD_DOCS=NO \
      -D BUILD_TESTS=NO \
      -D BUILD_PERF_TESTS=NO \
      -D BUILD_EXAMPLES=NO \
      -D WITH_WIN32UI=OFF \
      -D WITH_QT=OFF \
      -D OPENCV_GENERATE_PKGCONFIG=YES \
      -D WITH_GTK=OFF .. && \
    make -j4 && \
    make install && \
    ldconfig && \
    apt-get remove --purge --auto-remove -y ${PKG} && \
    apt-get remove --purge --auto-remove -y ${LIB} && \
    apt-get clean; \
    apt-get autoclean; \
    apt-get autoremove; \
    rm -rf /tmp/* /var/tmp/*; \
    rm -rf /var/lib/apt/lists/*; \
    rm -f /var/cache/apt/archives/*.deb \
        /var/cache/apt/archives/partial/*.deb \
        /var/cache/apt/*.bin; \
    cd && rm -rf /tmp/opencv && \
    apt-get update && apt-get install libgtk2.0-dev -qy


ENV PKG_CONFIG_PATH /usr/local/lib64/pkgconfig
ENV LD_LIBRARY_PATH /usr/local/lib64

ENTRYPOINT ["java", "-jar", "/usr/share/jimage/jimage.jar"]
ADD src/main/resources/* /usr/share/jimage/
ADD oci_key.pem /usr/share/jimage/oci_key.pem
ADD target/jimage-0.3-jar-with-dependencies.jar     /usr/share/jimage/jimage.jar
RUN apt-get update && apt-get install libgtk2.0-dev -qy
