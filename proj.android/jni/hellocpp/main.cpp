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

bool findPaddle(const char* sessionUuid, CCScene *pNodeParent)
{
	  LOGD("step0");            	
    CCAssert( sessionUuid, "Invalid sessionUuid");

    if(pNodeParent && pNodeParent->getChildrenCount() > 0)
    {
        CCObject* child;
        
        for(const auto &child: pNodeParent->getChildren()) {
            Paddle* pNode = (Paddle*) child;
        		LOGD("%s", pNode->getUuid());            	
            if(pNode && pNode->getTag() == 2014 && !strcmp(pNode->getUuid(), sessionUuid)){
								LOGD("Find %s", sessionUuid);            	
                return true;	
              }
        }
    }
    

   	return false;	
}

extern "C" {
	void Java_org_cocos2dx_cpp_AppActivity_SessionUpdate(JNIEnv* env, jobject thiz, jstring sessionName, jstring sessionUuid){
		const char *sName = env->GetStringUTFChars(sessionName, NULL);
		const char *sUuid = env->GetStringUTFChars(sessionUuid, NULL);
		
		LOGD("JNI sessionName:%s sessionUuid:%s", sName, sUuid);
		
	  if(Director::getInstance()->getRunningScene() && !findPaddle(sUuid, Director::getInstance()->getRunningScene()))	  
	  {
			Size visibleSize = Director::getInstance()->getVisibleSize();
			Point origin = Director::getInstance()->getVisibleOrigin();
	
			auto paddleTexture = Director::getInstance()->getTextureCache()->addImage("paddle.png");
			Paddle* paddle = Paddle::createWithTexture(paddleTexture, sName);
	
			paddle->setPosition(Point(origin.x + visibleSize.width/2,
	                origin.y + visibleSize.height/2));
	                
	    paddle->setUuid(sUuid);
	    paddle->setTag(2014);
	                		  	
			Director::getInstance()->getRunningScene()->addChild(paddle);
		}		  	
		
		env->ReleaseStringUTFChars(sessionName, sName);
		env->ReleaseStringUTFChars(sessionUuid, sUuid);
	}
}
