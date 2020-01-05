#include <jni.h>
#include "baseclass/log.h"
#define LOG_TAG "LM"
#include "FileOperator.h"
#include "mad/mad.h"
#include "string.h"
#include "pthread.h"
#define INPUT_BUFFER_SIZE	8192*5 /*(8192/4) */

#define STATUS_PLAYING 2001
#define STATUS_PAUSE 2002
#define STATUS_IDLE 2003
#define STATUS_WAITING 2004
#define STATUS_STOP 2005
#define STATUS_PROGRESS 2006
#define STATUS_FINISH 2007
#define STATUS_FINISH_ERROR 2008
#define STATUS_SEEK 2009
#define STATUS_RESTART 2011
#define STATUS_CACHING 2012
#define STATUS_CACHE_FINISH 2013

int MEDIA_TYPE_AUDIO =1;
int MEDIA_TYPE_AUDIO_STREAM=2;

struct StreamHandle {
    int length;
    int seek;
    struct mad_stream stream;
    struct mad_frame frame;
    struct mad_synth synth;
    unsigned char* input;
    unsigned char inputBuffer[INPUT_BUFFER_SIZE];
};

struct FileHandle {
    int file;
    int playStatus;
    int duration;
    unsigned char const *start;
    int64_t length;
    struct mad_stream stream;
    struct mad_frame frame;
    struct mad_synth synth;
    const  char* filePath;
    unsigned char inputBuffer[INPUT_BUFFER_SIZE];
    int commandPending;
};

static JavaVM  *VM=NULL;

jint JNI_OnLoad(JavaVM* vm,void* resolved){
    VM=vm;
    return JNI_VERSION_1_6;
}

void notifyStatusChanged(int status, const char* path, const char* note){
//    JNIEnv *jniEnv;
//    int res = (*VM)->GetEnv(VM,(void **) &jniEnv, JNI_VERSION_1_6);
//    if(res==JNI_OK){
//        jclass callbackClass = (*jniEnv)->FindClass(jniEnv,"com/merlin/player/Player");
//        jstring pathJstring = (*jniEnv)->NewStringUTF(jniEnv,path);
//        jstring noteJstring = (*jniEnv)->NewStringUTF(jniEnv,note);
//        jmethodID callbackMethod = (*jniEnv)->GetStaticMethodID(jniEnv,callbackClass,"notifyStatusChanged","(ILjava/lang/String;Ljava/lang/String;)V");
//        (*jniEnv)->CallStaticVoidMethod(jniEnv,callbackClass,callbackMethod,status,noteJstring,pathJstring);
////        (*jniEnv)->DeleteLocalRef(jniEnv,callbackMethod);
//        (*jniEnv)->DeleteLocalRef(jniEnv,callbackClass);
//        (*jniEnv)->DeleteLocalRef(jniEnv,pathJstring);
//        (*jniEnv)->DeleteLocalRef(jniEnv,noteJstring);
//    }
}

static inline signed int scale(mad_fixed_t sample){
    /* round */
    sample += (1L << (MAD_F_FRACBITS - 16));
    /* clip */
    if (sample >= MAD_F_ONE)
        sample = MAD_F_ONE - 1;
    else if (sample < -MAD_F_ONE)
        sample = -MAD_F_ONE;
    /* quantize */
    return sample >> (MAD_F_FRACBITS + 1 - 16);
}

