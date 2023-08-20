
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <unistd.h>
#include "xdraw.h"
#include <android/log.h>
// Android
#include <sys/socket.h>
#include <sys/un.h>

#include <android/native_window.h>
#include <android/native_window_jni.h>

// X11
#include <X11/Xutil.h>
#include<X11/extensions/XTest.h>
#include <X11/cursorfont.h>


bool unix_socket();
Display *mdisplay = NULL;

char displayEnv[512];

static bool X11_init(const char *display_ip, int display_id) {
    char displayEnv[512];
    memset(displayEnv, '\0', sizeof(displayEnv));

    if (display_ip == NULL) {
        __android_log_print(ANDROID_LOG_INFO, "tag1", "null");

        sprintf(displayEnv, "DISPLAY=unix/:0.0");
    } else {
        sprintf(displayEnv, "DISPLAY=%s:%d", display_ip, display_id);
    }
    putenv(displayEnv);
    mdisplay = XOpenDisplay(NULL);



    if (mdisplay == NULL)
        return false;
    else
        return true;
}


JNIEXPORT jint JNICALL
Java_com_pangbai_dowork_tool_jni_init(JNIEnv *env, jobject thiz, jboolean state,
                                      jstring display_addr, jint display_id) {
    bool x11_init_state = false;
    const char *display_ip;

    jstring ret;

    if ((bool) state == true) {
        display_ip = (*env)->GetStringUTFChars(env, display_addr, 0);
    } else {
        display_ip = (const char *) NULL;
    }
    if (mdisplay == NULL) {
        XInitThreads();
        x11_init_state = X11_init(display_ip, (int) display_id);
    }
    if (mdisplay != NULL) {
        unsigned int nchildren_return = 0;
        Window tn;
        Window root_return;
        Window *children_return;
        XQueryTree(mdisplay, DefaultRootWindow(mdisplay), &root_return, &tn, &children_return,
                   &nchildren_return);
        if (nchildren_return > 0) {

            return XGetImage(mdisplay, RootWindow(mdisplay, 0), 0, 0, DisplayWidth(mdisplay, 0),
                             DisplayHeight(mdisplay, 0), ~0, ZPixmap)->bytes_per_line;
        }
        return -1;
    } else {
        return -2;
//ret = (*env)->NewStringUTF(env, getenv("DISPLAY"));
    }
}

Window desktop;


JNIEXPORT jstring JNICALL
Java_com_pangbai_dowork_tool_jni_startx(JNIEnv *env, jobject thiz, jobject jsurface) {
//获取Surface

//判断Surface是否为空

    if (jsurface == NULL)
        return (*env)->NewStringUTF(env, "F2");
//bool类型线程是否运行

    bool isRunning = true;

//获取Xserver的根窗口复制给它



//Xserver图像结构体用于存窗口中的图像数据
    XImage *image;

//获取Xserver的根窗口

    desktop = RootWindow(mdisplay, 0);

//判断上个函数的返回结果是否为空

    XDefineCursor(mdisplay, desktop, XCreateFontCursor(mdisplay, XC_circle));

    XFlush(mdisplay);

    if (desktop == false)
        return (*env)->NewStringUTF(env, "F3");
    int width = 0, height = 0;

//控制Xserver图像结构体中的图像的宽高
    width = DisplayWidth(mdisplay, 0);

    height = DisplayHeight(mdisplay, 0);

//设置矩形宽高
/*
jclass jcl = (*env)->FindClass(env, "com/pangbai/dowork/display/display");
	//jclass jcl =  (*env)->GetObjectClass(env,thiz);
 jmethodID mid =  (*env)->GetStaticMethodID(env,jcl, "updateDisplay", "(II)V");
   (*env)->CallStaticVoidMethod(env,thiz, mid,width,height );
 (*env)->DeleteLocalRef(env,jcl);
	 */
//isRunning布尔值控制线程停此

    while (isRunning) {

//原生窗口用于等会显示从Xserver获取的图像
        ANativeWindow *mANativeWindow = NULL;

//获取窗口从Java Surfave

        mANativeWindow = ANativeWindow_fromSurface(env, jsurface);
        if (mANativeWindow == NULL)
            continue;


//设置窗口图像格式RGBA 8888
//native_window_set_scaling_mode(mANativeWindow,0);
//不设置默认为RGB565

//下面的代码删了就是565
//ANativeWindow_setBuffersGeometry(mANativeWindow, 1080, 1920,WINDOW_FORMAT_RGB_565);

// X图像结构体指针为NULL调用此函数给它复制，

//获取Xserver根窗口中的图像

//图像格式RGB888

        image = XGetImage(mdisplay, desktop, 0, 0, width, height, ~0, ZPixmap);
        if (image == NULL)
            continue;
        showImage(mANativeWindow, image);
//释放X图像结构体

        XDestroyImage(image);

    }
    return (*env)->NewStringUTF(env, "P");
}


