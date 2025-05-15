package com.brigadka.app.common

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

fun ComponentContext.coroutineScope(): CoroutineScope {
    val scope = MainScope()
    lifecycle.doOnDestroy {
        scope.cancel()
    }
    return scope
}