static inline void onFrameDecode(int mediaType,const char * path,struct mad_header header,struct mad_pcm pcm){
    /* pcm->samplerate contains the sampling frequency */
    unsigned int layer=header.layer;
    unsigned int mode=header.mode;
    unsigned long bitrate=header.bitrate;
    unsigned int sampleRate=pcm.samplerate;
    unsigned int channels = pcm.channels;
    unsigned int length= pcm.length;
    unsigned int samples=length;
    mad_fixed_t const *left_ch, *right_ch;
    left_ch   = pcm.samples[0];
    right_ch  = pcm.samples[1];
//    LOGD("bitrate %ld Mode %d Layer %d 通道 %d 采样率 %d",bitrate,mode,layer,channels,sampleRate);
    unsigned char* output = malloc(samples*channels*2);
    int index=0;
    while (samples--) {
        /* output sample(s) in 16-bit signed little-endian PCM */
        signed int sample = scale(*left_ch++);
        *(output+2*channels*index+0)=(sample >> 0) & 0xff;
        *(output+2*channels*index+1)=(sample >> 8) & 0xff;
        if (channels == 2) {
            sample = scale(*right_ch++);
            *(output+2*channels*index+2)=(sample >> 0) & 0xff;
            *(output+2*channels*index+3)=(sample >> 8) & 0xff;
        }
        index++;
    }
    int speed = sampleRate * 2;    /*播放速度是采样率的两倍 */
    length *= channels * 2;         //数据长度为pcm音频的4倍
    JNIEnv *jniEnv;
    int res = (*VM)->GetEnv(VM,(void **) &jniEnv, JNI_VERSION_1_6);
    if(res==JNI_OK){
//        jclass callbackClass = (*jniEnv)->FindClass(jniEnv,"com/merlin/player/Player");
//        jmethodID callbackMethod = (*jniEnv)->GetStaticMethodID(jniEnv,callbackClass,"onNativeDecodeFinish","(I[BIII)V");
//        jbyteArray data = (*jniEnv)->NewByteArray(jniEnv, length);
//        (*jniEnv)->SetByteArrayRegion(jniEnv, data, 0, length, output);
//        (*jniEnv)->CallStaticVoidMethod(jniEnv,callbackClass,callbackMethod,mediaType,data,channels,sampleRate,speed);
////        (*jniEnv)->DeleteLocalRef(jniEnv,callbackMethod);
//        (*jniEnv)->DeleteLocalRef(jniEnv, data);
//        (*jniEnv)->DeleteLocalRef(jniEnv,callbackClass);
//        notifyStatusChanged(STATUS_PROGRESS,path,"Update progress.");
    }
}

//static inline int readStreamNextFrame(struct StreamHandle * handle){
//    int inputBufferSize = 0;
//    do{
//        if(handle->stream.buffer == 0 || handle->stream.error == MAD_ERROR_BUFLEN){
//            if(handle->stream.next_frame != 0){
//                int leftOver = handle->stream.bufend - handle->stream.next_frame;
//                int i;
////                LOGD("deeee %d",leftOver);
//                for(i= 0;i<leftOver;i++){
//                    handle->inputBuffer[i] = handle->stream.next_frame[i];
//                }
//                for (int i = 0; i < INPUT_BUFFER_SIZE-leftOver; ++i) {
//                    handle->inputBuffer[leftOver+i]=handle->input[handle->seek+i];
//                }
//                inputBufferSize=sizeof(handle->inputBuffer);
//            }else{
//                for (int i = 0; i < INPUT_BUFFER_SIZE; ++i) {
//                    handle->inputBuffer[i]=handle->input[handle->seek+i];
//                }
//                 inputBufferSize=sizeof(handle->inputBuffer);
////                 LOGD("dddee是的发生 %d  %d",handle->seek, inputBufferSize);
//            }
//            handle->seek+=inputBufferSize;
//            mad_stream_buffer(&handle->stream,handle->inputBuffer,inputBufferSize);
//            handle->stream.error = MAD_ERROR_NONE;
//        }
//        int decodeResult=mad_frame_decode(&handle->frame,&handle->stream);
//        if(decodeResult){
//            if(handle->stream.error == MAD_ERROR_BUFLEN ||(MAD_RECOVERABLE(handle->stream.error))){
//                continue;
//            }
//        }else{
//            break;
//        }
//    }while (1);
//    mad_synth_frame(&handle->synth,&handle->frame);
//    onFrameDecode(MEDIA_TYPE_AUDIO_STREAM,NULL,handle->frame.header,handle->synth.pcm);
//    return inputBufferSize;
//}