static bool showImage(ANativeWindow *mANativeWindow, XImage *image) {

    ANativeWindow_Buffer buffer;

    ANativeWindow_lock(mANativeWindow, &buffer, 0);

//如果原生窗口图像格式是RGB 565 请调用draw

    draw(&buffer, image);

    ANativeWindow_unlockAndPost(mANativeWindow);

    ANativeWindow_release(mANativeWindow);
    return true;
}


//RGB565
static void draw(ANativeWindow_Buffer *buffer, XImage *image) {
    uint16_t *rect = (uint16_t *) buffer->bits;
    for (int y = 0; y < image->height; ++y) {
        for (int x = 0; x < image->width; ++x)
            rect[x] = (((uint16_t) (*(image->data + y * image->bytes_per_line + x * 4 + 2) &
                                    (int) 248)) << 8) |
                      (((uint16_t) (*(image->data + y * image->bytes_per_line + x * 4 + 1) &
                                    (int) 252)) << 3) |
                      (((uint16_t) (*(image->data + y * image->bytes_per_line + x * 4 + 0) &
                                    (int) 252)) >> 3);
        rect += buffer->stride;
    }
}

//RGBA8888
static void draw2(ANativeWindow_Buffer *buffer, XImage *image) {
    uint32_t *rect = (uint32_t *) buffer->bits;
    for (int y = 0; y < image->height; ++y) {
        for (int x = 0; x < image->width; ++x)
            rect[x] =
                    (((uint32_t) (*(image->data + y * image->bytes_per_line + x * 4 + 2))) << 16) |
                    (((uint32_t) (*(image->data + y * image->bytes_per_line + x * 4 + 1))) << 8) |
                    (((uint32_t) (*(image->data + y * image->bytes_per_line + x * 4 + 0))));
        rect += buffer->stride;
    }
}


bool unix_socket() {
    int sockfd;
    struct sockaddr_un server_addr;
    char *socket_path = "/data/data/com.pangbai.dowork/files/usr/tmp/.X11-unix/X0";

    // 创建套接字
    sockfd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sockfd == -1) {
        perror("socket");
        return false;
    }

    // 设置服务器地址
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sun_family = AF_UNIX;
    strncpy(server_addr.sun_path, socket_path, sizeof(server_addr.sun_path) - 1);

    // 连接到服务器
    if (connect(sockfd, (struct sockaddr *)&server_addr, sizeof(server_addr)) == -1) {
        perror("connect");
        close(sockfd);
        __android_log_print(ANDROID_LOG_INFO, "tag2", "errro");
        return false;
    }

    printf("Connected to X Server at %s\n", socket_path);

    // 将套接字转换为 Display 结构
    mdisplay = XOpenDisplay(NULL);
    if (!mdisplay) {
        __android_log_print(ANDROID_LOG_INFO, "tag3", "display");
        fprintf(stderr, "Unable to open display\n");
      //  close(sockfd);
        return false;
    }

    // 此处可以使用 display 来与 X 服务器进行交互

    // 关闭套接字和 Display
    /*
    XCloseDisplay(mdisplay);
    close(sockfd);*/

    return true;
}




    
    
  