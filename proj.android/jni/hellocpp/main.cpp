#include "AppDelegate.h"
#include "cocos2d.h"
#include "Paddle.h"
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


const char *getSessionValue(const char *srcString)
{
	
}


extern "C" {
	void Java_org_cocos2dx_cpp_AppActivity_SessionUpdate(JNIEnv* env, jobject thiz, jstring sessionName){
		const char *sName = env->GetStringUTFChars(sessionName, NULL);
		LOGD("org_cocos2dx_cpp_AppActivity_sessionUpdate:%s", sName);
		
	  if(!Director::getInstance()->getRunningScene()) return;	  

		Size visibleSize = Director::getInstance()->getVisibleSize();
		Point origin = Director::getInstance()->getVisibleOrigin();

		auto paddleTexture = Director::getInstance()->getTextureCache()->addImage("paddle.png");
		Paddle* paddle = Paddle::createWithTexture(paddleTexture, sName);

		paddle->setPosition(Point(origin.x + visibleSize.width/2,
                origin.y + visibleSize.height/2));
                		  	
		Director::getInstance()->getRunningScene()->addChild(paddle);
		  	
		env->ReleaseStringUTFChars(sessionName, sName);
	}
}