//JNIEXPORT jboolean JNICALL
//Java_com_merlin_player_Player_playBytes(JNIEnv *env, jobject thiz, jbyteArray data, jint offset,
//                                        jint length) {
//    int byteLength = data ==NULL?-1:(*env)->GetArrayLength(env,data);
//    if(byteLength <= 0){
//        LOGW("Can'T play bytes which is NULL.%d",byteLength);
//        return JNI_FALSE;
//    }
//    if(offset<0||offset>=byteLength){
//        LOGW("Can'T play bytes which offset out of bounds.%d %d",offset,byteLength);
//        return JNI_FALSE;
//    }
//    int targetLength=offset+length;
//    if (targetLength<=0||targetLength>byteLength){
//        LOGW("Can'T play bytes which target end out of bounds.%d %d %d",targetLength,offset,byteLength);
//        return JNI_FALSE;
//    }
//    jbyte* bytesStart =(*env)->GetByteArrayElements(env,data, 0);
//    LOGW("长度 %d",byteLength);
//    size_t handleSize=sizeof(struct StreamHandle);
//    struct StreamHandle* handle = (struct StreamHandle*)malloc(handleSize);
//    memset(handle,0,handleSize);
//    handle->input=(unsigned char *)bytesStart;
//    handle->seek=0;
//    handle->length=targetLength;
//    mad_stream_init(&(handle->stream));
//    mad_frame_init(&handle->frame);
//    mad_synth_init(&handle->synth);
//    //
//    int readLength=0;
//    while (readLength<byteLength){
//        readStreamNextFrame(handle);
//        if(handle->seek>=byteLength){
//            LOGD("End play media stream. %d",handle->length);
//            break;
//        }
//    }
//    LOGD("技术了 %d %d",readLength, byteLength);
//    (*env)->ReleaseByteArrayElements(env, szLics, szStr, 0);
//    mad_synth_finish(&handle->synth);
//    mad_frame_finish(&handle->frame);
//    mad_stream_finish(&handle->stream);
//    free(handle);
//    (*env)->ReleaseByteArrayElements(env,data,bytesStart,0);
//    return JNI_TRUE;
//}

//////////////////Play media file ////////////////////
//struct FileHandle* handle;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t  cond  = PTHREAD_COND_INITIALIZER;

void releaseMutex( char* debug){
    pthread_mutex_lock(&mutex);
    pthread_cond_signal(&cond);
    pthread_mutex_unlock(&mutex);
}

int pauseMedia(jboolean stop){
//    if (NULL!=handle){
//        if (!stop&&(handle->playStatus==STATUS_PLAYING||handle->playStatus==STATUS_WAITING)){
//            handle->playStatus=STATUS_PAUSE;
//            return JNI_TRUE;
//        }else if (stop&&(handle->playStatus==STATUS_PLAYING||handle->playStatus==STATUS_WAITING
//                         ||handle->playStatus==STATUS_PAUSE)){
//            handle->playStatus=STATUS_STOP;
//            releaseMutex("While stop play.");
//            return JNI_TRUE;
//        }
//    }
    return JNI_FALSE;
}

//static inline int readFileNextFrame(struct FileHandle* handle){
//    int inputBufferSize = 0;
//    do{
//        if(handle->stream.buffer == 0 || handle->stream.error == MAD_ERROR_BUFLEN){
//            if(handle->stream.next_frame != 0){
//                int leftOver = handle->stream.bufend - handle->stream.next_frame;
//                int i;
//                for(i= 0;i<leftOver;i++){
//                    handle->inputBuffer[i] = handle->stream.next_frame[i];
//                }
//                int readBytes = fileRead(handle->file, handle->inputBuffer+leftOver,INPUT_BUFFER_SIZE-leftOver);
//                if(readBytes == 0){
//                    return 0;
//                }
//                inputBufferSize = leftOver + readBytes;
//                handle->start += readBytes;
//            }else{
//                int readBytes = fileRead(handle->file,handle->inputBuffer,INPUT_BUFFER_SIZE);
//                if(readBytes == 0){
//                    return 0;
//                }
//                inputBufferSize = readBytes;
//                handle->start += readBytes;
//            }
//            mad_stream_buffer(&handle->stream,&handle->inputBuffer,inputBufferSize);
//            handle->stream.error = MAD_ERROR_NONE;
//        }
//        int decodeResult=mad_frame_decode(&handle->frame,&handle->stream);
//        if(decodeResult&&!handle->commandPending){
//            if(handle->stream.error == MAD_ERROR_BUFLEN ||(MAD_RECOVERABLE(handle->stream.error))){
//                continue;
//            }
//        }else{
//            break;
//        }
//    }while (1);
//     handle->commandPending=JNI_FALSE;
//     mad_synth_frame(&handle->synth, &handle->frame);
//     onFrameDecode(MEDIA_TYPE_AUDIO,handle->filePath, handle->frame.header, handle->synth.pcm);
//    return inputBufferSize;
//    return 0;
//}


