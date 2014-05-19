#include "AppDelegate.h"
#include "cocos2d.h"
#include "HelloWorldScene.h"
#include "CCEventType.h"
#include "platform/android/jni/JniHelper.h"
#include <jni.h>
#include <android/log.h>

#define  LOG_TAG    "main"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

using namespace cocos2d;

void cocos_android_app_init (JNIEnv* env, jobject thiz) {
    LOGD("cocos_android_app_init");
    AppDelegate *pAppDelegate = new AppDelegate();
}

extern "C" {
	void Java_org_cocos2dx_cpp_AppActivity_SessionUpdate(JNIEnv* env, jobject thiz, jstring sessionName, jstring sessionUuid, jstring avatarPath){
		const char *sName = env->GetStringUTFChars(sessionName, NULL);
		const char *sUuid = env->GetStringUTFChars(sessionUuid, NULL);
		const char *sAvatar = env->GetStringUTFChars(avatarPath, NULL);
		
		LOGD("JNI sessionName:%s sessionUuid:%s avatarpath:%s", sName, sUuid, sAvatar);
		
		HelloWorld *pScene = (HelloWorld *)Director::getInstance()->getRunningScene();
	  if(pScene)
	  {
	  	pScene->addNodeSync(sName, sUuid, sAvatar);
		}
			
		env->ReleaseStringUTFChars(sessionName, sName);
		env->ReleaseStringUTFChars(sessionUuid, sUuid);
		env->ReleaseStringUTFChars(avatarPath, sAvatar);
	}
}
