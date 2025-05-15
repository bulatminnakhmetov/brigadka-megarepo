import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.brigadka.app.data.api.BrigadkaApiService
import com.brigadka.app.di.CreateChatComponent
import com.brigadka.app.di.CreateProfileViewComponent
import com.brigadka.app.presentation.chat.conversation.ChatComponent
import com.brigadka.app.presentation.chat.list.ChatListComponent
import com.brigadka.app.presentation.profile.view.ProfileViewComponent
import com.brigadka.app.presentation.search.SearchComponent
import kotlinx.serialization.Serializable

class MainComponent(
    componentContext: ComponentContext,
    private val createProfileViewComponent: CreateProfileViewComponent,
    private val createSearchComponent: (ComponentContext, (Int) -> Unit) -> SearchComponent,
    private val createChatListComponent: (ComponentContext, (String) -> Unit) -> ChatListComponent,
    private val createChatComponent: CreateChatComponent
) : ComponentContext by componentContext {

    private val mainNavigation = StackNavigation<Config>()
    private val mainStack = childStack(
        source = mainNavigation,
        initialConfiguration = Config.Profile(),
        serializer = Config.serializer(),
        handleBackButton = true,
        childFactory = ::createChild
    )

    val childStack: Value<ChildStack<Config, Child>> = mainStack

    private fun createChild(
        configuration: Config,
        componentContext: ComponentContext
    ): Child = when (configuration) {
        is Config.Profile -> Child.Profile(
            createProfileViewComponent(componentContext, configuration.userID, {},
                { chatID -> navigateToChat(chatID) },
                { mainNavigation.pop() }
            )
        )
        is Config.Search -> Child.Search(
            createSearchComponent(componentContext, { userID ->
                navigateToProfile(userID)
            })
        )
        is Config.ChatList -> Child.ChatList(
            createChatListComponent(
                componentContext,
                { chatId -> navigateToChat(chatId) }
            )
        )
        is Config.Chat -> Child.Chat(
            createChatComponent(componentContext, configuration.chatId, {
                mainNavigation.pop()
            })
        )
        else -> throw IllegalArgumentException("Unknown configuration: $configuration")
    }

    fun navigateTo(screen: Config) {
        val stackItems = childStack.value.items
        val existingIndex = stackItems.indexOfFirst { it.configuration == screen }

        if (childStack.value.active.configuration == screen) {
            // Already on this screen, do nothing
            return
        }

        if (existingIndex != -1) {
            // Screen is in stack, bring to front
            mainNavigation.bringToFront(screen)
        } else {
            // Not in stack, push it
            mainNavigation.pushNew(screen)
        }
    }

    fun navigateToProfile(userID: Int? = null) {
        navigateTo(Config.Profile(userID))
    }

    fun navigateToSearch() {
        navigateTo(Config.Search)
    }

    fun navigateToChatList() {
        navigateTo(Config.ChatList)
    }

    fun navigateToChat(chatId: String) {
        navigateTo(Config.Chat(chatId))
    }
}

// These need to be outside the inner class to be properly serializable
@Serializable
sealed class Config {
    @Serializable
    data class Profile(val userID: Int? = null) : Config()

    @Serializable
    object Search : Config()

    @Serializable
    object ChatList : Config()

    @Serializable
    data class Chat(val chatId: String) : Config()
}

sealed class Child {
    data class Profile(val component: ProfileViewComponent) : Child()
    data class Search(val component: SearchComponent) : Child()
    data class ChatList(val component: ChatListComponent) : Child()
    data class Chat(val component: ChatComponent) : Child()
}