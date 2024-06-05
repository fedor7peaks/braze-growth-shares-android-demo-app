package com.braze.advancedsamples

import android.content.Context
import com.braze.Braze
import com.braze.advancedsamples.contentcards.ContentCardableObserver
import com.braze.advancedsamples.contentcards.model.Ad
import com.braze.advancedsamples.contentcards.model.ContentCardClass
import com.braze.advancedsamples.contentcards.model.ContentCardable
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.cardDescription
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.classType
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.created
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.dismissable
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.extras
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.html
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.idString
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.image
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.messageHeader
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.messageTitle
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.title
import com.braze.advancedsamples.contentcards.model.ContentCardable.Keys.urlString
import com.braze.advancedsamples.contentcards.model.Coupon
import com.braze.advancedsamples.contentcards.model.FullPageMessage
import com.braze.advancedsamples.contentcards.model.Group
import com.braze.advancedsamples.contentcards.model.Tile
import com.braze.advancedsamples.contentcards.model.WebViewMessage
import com.braze.advancedsamples.inapp.slideup.CustomInAppMessageViewWrapperFactory
import com.braze.events.ContentCardsUpdatedEvent
import com.braze.models.cards.ImageOnlyCard
import com.braze.models.cards.CaptionedImageCard
import com.braze.models.cards.Card
import com.braze.models.cards.ShortNewsCard
import com.braze.ui.inappmessage.BrazeInAppMessageManager


class BrazeManager private constructor(private val context: Context) {

    private var registeredForContentCardUpdates: Boolean = false
    private val contentCardableObservers = mutableSetOf<ContentCardableObserver>()
    private var cardList: List<Card> = listOf()

    fun userLogin(username:String){
        Braze.getInstance(context).changeUser(username)
    }

    fun registerForContentCardUpdates(){
        if (!registeredForContentCardUpdates){
            Braze.getInstance(context).subscribeToContentCardsUpdates(this::onContentCardsUpdated)
            registeredForContentCardUpdates = true
        }
    }

    fun registerContentCardableObserver(observer: ContentCardableObserver){
        registerForContentCardUpdates()
        contentCardableObservers.add(observer)
        requestContentCardUpdate()
        if (cardList.isNotEmpty()){
            observer.onContentCardsChanged(mapCardsToCardables(cardList))
        }
    }

    fun unregisterContentCardableObserver(observer: ContentCardableObserver){
        contentCardableObservers.remove(observer)
    }

    fun configureCustomInAppMessageViewFactory(){
        BrazeInAppMessageManager.getInstance().setCustomInAppMessageViewWrapperFactory(
            CustomInAppMessageViewWrapperFactory()
        )
    }

    fun requestContentCardUpdate(){
        Braze.getInstance(context).requestContentCardsRefresh(false)
    }

    private fun mapCardsToCardables(cards: List<Card>):List<ContentCardable>{
        return cards.mapNotNull { card ->
            val metadata = HashMap<String, Any>()
            metadata[idString] = card.id
            metadata[created] = card.created
            metadata[dismissable] = card.isDismissibleByUser

            card.url?.let { metadata[urlString] = it }
            card.extras?.let { metadata[extras] = it }
            card.extras?.let {
                metadata[classType] = it[classType] as String
                it[html]?.let { h -> metadata[html] = h }
                it[messageHeader]?.let { s -> metadata[messageHeader] = s }
                it[messageTitle]?.let { s -> metadata[messageTitle] =  s }
            }
            when (card) {
                is ImageOnlyCard -> {
                    metadata[image] = card.imageUrl
                }
                is CaptionedImageCard -> {
                    metadata[messageTitle] = card.title
                    metadata[image] = card.imageUrl
                    metadata[cardDescription] = card.description
                }
                is ShortNewsCard -> {
                    metadata[title] = card.title.orEmpty()
                    metadata[cardDescription] = card.description
                    metadata[image] = card.imageUrl
                }
            }

            createContentCardable(metadata, ContentCardClass.valueFrom(metadata[classType] as? String))
        }
    }

    private fun onContentCardsUpdated(event: ContentCardsUpdatedEvent){
        this.cardList = event.allCards
        val cardables = mapCardsToCardables(event.allCards)
        contentCardableObservers.forEach {
            it.onContentCardsChanged(cardables)
        }
    }

    private fun createContentCardable(metadata: Map<String, Any>, type: ContentCardClass?): ContentCardable?{
        return when(type){
            ContentCardClass.AD -> Ad(metadata)
            ContentCardClass.MESSAGE_WEB_VIEW -> WebViewMessage(metadata)
            ContentCardClass.MESSAGE_FULL_PAGE -> FullPageMessage(metadata)
            ContentCardClass.ITEM_GROUP -> Group(metadata)
            ContentCardClass.ITEM_TILE -> Tile(metadata)
            ContentCardClass.COUPON -> Coupon(metadata)
            else -> null
        }
    }

    fun logCustomEvent(evt:String){
        Braze.getInstance(context).logCustomEvent(evt)
    }

    fun logContentCardClicked(idString: String?) {
        getContentCard(idString)?.logClick()
    }

    fun logContentCardImpression(idString: String?) {
        getContentCard(idString)?.logImpression()
    }

    fun logContentCardDismissed(idString: String?) {
        getContentCard(idString)?.isDismissed = true
    }


    private fun getContentCard(idString: String?): Card? {
        return cardList.find { it.id == idString }.takeIf { it != null }
    }

    companion object : SingletonHolder<BrazeManager, Context>(::BrazeManager)

}