void notifyStatusChange(int status,jobject media, const char* note){

}

struct BufferHandle{
    jobject media;
    struct mad_stream stream;
    struct mad_frame frame;
    struct mad_synth synth;
    int index;
    int playStatus;
    unsigned char a[INPUT_BUFFER_SIZE];
};

struct BufferHandle* handle;

unsigned char * readBytes(JNIEnv* env,jobject media,int bufferSize){
    jbyteArray bufferJByteArray =(*env)->NewByteArray(env,bufferSize+1);
//        (*env)->SetByteArrayRegion(env,bufferJByteArray, 0,length,0);
    jclass readClass = (*env)->FindClass(env,"com/merlin/player/Buffer");
    jmethodID  readMethodId=(*env)->GetMethodID(env,readClass,"read","([BD)I");
    jint readed=(*env)->CallIntMethod(env,media,readMethodId,bufferJByteArray,0);
    (*env)->DeleteLocalRef(env, readClass);
    //(*env)->DeleteLocalRef(env, readMethodId);
//        LOGD("读取 %d", readed);
    unsigned char * bytes=NULL;
    if (readed>0){
        jbyte* bytesData =(*env)->GetByteArrayElements(env,bufferJByteArray, 0);
        bytes=(unsigned char *)bytesData;
        (*env)->ReleaseByteArrayElements(env,bufferJByteArray,bytesData,0);
    }
    (*env)->DeleteLocalRef(env, bufferJByteArray);
    return bytes;
}

