#include "Paddle.h"

Paddle::Paddle(void)
{
	uuid = " ";
  _state = kPaddleStateUngrabbed;
}

Paddle::~Paddle(void)
{
}

Rect Paddle::getRect()
{
    auto s = getTexture()->getContentSize();
    return Rect(-s.width / 2, -s.height / 2, s.width, s.height);
}

Paddle* Paddle::create(const std::string& filename, const char *name)
{
    Paddle *sprite = new Paddle();
    if (sprite && sprite->initWithFile(filename))
    {
        sprite->autorelease();
        
				auto label = LabelTTF::create(name, "Arial", 24);
				label->setPosition(sprite->getTexture()->getContentSize().width/2, sprite->getTexture()->getContentSize().height/2);
				sprite->addChild(label);
		        
        return sprite;
    }
    CC_SAFE_DELETE(sprite);
    return nullptr;
}


Paddle* Paddle::createWithTexture(Texture2D* aTexture, const char *name)
{
    Paddle* pPaddle = new Paddle();
    pPaddle->initWithTexture(aTexture);
    pPaddle->autorelease();

		auto label = LabelTTF::create(name, "Arial", 24);
		label->setPosition(pPaddle->getTexture()->getContentSize().width/2, pPaddle->getTexture()->getContentSize().height/2);
		pPaddle->addChild(label);
		
    return pPaddle;
}

bool Paddle::initWithTexture(Texture2D* aTexture)
{
    if( Sprite::initWithTexture(aTexture) )
    {
        _state = kPaddleStateUngrabbed;
    }
    
    return true;
}

void Paddle::onEnter()
{
    Sprite::onEnter();
    
    // Register Touch Event
    auto listener = EventListenerTouchOneByOne::create();
    listener->setSwallowTouches(true);
    
    listener->onTouchBegan = CC_CALLBACK_2(Paddle::onTouchBegan, this);
    listener->onTouchMoved = CC_CALLBACK_2(Paddle::onTouchMoved, this);
    listener->onTouchEnded = CC_CALLBACK_2(Paddle::onTouchEnded, this);
    
    _eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);
}

void Paddle::onExit()
{
//    auto director = Director::getInstance();
//    director->getTouchDispatcher()->removeDelegate(this);
    Sprite::onExit();
}    

bool Paddle::containsTouchLocation(Touch* touch)
{
    return getRect().containsPoint(convertTouchToNodeSpaceAR(touch));
}

bool Paddle::onTouchBegan(Touch* touch, Event* event)
{
    CCLOG("Paddle::onTouchBegan id = %d, x = %f, y = %f", touch->getID(), touch->getLocation().x, touch->getLocation().y);
    
    if (_state != kPaddleStateUngrabbed) return false;
    if ( !containsTouchLocation(touch) ) return false;
    
    _state = kPaddleStateGrabbed;
    CCLOG("return true");
    return true;
}

void Paddle::onTouchMoved(Touch* touch, Event* event)
{
    // If it weren't for the TouchDispatcher, you would need to keep a reference
    // to the touch from touchBegan and check that the current touch is the same
    // as that one.
    // Actually, it would be even more complicated since in the Cocos dispatcher
    // you get Sets instead of 1 UITouch, so you'd need to loop through the set
    // in each touchXXX method.
    
    CCLOG("Paddle::onTouchMoved id = %d, x = %f, y = %f", touch->getID(), touch->getLocation().x, touch->getLocation().y);
    
    CCASSERT(_state == kPaddleStateGrabbed, "Paddle - Unexpected state!");    
    
    auto touchPoint = touch->getLocation();
    
    setPosition( Point(touchPoint.x, touchPoint.y) );
}

Paddle* Paddle::clone() const
{
    Paddle* ret = Paddle::createWithTexture(_texture, "");
    ret->_state = _state;
    ret->setPosition(getPosition());
    ret->setAnchorPoint(getAnchorPoint());
    return ret;
}

void Paddle::onTouchEnded(Touch* touch, Event* event)
{
    CCASSERT(_state == kPaddleStateGrabbed, "Paddle - Unexpected state!");    
    
    _state = kPaddleStateUngrabbed;
} 


void Paddle::setUuid(const char *sUuid)
{
	uuid = sUuid;
}
		
