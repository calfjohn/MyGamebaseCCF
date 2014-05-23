#include "HelloWorldScene.h"
#include "Paddle.h"


typedef struct{
    std::string sName;
    std::string sUuid;
    std::string sFile;
}myNode;

std::list<myNode> listMyNode;    


USING_NS_CC;

Scene* HelloWorld::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::create();
    
    // 'layer' is an autorelease object
    auto layer = HelloWorld::create();

    // add layer as a child to scene
    scene->addChild(layer);

    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool HelloWorld::init()
{
    //////////////////////////////
    // 1. super init first
    if ( !Layer::init() )
    {
        return false;
    }
    
    Size visibleSize = Director::getInstance()->getVisibleSize();
    Point origin = Director::getInstance()->getVisibleOrigin();

    /////////////////////////////
    // 2. add a menu item with "X" image, which is clicked to quit the program
    //    you may modify it.

    // add a "close" icon to exit the progress. it's an autorelease object
    auto closeItem = MenuItemImage::create(
                                           "CloseNormal.png",
                                           "CloseSelected.png",
                                           CC_CALLBACK_1(HelloWorld::menuCloseCallback, this));
    
	closeItem->setPosition(Point(origin.x + visibleSize.width - closeItem->getContentSize().width/2 ,
                                origin.y + closeItem->getContentSize().height/2));

    // create menu, it's an autorelease object
    auto menu = Menu::create(closeItem, NULL);
    menu->setPosition(Point::ZERO);
    this->addChild(menu, 1);

    /////////////////////////////
    // 3. add your codes below...

    // add a label shows "Hello World"
    // create and initialize a label
    
    auto label = LabelTTF::create("Hello World", "Arial", 24);
    
    // position the label on the center of the screen
    label->setPosition(Point(origin.x + visibleSize.width/2,
                            origin.y + visibleSize.height - label->getContentSize().height));

    // add the label as a child to this layer
    this->addChild(label, 1);

    // add "HelloWorld" splash screen"
    auto sprite = Sprite::create("HelloWorld.png");

    // position the sprite on the center of the screen
    sprite->setPosition(Point(visibleSize.width/2 + origin.x, visibleSize.height/2 + origin.y));

    // add the sprite as a child to this layer
    this->addChild(sprite, 0);
    
    listMyNode.clear();
      
    return true;
}


void HelloWorld::menuCloseCallback(Ref* pSender)
{
#if (CC_TARGET_PLATFORM == CC_PLATFORM_WP8) || (CC_TARGET_PLATFORM == CC_PLATFORM_WINRT)
	MessageBox("You pressed the close button. Windows Store Apps do not implement a close button.","Alert");
    return;
#endif

    Director::getInstance()->end();

#if (CC_TARGET_PLATFORM == CC_PLATFORM_IOS)
    exit(0);
#endif
}

void HelloWorld::addNodeSync(const char* sName, const char* sUuid, const char* sFile)
{
		CCLog("HelloWorld::addNodeSync Enter");
		
    _nodeMutex.lock();
		CCLog("HelloWorld::_nodeMutex.lock()");

    myNode nodeTemp;
    nodeTemp.sName = sName;
    nodeTemp.sFile = sFile;
    nodeTemp.sUuid = sUuid;
 
    listMyNode.push_back(nodeTemp);

		CCLog("HelloWorld::_nodeMutex.unlock()");
    _nodeMutex.unlock();
    
    this->schedule(schedule_selector(HelloWorld::addNode),0,0,0);
    	
		CCLog("HelloWorld::addNodeSync Leave");    	
}

void HelloWorld::addNode(float dt)
{
		CCLog("HelloWorld::addNode Enter");
		
    _nodeMutex.lock();
    
    Size visibleSize = Director::getInstance()->getVisibleSize();
    Point origin = Director::getInstance()->getVisibleOrigin();
    
    for( auto it=listMyNode.begin(); it!=listMyNode.end(); ++it ) {
				if(findPaddle(it->sUuid.c_str())) continue;

        Paddle* paddle = Paddle::create(it->sFile.c_str(), it->sName.c_str());
        paddle->setPosition(Point(origin.x + visibleSize.width/2,
                                  origin.y + visibleSize.height/2));
        
	    paddle->setUuid(it->sUuid.c_str());
	    paddle->setTag(2014);
        
     	addChild(paddle);
    }
    listMyNode.clear();
    
    _nodeMutex.unlock();
    
   CCLog("HelloWorld::addNode Leave");

}

bool HelloWorld::findPaddle(const char* sSession)
{
    if(!sSession) return false;

    for (auto& child : _children)
    {
        Paddle* pNode = (Paddle*) child;
        if(pNode && pNode->getTag() == 2014
           && !strcmp(pNode->getUuid(), sSession))
        {
            return true;
        }
    }
    
   	return false;	
}