JNIEXPORT jboolean JNICALL
Java_com_merlin_player_Player_play(JNIEnv *env, jobject thiz, jobject media, jdouble seek) {
    if(NULL ==media){
        LOGW(" Can't play media,Buffer is NULL.");
        notifyStatusChange(STATUS_FINISH_ERROR,media,"Buffer id NULL.");
        return JNI_FALSE;
    }
    jclass bufferClass = (*env)->FindClass(env,"com/merlin/player/Buffer");
    jmethodID  methodId=(*env)->GetMethodID(env,bufferClass,"open","(Ljava/lang/String;)Z");
    jstring noteJstring = (*env)->NewStringUTF(env,"While play.");
    jboolean open=(*env)->CallBooleanMethod(env,media,methodId,seek,noteJstring);
    (*env)->DeleteLocalRef(env,noteJstring);
    (*env)->DeleteLocalRef(env, bufferClass);
    (*env)->DeleteLocalRef(env, methodId);
    if(!open) {
        LOGW("Failed open media buffer.");
        return JNI_FALSE;
    }
    if (NULL!=handle){
        LOGD("Stop media before play new media.");
        pauseMedia(JNI_TRUE);
    }
    size_t handleSize=sizeof(struct BufferHandle);
    handle = (struct BufferHandle*)malloc(handleSize);
    handle->media=media;
    mad_stream_init(&(handle->stream));
    mad_frame_init(&handle->frame);
    mad_synth_init(&handle->synth);
    LOGD("Playing media ");
    int bufferSize=1024*1024;
    unsigned char inputBuffer [bufferSize];
    while (JNI_TRUE){
        if (handle->playStatus==STATUS_STOP){
            LOGD("Stop play media file.");
            notifyStatusChange(STATUS_STOP,media,"Stop media.");
            break;
        }
        if (handle->playStatus==STATUS_PAUSE){
            LOGD("Pause play media file.");
            notifyStatusChange(STATUS_PAUSE,media,"Pause media.");
//            pthread_mutex_lock(&mutex);
//            pthread_cond_wait(&cond, &mutex);
//            pthread_mutex_unlock(&mutex);
            continue;
        }
        //

        do{
            if (handle->stream.buffer == 0 || handle->stream.error == MAD_ERROR_BUFLEN){
                if(handle->stream.next_frame != 0){
                    int leftOver = handle->stream.bufend - handle->stream.next_frame;
                    int i;
                    for(i= 0;i<leftOver;i++){
                        inputBuffer[i] = handle->stream.next_frame[i];
                    }
//                    int readBytes = fileRead(handle->file, handle->inputBuffer+leftOver,INPUT_BUFFER_SIZE-leftOver);
//                    if(readBytes == 0){
//                        return 0;
//                    }
//                    inputBufferSize = leftOver + readBytes;
//                    handle->start += readBytes;
                }else{
//                    int readBytes = fileRead(handle->file,handle->inputBuffer,INPUT_BUFFER_SIZE);
//                    if(readBytes == 0){
//                        return 0;
//                    }
//                    inputBufferSize = readBytes;
//                    handle->start += readBytes;
                }
//                mad_stream_buffer(&handle->stream,&handle->inputBuffer,inputBufferSize);
//                handle->stream.error = MAD_ERROR_NONE;
            }
        }while (JNI_TRUE);
//        readBytes(env,media,1024*1024);
//        int length=1024*1024;
//        jbyteArray bufferJByteArray =(*env)->NewByteArray(env,length+1);
////        (*env)->SetByteArrayRegion(env,bufferJByteArray, 0,length,0);
//        jclass readClass = (*env)->FindClass(env,"com/merlin/player/Buffer");
//        jmethodID  readMethodId=(*env)->GetMethodID(env,readClass,"read","([BD)I");
//        jint readed=(*env)->CallIntMethod(env,media,readMethodId,bufferJByteArray,0);
//        (*env)->DeleteLocalRef(env, readClass);
//        //(*env)->DeleteLocalRef(env, readMethodId);
////        LOGD("读取 %d", readed);
//        unsigned char * bytes=NULL;
//        if (readed>0){
//            jbyte* bytesData =(*env)->GetByteArrayElements(env,bufferJByteArray, 0);
//            bytes=(unsigned char *)bytesData;
//            (*env)->ReleaseByteArrayElements(env,bufferJByteArray,bytesData,0);
//        }
//        (*env)->DeleteLocalRef(env, bufferJByteArray);
//        if(bytes==NULL){
//            continue;
//        }
//        do{
//            if(handle->stream.buffer == 0 || handle->stream.error == MAD_ERROR_BUFLEN){
//                if(handle->stream.next_frame != 0){
//                    int leftOver = handle->stream.bufend - handle->stream.next_frame;
//                    int i;
//                    for(i= 0;i<leftOver;i++){
//                        handle->inputBuffer[i] = handle->stream.next_frame[i];
//                    }
//                    int readBytes = fileRead(handle->file, handle->inputBuffer+leftOver,INPUT_BUFFER_SIZE-leftOver);
//                    if(readBytes == 0){
//                        return 0;
//                    }
//                    inputBufferSize = leftOver + readBytes;
//                    handle->start += readBytes;
//                }else{
//                    int readBytes = fileRead(handle->file,handle->inputBuffer,INPUT_BUFFER_SIZE);
//                    if(readBytes == 0){
//                        return 0;
//                    }
//                    inputBufferSize = readBytes;
//                    handle->start += readBytes;
//                }
//                mad_stream_buffer(&handle->stream,&handle->inputBuffer,inputBufferSize);
//                handle->stream.error = MAD_ERROR_NONE;
//            }

//        }while(JNI_TRUE);
    }
    mad_synth_finish(&handle->synth);
    mad_frame_finish(&handle->frame);
    mad_stream_finish(&handle->stream);
    handle->playStatus=STATUS_IDLE;
    notifyStatusChange(STATUS_IDLE,media,"Player idle.");
    LOGD("Finish play media file.");
    return JNI_TRUE;
}


int play(const char* filePath,jfloat seek){
//    int fd = fileOpen(filePath,_RDONLY);
//    struct stat fileStat;
//    int statResult=fstat(fd, &fileStat);
//    if(fd == -1|| fd == -2||statResult<0){
//        LOGW(" Can't play media,File open failed.%d %d %s",fd,statResult,filePath);
//        fileClose(fd);
//        notifyStatusChanged(STATUS_FINISH_ERROR,filePath,"File open failed.");
//        return JNI_FALSE;
//    }
//    if (NULL!=handle){
//        LOGD("Stop media before play new media.%s",filePath,"Stop before new play.");
//        pauseMedia(JNI_TRUE);
//    }
//    int64_t length= fileStat.st_size;
//    int64_t seekPosition=seek>=0&&seek<=1?seek*(float)length:(seek<0?0:seek);
//    seekPosition=seekPosition>=0&&seekPosition<=length?seekPosition:0;
//    LOGW("Playing media %f %d %d %s", seek,seekPosition,length, filePath);
//    fileSeek (fd,seekPosition, SEEK_SET);
//    if (seekPosition>0){
//        notifyStatusChanged(STATUS_SEEK,filePath,"Seek while start.");
//    }
//    size_t handleSize=sizeof(struct FileHandle);
//    handle = (struct FileHandle*)malloc(handleSize);
//    memset(handle,0,handleSize);
//    handle->playStatus=STATUS_PLAYING;
//    handle->filePath=filePath;
//    handle->start=seekPosition;
//    handle->file=fd;
//    handle->length=length;
//    handle->commandPending=JNI_FALSE;
//    mad_stream_init(&(handle->stream));
//    mad_frame_init(&handle->frame);
//    mad_synth_init(&handle->synth);
//    //
//    while (handle->start<=length){
//        if (handle->playStatus==STATUS_STOP){
//            LOGD("Stop play media file.%d %d %s",handle->start,handle->length,handle->filePath);
//            notifyStatusChanged(STATUS_STOP,filePath,"Stop media.");
//            break;
//        }
//        if (handle->playStatus==STATUS_PAUSE){
//            LOGD("Pause play media file.%d %d %s",handle->start,handle->length,handle->filePath);
//            notifyStatusChanged(STATUS_PAUSE,filePath,"Pause media.");
//            pthread_mutex_lock(&mutex);
//            pthread_cond_wait(&cond, &mutex);
//            pthread_mutex_unlock(&mutex);
//            continue;
//        }
//        readFileNextFrame(handle);
//        if (handle->start>=length){
//            LOGD("End play media file.%d %d %s",handle->start,handle->length,handle->filePath);
//            notifyStatusChanged(STATUS_FINISH,filePath,"Finish media.");
//            break;
//        }
//    }
//    mad_synth_finish(&handle->synth);
//    mad_frame_finish(&handle->frame);
//    mad_stream_finish(&handle->stream);
//    handle->playStatus=STATUS_IDLE;
//    fileClose(fd);
//    free(handle);
//    handle=NULL;
//    notifyStatusChanged(STATUS_IDLE,filePath,"Player idle.");
//    LOGD("Finish play media file.%s",filePath);
    return 0;
}

JNIEXPORT jboolean
Java_com_merlin_player_Player_playFile(JNIEnv *env,jobject type,jstring path,jfloat seek){
//    const char* filePath=(*env)->GetStringUTFChars(env,path,0);
    return JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_merlin_player_Player_getPlayerStatus(JNIEnv *env, jobject thiz) {
    return -1;
}

JNIEXPORT jlong JNICALL
Java_com_merlin_player_Player_seek(JNIEnv *env, jobject thiz, jfloat seek) {
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_com_merlin_player_Player_start(JNIEnv *env, jobject thiz, jfloat seek) {
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL
Java_com_merlin_player_Player_getPosition(JNIEnv *env, jobject thiz) {
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_merlin_player_Player_getDuration(JNIEnv *env, jobject thiz) {
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_com_merlin_player_Player_pause(JNIEnv *env, jobject thiz, jboolean stop) {
    return JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_merlin_player_Player_getPlayingPath(JNIEnv *env, jobject thiz) {

    return NULL;
}